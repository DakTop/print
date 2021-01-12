package com.moria.lib.printer.cmd;

/**
 * Author  moria
 * Date    2020/9/11
 * Time    16:31
 */
public enum FONT {
    FONTA(0),
    FONTB(1),
    FONTC(2);

    private final int value;

    FONT(int value) {
        this.value = value;
    }

    public byte getValue() {
        return (byte) this.value;
    }
}
