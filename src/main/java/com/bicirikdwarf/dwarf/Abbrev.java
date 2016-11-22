package com.bicirikdwarf.dwarf;

import static com.bicirikdwarf.utils.ElfUtils.debugging;
import static com.bicirikdwarf.utils.ElfUtils.log;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.bicirikdwarf.utils.Leb128;

public class Abbrev {
	public int number;
	public int offset;
	public DwTagType tag;
	public boolean has_children;

	public List<AbbrevEntry> entries;

	public Abbrev() {
		entries = new ArrayList<>();
	}

	public void parse(ByteBuffer buffer) {
		offset = buffer.position();
		number = (int) Leb128.getULEB128(buffer);
		if (number == 0)
			return;

		tag = DwTagType.byValue((int) Leb128.getULEB128(buffer));
		has_children = !(buffer.get() == 0);

		if (debugging())
			log("Abbrev pos:" + Integer.toHexString(offset) + " n:" + number
					+ " t:" + tag.toString());

		while (true) {
			AbbrevEntry entry = new AbbrevEntry();
			entry.parse(buffer);

			if (entry.isNull())
				break;

			if (debugging())
				log("  Entry at:" + entry.at.toString() + " form:"
						+ entry.form.toString());
			entries.add(entry);
		}
	}

	public List<AbbrevEntry> getEntries() {
		return entries;
	}
}