package com.bicirikdwarf.elf;

import java.nio.ByteBuffer;

import com.bicirikdwarf.utils.Unsigned;

public class Phdr {
	long p_type; // entry type - word
	long p_offset; // file offset - off
	long p_vaddr; // virtual address - addr
	long p_paddr; // physical address - addr
	long p_filesz; // file size - word
	long p_memsz; // memory size - word
	long p_flags; // entry flags - word
	long p_align; // memory/file alignment - word

	public void parse(ByteBuffer buffer) {
		p_type = Unsigned.getU32(buffer);
		p_offset = Unsigned.getU32(buffer);
		p_vaddr = Unsigned.getU32(buffer);
		p_paddr = Unsigned.getU32(buffer);
		p_filesz = Unsigned.getU32(buffer);
		p_memsz = Unsigned.getU32(buffer);
		p_flags = Unsigned.getU32(buffer);
		p_align = Unsigned.getU32(buffer);
	}
}