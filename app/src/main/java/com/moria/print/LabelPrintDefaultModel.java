package com.moria.print;

import android.text.TextUtils;

import com.moria.lib.printer.helper.PrinterHelper;
import com.moria.lib.printer.label.bean.LabelPoint;
import com.moria.lib.printer.label.bean.LabelProductInfo;
import com.moria.lib.printer.label.cmd.LabelCommand;
import com.moria.lib.printer.label.cmd.LabelHelper;
import com.moria.lib.printer.label.cmd.LabelPointFactory;
import com.moria.lib.printer.label.template.LabelModel;
import com.moria.print.LabelPrintEntity;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Vector;

/**
 * Author  moria
 * Date    2020/12/3
 * Time    16:58
 */
public class LabelPrintDefaultModel extends LabelModel {

    private static final int CODE_HEIGHT = 30; // 一维码的高度
    private static final float gap = 8f; // 可以是小数
    private int sizeX = 70;
    private int sizeY = 38;
    private LabelPointFactory mLabelPointFactory = new LabelPointFactory(sizeX, sizeY);


    private LabelPoint labelName = mLabelPointFactory.createPointWithRatioXY(0.04f, 0.07f, 0, 0);
    private LabelPoint barCode = mLabelPointFactory.createPointWithRatioByFactorY(0.143f, 0.642f, 8);
    private LabelPoint unit = mLabelPointFactory.createPointWithRatioXY(0.41f, 0.428f, 4, 0);
    private LabelPoint goodsFrom = mLabelPointFactory.createPointWithRatioXY(0.13f, 0.428f, 0, 0);
    private LabelPoint goodsType = mLabelPointFactory.createPointWithRatioXY(0.13f, 0.528f, 0, 0);
    private LabelPoint goodsBrand = mLabelPointFactory.createPointWithRatioXY(0.41f, 0.528f, 0, 0);

    private LabelPoint shopProductPrice = mLabelPointFactory.createPointWithRatioXY(0.69f, 0.528f, 0, 0);
    private LabelPoint shopMemPriceTitle = mLabelPointFactory.createPointWithRatioXY(0.69f, 0.645f, 0, 0);
    private LabelPoint shopMemPrice = mLabelPointFactory.createPointWithRatioXY(0.69f, 0.747f, 0, 0);
    private LabelPoint shopSpecPriceTitle = mLabelPointFactory.createPointWithRatioXY(0.87f, 0.420f, 0, 0);
    private LabelPoint shopSpecPrice = mLabelPointFactory.createPointWithRatioXY(0.87f, 0.520f, 0, 0);

    //单行1号字体
    private LabelPoint shopProductNameSmallSingleLine = mLabelPointFactory.createPointWithRatioXY(0.230f, 0.240f, -2 * 8, 8);
    //单行2号字体
    private LabelPoint shopProductNameBigSingleLine = mLabelPointFactory.createPointWithRatioXY(0.226f, 0.203f, -2 * 8, 8);
    //字符超过16为 上下显示
    private LabelPoint productNameLineFirst = mLabelPointFactory.createPointWithRatioXY(0.230f, 0.208f, -2 * 8, 8);
    private LabelPoint productNameLineSecond = mLabelPointFactory.createPointWithRatioXY(0.230f, 0.287f, -2 * 8, 8);
    //
    private static final int BIG_FONT_SIZE_SINGLE_LINE_COUNT = 18;
    private static final int SMALL_FONT_SIZE_SINGLE_LINE_COUNT = 36;
    private List<LabelPrintEntity> productInfoList;

    public LabelPrintDefaultModel(List<LabelPrintEntity> productInfoList) {
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
            LabelPrintEntity productInfo = productInfoList.get(i);
            addMyText(tsc, productInfo);
            // 设置标签的条码
            tsc.add1DBarcode(barCode.x, barCode.y, LabelCommand.BARCODETYPE.CODE128, CODE_HEIGHT, LabelCommand.READABEL.EANBEL, LabelCommand.ROTATION.ROTATION_0, productInfo.getGoodsCode());
            tsc.addPrint(printCount); // 打印标签
            tsc.addCls();// 清除打印缓冲区
        }

