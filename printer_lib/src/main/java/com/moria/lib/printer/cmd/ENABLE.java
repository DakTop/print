package com.moria.lib.printer.cmd;

/**
 * Author  moria
 * Date    2020/9/11
 * Time    16:31
 */
public enum ENABLE {
    OFF(0),
    ON(1);

    private final int value;

    ENABLE(int value) {
        this.value = value;
    }

    public byte getValue() {
        return (byte) this.value;
    }
}
