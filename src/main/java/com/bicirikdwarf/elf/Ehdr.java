package com.bicirikdwarf.elf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.bicirikdwarf.utils.Unsigned;

// Elf Section header
public class Ehdr {
	byte[] e_ident = new byte[Elf32Context.EI_NIDENT]; // ident bytes
	int e_type; // file type - half
	ElfMachineType e_machine; // target machine - half
	long e_version; // file version - word
	long e_entry; // start address - addr
	long e_phoff; // phdr file offset - off
	long e_shoff; // shdr file offset - off
	long e_flags; // file flags - word
	int e_ehsize; // sizeof ehdr - half
	int e_phentsize; // sizeof phdr - half
	int e_phnum; // number phdrs - half
	int e_shentsize; // sizeof shdr - half
	int e_shnum; // number shdrs - half
	int e_shstrndx; // shdr string index - half

	public void parse(ByteBuffer buffer) throws IOException {
		buffer.get(e_ident);

		byte[] actual = Arrays.copyOf(e_ident, 4);
		byte[] expected = new byte[] { 0x7f, (byte) 'E', (byte) 'L', (byte) 'F' };
		if (!Arrays.equals(actual, expected)) {
			throw new IOException("invalid elf file");
		}

		e_type = Unsigned.getU16(buffer);
		e_machine = ElfMachineType.byValue(Unsigned.getU16(buffer));
		e_version = Unsigned.getU32(buffer);

		e_entry = Unsigned.getU32(buffer);
		e_phoff = Unsigned.getU32(buffer);
		e_shoff = Unsigned.getU32(buffer);

		e_flags = Unsigned.getU32(buffer);
		e_ehsize = Unsigned.getU16(buffer);
		e_phentsize = Unsigned.getU16(buffer);
		e_phnum = Unsigned.getU16(buffer);
		e_shentsize = Unsigned.getU16(buffer);
		e_shnum = Unsigned.getU16(buffer);
		e_shstrndx = Unsigned.getU16(buffer);
	}
}