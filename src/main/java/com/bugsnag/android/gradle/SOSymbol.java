package com.bugsnag.android.gradle;

import com.bicirikdwarf.dwarf.DebugLineEntry;
import com.bicirikdwarf.elf.ElfSymbol;

/**
 * The Symbol information to send to Bugsnag
 */
public class SOSymbol {
    private long address;
    private String methodName;
    private String filename;
    private int lineNumber;

    public SOSymbol(ElfSymbol symbol) {
        address = symbol.st_value;
        methodName = symbol.symbol_name;
    }

    public SOSymbol(DebugLineEntry entry) {
        address = entry.getAddress();
        filename = entry.getFilename();
        lineNumber = entry.getLineNumber();
    }

    public long getAddress() {
        return address;
    }

    public void setAddress(long address) {
        this.address = address;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
}
