package com.bicirikdwarf.elf;

public enum RelocationType {
	R_386_NONE((byte) 0), // No reloc
	R_386_32((byte) 1), // Direct 32 bit
	R_386_PC32((byte) 2), // PC relative 32 bit
	R_386_GOT32((byte) 3), // 32 bit GOT entry
	R_386_PLT32((byte) 4), // 32 bit PLT address
	R_386_COPY((byte) 5), // Copy symbol at runtime
	R_386_GLOB_DAT((byte) 6), // Create GOT entry
	R_386_JUMP_SLOT((byte) 7), // Create PLT entry
	R_386_RELATIVE((byte) 8), // Adjust by program base
	R_386_GOTOFF((byte) 9), // 32 bit offset to GOT
	R_386_GOTPC((byte) 10), // 32 bit PC relative offset to GOT
	R_386_32PLT((byte) 11), // Used by Sun
	FIRST_INVALID_RELOC((byte) 12), //
	LAST_INVALID_RELOC((byte) 13), //
	R_386_TLS_TPOFF((byte) 14), //
	R_386_TLS_IE((byte) 15), //
	R_386_TLS_GOTIE((byte) 16), //
	R_386_TLS_LE((byte) 17), //
	R_386_TLS_GD((byte) 18), //
	R_386_TLS_LDM((byte) 19), //
	R_386_16((byte) 20), //
	R_386_PC16((byte) 21), //
	R_386_8((byte) 22), //
	R_386_PC8((byte) 23), //
	R_386_TLS_GD_32((byte) 24), //
	R_386_TLS_GD_PUSH((byte) 25), //
	R_386_TLS_GD_CALL((byte) 26), //
	R_386_TLS_GD_POP((byte) 27), //
	R_386_TLS_LDM_32((byte) 28), //
	R_386_TLS_LDM_PUSH((byte) 29), //
	R_386_TLS_LDM_CALL((byte) 30), //
	R_386_TLS_LDM_POP((byte) 31), //
	R_386_TLS_LDO_32((byte) 32), //
	R_386_TLS_IE_32((byte) 33), //
	R_386_TLS_LE_32((byte) 34), //
	R_386_TLS_DTPMOD32((byte) 35), //
	R_386_TLS_DTPOFF32((byte) 36), //
	R_386_TLS_TPOFF32((byte) 37), //
	R_386_TLS_GOTDESC((byte) 39), //
	R_386_TLS_DESC_CALL((byte) 40), //
	R_386_TLS_DESC((byte) 41), //
	R_386_IRELATIVE((byte) 42), // Adjust indirectly by program base
	R_386_USED_BY_INTEL_200((byte) 200), //
	R_386_GNU_VTINHERIT((byte) 250), //
	R_386_GNU_VTENTRY((byte) 251); //

	private byte value;

	RelocationType(byte value) {
		this.value = value;
	}

	public byte value() {
		return value;
	}
}
