package com.bicirikdwarf.elf;

public enum ElfMachineType {
	EM_NONE(0), // No machine
	EM_M32(1), // AT&T WE 32100
	EM_SPARC(2), // SUN SPARC
	EM_386(3), // Intel 80386
	EM_68K(4), // Motorola m68k family
	EM_88K(5), // Motorola m88k family
	EM_486(6), // Intel 80486// Reserved for future use
	EM_860(7), // Intel 80860
	EM_MIPS(8), // MIPS R3000 (officially, big-endian only)
	EM_S370(9), // IBM System/370
	EM_MIPS_RS3_LE(10), // MIPS R3000 little-endian (Oct 4 1999 Draft)
						// Deprecated
	EM_res011(11), // Reserved
	EM_res012(12), // Reserved
	EM_res013(13), // Reserved
	EM_res014(14), // Reserved
	EM_PARISC(15), // HPPA
	EM_res016(16), // Reserved
	EM_VPP550(17), // Fujitsu VPP500
	EM_SPARC32PLUS(18), // Sun's "v8plus"
	EM_960(19), // Intel 80960
	EM_PPC(20), // PowerPC
	EM_PPC64(21), // 64-bit PowerPC
	EM_S390(22), // IBM S/390
	EM_SPU(23), // Sony/Toshiba/IBM SPU
	EM_res024(24), // Reserved
	EM_res025(25), // Reserved
	EM_res026(26), // Reserved
	EM_res027(27), // Reserved
	EM_res028(28), // Reserved
	EM_res029(29), // Reserved
	EM_res030(30), // Reserved
	EM_res031(31), // Reserved
	EM_res032(32), // Reserved
	EM_res033(33), // Reserved
	EM_res034(34), // Reserved
	EM_res035(35), // Reserved
	EM_V800(36), // NEC V800 series
	EM_FR20(37), // Fujitsu FR20
	EM_RH32(38), // TRW RH32
	EM_MCORE(39), // Motorola M*Core May also be taken by Fujitsu MMA

	EM_RCE(39), // Old name for MCore
	EM_ARM(40), // ARM
	EM_OLD_ALPHA(41), // Digital Alpha
	EM_SH(42), // Renesas (formerly Hitachi) / SuperH SH
	EM_SPARCV9(43), // SPARC v9 64-bit
	EM_TRICORE(44), // Siemens Tricore embedded processor
	EM_ARC(45), // ARC Cores
	EM_H8_300(46), // Renesas (formerly Hitachi) H8/300
	EM_H8_300H(47), // Renesas (formerly Hitachi) H8/300H
	EM_H8S(48), // Renesas (formerly Hitachi) H8S
	EM_H8_500(49), // Renesas (formerly Hitachi) H8/500
	EM_IA_64(50), // Intel IA-64 Processor
	EM_MIPS_X(51), // Stanford MIPS-X
	EM_COLDFIRE(52), // Motorola Coldfire
	EM_68HC12(53), // Motorola M68HC12
	EM_MMA(54), // Fujitsu Multimedia Accelerator
	EM_PCP(55), // Siemens PCP
	EM_NCPU(56), // Sony nCPU embedded RISC processor
	EM_NDR1(57), // Denso NDR1 microprocessor
	EM_STARCORE(58), // Motorola Star*Core processor
	EM_ME16(59), // Toyota ME16 processor
	EM_ST100(60), // STMicroelectronics ST100 processor
	EM_TINYJ(61), // Advanced Logic Corp. TinyJ embedded processor

	EM_X86_64(62), // Advanced Micro Devices X86-64 processor
	EM_PDSP(63), // Sony DSP Processor
	EM_PDP10(64), // Digital Equipment Corp. PDP-10
	EM_PDP11(65), // Digital Equipment Corp. PDP-11
	EM_FX66(66), // Siemens FX66 microcontroller
	EM_ST9PLUS(67), // STMicroelectronics ST9+ 8/16 bit microcontroller

	EM_ST7(68), // STMicroelectronics ST7 8-bit microcontroller

