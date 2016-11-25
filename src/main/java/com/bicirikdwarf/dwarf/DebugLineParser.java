/* Dwarf2NameFinder.java -- decodes the DWARF-2 debug_line section.
   Copyright (C) 2005  Free Software Foundation, Inc.

   This file is part of libgcj.

This software is copyrighted work licensed under the terms of the
Libgcj License.  Please consult the file "LIBGCJ_LICENSE" for
details.  */

// Written by Casey Marshall <csm@gnu.org>


package com.bicirikdwarf.dwarf;

// import gnu.classpath.Configuration;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * An interpreter for DWARF-2 "debug_line" byte codes, which, if given
 * the program counter of a running program, can determine the source
 * file and line number (and column number, but that information is not
 * currently emitted by GCC) of that statement.
 */
public class DebugLineParser {

    private static final int DW_LNS_extended_op = 0;
    private static final int DW_LNS_copy = 1;
    private static final int DW_LNS_advance_pc = 2;
    private static final int DW_LNS_advance_line = 3;
    private static final int DW_LNS_set_file = 4;
    private static final int DW_LNS_set_column = 5;
    private static final int DW_LNS_negate_stmt = 6;
    private static final int DW_LNS_set_basic_block = 7;
    private static final int DW_LNS_const_add_pc = 8;
    private static final int DW_LNS_fixed_advance_pc = 9;
    private static final int DW_LNS_set_prologue_end = 10;
    private static final int DW_LNS_set_epilogue_begin = 11;
    private static final int DW_LNS_set_isa = 12;

    private static final int DW_LNE_end_sequence = 1;
    private static final int DW_LNE_set_address = 2;
    private static final int DW_LNE_define_file = 3;

    private static class dw2_debug_line {
        long total_length;
        int version;
        long prologue_length;
        int minimum_instruction_length;
        boolean default_is_stmt;
        byte line_base;
        int line_range;
        int opcode_base;
        final byte[] standard_opcode_lengths = new byte[12];

        private void get(ByteBuffer b) {
            total_length = (long) b.getInt() & 0xFFFFFFFFL;
            version = b.getShort() & 0xFFFF;
            prologue_length = (long) b.getInt() & 0xFFFFFFFFL;
            minimum_instruction_length = b.get() & 0xFF;
            default_is_stmt = b.get() != 0;
            line_base = b.get();
            line_range = b.get() & 0xFF;
            opcode_base = b.get() & 0xFF;
            b.get(standard_opcode_lengths);
        }

        public String toString() {
            StringBuffer str = new StringBuffer(super.toString());
            str.append(" [ total_length: ").append(total_length);
            str.append("; version: ").append(version);
            str.append("; prologue_length: ").append(prologue_length);
            str.append("; minimum_instruction_length: ").append(minimum_instruction_length);
            str.append("; default_is_stmt: ").append(default_is_stmt);
            str.append("; line_base: ").append(line_base);
            str.append("; line_range: ").append(line_range);
            str.append("; opcode_base: ").append(opcode_base);
            str.append("; standard_opcode_lengths: { ");
            str.append(standard_opcode_lengths[0]).append(", ");
            str.append(standard_opcode_lengths[1]).append(", ");
            str.append(standard_opcode_lengths[2]).append(", ");
            str.append(standard_opcode_lengths[3]).append(", ");
            str.append(standard_opcode_lengths[4]).append(", ");
            str.append(standard_opcode_lengths[5]).append(", ");
            str.append(standard_opcode_lengths[6]).append(", ");
            str.append(standard_opcode_lengths[7]).append(", ");
            str.append(standard_opcode_lengths[8]).append(", ");
            str.append(standard_opcode_lengths[9]).append(", ");
            str.append(standard_opcode_lengths[10]).append(", ");
            str.append(standard_opcode_lengths[11]).append(" } ]");
            return str.toString();
        }
    }

    public static List<DebugLineEntry> parse(ByteBuffer buffer) {
        List<DebugLineEntry> returnValue = new ArrayList<>();

        if (buffer == null)
            return returnValue;

        int originalLimit = buffer.limit();

        buffer.position(0);
        buffer.limit(buffer.capacity());

        while (buffer.position() < originalLimit) {
            final int begin = buffer.position();
            dw2_debug_line header = new dw2_debug_line();
            header.get(buffer);

            final int end = (int) (begin + header.total_length + 4);
            final int prologue_end = (int) (begin + header.prologue_length + 9);

            if (header.version != 2 /*|| header.opcode_base != 10*/) {
                buffer.position(end);
                continue;
            }

            buffer.limit(prologue_end);
            ByteBuffer prologue = buffer.slice();

            // get the directories; they end with a single null byte.
            String dname;
            LinkedList dnames = new LinkedList();
            while ((dname = getString(prologue)).length() > 0) {
                dnames.add(dname);
            }

            // Read the file names.
            LinkedList fnames = new LinkedList();
            while (prologue.hasRemaining()) {
                String fname = getString(prologue);

                long dir = getUleb128(prologue);
                long time = getUleb128(prologue);
                long size = getUleb128(prologue);

                fnames.add(dnames.get((int)dir - 1) + File.separator + fname);
            }
            prologue = null;

            buffer.limit(end);
            buffer.position(prologue_end + 1);
            ByteBuffer section = buffer.slice();
            buffer.limit(buffer.capacity());
            buffer.position(end);

            interpret(section, fnames, header, returnValue);
        }

        return returnValue;
    }

