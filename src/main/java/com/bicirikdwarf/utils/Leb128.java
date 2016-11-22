package com.bicirikdwarf.utils;

import java.nio.ByteBuffer;

public class Leb128 {
	public static long getULEB128(ByteBuffer buf) {
		long val = 0;
		byte b;
		int shift = 0;

		while (true) {
			b = buf.get();
			val |= ((long) (b & 0x7f)) << shift;
			if ((b & 0x80) == 0)
				break;
			shift += 7;
		}

		return val;
	}

	public static int getSLEB128(ByteBuffer buffer) {
		int result = 0;
		for (int i = 0; i < 5; i++) {
			byte b = buffer.get();
			result |= ((b & 0x7f) << (7 * i));
			if ((b & 0x80) == 0) {
				int s = 32 - (7 * (i + 1));
				result = (result << s) >> s;
				break;
			}
		}
		return result;
	}

}
