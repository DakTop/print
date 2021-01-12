package com.moria.lib.printer.label.bean;

/**
 * Author  moria
 * Date    2020/9/14
 * Time    10:56
 */
public class LabelPoint {
    public static final int COORDINATE_DIV_MM = 8; // coordinate除以纸片的比例

    public int x;
    public int y;

    public LabelPoint(int finalX, int finalY) {
        this.x = finalX;
        this.y = finalY;
    }

    public LabelPoint(int mmX, int mmY, int factorY) {
        this.x = mmX * COORDINATE_DIV_MM;
        this.y = mmY * COORDINATE_DIV_MM + factorY;
    }

}
