package com.bicirikdwarf.utils;

import java.io.PrintWriter;
import java.nio.ByteBuffer;

public class ElfUtils {

	public static PrintWriter debugLog = null;

	public static boolean debugging() {
		return debugLog != null;
	}

	public static void log(String line) {
		if (debugLog != null)
			debugLog.println(line);
	}

	/**
	 * Parses next null-terminated string.
	 */
	public static String getNTString(ByteBuffer buffer) {
		StringBuilder result = new StringBuilder();
		while (buffer.remaining() > 0) {
			char c = (char) buffer.get();
			if (c == '\0')
				break;
			result.append(c);
		}

		return result.toString();
	}

	public static void dumpNextNBytes(ByteBuffer buffer, int n) {
		if (!debugging())
			return;

		int oldPosition = buffer.position();

		byte[] bytesToRead = new byte[Math.min(buffer.remaining(), n)];

		buffer.get(bytesToRead);
		log(Integer.toHexString(oldPosition) + "  " + getHexString(bytesToRead));
		buffer.position(oldPosition);
	}

	public static String getHexString(byte[] data) {
		StringBuilder result = new StringBuilder();

		for (byte b : data) {
			String hex = Integer.toHexString(b);
			if (hex.length() == 1)
				hex = "0" + hex;
			result.append(hex);
		}

		return result.toString();
	}

	/**
	 * Please note that this moved the source buffer forward.
	 * @param source
	 * @param count
	 * @return
	 */
	public static ByteBuffer getByteBuffer( ByteBuffer source, int count ) {
		byte [] data = new byte[count];
		source.get(data);
		ByteBuffer result = ByteBuffer.wrap(data);
		result.order(source.order());
		return result;
	}
	
	public static ByteBuffer cloneSection( ByteBuffer source, int start, int count ) {
		int oldPosition = source.position();
		source.position(start);
		ByteBuffer result = source.slice();
		result.order(source.order());
		result.limit(count);
		source.position(oldPosition);
		return result;
	}
}
