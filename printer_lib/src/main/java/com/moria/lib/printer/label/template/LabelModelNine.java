package com.moria.lib.printer.label.template;

import android.text.TextUtils;

import com.moria.lib.printer.helper.PrinterHelper;
import com.moria.lib.printer.label.bean.LabelPoint;
import com.moria.lib.printer.label.bean.LabelProductInfo;
import com.moria.lib.printer.label.cmd.LabelCommand;
import com.moria.lib.printer.label.cmd.LabelHelper;
import com.moria.lib.printer.label.cmd.LabelPointFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Vector;


/**
 * Author  moria
 * Date    2020/9/14
 * Time    12:53
 */
public class LabelModelNine extends LabelModel {

    private static final float gap = 3f;
    private int sizeX = 70;
    private int sizeY = 38;
    private LabelPointFactory mLabelPointFactory = new LabelPointFactory(sizeX, sizeY);

    private LabelPoint shopName = mLabelPointFactory.createPointWithRatioXY(0.390f, 0.050f, 8, 4);
    private LabelPoint unit = mLabelPointFactory.createPointWithRatioXY(0.507f, 0.500f, 8, 4);

    private LabelPoint shopProductPrice = mLabelPointFactory.createPointWithRatioXY(0.285f, 0.684f, -8, 8 * 2);

    private List<LabelProductInfo> productInfoList;

    public LabelModelNine(List<LabelProductInfo> productInfoList) {
        this.productInfoList = productInfoList;
    }

    //单行1号字体
    private LabelPoint shopProductNameSmallSingleLine = mLabelPointFactory.createPointWithRatioXY(0.200f, 0.220f, -2 * 8, 8);
    //单行2号字体
    private LabelPoint shopProductNameBigSingleLine = mLabelPointFactory.createPointWithRatioXY(0.220f, 0.180f, -2 * 8, 8);
    //字符超过16为 上下显示
    private LabelPoint productNameLineFirst = mLabelPointFactory.createPointWithRatioXY(0.200f, 0.190f, -2 * 8, 8);
    private LabelPoint productNameLineSecond = mLabelPointFactory.createPointWithRatioXY(0.200f, 0.269f, -2 * 8, 8);
    //
    private static final int BIG_FONT_SIZE_SINGLE_LINE_COUNT = 18;
    private static final int SMALL_FONT_SIZE_SINGLE_LINE_COUNT = 38;


    public byte[] getPrintBytes() {
        //标签打印
        LabelCommand tsc = new LabelCommand();
        tsc.addSize(sizeX, sizeY);
        tsc.addGap(gap);
        tsc.addDirection(direction, LabelCommand.MIRROR.NORMAL); // 设置打印方向
        tsc.addReference(0, 0);
        tsc.addTear(LabelCommand.ENABLE.ON);
        //tsc.addHome();
        tsc.addCls();// 清除打印缓冲区
        tsc.addDensity(LabelCommand.DENSITY.DNESITY15);

        for (int i = 0; i < productInfoList.size(); i++) {
            LabelProductInfo productInfo = productInfoList.get(i);
            addMyText(tsc, productInfo);
            tsc.addPrint(1, 1);
            tsc.addCls();
        }

        Vector<Byte> datas = tsc.getCommand();
        return LabelHelper.ByteTo_byte(datas);
    }

    private void addMyText(LabelCommand tsc, LabelProductInfo productInfo) {
        if (!TextUtils.isEmpty(productInfo.shopName)) {
            addChineseText(tsc, shopName, toSmallerString(productInfo.shopName, 11));
        }
        int nameLength = getBytesLength(productInfo.shopProductName);
        String firstLineName = "";
        String secondLineName = "";
        try {
            if (nameLength > SMALL_FONT_SIZE_SINGLE_LINE_COUNT) {//有二行文字
                firstLineName = PrinterHelper.getStrByByte(productInfo.shopProductName, SMALL_FONT_SIZE_SINGLE_LINE_COUNT);
                secondLineName = productInfo.shopProductName.substring(firstLineName.length());
                //
                addChineseText(tsc, productNameLineFirst, firstLineName);
                addChineseText(tsc, productNameLineSecond, secondLineName);
            } else {//有一行文字
                if (nameLength > BIG_FONT_SIZE_SINGLE_LINE_COUNT) { //判断字符长度是否超多大字体单行所限制的字数
                    addChineseText(tsc, shopProductNameSmallSingleLine, productInfo.shopProductName);
                } else {
                    addChineseText(tsc, shopProductNameBigSingleLine, productInfo.shopProductName, LabelCommand.FONTMUL.MUL_2);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(productInfo.unit)) {
            addChineseText(tsc, unit, productInfo.unit);
        }
        if (!TextUtils.isEmpty(productInfo.shopProductPrice)) {
            BigDecimal price = new BigDecimal(productInfo.shopProductPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
            int priceYuan = price.intValue();
            String shouJia = price.toString();

            LabelCommand.FONTTYPE fontType;
            LabelCommand.FONTMUL fontMul = LabelCommand.FONTMUL.MUL_1;
            int adJustY = 0;
            if (priceYuan > 9999) {
                shouJia = priceYuan + "";
                fontType = LabelCommand.FONTTYPE.FONT_4;
            } else if (priceYuan > 99) {
                fontType = LabelCommand.FONTTYPE.FONT_4;
            } else {
                fontType = LabelCommand.FONTTYPE.FONT_3;
                fontMul = LabelCommand.FONTMUL.MUL_2;
                adJustY = -16;
            }
            tsc.addText(shopProductPrice.x, shopProductPrice.y + adJustY, fontType, LabelCommand.ROTATION.ROTATION_0, fontMul, fontMul, shouJia);
        }
    }

}
