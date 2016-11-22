package com.bicirikdwarf.utils;

import java.nio.ByteBuffer;

// taken from:
// http://stackoverflow.com/questions/9883472/is-it-possible-to-have-an-unsigned-bytebuffer-in-java
public class Unsigned {
	public static short getU8(ByteBuffer bb) {
		return ((short) (bb.get() & 0xff));
	}

	public static void putU8(ByteBuffer bb, int value) {
		bb.put((byte) (value & 0xff));
	}

	public static short getU8(ByteBuffer bb, int position) {
		return ((short) (bb.get(position) & (short) 0xff));
	}

	public static void putU8(ByteBuffer bb, int position, int value) {
		bb.put(position, (byte) (value & 0xff));
	}

	// ---------------------------------------------------------------

	public static int getU16(ByteBuffer bb) {
		return (bb.getShort() & 0xffff);
	}

	public static void putU16(ByteBuffer bb, int value) {
		bb.putShort((short) (value & 0xffff));
	}

	public static int getU16(ByteBuffer bb, int position) {
		return (bb.getShort(position) & 0xffff);
	}

	public static void putU16(ByteBuffer bb, int position, int value) {
		bb.putShort(position, (short) (value & 0xffff));
	}

	// ---------------------------------------------------------------

	public static long getU32(ByteBuffer bb) {
		return (bb.getInt() & 0xffffffffL);
	}

	public static void putU32(ByteBuffer bb, long value) {
		bb.putInt((int) (value & 0xffffffffL));
	}

	public static long getU32(ByteBuffer bb, int position) {
		return (bb.getInt(position) & 0xffffffffL);
	}

	public static void putU32(ByteBuffer bb, int position, long value) {
		bb.putInt(position, (int) (value & 0xffffffffL));
	}
}
