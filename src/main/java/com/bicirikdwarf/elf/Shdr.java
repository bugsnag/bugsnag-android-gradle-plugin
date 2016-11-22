package com.bicirikdwarf.elf;

import java.nio.ByteBuffer;

import com.bicirikdwarf.utils.Unsigned;

public class Shdr {
	long sh_name; // section name - word
	long sh_type; // SHT_... - word
	long sh_flags; // SHF_... - word
	long sh_addr; // virtual address - addr
	long sh_offset; // file offset - off
	long sh_size; // section size - word
	long sh_link; // misc info - word
	long sh_info; // misc info - word
	long sh_addralign; // memory alignment - word
	long sh_entsize; // entry size if table - word

	public void parse(ByteBuffer buffer) {
		sh_name = Unsigned.getU32(buffer);
		sh_type = buffer.getInt();
		sh_flags = buffer.getInt();
		sh_addr = Unsigned.getU32(buffer);
		sh_offset = Unsigned.getU32(buffer);
		sh_size = Unsigned.getU32(buffer);
		sh_link = buffer.getInt();
		sh_info = buffer.getInt();
		sh_addralign = buffer.getInt();
		sh_entsize = buffer.getInt();
	}
}