    private static void interpret(ByteBuffer section,
                                     LinkedList fnames,
                                     dw2_debug_line header,
                                     List<DebugLineEntry> fileLocationMap) {
        long address = 0;
        long base_address = 0;
        String define_file = null;
        int fileno = 0;
        int lineno = 1;
        int prev_fileno = 0;
        int prev_lineno = 1;
        final int const_pc_add = 245 / header.line_range;

        long min_address = -1;
        long max_address = 0;

        section.position(0);
        section.limit(section.capacity());

        interpret:
        while (section.hasRemaining()) {
            int opcode = section.get() & 0xFF;

            if (opcode < header.opcode_base) {
                switch (opcode) {
                    case DW_LNS_extended_op: {
                        long insn_len = getUleb128(section);
                        opcode = section.get();

                        switch (opcode) {
                            case DW_LNE_end_sequence:
                                if (min_address != -1 && max_address != 0) {
                                    min_address = -1;
                                    max_address = 0;
                                }

                                prev_lineno = lineno = 1;
                                prev_fileno = fileno = 0;
                                base_address = address = 0;
                                break;

                            case DW_LNE_set_address:
                                base_address = section.get() & 0xFF;
                                base_address |= (section.get() & 0xFFL) << 8;
                                base_address |= (section.get() & 0xFFL) << 16;
                                base_address |= (section.get() & 0xFFL) << 24;
                                address = base_address;
                                break;

                            case DW_LNE_define_file:
                                define_file = getString(section);
                                getUleb128(section);
                                getUleb128(section);
                                getUleb128(section);
                                break;

                            default:
                                section.position(section.position() + (int) insn_len);
                                break;
                        }

                    }
                    case DW_LNS_copy:

                        // Only record information if a line number has been set
                        if (lineno > 1) {
                            String sourceFile = (String) ((prev_fileno >= 0 && prev_fileno < fnames.size())
                                    ? fnames.get(prev_fileno) : define_file);
                            fileLocationMap.add(new DebugLineEntry(address, sourceFile, lineno));
                        }

                        prev_lineno = lineno;
                        prev_fileno = fileno;
                        break;

                    case DW_LNS_advance_pc: {
                        long amt = getUleb128(section);
                        address += amt * header.minimum_instruction_length;

                        if (ucomp(min_address, address) > 0)
                            min_address = address;
                        if (ucomp(max_address, address) < 0)
                            max_address = address;

                    }
                    break;

                    case DW_LNS_advance_line: {
                        long amt = getSleb128(section);
                        prev_lineno = lineno;
                        lineno += (int) amt;
                    }
                    break;

                    case DW_LNS_set_file:
                        prev_fileno = fileno;
                        fileno = (int) getUleb128(section) - 1;
                        break;

                    case DW_LNS_set_column:
                        getUleb128(section);
                        break;

                    case DW_LNS_negate_stmt:
                        break;

                    case DW_LNS_set_basic_block:
                        break;

                    case DW_LNS_const_add_pc:
                        address += const_pc_add;

                        if (ucomp(min_address, address) > 0)
                            min_address = address;
                        if (ucomp(max_address, address) < 0)
                            max_address = address;
                        break;

                    case DW_LNS_fixed_advance_pc: {
                        int amt = section.getShort() & 0xFFFF;
                        address += amt;
                    }
                    break;

                    case DW_LNS_set_prologue_end:
                        break;

                    case DW_LNS_set_epilogue_begin:
                        break;

                    case DW_LNS_set_isa: {
                        int amt = section.getShort() & 0xFFFF;
                    }
                    break;
                }
            } else {
                int adj = (opcode & 0xFF) - header.opcode_base;
                int addr_adv = (adj / header.line_range) * header.minimum_instruction_length;
                int line_adv = header.line_base + (adj % header.line_range);
                long new_addr = address + addr_adv;
                int new_line = lineno + line_adv;

                String sourceFile = (String) ((fileno >= 0 && fileno < fnames.size())
                        ? fnames.get(fileno) : define_file);
                fileLocationMap.add(new DebugLineEntry(new_addr, sourceFile, new_line));

                prev_lineno = lineno;
                prev_fileno = fileno;
                lineno = new_line;
                address = new_addr;

                if (ucomp(min_address, address) > 0)
                    min_address = address;
                if (ucomp(max_address, address) < 0)
                    max_address = address;
            }
        }
    }

    private class CacheEntry {
        final dw2_debug_line header;
        final LinkedList fileNames;
        final ByteBuffer section;

        CacheEntry(LinkedList fileNames, ByteBuffer section, dw2_debug_line header) {
            this.fileNames = fileNames;
            this.section = section;
            this.header = header;
        }
    }

    private static String getString(ByteBuffer buf) {
        int pos = buf.position();
        int len = 0;
        byte b;
        while (buf.get() != 0) len++;
        byte[] bytes = new byte[len];
        buf.position(pos);
        buf.get(bytes);
        buf.get();
        return new String(bytes);
    }

    private static long getUleb128(ByteBuffer buf) {
        long val = 0;
        byte b;
        int shift = 0;

        while (true) {
            b = buf.get();
            val |= (b & 0x7f) << shift;
            if ((b & 0x80) == 0)
                break;
            shift += 7;
        }

        return val;
    }

    private static long getSleb128(ByteBuffer buf) {
        long val = 0;
        int shift = 0;
        byte b;
        int size = 8 << 3;

        while (true) {
            b = buf.get();
            val |= (b & 0x7f) << shift;
            shift += 7;
            if ((b & 0x80) == 0)
                break;
        }

        if (shift < size && (b & 0x40) != 0)
            val |= -(1 << shift);

        return val;
    }

    static int ucomp(long l1, long l2) {
        if (l1 == l2)
            return 0;

        if (l1 < 0) {
            if (l2 < 0) {
                if (l1 < l2)
                    return 1;
                else
                    return -1;
            }
            return 1;
        } else {
            if (l2 >= 0) {
                if (l1 < l2)
                    return -1;
                else
                    return 1;
            }
            return -1;
        }
    }
}
