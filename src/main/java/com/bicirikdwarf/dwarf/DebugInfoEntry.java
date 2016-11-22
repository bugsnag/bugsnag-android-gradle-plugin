package com.bicirikdwarf.dwarf;

import com.bicirikdwarf.utils.ElfUtils;
import com.bicirikdwarf.utils.Leb128;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bicirikdwarf.utils.ElfUtils.debugging;
import static com.bicirikdwarf.utils.ElfUtils.log;

public class DebugInfoEntry {
	public int address;
	DebugInfoEntry parent;
	DebugInfoEntry sibling;
	List<DebugInfoEntry> children;
	CompilationUnit cu;

	private Map<DwAtType, Object> attribs;
	private Abbrev abbrev;

	protected DebugInfoEntry(CompilationUnit cu, DebugInfoEntry parent) {
		this.children = new ArrayList<>();
		this.cu = cu;
		this.attribs = new HashMap<>();
		this.parent = parent;
	}

	public static DebugInfoEntry parse(ByteBuffer buffer, CompilationUnit cu, DebugInfoEntry parent) {
		ElfUtils.dumpNextNBytes(buffer, 8);
		DebugInfoEntry result = new DebugInfoEntry(cu, parent);

		int address = buffer.position();
		int abbrevNumber = (int) Leb128.getULEB128(buffer);
		if (abbrevNumber == 0)
			return null;

		Abbrev abbrev = cu.getAbbrev(abbrevNumber);

		result.address = address;
		cu.getDwarfContext().registerDie(result);
		result.abbrev = abbrev;

		if (debugging())
			log("Abv no:" + abbrevNumber + " off:" + Integer.toHexString(abbrev.offset) + " tag:"
					+ abbrev.tag.toString());

		for (AbbrevEntry entry : abbrev.entries) {
			Object value = cu.getAttribValue(buffer, entry.form);

			result.attribs.put(entry.at, value);

			if (debugging())
				log("  " + entry.at.toString() + "=" + value.toString() + " : " + entry.form.toString());
		}

		if (abbrev.has_children) {
			while (true) {
				DebugInfoEntry child = parse(buffer, cu, result);
				if (child == null)
					break;
				result.children.add(child);
			}
		}

		return result;
	}

	public int getAddress() {
		return address;
	}

	public DebugInfoEntry getParent() {
		return this.parent;
	}

	public List<DebugInfoEntry> getChildren() {
		return children;
	}

	public Abbrev getAbbrev() {
		return abbrev;
	}

	public DebugInfoEntry getSibling() {
		return null;
	}

	public Object getAttribValue(DwAtType at) {
		return attribs.get(at);
	}

	public Map<DwAtType, Object> getAttribs() {
		return attribs;
	}
}
