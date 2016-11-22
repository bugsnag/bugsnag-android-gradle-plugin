package com.bicirikdwarf.elf;

import static com.bicirikdwarf.utils.ElfUtils.debugging;
import static com.bicirikdwarf.utils.ElfUtils.log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bicirikdwarf.utils.ElfUtils;

// Note: Java does not support unsigned types. So we have to use one
// larger type for unsigned mappings. (@see Unsigned) class properly handles
// parsing.
// elf, C, Java
// addr = unsigned int, long
// half = unsigned short, int
// off = unsigned int, long
// sword = int, int
// word = unsigned int, long
public class Elf32Context {
	public static int EI_NIDENT = 16;
	public static int SHDR_SIZE = 40;

	final ByteBuffer elfBuffer;

	Ehdr ehdr;
	List<Sym> symbols;
	List<Shdr> shdrs;
	Map<String, Shdr> shdrsByName;
	String buildNote;

	private ByteBuffer shstrtab_buffer; // string-table section header
	private ByteBuffer strtab_buffer;

	public Elf32Context(ByteBuffer buffer) throws IOException {
		this.elfBuffer = buffer;

		ehdr = new Ehdr();
		shdrs = new ArrayList<>();
		shdrsByName = new HashMap<>();
		symbols = new ArrayList<>();

		init();
	}

	private void init() throws IOException {
		ehdr.parse(elfBuffer);

		for (int i = 0; i < ehdr.e_shnum; i++) {
			Shdr shdr = readSectionHeader(i);
			if (i == ehdr.e_shstrndx)
				this.shstrtab_buffer = this.getSectionBuffer(shdr);

			shdrs.add(shdr);
		}

		for (int i = 0; i < ehdr.e_shnum; i++) {
			String name = readSectionHeaderString((int) shdrs.get(i).sh_name);
			if (!name.isEmpty()) {
				Shdr shdr = shdrs.get(i);
				shdrsByName.put(name, shdr);
				if (debugging())
					log("Section n:" + name + " off:"
							+ Integer.toHexString((int) shdr.sh_offset)
							+ " addr:"
							+ Integer.toHexString((int) shdr.sh_addr) + " s:"
							+ Integer.toHexString((int) shdr.sh_size));
			}
		}

		strtab_buffer = getSectionBufferByName(".strtab");
		initSymbols();
		initBuildNote();
	}

	private void initSymbols() {
		Shdr symtab = getSectionByName(".symtab");
		if (symtab == null)
			return;

		ByteBuffer symtabBuffer = getSectionBuffer(symtab);

		while (symtabBuffer.remaining() >= 16) {
			int position = symtabBuffer.position();

			Sym symbol = new Sym();
			symbol.parse(symtabBuffer);

			String name = readString(symbol.st_name);
			if (!name.isEmpty()) {
				symbol.symbol_name = name;
				symbols.add(symbol);

				if (debugging())
					log("Symbol at:" + Integer.toHexString(position) + " n:"
							+ name + "  s:" + symbol.st_info);
			}

		}
	}

	private void initBuildNote() {
		Shdr buildNoteTab = getSectionByName(".note.gnu.build-id");
		if (buildNoteTab == null)
			return;

		ByteBuffer buildNoteBuffer = getSectionBuffer(buildNoteTab);

		// spec see p35 http://www.skyfree.org/linux/references/ELF_Format.pdf
		int namesz = buildNoteBuffer.getInt();
		int descsz = buildNoteBuffer.getInt();
		int type  = buildNoteBuffer.getInt();

		if (type == 3) {
			String name = "";

			for (int i = 0; i < namesz; i++) {
				char c = (char)buildNoteBuffer.get();

				if (c != '\0') {
					name += c;
				}
			}

			// the name will be padded out to 4 byte alignment
			if (namesz % 4 != 0) {
				for (int i = 0; i < namesz % 4; i++) {
					buildNoteBuffer.get();
				}
			}

			// Get the build ID
			String desc = "";
			for (int i = 0; i < descsz; i++) {
				byte b = (byte)buildNoteBuffer.get();

				desc += Integer.toHexString(Byte.toUnsignedInt(b));
			}

			buildNote = desc;
		}
	}

	public String getBuildNote() {
		return buildNote;
	}

	public Ehdr getElfHeader() {
		return ehdr;
	}

	public Set<String> getSectionNames() {
		return shdrsByName.keySet();
	}

	public Shdr getSectionByName(String name) {
		if (!shdrsByName.containsKey(name))
			return null;
		return shdrsByName.get(name);
	}

	public List<Sym> getSymbols() {
		return symbols;
	}

	public ByteBuffer getSectionBuffer(Shdr shdr) {
		return ElfUtils.cloneSection(elfBuffer, (int) shdr.sh_offset, (int) shdr.sh_size);
	}

	public ByteBuffer getSectionBufferByName(String sectionName)
			throws InvalidParameterException {
		Shdr shdr = getSectionByName(sectionName);

		if (shdr == null)
			throw new InvalidParameterException(sectionName
					+ " section not found.");

		return getSectionBuffer(shdr);
	}

	private Shdr readSectionHeader(int index) {
		Shdr result = new Shdr();
		int location = (int) (ehdr.e_shoff + (index * SHDR_SIZE));
		elfBuffer.position(location);
		result = new Shdr();
		result.parse(elfBuffer);
		return result;
	}

	public String readString(int offset) {
		strtab_buffer.position(offset);
		return ElfUtils.getNTString(strtab_buffer);
	}

	private String readSectionHeaderString(int offset) {
		shstrtab_buffer.position(offset);
		return ElfUtils.getNTString(shstrtab_buffer);
	}
}
