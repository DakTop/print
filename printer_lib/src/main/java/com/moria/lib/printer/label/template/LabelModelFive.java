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
public class LabelModelFive extends LabelModel {


    private static final int CODE_HEIGHT = 30; // 一维码的高度
    private static final float gap = 6f; // 可以是小数
    private int sizeX = 70;
    private int sizeY = 38;
    private LabelPointFactory mLabelPointFactory = new LabelPointFactory(sizeX, sizeY);

    private LabelPoint shopName = mLabelPointFactory.createPointWithRatioByFactorY(0.464f, 0.076f, 8);
    private LabelPoint shopProductName = mLabelPointFactory.createPointWithRatioByFactorY(0.201f, 0.242f, 8);
    private LabelPoint barCode = mLabelPointFactory.createPointWithRatioByFactorY(0.148f, 0.642f, 8);
    private LabelPoint unit = mLabelPointFactory.createPointWithRatioXY(0.387f, 0.428f, 4, 0);

    private LabelPoint shopProductPrice = mLabelPointFactory.createPointWithRatioByFactorY(0.705f, 0.734f, 16);

    //单行1号字体
    private LabelPoint shopProductNameSmallSingleLine = mLabelPointFactory.createPointWithRatioXY(0.236f, 0.250f, -2 * 8, 8);
    //单行2号字体
    private LabelPoint shopProductNameBigSingleLine = mLabelPointFactory.createPointWithRatioXY(0.230f, 0.213f, -2 * 8, 8);
    //字符超过16为 上下显示
    private LabelPoint productNameLineFirst = mLabelPointFactory.createPointWithRatioXY(0.236f, 0.218f, -2 * 8, 8);
    private LabelPoint productNameLineSecond = mLabelPointFactory.createPointWithRatioXY(0.236f, 0.297f, -2 * 8, 8);
    //
    private static final int BIG_FONT_SIZE_SINGLE_LINE_COUNT = 18;
    private static final int SMALL_FONT_SIZE_SINGLE_LINE_COUNT = 36;
    private List<LabelProductInfo> productInfoList;

    public LabelModelFive(List<LabelProductInfo> productInfoList) {
        this.productInfoList = productInfoList;
    }

    public byte[] getPrintBytes() {
        //标签打印
        LabelCommand tsc = new LabelCommand();
        // 设置格式
        //tsc.addHome(); // HOME命令的值的height最大30mm
        tsc.addSize(sizeX, sizeY); // 设置标签尺寸，按照实际尺寸设置(70,50)(40,30)(70,40)
        tsc.addGap(gap); // 设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0
        tsc.addDirection(direction, LabelCommand.MIRROR.NORMAL); // 设置打印方向
        tsc.addReference(0, 0); // 设置原点坐标, 如果衫底不是1.3mm则需要平移x, 衫底大于1.3mm, 则扩大x
        tsc.addTear(LabelCommand.ENABLE.ON); // 撕纸模式开启
        tsc.addCls();// 清除打印缓冲区
        tsc.addDensity(LabelCommand.DENSITY.DNESITY15);

        // 填充内容
        for (int i = 0; i < productInfoList.size(); i++) {
            LabelProductInfo productInfo = productInfoList.get(i);
            addMyText(tsc, productInfo);
            // 设置标签的条码
            tsc.add1DBarcode(barCode.x, barCode.y, LabelCommand.BARCODETYPE.CODE128, CODE_HEIGHT, LabelCommand.READABEL.EANBEL, LabelCommand.ROTATION.ROTATION_0, productInfo.barCode);
            tsc.addPrint(printCount); // 打印标签
            tsc.addCls();// 清除打印缓冲区
        }

        Vector<Byte> datas = tsc.getCommand(); // 发送数据
        return LabelHelper.ByteTo_byte(datas);
    }

    private void addMyText(LabelCommand tsc, LabelProductInfo productInfo) {
        if (!TextUtils.isEmpty(productInfo.shopName)) {
            addChineseText(tsc, shopName, toSmallerString(productInfo.shopName, 10)); // 10个后...
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
            //unit.x = unit.x - 8;
            addChineseText(tsc, unit, productInfo.unit);
        }
        if (!TextUtils.isEmpty(productInfo.shopProductPrice)) {
            BigDecimal price = new BigDecimal(productInfo.shopProductPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
            int priceYuan = price.intValue();
            String shouJia = price.toString();

            LabelCommand.FONTTYPE fontType;
            LabelCommand.FONTMUL fontMul = LabelCommand.FONTMUL.MUL_1;
            int adjustX = 0;
            int adjustY = 0;
            if (priceYuan > 9999) {
                shouJia = priceYuan + "";
                fontType = LabelCommand.FONTTYPE.FONT_4;
            } else if (priceYuan > 99) {
                fontType = LabelCommand.FONTTYPE.FONT_4;
                adjustX = -8;
            } else {
                fontType = LabelCommand.FONTTYPE.FONT_3;
                fontMul = LabelCommand.FONTMUL.MUL_2;
                adjustY = -8;
            }
            tsc.addText(shopProductPrice.x + adjustX, shopProductPrice.y + adjustY, fontType, LabelCommand.ROTATION.ROTATION_0, fontMul, fontMul, shouJia);
        }
    }

}
