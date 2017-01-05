package com.bicirikdwarf.elf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import com.bicirikdwarf.utils.Unsigned;

// Elf Section header
public class Ehdr {
	public enum EELFClass { INVALID, ELF32, ELF64 };
	public enum EELFEncoding { INVALID, DATA2LSB, DATA2MSB };

	byte[] e_ident = new byte[Elf32Context.EI_NIDENT]; // ident bytes
	EELFClass e_ident_class;
	EELFEncoding e_ident_encoding;
	
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
		
		switch(e_ident[4]) {
			case 0x01:
				e_ident_class = EELFClass.ELF32;
				break;
			case 0x02:
				e_ident_class = EELFClass.ELF64;
				break;
			default:
				e_ident_class = EELFClass.INVALID;
				break;
		}

		switch(e_ident[5]) {
			case 0x01:
				e_ident_encoding = EELFEncoding.DATA2LSB; // 2's complement, little endian
				buffer.order(ByteOrder.LITTLE_ENDIAN);
				break;
			case 0x02:
				e_ident_encoding = EELFEncoding.DATA2MSB; // 2's complement, big endian
				buffer.order(ByteOrder.BIG_ENDIAN);
				break;
			default:
				e_ident_encoding = EELFEncoding.INVALID;
				break;
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