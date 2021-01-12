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
public class LabelModelEight extends LabelModel {

    private static final int CODE_HEIGHT = 30;
    private static final float gap = 3f;
    private int sizeX = 70;
    private int sizeY = 38;
    private LabelPointFactory mLabelPointFactory = new LabelPointFactory(sizeX, sizeY);

    private LabelPoint shopName = mLabelPointFactory.createPointWithRatioByFactorY(0.405f, 0.071f, 8);
    private LabelPoint barCode = mLabelPointFactory.createPointWithRatioXY(0.114f, 0.731f, 8, 0);
    private LabelPoint unit = mLabelPointFactory.createPointWithRatioXY(0.814f, 0.500f, 4, 0);

    private LabelPoint shopProductPrice = mLabelPointFactory.createPointWithRatioXY(0.692f, 0.776f, 8, 16);

    private List<LabelProductInfo> productInfoList;

    public LabelModelEight(List<LabelProductInfo> productInfoList) {
        this.productInfoList = productInfoList;
    }

    //单行1号字体
    private LabelPoint shopProductNameSmallSingleLine = mLabelPointFactory.createPointWithRatioXY(0.125f, 0.290f, -2 * 8, 8);
    //单行2号字体
    private LabelPoint shopProductNameBigSingleLine = mLabelPointFactory.createPointWithRatioXY(0.150f, 0.210f, -2 * 8, 8);
    //字符超过16为 上下显示
    private LabelPoint productNameLineFirst = mLabelPointFactory.createPointWithRatioXY(0.125f, 0.220f, -2 * 8, 8);
    private LabelPoint productNameLineSecond = mLabelPointFactory.createPointWithRatioXY(0.125f, 0.299f, -2 * 8, 8);
    //
    private static final int BIG_FONT_SIZE_SINGLE_LINE_COUNT = 20;
    private static final int SMALL_FONT_SIZE_SINGLE_LINE_COUNT = 40;

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
            tsc.add1DBarcode(barCode.x, barCode.y, LabelCommand.BARCODETYPE.CODE128, CODE_HEIGHT, LabelCommand.READABEL.EANBEL, LabelCommand.ROTATION.ROTATION_0, productInfo.barCode);
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
            if (priceYuan > 9999) {
                shouJia = priceYuan + "";
                fontType = LabelCommand.FONTTYPE.FONT_3;
            } else if (priceYuan > 99) {
                fontType = LabelCommand.FONTTYPE.FONT_3;
            } else {
                fontType = LabelCommand.FONTTYPE.FONT_4;
            }
            tsc.addText(shopProductPrice.x, shopProductPrice.y, fontType, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, shouJia);
        }
    }

}
