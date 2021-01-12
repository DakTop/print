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
public class LabelModelEleven extends LabelModel {
    
    private static final float gap = 0f;
    private int sizeX = 80;
    private int sizeY = 39;
    private LabelPointFactory mLabelPointFactory = new LabelPointFactory(sizeX, sizeY);
    
    private LabelPoint shopProductName = mLabelPointFactory.createPointWithRatioByFactorY(0.200f, 0.235f, 0);
    private LabelPoint unit = mLabelPointFactory.createPointWithRatioByFactorY(0.475f, 0.787f, 0);
    
    private LabelPoint shopProductPrice = mLabelPointFactory.createPointWithRatioByFactorY(0.637f, 0.425f, 0);

    private List<LabelProductInfo> productInfoList;

    public LabelModelEleven(List<LabelProductInfo> productInfoList) {
        this.productInfoList = productInfoList;
    }

    public byte[] getPrintBytes() {
        //标签打印
        LabelCommand tsc = new LabelCommand();
        tsc.addSize(sizeX, sizeY);
        tsc.addGap(gap);
        tsc.addDirection(direction, LabelCommand.MIRROR.NORMAL); // 设置打印方向
        tsc.addReference(0, -100);
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
            addChineseText(tsc, shopProductName, toSmallerString(productInfo.shopProductName, 9));
        }
        if (!TextUtils.isEmpty(productInfo.unit)) {
            addChineseText(tsc, unit, productInfo.unit);
        }
        if (!TextUtils.isEmpty(productInfo.shopProductPrice)) {
            BigDecimal price = new BigDecimal(productInfo.shopProductPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
            int priceYuan =  price.intValue();
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
