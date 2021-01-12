package com.moria.lib.printer.label.cmd;

import android.util.Log;

import com.moria.lib.printer.label.bean.LabelPoint;

/**
 * Author  moria
 * Date    2020/9/14
 * Time    10:53
 */
public class LabelPointFactory {
    
    private static final String TAG = LabelPointFactory.class.getSimpleName() + "/zmr";
    private int sizeWidth;
    private int sizeHeight;
    
    /**
     * 坐标构造器
     *
     * @param width 标签纸的宽度
     * @param height 标签纸的高度
     */
    public LabelPointFactory(int width, int height) {
        this.sizeWidth = width;
        this.sizeHeight = height;
    }
    
    /**
     *
     * @param x 相对原点的x轴毫米偏移量
     * @param y 相对原点的y轴毫米偏移量
     * @param factorY 相对原点的Y轴坐标微调偏移量
     * @return 构造的坐标点
     */
    @SuppressWarnings("unused")
    public LabelPoint createPoint(int x, int y, int factorY) {
        int finalX = x * LabelPoint.COORDINATE_DIV_MM;
        int finalY = y * LabelPoint.COORDINATE_DIV_MM * y + factorY;
        return new LabelPoint(finalX, finalY);
    }
    
    public LabelPoint createPointWithRatioByFactorY(float ratioX, float ratioY, int factorY) {
        if (sizeWidth == 0 || sizeHeight == 0) {
            Log.e(TAG, "size should bigger than 0, but sizeWidth = " + sizeWidth + " sizeHeight = " + sizeHeight);
        }
        int finalX = (int) (ratioX * sizeWidth) * LabelPoint.COORDINATE_DIV_MM;
        int finalY = (int) (ratioY * sizeHeight * LabelPoint.COORDINATE_DIV_MM + factorY);
        return new LabelPoint(finalX, finalY);
    }
    
    /**
     *
     * @param ratioX 相对原点的X方向的距离占据宽度的比例
     * @param ratioY 相对原点的Y方向的距离占据高度的比例
     * @param factorX 相对原点的X轴坐标微调偏移量
     * @param factorY 相对原点的Y轴坐标微调偏移量
     * @return 构造的坐标点
     */
    public LabelPoint createPointWithRatioXY(float ratioX, float ratioY, int factorX, int factorY) {
        if (sizeWidth == 0 || sizeHeight == 0) {
            Log.e(TAG, "size should bigger than 0, but sizeWidth = " + sizeWidth + " sizeHeight = " + sizeHeight);
        }
        int finalX = (int) (ratioX * sizeWidth * LabelPoint.COORDINATE_DIV_MM + factorX);
        int finalY = (int) (ratioY * sizeHeight * LabelPoint.COORDINATE_DIV_MM + factorY);
        return new LabelPoint(finalX, finalY);
    }
}
