package com.moria.lib.printer.label.template;

import android.text.TextUtils;

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
public class LabelModelFifteen extends LabelModel {
    
    private static final int CODE_HEIGHT = 70;
    private static final float gap = 3f;
    private int sizeX = 80;
    private int sizeY = 50;
    private LabelPointFactory mLabelPointFactory = new LabelPointFactory(sizeX, sizeY);
    
    private LabelPoint shopProductName = mLabelPointFactory.createPointWithRatioByFactorY(0.083f, 0.092f, 0);
    private LabelPoint barCode = mLabelPointFactory.createPointWithRatioXY(0.150f, 0.530f, 0, 0);
    
    private LabelPoint priceBackground = mLabelPointFactory.createPointWithRatioByFactorY(0.328f, 0.440f, -2 * 8);
    private LabelPoint shopProductPrice = mLabelPointFactory.createPointWithRatioByFactorY(0.370f, 0.390f, 0);

    private List<LabelProductInfo> productInfoList;

    public LabelModelFifteen(List<LabelProductInfo> productInfoList) {
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
        //tsc.addHome();
        tsc.addCls();// 清除打印缓冲区
        tsc.addDensity(LabelCommand.DENSITY.DNESITY15);
        
        for (int i = 0; i < productInfoList.size(); i++) {
            LabelProductInfo productInfo = productInfoList.get(i);
            addMyText(tsc, productInfo);
            byte narrow = 3;
            byte width = 3;
            tsc.add1DBarcodeNarrowWidth(barCode.x, barCode.y + 5 * 8, LabelCommand.BARCODETYPE.CODE128, CODE_HEIGHT, LabelCommand.READABEL.EANBEL, LabelCommand.ROTATION.ROTATION_0, productInfo.barCode, narrow, width);
            tsc.addPrint(1, 1);
            tsc.addCls();
        }
        
        Vector<Byte> datas = tsc.getCommand();
        return LabelHelper.ByteTo_byte(datas);
    }
    
    private void addMyText(LabelCommand tsc, LabelProductInfo productInfo) {
        if (!TextUtils.isEmpty(productInfo.shopProductName)) {
            addChineseText(tsc, shopProductName, toSmallerString(productInfo.shopProductName, 19));
        }
        if (!TextUtils.isEmpty(productInfo.shopProductPrice)) {
            BigDecimal price = new BigDecimal(productInfo.shopProductPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
            int priceYuan =  price.intValue();
            String shouJia = price.toString();
            
            LabelCommand.FONTTYPE fonttype;
            if (priceYuan > 9999) {
                shouJia = priceYuan + "";
                fonttype = LabelCommand.FONTTYPE.FONT_4;
            } else if (priceYuan > 99) {
                fonttype = LabelCommand.FONTTYPE.FONT_4;
            } else {
                fonttype = LabelCommand.FONTTYPE.FONT_5;
            }
            String danWei = "";
            if (!TextUtils.isEmpty(productInfo.unit)) {
                danWei = "/" + productInfo.unit;
            }
            tsc.addText(priceBackground.x - 10 * 8, priceBackground.y, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "￥                  元" + danWei);
            tsc.addText(shopProductPrice.x - 12 * 8, shopProductPrice.y, fonttype, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_2, LabelCommand.FONTMUL.MUL_2, shouJia);
        }
    }
    
}