	EM_68HC16(69), // Motorola MC68HC16 Microcontroller
	EM_68HC11(70), // Motorola MC68HC11 Microcontroller
	EM_68HC08(71), // Motorola MC68HC08 Microcontroller
	EM_68HC05(72), // Motorola MC68HC05 Microcontroller
	EM_SVX(73), // Silicon Graphics SVx
	EM_ST19(74), // STMicroelectronics ST19 8-bit cpu
	EM_VAX(75), // Digital VAX
	EM_CRIS(76), // Axis Communications 32-bit embedded processor

	EM_JAVELIN(77), // Infineon Technologies 32-bit embedded cpu

	EM_FIREPATH(78), // Element 14 64-bit DSP processor
	EM_ZSP(79), // LSI Logic's 16-bit DSP processor
	EM_MMIX(80), // Donald Knuth's educational 64-bit processor

	EM_HUANY(81), // Harvard's machine-independent format
	EM_PRISM(82), // SiTera Prism
	EM_AVR(83), // Atmel AVR 8-bit microcontroller
	EM_FR30(84), // Fujitsu FR30
	EM_D10V(85), // Mitsubishi D10V
	EM_D30V(86), // Mitsubishi D30V
	EM_V850(87), // Renesas V850 (formerly NEC V850)
	EM_M32R(88), // Renesas M32R (formerly Mitsubishi M32R)
	EM_MN10300(89), // Matsushita MN10300
	EM_MN10200(90), // Matsushita MN10200
	EM_PJ(91), // picoJava
	EM_OPENRISC(92), // OpenRISC 32-bit embedded processor
	EM_ARC_A5(93), // ARC Cores Tangent-A5
	EM_XTENSA(94), // Tensilica Xtensa Architecture
	EM_VIDEOCORE(95), // Alphamosaic VideoCore processor
	EM_TMM_GPP(96), // Thompson Multimedia General Purpose Processor

	EM_NS32K(97), // National Semiconductor 32000 series
	EM_TPC(98), // Tenor Network TPC processor
	EM_SNP1K(99), // Trebia SNP 1000 processor
	EM_ST200(100), // STMicroelectronics ST200 microcontroller
	EM_IP2K(101), // Ubicom IP2022 micro controller
	EM_MAX(102), // MAX Processor
	EM_CR(103), // National Semiconductor CompactRISC
	EM_F2MC16(104), // Fujitsu F2MC16
	EM_MSP430(105), // TI msp430 micro controller
	EM_BLACKFIN(106), // ADI Blackfin
	EM_SE_C33(107), // S1C33 Family of Seiko Epson processors
	EM_SEP(108), // Sharp embedded microprocessor
	EM_ARCA(109), // Arca RISC Microprocessor
	EM_UNICORE(110), // Microprocessor series from PKU-Unity Ltd. and MPRC of
						// Peking University

	EM_EXCESS(111), // eXcess: 16/32/64-bit configurable embedded CPU

	EM_DXP(112), // Icera Semiconductor Inc. Deep Execution Processor

	EM_ALTERA_NIOS2(113), // Altera Nios II soft-core processor

	EM_CRX(114), // National Semiconductor CRX
	EM_XGATE(115), // Motorola XGATE embedded processor
	EM_C166(116), // Infineon C16x/XC16x processor
	EM_M16C(117), // Renesas M16C series microprocessors
	EM_DSPIC30F(118), // Microchip Technology dsPIC30F Digital Signal Controller

	EM_CE(119), // Freescale Communication Engine RISC core
	EM_M32C(120), // Renesas M32C series microprocessors
	EM_res121(121), // Reserved
	EM_res122(122), // Reserved
	EM_res123(123), // Reserved
	EM_res124(124), // Reserved
	EM_res125(125), // Reserved
	EM_res126(126), // Reserved
	EM_res127(127), // Reserved
	EM_res128(128), // Reserved
	EM_res129(129), // Reserved
	EM_res130(130), // Reserved
	EM_TSK3000(131), // Altium TSK3000 core
	EM_RS08(132), // Freescale RS08 embedded processor
	EM_res133(133), // Reserved
	EM_ECOG2(134), // Cyan Technology eCOG2 microprocessor
	EM_SCORE(135), // Sunplus Score
	EM_SCORE7(135), // Sunplus S+core7 RISC processor
	EM_DSP24(136), // New Japan Radio (NJR) 24-bit DSP Processor

