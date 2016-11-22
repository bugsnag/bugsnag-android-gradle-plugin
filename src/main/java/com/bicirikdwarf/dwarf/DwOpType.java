package com.bicirikdwarf.dwarf;

public enum DwOpType {
	DW_OP_addr((byte) 0x03), //
	DW_OP_deref((byte) 0x06), //
	DW_OP_const1u((byte) 0x08), //
	DW_OP_const1s((byte) 0x09), //
	DW_OP_const2u((byte) 0x0a), //
	DW_OP_const2s((byte) 0x0b), //
	DW_OP_const4u((byte) 0x0c), //
	DW_OP_const4s((byte) 0x0d), //
	DW_OP_const8u((byte) 0x0e), //
	DW_OP_const8s((byte) 0x0f), //
	DW_OP_constu((byte) 0x10), //
	DW_OP_consts((byte) 0x11), //
	DW_OP_dup((byte) 0x12), //
	DW_OP_drop((byte) 0x13), //
	DW_OP_over((byte) 0x14), //
	DW_OP_pick((byte) 0x15), //
	DW_OP_swap((byte) 0x16), //
	DW_OP_rot((byte) 0x17), //
	DW_OP_xderef((byte) 0x18), //
	DW_OP_abs((byte) 0x19), //
	DW_OP_and((byte) 0x1a), //
	DW_OP_div((byte) 0x1b), //
	DW_OP_minus((byte) 0x1c), //
	DW_OP_mod((byte) 0x1d), //
	DW_OP_mul((byte) 0x1e), //
	DW_OP_neg((byte) 0x1f), //
	DW_OP_not((byte) 0x20), //
	DW_OP_or((byte) 0x21), //
	DW_OP_plus((byte) 0x22), //
	DW_OP_plus_uconst((byte) 0x23), //
	DW_OP_shl((byte) 0x24), //
	DW_OP_shr((byte) 0x25), //
	DW_OP_shra((byte) 0x26), //
	DW_OP_xor((byte) 0x27), //
	DW_OP_bra((byte) 0x28), //
	DW_OP_eq((byte) 0x29), //
	DW_OP_ge((byte) 0x2a), //
	DW_OP_gt((byte) 0x2b), //
	DW_OP_le((byte) 0x2c), //
	DW_OP_lt((byte) 0x2d), //
	DW_OP_ne((byte) 0x2e), //
	DW_OP_skip((byte) 0x2f), //
	DW_OP_lit0((byte) 0x30), //
	DW_OP_lit1((byte) 0x31), //
	DW_OP_lit2((byte) 0x32), //
	DW_OP_lit3((byte) 0x33), //
	DW_OP_lit4((byte) 0x34), //
	DW_OP_lit5((byte) 0x35), //
	DW_OP_lit6((byte) 0x36), //
	DW_OP_lit7((byte) 0x37), //
	DW_OP_lit8((byte) 0x38), //
	DW_OP_lit9((byte) 0x39), //
	DW_OP_lit10((byte) 0x3a), //
	DW_OP_lit11((byte) 0x3b), //
	DW_OP_lit12((byte) 0x3c), //
	DW_OP_lit13((byte) 0x3d), //
	DW_OP_lit14((byte) 0x3e), //
	DW_OP_lit15((byte) 0x3f), //
	DW_OP_lit16((byte) 0x40), //
	DW_OP_lit17((byte) 0x41), //
	DW_OP_lit18((byte) 0x42), //
	DW_OP_lit19((byte) 0x43), //
	DW_OP_lit20((byte) 0x44), //
	DW_OP_lit21((byte) 0x45), //
	DW_OP_lit22((byte) 0x46), //
	DW_OP_lit23((byte) 0x47), //
	DW_OP_lit24((byte) 0x48), //
	DW_OP_lit25((byte) 0x49), //
	DW_OP_lit26((byte) 0x4a), //
	DW_OP_lit27((byte) 0x4b), //
	DW_OP_lit28((byte) 0x4c), //
	DW_OP_lit29((byte) 0x4d), //
	DW_OP_lit30((byte) 0x4e), //
	DW_OP_lit31((byte) 0x4f), //
	DW_OP_reg0((byte) 0x50), //
	DW_OP_reg1((byte) 0x51), //
	DW_OP_reg2((byte) 0x52), //
	DW_OP_reg3((byte) 0x53), //
	DW_OP_reg4((byte) 0x54), //
	DW_OP_reg5((byte) 0x55), //
	DW_OP_reg6((byte) 0x56), //
	DW_OP_reg7((byte) 0x57), //
	DW_OP_reg8((byte) 0x58), //
	DW_OP_reg9((byte) 0x59), //
	DW_OP_reg10((byte) 0x5a), //
	DW_OP_reg11((byte) 0x5b), //
	DW_OP_reg12((byte) 0x5c), //
	DW_OP_reg13((byte) 0x5d), //
	DW_OP_reg14((byte) 0x5e), //
	DW_OP_reg15((byte) 0x5f), //
	DW_OP_reg16((byte) 0x60), //
	DW_OP_reg17((byte) 0x61), //
	DW_OP_reg18((byte) 0x62), //
	DW_OP_reg19((byte) 0x63), //
	DW_OP_reg20((byte) 0x64), //
	DW_OP_reg21((byte) 0x65), //
	DW_OP_reg22((byte) 0x66), //
	DW_OP_reg23((byte) 0x67), //
	DW_OP_reg24((byte) 0x68), //
	DW_OP_reg25((byte) 0x69), //
	DW_OP_reg26((byte) 0x6a), //
	DW_OP_reg27((byte) 0x6b), //
	DW_OP_reg28((byte) 0x6c), //
	DW_OP_reg29((byte) 0x6d), //
	DW_OP_reg30((byte) 0x6e), //
	DW_OP_reg31((byte) 0x6f), //
	DW_OP_breg0((byte) 0x70), //
	DW_OP_breg1((byte) 0x71), //
	DW_OP_breg2((byte) 0x72), //
	DW_OP_breg3((byte) 0x73), //
	DW_OP_breg4((byte) 0x74), //
	DW_OP_breg5((byte) 0x75), //
	DW_OP_breg6((byte) 0x76), //
	DW_OP_breg7((byte) 0x77), //
	DW_OP_breg8((byte) 0x78), //
	DW_OP_breg9((byte) 0x79), //
	DW_OP_breg10((byte) 0x7a), //
	DW_OP_breg11((byte) 0x7b), //
	DW_OP_breg12((byte) 0x7c), //
	DW_OP_breg13((byte) 0x7d), //
	DW_OP_breg14((byte) 0x7e), //
	DW_OP_breg15((byte) 0x7f), //
	DW_OP_breg16((byte) 0x80), //
	DW_OP_breg17((byte) 0x81), //
	DW_OP_breg18((byte) 0x82), //
	DW_OP_breg19((byte) 0x83), //
	DW_OP_breg20((byte) 0x84), //
	DW_OP_breg21((byte) 0x85), //
	DW_OP_breg22((byte) 0x86), //
	DW_OP_breg23((byte) 0x87), //
	DW_OP_breg24((byte) 0x88), //
	DW_OP_breg25((byte) 0x89), //
	DW_OP_breg26((byte) 0x8a), //
	DW_OP_breg27((byte) 0x8b), //
	DW_OP_breg28((byte) 0x8c), //
	DW_OP_breg29((byte) 0x8d), //
	DW_OP_breg30((byte) 0x8e), //
	DW_OP_breg31((byte) 0x8f), //
	DW_OP_regx((byte) 0x90), //
	DW_OP_fbreg((byte) 0x91), //
	DW_OP_bregx((byte) 0x92), //
	DW_OP_piece((byte) 0x93), //
	DW_OP_deref_size((byte) 0x94), //
	DW_OP_xderef_size((byte) 0x95), //
	DW_OP_nop((byte) 0x96), //
	DW_OP_push_object_address((byte) 0x97), // DWARF3
	DW_OP_call2((byte) 0x98), // DWARF3
	DW_OP_call4((byte) 0x99), // DWARF3
	DW_OP_call_ref((byte) 0x9a), // DWARF3
	DW_OP_form_tls_address((byte) 0x9b), // DWARF3f
	DW_OP_call_frame_cfa((byte) 0x9c), // DWARF3f
	DW_OP_bit_piece((byte) 0x9d), // DWARF3f
	DW_OP_implicit_value((byte) 0x9e), // DWARF4
	DW_OP_stack_value((byte) 0x9f), // DWARF4
	DW_OP_implicit_pointer((byte) 0xa0), // DWARF5
	DW_OP_addrx((byte) 0xa1), // DWARF5
	DW_OP_constx((byte) 0xa2), // DWARF5

