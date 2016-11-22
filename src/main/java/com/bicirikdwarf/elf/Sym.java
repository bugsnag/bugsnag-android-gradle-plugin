package com.bicirikdwarf.elf;

import java.nio.ByteBuffer;

public class Sym {
	public int st_name;
	public int st_value;
	public int st_size;
	public byte st_info;
	public byte st_other;
	public short st_shndx;

	public String symbol_name;
	public String filename;
	public int line_number;

	public void parse(ByteBuffer buffer) {
		st_name = buffer.getInt();
		st_value = buffer.getInt();
		st_size = buffer.getInt();
		st_info = buffer.get();
		st_other = buffer.get();
		st_shndx = buffer.getShort();
	}
}