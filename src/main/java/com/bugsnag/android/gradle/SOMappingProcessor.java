package com.bugsnag.android.gradle;

import com.bicirikdwarf.dwarf.CompilationUnit;
import com.bicirikdwarf.dwarf.DebugInfoEntry;
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
import java.util.Comparator;
import java.util.List;

class SOMappingProcessor {

    private static class SharedObjectFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".so");
        }
    }

    public static SOMappingFile[] GenerateMappingFiles(File inputDirectory, File outputDirectory) {

        List<SOMappingFile> outputFiles = new ArrayList<>();

        for (File archDir : inputDirectory.listFiles()) {
            if (archDir.isDirectory()) {
                String arch = archDir.getName();

                for (File sharedObject : archDir.listFiles(new SharedObjectFilter())) {

                    try {
                        List<Sym> symbols = getSymbols(sharedObject);

                        File outputFile = new File(outputDirectory.getAbsolutePath() + File.separator + arch + ".txt");
                        PrintWriter writer = new PrintWriter(outputFile.getAbsolutePath(), "UTF-8");

                        for (Sym symbol : symbols) {
                            writer.print(symbol.st_value + " " + symbol.symbol_name);

                            if (symbol.filename != null) {
                                writer.print(" " + symbol.filename + " " + symbol.line_number);
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

    private static List<Sym> getSymbols(File f) throws IOException {
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

        // Sort the symbols by address
        elf.getSymbols().sort(new Comparator<Sym>() {
            public int compare(Sym obj1, Sym obj2) {
                return Long.compare(obj1.st_value, obj2.st_value);
            }
        });


        for (Sym symbol : elf.getSymbols()) {

            // Look for a debug info entry with matching pointer
            for (CompilationUnit unit : dwarf.getCompilationUnits()) {
                for (DebugInfoEntry die : unit.getCompileUnit().getChildren()) {
                    if (die.getAttribValue(DwAtType.DW_AT_low_pc) != null
                            && (symbol.st_value == (int)die.getAttribValue(DwAtType.DW_AT_low_pc)
                            || symbol.st_value - 1 == (int)die.getAttribValue(DwAtType.DW_AT_low_pc))) { // HACK: arm files seem to have 1 byte difference?

                        if (unit.getCompileUnit().getAttribValue(DwAtType.DW_AT_name) != null) {
                            symbol.filename = (String)unit.getCompileUnit().getAttribValue(DwAtType.DW_AT_name);
                        }

                        if (die.getAttribValue(DwAtType.DW_AT_decl_line) != null) {
                            symbol.line_number = Integer.valueOf(die.getAttribValue(DwAtType.DW_AT_decl_line).toString());
                        }

                        // HACK: arm files seem to have a lot of $t and $d named symbols which we can replace here
                        if (die.getAttribValue(DwAtType.DW_AT_name) != null) {
                            symbol.symbol_name = (String)die.getAttribValue(DwAtType.DW_AT_name);
                        }
                    }
                }
            }
        }

        return elf.getSymbols();
    }

}