	// GNU extensions.
	DW_OP_GNU_push_tls_address((byte) 0xe0), // GNU

	// Follows extension so dwarfdump prints the most-likely-useful name.
	DW_OP_lo_user((byte) 0xe0),

	DW_OP_GNU_uninit((byte) 0xf0), // GNU
	DW_OP_GNU_encoded_addr((byte) 0xf1), // GNU
	DW_OP_GNU_implicit_pointer((byte) 0xf2), // GNU
	DW_OP_GNU_entry_value((byte) 0xf3), // GNU
	DW_OP_GNU_const_type((byte) 0xf4), // GNU
	DW_OP_GNU_regval_type((byte) 0xf5), // GNU
	DW_OP_GNU_deref_type((byte) 0xf6), // GNU
	DW_OP_GNU_convert((byte) 0xf7), // GNU
	DW_OP_GNU_reinterpret((byte) 0xf9), // GNU
	DW_OP_GNU_parameter_ref((byte) 0xfa), // GNU
	DW_OP_GNU_addr_index((byte) 0xfb), // GNU DebugFission
	DW_OP_GNU_const_index((byte) 0xfc), // GNU DebugFission

	// HP extensions.
	DW_OP_HP_unknown((byte) 0xe0), // HP conflict: GNU
	DW_OP_HP_is_value((byte) 0xe1), // HP
	DW_OP_HP_fltconst4((byte) 0xe2), // HP
	DW_OP_HP_fltconst8((byte) 0xe3), // HP
	DW_OP_HP_mod_range((byte) 0xe4), // HP
	DW_OP_HP_unmod_range((byte) 0xe5), // HP
	DW_OP_HP_tls((byte) 0xe6), // HP

	DW_OP_INTEL_bit_piece((byte) 0xe8), // Intel: made obsolete by
										// DW_OP_bit_piece above.

	// Apple extension.
	DW_OP_APPLE_uninit((byte) 0xf0), // Apple
	DW_OP_PGI_omp_thread_num((byte) 0xf8), // PGI (STMicroelectronics)

	DW_OP_hi_user((byte) 0xff);

	private byte value;

	DwOpType(byte value) {
		this.value = value;
	}
	
	public static DwOpType byValue(int value) {
		for (DwOpType a : DwOpType.values()) {
			if (a.value == value)
				return a;
		}

		return null;
	}

	public byte value() {
		return value;
	}
}
