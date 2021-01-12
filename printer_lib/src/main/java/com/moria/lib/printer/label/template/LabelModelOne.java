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
public class LabelModelOne extends LabelModel {

    private static final float gap = 3f;
    private int sizeX = 50;
    private int sizeY = 35;
    private LabelPointFactory mLabelPointFactory = new LabelPointFactory(sizeX, sizeY);
    private LabelPoint shopProductPrice = mLabelPointFactory.createPointWithRatioXY(0.520f, 0.628f, -2 * 8, -4 * 8);
    private List<LabelProductInfo> productInfoList;

    public LabelModelOne(List<LabelProductInfo> productInfoList) {
        this.productInfoList = productInfoList;
    }

    private static final int BIG_FONT_SIZE_SINGLE_LINE_COUNT = 12;
    private static final int SMALL_FONT_SIZE_SINGLE_LINE_COUNT = 26;
    private static final int SMALL_FONT_SIZE_TWO_LINE_COUNT = 52;

    //单行1号字体
    private LabelPoint shopProductNameSmallSingleLine = mLabelPointFactory.createPointWithRatioXY(0.220f, 0.085f, -2 * 8, 0);
    //单行2号字体
    private LabelPoint shopProductNameBigSingleLine = mLabelPointFactory.createPointWithRatioXY(0.240f, 0.015f, -2 * 8, 8);

    //字符超过16为 上下显示
    private LabelPoint shopProductNameLineFirst = mLabelPointFactory.createPointWithRatioXY(0.220f, 0.002f, -2 * 8, 8);
    private LabelPoint shopProductNameLineSecond = mLabelPointFactory.createPointWithRatioXY(0.220f, 0.082f, -2 * 8, 8);
    private LabelPoint shopProductNameLineThird = mLabelPointFactory.createPointWithRatioXY(0.220f, 0.162f, -2 * 8, 8);

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

        if (!TextUtils.isEmpty(productInfo.shopProductName)) {
            int nameLength = getBytesLength(productInfo.shopProductName);
            String firstLineName = "";
            String secondLineName = "";
            String thirdLineName = "";
            try {
                if (nameLength > SMALL_FONT_SIZE_TWO_LINE_COUNT) {//有三行文字
                    firstLineName = PrinterHelper.getStrByByte(productInfo.shopProductName, SMALL_FONT_SIZE_SINGLE_LINE_COUNT);
                    secondLineName = PrinterHelper.getStrByByte(productInfo.shopProductName.substring(firstLineName.length()), SMALL_FONT_SIZE_SINGLE_LINE_COUNT);
                    thirdLineName = productInfo.shopProductName.substring(firstLineName.length() * 2);
                    //
                    addChineseText(tsc, shopProductNameLineFirst, firstLineName);
                    addChineseText(tsc, shopProductNameLineSecond, secondLineName);
                    addChineseText(tsc, shopProductNameLineThird, thirdLineName);
                } else if (nameLength > SMALL_FONT_SIZE_SINGLE_LINE_COUNT) {//有二行文字
                    firstLineName = PrinterHelper.getStrByByte(productInfo.shopProductName, SMALL_FONT_SIZE_SINGLE_LINE_COUNT);
                    secondLineName = productInfo.shopProductName.substring(firstLineName.length());
                    //
                    addChineseText(tsc, shopProductNameLineFirst, firstLineName);
                    addChineseText(tsc, shopProductNameLineSecond, secondLineName);
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
        }

        if (!TextUtils.isEmpty(productInfo.shopProductPrice)) {
            BigDecimal price = new BigDecimal(productInfo.shopProductPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
            int priceYuan = price.intValue();
            String shouJia = price.toString();

            LabelCommand.FONTTYPE fontType;
            LabelCommand.FONTMUL fontMul = LabelCommand.FONTMUL.MUL_1;
            if (priceYuan > 9999) {
                shouJia = priceYuan + "";
                fontType = LabelCommand.FONTTYPE.FONT_4;
            } else if (priceYuan > 99) {
                fontType = LabelCommand.FONTTYPE.FONT_4;
            } else {
                fontType = LabelCommand.FONTTYPE.FONT_3;
                fontMul = LabelCommand.FONTMUL.MUL_2;
            }
            tsc.addText(shopProductPrice.x, shopProductPrice.y, fontType, LabelCommand.ROTATION.ROTATION_0, fontMul, fontMul, shouJia);
        }
    }

}