        Vector<Byte> datas = tsc.getCommand(); // 发送数据
        return LabelHelper.ByteTo_byte(datas);
    }

    private void addMyText(LabelCommand tsc, LabelPrintEntity productInfo) {

        if (!TextUtils.isEmpty(productInfo.getGoodsName())) {
            int nameLength = getBytesLength(productInfo.getGoodsName() + "");
            String firstLineName = "";
            String secondLineName = "";
            try {
                if (nameLength > SMALL_FONT_SIZE_SINGLE_LINE_COUNT) {//有二行文字
                    firstLineName = PrinterHelper.getStrByByte(productInfo.getGoodsName(), SMALL_FONT_SIZE_SINGLE_LINE_COUNT);
                    secondLineName = productInfo.getGoodsName().substring(firstLineName.length());
                    //
                    addChineseText(tsc, productNameLineFirst, firstLineName);
                    addChineseText(tsc, productNameLineSecond, secondLineName);
                } else {//有一行文字
                    if (nameLength > BIG_FONT_SIZE_SINGLE_LINE_COUNT) { //判断字符长度是否超多大字体单行所限制的字数
                        addChineseText(tsc, shopProductNameSmallSingleLine, productInfo.getGoodsName());
                    } else {
                        addChineseText(tsc, shopProductNameBigSingleLine, productInfo.getGoodsName(), LabelCommand.FONTMUL.MUL_2);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!TextUtils.isEmpty(productInfo.getGoodsUnit())) {
            //unit.x = unit.x - 8;
            addChineseText(tsc, unit, productInfo.getGoodsUnit());
        }


        String gfVal = productInfo.getGoodsFrom();
        if (!TextUtils.isEmpty(gfVal)) {//产地
            if (gfVal.length() > 4) {
                gfVal = gfVal.substring(0, 3) + "...";
            }
            addChineseText(tsc, goodsFrom, gfVal);
        }

        if (!TextUtils.isEmpty(productInfo.getGoodsType())) {
            addChineseText(tsc, goodsType, productInfo.getGoodsType());
        }

        if (!TextUtils.isEmpty(productInfo.getGoodsType())) {
            addChineseText(tsc, goodsType, productInfo.getGoodsType());
        }


        LabelCommand.FONTMUL priceScale = LabelCommand.FONTMUL.MUL_2;

        if (addBottomPrice(tsc, "会员特价", productInfo.getMemSpecPrice())) {//会员价不为空
            priceScale = LabelCommand.FONTMUL.MUL_1;
            addRightPrice(tsc, "特价", productInfo.getSpecialPrice());
        } else if (addBottomPrice(tsc, "会员价", productInfo.getMemPrice())) {//会员特价不为空
            priceScale = LabelCommand.FONTMUL.MUL_1;
            addRightPrice(tsc, "特价", productInfo.getSpecialPrice());
        } else if (addBottomPrice(tsc, "特价", productInfo.getSpecialPrice())) {//特价
            priceScale = LabelCommand.FONTMUL.MUL_1;
        } else {
            priceScale = LabelCommand.FONTMUL.MUL_2;
        }
        //零售价
        if (!TextUtils.isEmpty(productInfo.getRetailPrice())) {
            tsc.addText(shopProductPrice.x, shopProductPrice.y, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, priceScale, priceScale, productInfo.getRetailPrice());
        }

        //标签名称
        if (!TextUtils.isEmpty(productInfo.getLabelName())) {
            addChineseText(tsc, labelName, productInfo.getLabelName(), LabelCommand.FONTMUL.MUL_2);
        }

        //品牌
        if (!TextUtils.isEmpty(productInfo.getGoodsBrand())) {
            addChineseText(tsc, goodsBrand, productInfo.getGoodsBrand());
        }

    }

    private boolean addBottomPrice(LabelCommand tsc, String title, String price) {
        if (!TextUtils.isEmpty(price)) {
            double sp = Double.valueOf(price);
            if (sp > 0) {
                addChineseText(tsc, shopMemPriceTitle, title);
                tsc.addText(shopMemPrice.x, shopMemPrice.y, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_2, LabelCommand.FONTMUL.MUL_2, price);
                return true;
            }
        }
        return false;
    }

    private boolean addRightPrice(LabelCommand tsc, String title, String price) {
        if (!TextUtils.isEmpty(price)) {
            double sp = Double.valueOf(price);
            if (sp > 0) {
                addChineseText(tsc, shopSpecPriceTitle, title);
                addChineseText(tsc, shopSpecPrice, price);
                return true;
            }
        }
        return false;
    }

}
