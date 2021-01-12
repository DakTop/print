package com.moria.lib.printer.label.template;

import android.text.TextUtils;

import com.moria.lib.printer.helper.PrinterHelper;
import com.moria.lib.printer.label.bean.LabelPoint;
import com.moria.lib.printer.label.bean.LabelProductInfo;
import com.moria.lib.printer.label.cmd.LabelCommand;
import com.moria.lib.printer.label.cmd.LabelHelper;
import com.moria.lib.printer.label.cmd.LabelPointFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Vector;


/**
 * Author  moria
 * Date    2020/9/14
 * Time    12:53
 */
public class LabelModelTwo extends LabelModel {

    private static final int CODE_HEIGHT = 25;

    private static final float gap = 3f;
    private int sizeX = 66;
    private int sizeY = 35;

    private static final int BIG_FONT_SIZE_COUNT = 16;
    private static final int SMALL_FONT_SIZE_COUNT = 32;

    private LabelPointFactory mLabelPointFactory = new LabelPointFactory(sizeX, sizeY);
    //单行1号字体
    private LabelPoint shopProductNameSmallSingleLine = mLabelPointFactory.createPointWithRatioXY(0.240f, 0.100f, -2 * 8, 8);
    //单行2号字体
    private LabelPoint shopProductNameBigSingleLine = mLabelPointFactory.createPointWithRatioXY(0.240f, 0.060f, -2 * 8, 8);

    //字符超过16为 上下显示
    private LabelPoint shopProductNameLineUp = mLabelPointFactory.createPointWithRatioXY(0.240f, 0.062f, -2 * 8, 8);
    private LabelPoint shopProductNameLineDown = mLabelPointFactory.createPointWithRatioXY(0.240f, 0.142f, -2 * 8, 8);

    private LabelPoint barCode = mLabelPointFactory.createPointWithRatioXY(0.257f, 0.371f, -13 * 8, 8);
    private LabelPoint unit = mLabelPointFactory.createPointWithRatioByFactorY(0.136f, 0.714f, 0);

    private LabelPoint shopProductPrice = mLabelPointFactory.createPointWithRatioXY(0.621f, 0.657f, -2 * 8, -3 * 8);

    private List<LabelProductInfo> productInfoList;

    public LabelModelTwo(List<LabelProductInfo> productInfoList) {
        this.productInfoList = productInfoList;
    }

    public byte[] getPrintBytes() {
        //标签打印
        LabelCommand tsc = new LabelCommand();
        tsc.addSize(sizeX, sizeY);
        tsc.addGap(gap);
        tsc.addDirection(direction, LabelCommand.MIRROR.NORMAL); // 设置打印方向
        tsc.addReference(0, 0);
        tsc.addTear(LabelCommand.ENABLE.ON);
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

        if (!TextUtils.isEmpty(productInfo.shopProductName)) {
            int nameLength = getBytesLength(productInfo.shopProductName);
            if (nameLength > BIG_FONT_SIZE_COUNT) {
                if (nameLength > SMALL_FONT_SIZE_COUNT) {
                    String pName = productInfo.shopProductName;
                    String upName = "";
                    try {
                        upName = PrinterHelper.getStrByByte(pName, SMALL_FONT_SIZE_COUNT);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    addChineseText(tsc, shopProductNameLineUp, upName);
                    addChineseText(tsc, shopProductNameLineDown, pName.substring(upName.length()));
                } else {
                    addChineseText(tsc, shopProductNameSmallSingleLine, productInfo.shopProductName);
                }
            } else {
                addChineseText(tsc, shopProductNameBigSingleLine, productInfo.shopProductName, LabelCommand.FONTMUL.MUL_2);

            }
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
