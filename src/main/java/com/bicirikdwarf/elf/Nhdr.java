package com.bicirikdwarf.elf;

import java.nio.ByteBuffer;

import com.bicirikdwarf.utils.Unsigned;

public class Nhdr {
	long n_namesz; // length of note's name - word
	long n_descsz; // length of note's "desc" - word
	long n_type; // type of note - word

	public void parse(ByteBuffer buffer) {
		n_namesz = Unsigned.getU32(buffer);
		n_descsz = Unsigned.getU32(buffer);
		n_type = Unsigned.getU32(buffer);
	}
}