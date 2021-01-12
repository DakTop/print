package com.moria.lib.printer.label.template;

import android.annotation.SuppressLint;

import com.moria.lib.printer.label.bean.LabelPoint;
import com.moria.lib.printer.label.bean.LabelProductInfo;
import com.moria.lib.printer.label.cmd.LabelCommand;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Author  moria
 * Date    2020/9/14
 * Time    10:53
 */
public abstract class LabelModel {

    @SuppressWarnings("unused")
    static final int DEFAULT_SHAN_DI = (int) (1.3f * 8); // 衫底, 打印机默认的衫底, 打印机会把所有的x都加上1.3mm
    protected LabelCommand.DIRECTION direction = LabelCommand.DIRECTION.BACKWARD;//默认是正向
    protected int printCount = 1;

    public abstract byte[] getPrintBytes();

    protected void addChineseText(LabelCommand tsc, LabelPoint LabelPoint, String label) {
        tsc.addText(LabelPoint.x, LabelPoint.y, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, label);
    }

    protected void addChineseText(LabelCommand tsc, LabelCommand.FONTTYPE font, LabelPoint LabelPoint, LabelCommand.FONTMUL scale, String label) {
        tsc.addText(LabelPoint.x, LabelPoint.y, font, LabelCommand.ROTATION.ROTATION_0, scale, scale, label);
    }

    protected void addChineseText(LabelCommand tsc, LabelPoint LabelPoint, String label, LabelCommand.FONTMUL scale) {
        tsc.addText(LabelPoint.x, LabelPoint.y, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, scale, scale, label);
    }

    /**
     * FORWARD(0), BACKWARD(1);
     *
     * @param direction
     */
    public void setDirection(LabelCommand.DIRECTION direction) {
        this.direction = direction;
    }

    /**
     * 每个价签打印张数
     *
     * @param printCount
     */
    public void setPrintCount(int printCount) {
        this.printCount = printCount;
    }

    protected String toSmallerString(String origin, int largestLen) {
        int length = origin.length();
        String smaller;
        if (length > largestLen) {
            smaller = origin.substring(0, largestLen) + "...";
        } else {
            smaller = origin;
        }
        return smaller;
    }

    /**
     * 获取数据长度
     *
     * @param msg
     * @return
     */
    @SuppressLint("NewApi")
    protected static int getBytesLength(String msg) {
        return msg.getBytes(Charset.forName("GB2312")).length;
    }

}
