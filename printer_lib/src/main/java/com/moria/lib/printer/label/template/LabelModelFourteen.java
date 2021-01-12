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
public class LabelModelFourteen extends LabelModel {

    private static final int CODE_HEIGHT = 60;
    private static final float gap = 3f;
    private int sizeX = 70;
    private int sizeY = 50;
    private LabelPointFactory mLabelPointFactory = new LabelPointFactory(sizeX, sizeY);

    private LabelPoint shopProductName = mLabelPointFactory.createPointWithRatioByFactorY(0.097f, 0.088f, 0);
    private LabelPoint barCode = mLabelPointFactory.createPointWithRatioXY(0.171f, 0.630f, 2 * 8, 3 * 8);

    private LabelPoint priceBackground = mLabelPointFactory.createPointWithRatioByFactorY(0.307f, 0.540f, 0);

    private LabelPoint shopProductPrice = mLabelPointFactory.createPointWithRatioByFactorY(0.350f, 0.490f, 2 * 8);

    private List<LabelProductInfo> productInfoList;

    public LabelModelFourteen(List<LabelProductInfo> productInfoList) {
        this.productInfoList = productInfoList;
    }

    //大字号文字位置
    private LabelPoint productNameLineFirst = mLabelPointFactory.createPointWithRatioXY(0.070f, 0.060f, -2 * 8, 8);
    private LabelPoint productNameLineSecond = mLabelPointFactory.createPointWithRatioXY(0.070f, 0.180f, -2 * 8, 8);

    //小字号文字位置
    private LabelPoint smallProductNameLineFirst = mLabelPointFactory.createPointWithRatioXY(0.070f, 0.120f, -2 * 8, 8);
    private LabelPoint smallProductNameLineSecond = mLabelPointFactory.createPointWithRatioXY(0.070f, 0.199f, -2 * 8, 8);
    //
    private static final int FONT_SIZE_SINGLE_LINE_COUNT = 22;
    private static final int FONT_SIZE_TWO_LINE_COUNT = 44;

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
        tsc.addDensity(LabelCommand.DENSITY.DNESITY10);

        for (int i = 0; i < productInfoList.size(); i++) {
            LabelProductInfo productInfo = productInfoList.get(i);
            addMyText(tsc, productInfo);
            byte narrow = 3;
            byte width = 3;
            tsc.add1DBarcodeNarrowWidth(barCode.x, barCode.y, LabelCommand.BARCODETYPE.CODE128, CODE_HEIGHT, LabelCommand.READABEL.EANBEL, LabelCommand.ROTATION.ROTATION_0, productInfo.barCode, narrow, width);
            tsc.addPrint(1, 1);
            tsc.addCls();
        }

        Vector<Byte> datas = tsc.getCommand();
        return LabelHelper.ByteTo_byte(datas);
    }

    private void addMyText(LabelCommand tsc, LabelProductInfo productInfo) {
        int nameLength = getBytesLength(productInfo.shopProductName);
        String firstLineName = "";
        String secondLineName = "";
        try {
            if (nameLength > FONT_SIZE_TWO_LINE_COUNT) {//有三行文字
                firstLineName = PrinterHelper.getStrByByte(productInfo.shopProductName, FONT_SIZE_TWO_LINE_COUNT);
                secondLineName = productInfo.shopProductName.substring(firstLineName.length());
                //
                addChineseText(tsc, smallProductNameLineFirst, firstLineName);
                addChineseText(tsc, smallProductNameLineSecond, secondLineName);
            } else if (nameLength > FONT_SIZE_SINGLE_LINE_COUNT) {//有二行文字
                firstLineName = PrinterHelper.getStrByByte(productInfo.shopProductName, FONT_SIZE_SINGLE_LINE_COUNT);
                secondLineName = productInfo.shopProductName.substring(firstLineName.length());
                //
                addChineseText(tsc, productNameLineFirst, firstLineName, LabelCommand.FONTMUL.MUL_2);
                addChineseText(tsc, productNameLineSecond, secondLineName, LabelCommand.FONTMUL.MUL_2);
            } else {//有一行文字
                addChineseText(tsc, productNameLineFirst, productInfo.shopProductName, LabelCommand.FONTMUL.MUL_2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(productInfo.shopProductPrice)) {
            BigDecimal price = new BigDecimal(productInfo.shopProductPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
            int priceYuan = price.intValue();
            String shouJia = price.toString();

            LabelCommand.FONTTYPE fontType;
            LabelCommand.FONTMUL fontmul = LabelCommand.FONTMUL.MUL_1;
            String nullChars;
            if (priceYuan > 9999) {
                shouJia = priceYuan + "";
                fontType = LabelCommand.FONTTYPE.FONT_4;
                nullChars = "              ";
            } else if (priceYuan > 999) {
                fontType = LabelCommand.FONTTYPE.FONT_4;
                nullChars = "               ";
            } else if (priceYuan > 99) {
                fontType = LabelCommand.FONTTYPE.FONT_4;
                nullChars = "              ";
            } else {
                fontType = LabelCommand.FONTTYPE.FONT_3;
                fontmul = LabelCommand.FONTMUL.MUL_2;
                nullChars = "              ";
            }
            String danWei = "";
            if (!TextUtils.isEmpty(productInfo.unit)) {
                danWei = "/" + productInfo.unit;
            }
            tsc.addText(priceBackground.x, priceBackground.y, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "￥" + nullChars + "元" + danWei);
            tsc.addText(shopProductPrice.x, shopProductPrice.y, fontType, LabelCommand.ROTATION.ROTATION_0, fontmul, fontmul, shouJia);
        }
    }

}
