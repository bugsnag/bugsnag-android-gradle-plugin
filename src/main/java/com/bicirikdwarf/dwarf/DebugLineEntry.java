package com.bicirikdwarf.dwarf;

/**
 * Class to store a location in a file
 */
public class DebugLineEntry {

    private long address;
    private String filename;
    private int lineNumber;

    DebugLineEntry(long address, String filename, int lineNumber) {
        this.address = address;
        this.filename = filename;
        this.lineNumber = lineNumber;
    }

    public long getAddress() {
        return address;
    }

    public void setAddress(long address) {
        this.address = address;
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
