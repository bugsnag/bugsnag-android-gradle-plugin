package com.bicirikdwarf.dwarf;

import static com.bicirikdwarf.utils.ElfUtils.debugging;
import static com.bicirikdwarf.utils.ElfUtils.log;

import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.Map;

import com.bicirikdwarf.utils.ElfUtils;

public class CompilationUnit {
	private final Dwarf32Context dwarf32;

	public int address;
	public int length;
	public int version;
	public int abbrev_offset;
	public int pointer_size;

	private Map<Integer, Abbrev> abbrevList;
	
	private DebugInfoEntry compileUnit;

	/**
	 * @param dwarf32
	 */
	CompilationUnit(Dwarf32Context dwarf32, int address) {
		this.dwarf32 = dwarf32;
		this.address = address;
	}

	public void parse(ByteBuffer buffer) {
		ElfUtils.dumpNextNBytes(buffer, 16);

		length = buffer.getInt();
		version = buffer.getShort();
		abbrev_offset = buffer.getInt();
		pointer_size = buffer.get();

		int initial_length_size = 4;
		if (length == 0xffffffff) {
			length = (int) buffer.getLong();
			initial_length_size = 12;
		}

		if (debugging())
			log("CompilationUnit l:" + length + " v:" + version + " abv off:"
					+ Integer.toHexString(abbrev_offset) + " adrsz:"
					+ pointer_size);

		abbrevList = getAbbrevSequence(abbrev_offset);
		this.compileUnit = DebugInfoEntry.parse(buffer, this, null);
		
		// Dwarf file format does not include length data to length count.
		buffer.position(address + length + initial_length_size);
	}

	public Map<Integer, Abbrev> getAbbrevSequence(int index) {
		Map<Integer, Abbrev> result = this.dwarf32.abbrevSequences
				.get(abbrev_offset);
		if (result == null)
			throw new InvalidParameterException("No abbrev sequence at 0x"
					+ Integer.toHexString(index));
		return result;
	}

	public Map<Integer, Abbrev> getAbbrevList() {
		return abbrevList;
	}

	public void setAbbrevList(Map<Integer, Abbrev> abbrevList) {
		this.abbrevList = abbrevList;
	}

	public Abbrev getAbbrev(int abbrevNumber) {
		if (!abbrevList.containsKey(abbrevNumber))
			throw new InvalidParameterException("Abbrev Sequence at 0x"
					+ Integer.toHexString(abbrev_offset)
					+ " does not contain an abbrev with tag "
					+ abbrevNumber);
		
		return abbrevList.get(abbrevNumber);
	}
	
	public Object getAttribValue( ByteBuffer buffer, DwFormType form ) {
		return dwarf32.getAttributeValue( form, this, buffer );
	}

	public Dwarf32Context getDwarfContext() {
		return dwarf32;
	}

	public DebugInfoEntry getCompileUnit() {
		return compileUnit;
	}
}