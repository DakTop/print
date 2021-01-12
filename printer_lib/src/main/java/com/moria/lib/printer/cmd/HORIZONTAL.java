package com.moria.lib.printer.cmd;

/**
 * Author  moria
 * Date    2020/9/11
 * Time    16:31
 */
public enum HORIZONTAL {
    LEFT(0),
    CENTER(1),
    RIGHT(2);

    private final int value;

    HORIZONTAL(int value) {
        this.value = value;
    }

    public byte getValue() {
        return (byte) this.value;
    }
}
