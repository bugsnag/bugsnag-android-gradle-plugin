package com.bicirikdwarf.dwarf;

import java.nio.ByteBuffer;

import com.bicirikdwarf.utils.Leb128;

public class AbbrevEntry {
	public DwAtType at;
	public DwFormType form;

	public void parse(ByteBuffer buffer) {
		at = DwAtType.byValue((int) Leb128.getULEB128(buffer));
		form = DwFormType.byValue((int) Leb128.getULEB128(buffer));
	}

	public boolean isNull() {
		return (at == null || at.value() == 0)
				&& (form == null || form.value() == 0);
	}
}