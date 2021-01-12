package com.moria.lib.printer.label.bean;

/**
 * Author  moria
 * Date    2020/9/14
 * Time    11:53
 */
public class LabelProductInfo {
    
    public String shopName;
    public String shopProductName;
    public String barCode;
    public String unit;
    public String shopProductPrice;
    
    public LabelProductInfo() {}
    
    public LabelProductInfo(String shopName, String shopProductName, String barCode, String unit, String shopProductPrice) {
        this.shopName = shopName;
        this.shopProductName = shopProductName;
        this.barCode = barCode;
        this.unit = unit;
        this.shopProductPrice = shopProductPrice;
    }
}
