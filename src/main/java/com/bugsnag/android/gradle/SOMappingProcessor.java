package com.bugsnag.android.gradle;

import com.bicirikdwarf.dwarf.CompilationUnit;
import com.bicirikdwarf.dwarf.DebugInfoEntry;
import com.bicirikdwarf.dwarf.DebugLineEntry;
import com.bicirikdwarf.dwarf.DwAtType;
import com.bicirikdwarf.dwarf.Dwarf32Context;
import com.bicirikdwarf.elf.Elf32Context;
import com.bicirikdwarf.elf.Sym;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class SOMappingProcessor {

    private static class SharedObjectFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".so");
        }
    }

    private static final String DELIMITER = " | ";

    public static SOMappingFile[] GenerateMappingFiles(File inputDirectory, File outputDirectory, File projectDir) {

        List<SOMappingFile> outputFiles = new ArrayList<>();

        for (File archDir : inputDirectory.listFiles()) {
            if (archDir.isDirectory()) {
                String arch = archDir.getName();

                for (File sharedObject : archDir.listFiles(new SharedObjectFilter())) {

                    try {
                        List<SOSymbol> symbols = getSymbols(sharedObject);

                        File outputFile = new File(outputDirectory.getAbsolutePath() + File.separator + arch + ".txt");
                        PrintWriter writer = new PrintWriter(outputFile.getAbsolutePath(), "UTF-8");

                        for (SOSymbol symbol : symbols) {
                            writer.print(symbol.getAddress() + DELIMITER + symbol.getMethodName());

                            if (symbol.getFilename() != null) {

                                // Strip some of the filename
                                String filename = symbol.getFilename();
                                if (filename.contains(projectDir.getAbsolutePath())) {
                                    filename = filename.substring(filename.indexOf(projectDir.getAbsolutePath())
                                        + projectDir.getAbsolutePath().length());
                                }

                                writer.print(DELIMITER + filename + DELIMITER + symbol.getLineNumber());
                            }

                            // Output a newline if this is not the last symbol
                            if (symbol != symbols.get(symbols.size() -1)) {
                                writer.println();
                            }
                        }

                        writer.close();

                        outputFiles.add(new SOMappingFile(sharedObject.getName(), outputFile, arch));
                    } catch (Exception e) {
                        System.out.println("arch = " + arch + "  failed to generate symbols = " + e.getMessage());
                    }
                }
            }
        }

        return outputFiles.toArray(new SOMappingFile[outputFiles.size()]);
    }

    private static List<SOSymbol> getSymbols(File f) throws IOException {
        RandomAccessFile aFile = new RandomAccessFile(f, "r");
        FileChannel inChannel = aFile.getChannel();
        long fileSize = inChannel.size();
        ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
        inChannel.read(buffer);
        buffer.flip();
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        inChannel.close();
        aFile.close();

        Elf32Context elf = new Elf32Context(buffer);
        Dwarf32Context dwarf = new Dwarf32Context(elf);

        cleanElfSymbols(elf);

        Map<Long, SOSymbol> symbols = new HashMap<>();

        // Create shared object symbols for all the elf symbols
        for (Sym symbol : elf.getSymbols()) {
            SOSymbol so = new SOSymbol(symbol);
            findCompileUnitInformation(dwarf.getCompilationUnits(), so);

            if (so.getMethodName() != null) {
                symbol.symbol_name = so.getMethodName();
            }
            symbols.put(so.getAddress(), so);
        }

        // Also add shared object symbols for all the debug line entries
        for (DebugLineEntry entry : dwarf.getDebugLineEntries()) {
            SOSymbol so = new SOSymbol(entry);

            if (symbols.containsKey(so.getAddress())) {
                SOSymbol existingEntry = symbols.get(so.getAddress());

                existingEntry.setFilename(entry.getFilename());

                // Choose the largest line number in the case where there are two conflicting entries
                // This can happen if the code has been optimised, and the largest line number seems more useful
                if (existingEntry.getLineNumber() < so.getLineNumber()) {
                    symbols.get(so.getAddress()).setLineNumber(entry.getLineNumber());
                }
            } else {
                findMethodName(elf.getSymbols(), so);
                symbols.put(so.getAddress(), so);
            }
        }

        List<SOSymbol> output = symbols.values().stream().collect(Collectors.toList());

        // Sort the debug Entries by Address
        output.sort(new Comparator<SOSymbol>() {
            public int compare(SOSymbol obj1, SOSymbol obj2) {
                return Long.compare(obj1.getAddress(), obj2.getAddress());
            }
        });

        //System.out.println("build note = " + elf.getBuildNote());

        return output;
    }

    private static void cleanElfSymbols(Elf32Context elf) {

        // Sort the elf Entries by Address
        elf.getSymbols().sort(new Comparator<Sym>() {
            public int compare(Sym obj1, Sym obj2) {
                return Long.compare(obj1.st_value, obj2.st_value);
            }
        });

        // Remove all ARM ELF special symbols from the list
        // $a - At the start of a region of code containing ARM instructions.
        // $t - At the start of a region of code containing THUMB instructions.
        // $d - At the start of a region of data.
        int index = 0;
        while (index < elf.getSymbols().size()) {
            Sym current = elf.getSymbols().get(index);

            if (current.symbol_name.equals("$a")
                || current.symbol_name.equals("$t")
                || current.symbol_name.equals("$d")) {

                // HACK: It seems that these special symbols make the function offsets out of line with the debug info
                // Check to see if there is another symbol in the next byte, and set the address to this symbols
                // address to bring it in line with the other symbols for matching later
                if (elf.getSymbols().size() > index + 1
                    && elf.getSymbols().get(index + 1).st_value == current.st_value + 1) {
                    elf.getSymbols().get(index + 1).st_value = current.st_value;
                }

                elf.getSymbols().remove(index);
            } else {
                index++;
            }
        }
    }

    private static void findMethodName(List<Sym> elfSymbols, SOSymbol so) {
        for (Sym symbol : elfSymbols) {

            if (symbol.st_value <= so.getAddress()
                && symbol.st_value + symbol.st_size > so.getAddress()) {

                so.setMethodName(symbol.symbol_name);
                return;
            }
        }
    }

    private static void findCompileUnitInformation(Collection<CompilationUnit> compileUnits, SOSymbol so) {
        // Look for a debug info entry with matching pointer
        for (CompilationUnit unit : compileUnits) {
            for (DebugInfoEntry die : unit.getCompileUnit().getChildren()) {
                if (die.getAttribValue(DwAtType.DW_AT_low_pc) != null
                    && so.getAddress() == (int)die.getAttribValue(DwAtType.DW_AT_low_pc)) {

                    if (unit.getCompileUnit().getAttribValue(DwAtType.DW_AT_name) != null) {
                        so.setFilename((String)unit.getCompileUnit().getAttribValue(DwAtType.DW_AT_name));
                    }

                    if (die.getAttribValue(DwAtType.DW_AT_decl_line) != null) {
                        so.setLineNumber(Integer.valueOf(die.getAttribValue(DwAtType.DW_AT_decl_line).toString()));
                    }

                    return;
                }
            }
        }
    }

}