	EM_VIDEOCORE3(137), // Broadcom VideoCore III processor
	EM_LATTICEMICO32(138), // RISC processor for Lattice FPGA architecture

	EM_SE_C17(139), // Seiko Epson C17 family
	EM_TI_C6000(140), // Texas Instruments TMS320C6000 DSP family

	EM_TI_C2000(141), // Texas Instruments TMS320C2000 DSP family

	EM_TI_C5500(142), // Texas Instruments TMS320C55x DSP family

	EM_res143(143), // Reserved
	EM_res144(144), // Reserved
	EM_res145(145), // Reserved
	EM_res146(146), // Reserved
	EM_res147(147), // Reserved
	EM_res148(148), // Reserved
	EM_res149(149), // Reserved
	EM_res150(150), // Reserved
	EM_res151(151), // Reserved
	EM_res152(152), // Reserved
	EM_res153(153), // Reserved
	EM_res154(154), // Reserved
	EM_res155(155), // Reserved
	EM_res156(156), // Reserved
	EM_res157(157), // Reserved
	EM_res158(158), // Reserved
	EM_res159(159), // Reserved
	EM_MMDSP_PLUS(160), // STMicroelectronics 64bit VLIW Data Signal Processor

	EM_CYPRESS_M8C(161), // Cypress M8C microprocessor
	EM_R32C(162), // Renesas R32C series microprocessors
	EM_TRIMEDIA(163), // NXP Semiconductors TriMedia architecture family

	EM_QDSP6(164), // QUALCOMM DSP6 Processor
	EM_8051(165), // Intel 8051 and variants
	EM_STXP7X(166), // STMicroelectronics STxP7x family
	EM_NDS32(167), // Andes Technology compact code size embedded RISC processor
					// family

	EM_ECOG1(168), // Cyan Technology eCOG1X family
	EM_ECOG1X(168), // Cyan Technology eCOG1X family
	EM_MAXQ30(169), // Dallas Semiconductor MAXQ30 Core Micro-controllers

	EM_XIMO16(170), // New Japan Radio (NJR) 16-bit DSP Processor

	EM_MANIK(171), // M2000 Reconfigurable RISC Microprocessor
	EM_CRAYNV2(172), // Cray Inc. NV2 vector architecture
	EM_RX(173), // Renesas RX family
	EM_METAG(174), // Imagination Technologies META processor architecture

	EM_MCST_ELBRUS(175), // MCST Elbrus general purpose hardware architecture

	EM_ECOG16(176), // Cyan Technology eCOG16 family
	EM_CR16(177), // National Semiconductor CompactRISC 16-bit processor

	EM_ETPU(178), // Freescale Extended Time Processing Unit
	EM_SLE9X(179), // Infineon Technologies SLE9X core
	EM_L1OM(180), // Intel L1OM
	EM_K1OM(181), // Intel K1OM
	EM_INTEL182(182), // Reserved by Intel
	EM_res183(183), // Reserved by ARM
	EM_res184(184), // Reserved by ARM
	EM_AVR32(185), // Atmel Corporation 32-bit microprocessor family

	EM_STM8(186), // STMicroeletronics STM8 8-bit microcontroller

	EM_TILE64(187), // Tilera TILE64 multicore architecture family

	EM_TILEPRO(188), // Tilera TILEPro multicore architecture family

	EM_MICROBLAZE(189), // Xilinx MicroBlaze 32-bit RISC soft processor core

	EM_CUDA(190), // NVIDIA CUDA architecture
	EM_TILEGX(191), // Tilera TILE-Gx multicore architecture family

	// picoJava
	EM_PJ_OLD(99),

	// D30V backend magic number. Written in the absence of an ABI.
	EM_CYGNUS_D30V(0x7676);

	private int value;

	ElfMachineType(int value) {
		this.value = value;
	}

	public static ElfMachineType byValue(int value) {
		for (ElfMachineType t : ElfMachineType.values()) {
			if (t.value == value)
				return t;
		}

		return null;
	}

	public int value() {
		return this.value;
	}
}
