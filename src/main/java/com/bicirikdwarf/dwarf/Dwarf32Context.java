package com.bicirikdwarf.dwarf;

import static com.bicirikdwarf.utils.ElfUtils.debugging;
import static com.bicirikdwarf.utils.ElfUtils.log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.bicirikdwarf.elf.Elf32Context;
import com.bicirikdwarf.utils.ElfUtils;
import com.bicirikdwarf.utils.Leb128;

public class Dwarf32Context {
	Elf32Context elf;

	Map<Integer, Abbrev> abbrevs;
	Map<Integer, Map<Integer, Abbrev>> abbrevSequences;
	private Map<Integer, CompilationUnit> compilationUnits;
	
	Map<Integer, DebugInfoEntry> dies;

	ByteBuffer debug_str_buffer;

	public Dwarf32Context(Elf32Context elf) throws IOException {
		this.elf = elf;

		abbrevs = new HashMap<>();
		abbrevSequences = new HashMap<>();
		compilationUnits = new HashMap<>();
		dies = new HashMap<>();

		init();
	}

	private void init() throws IOException {
		debug_str_buffer = elf.getSectionBufferByName(".debug_str");

		initDebugAbbrev();
		initDebugInfo();
	}

	private void initDebugAbbrev() throws IOException {
		ByteBuffer buffer = elf.getSectionBufferByName(".debug_abbrev");

		Map<Integer, Abbrev> abbrevSequence = null;
		int sequenceOffset = 0;

		while (buffer.hasRemaining()) {
			int offset = buffer.position();

			Abbrev abbrev = new Abbrev();
			abbrev.parse(buffer);

			if (abbrev.number == 0) {
				if (abbrevSequence != null) {
					if (debugging())
						log("Creating abbrev sequence entry at 0x"
								+ Integer.toHexString(sequenceOffset));
					abbrevSequences.put(sequenceOffset, abbrevSequence);
					sequenceOffset = buffer.position();
					abbrevSequence = null;
				}
			}

			if (abbrevSequence == null)
				abbrevSequence = new HashMap<>();

			if (abbrevSequence.containsKey(abbrev.number))
				throw new InvalidParameterException(
						"Overlapping abbrev number " + abbrev.number
								+ "for sequence starting at "
								+ Integer.toHexString(sequenceOffset));

			abbrevSequence.put(abbrev.number, abbrev);
			abbrevs.put(offset, abbrev);
		}

		if (abbrevSequence != null)
			abbrevSequences.put(sequenceOffset, abbrevSequence);
	}

	private void initDebugInfo() throws IOException {
		ByteBuffer buffer = elf.getSectionBufferByName(".debug_info");

		while (buffer.remaining() > 11) {
			int address = buffer.position();
			CompilationUnit cu = new CompilationUnit(this, address);
			cu.parse(buffer);
			compilationUnits.put(address, cu);
		}
	}

	private String readDebugString(int offset) {
		debug_str_buffer.position(offset);
		return ElfUtils.getNTString(debug_str_buffer);
	}
	
	public DebugInfoEntry getDieByAddress(int address) { 
		if(dies.containsKey(address)) 
			return dies.get(address);
		
		return null;
	}
	
	public void registerDie(DebugInfoEntry die) {
		int address = die.getAddress();
		
		if(dies.containsKey(address))
			throw new InvalidParameterException("DIE at address 0x" + Integer.toHexString(address) + " already exists");

		dies.put(address, die);
	}

	public Collection<CompilationUnit> getCompilationUnits() {
		return compilationUnits.values();
	}
	
	public Object getAttributeValue(DwFormType form, CompilationUnit cu,
			ByteBuffer buffer) {
		switch (form) {
		case DW_FORM_string:
			return ElfUtils.getNTString(buffer);

		case DW_FORM_addr:
			return buffer.getInt();

		case DW_FORM_strp: {
			int stringOffset = buffer.getInt();
			return readDebugString(stringOffset);
		}

		case DW_FORM_data1:
			return buffer.get() & 0xff;
		case DW_FORM_data2:
			return buffer.getShort();
		case DW_FORM_data4:
			return buffer.getInt();
		case DW_FORM_data8:
			return buffer.getLong();

		case DW_FORM_ref1: {
			Byte data = buffer.get();
			return data + cu.address;
		}

		case DW_FORM_ref2: {
			Short data = buffer.getShort();
			return data + cu.address;
		}

		case DW_FORM_ref4: {
			Integer data = buffer.getInt();
			return data + cu.address;
		}

		case DW_FORM_ref8: {
			Long data = buffer.getLong();
			return data + cu.address;
		}

		case DW_FORM_block: {
			long size = Leb128.getULEB128(buffer);
			byte bytes[] = new byte[(int) size];
			for (int z = 0; z < size; z++) {
				bytes[z] = (byte) (buffer.get() & 0xff);
			}
			return bytes;
		}

		case DW_FORM_block1: {
			int size = buffer.get() & 0xff;
			byte bytes[] = new byte[size];
			for (int z = 0; z < size; z++) {
				bytes[z] = (byte) (buffer.get() & 0xff);
			}
			return bytes;
		}

		case DW_FORM_block2: {
			short size = buffer.getShort();
			byte bytes[] = new byte[size];
			for (int z = 0; z < size; z++) {
				bytes[z] = (byte) (buffer.get() & 0xff);
			}
			return bytes;
		}

		case DW_FORM_block4: {
			int size = buffer.getInt();
			byte bytes[] = new byte[size];
			for (int z = 0; z < size; z++) {
				bytes[z] = (byte) (buffer.get() & 0xff);
			}
			return bytes;
		}

		case DW_FORM_ref_udata:
			return Leb128.getULEB128(buffer);
		case DW_FORM_flag:
			return buffer.get();

		case DW_FORM_sec_offset:
			return buffer.getInt();

		case DW_FORM_flag_present:
			// byte value = buffer.get();
			return 1;

		case DW_FORM_exprloc: {
			long size = Leb128.getULEB128(buffer);
			return ElfUtils.getByteBuffer(buffer, (int) size);
		}
		
		case DW_FORM_sdata:
			return Leb128.getSLEB128(buffer);
		case DW_FORM_udata:
			return Leb128.getULEB128(buffer);

		default:
			throw new InvalidParameterException("Unsupport DW_FORM_? = 0x"
					+ Integer.toHexString(form.value()));
		}
	}
}
