package com.moria.print;

public class LabelPrintEntity {
    private String labelName;//标签名称
    private String goodsName;//商品名称
    private String goodsFrom;//产地
    private String goodsUnit;//单位
    private String goodsType;//规格
    private String goodsBrand;//品牌
    private String goodsCode;//条码
    private String retailPrice;//零售价
    private String specialPrice;//特价
    private String memPrice;//会员价
    private String memSpecPrice;//会员特价

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getGoodsFrom() {
        return goodsFrom;
    }

    public void setGoodsFrom(String goodsFrom) {
        this.goodsFrom = goodsFrom;
    }

    public String getGoodsUnit() {
        return goodsUnit;
    }

    public void setGoodsUnit(String goodsUnit) {
        this.goodsUnit = goodsUnit;
    }

    public String getGoodsType() {
        return goodsType;
    }

    public void setGoodsType(String goodsType) {
        this.goodsType = goodsType;
    }

    public String getGoodsBrand() {
        return goodsBrand;
    }

    public void setGoodsBrand(String goodsBrand) {
        this.goodsBrand = goodsBrand;
    }

    public String getGoodsCode() {
        return goodsCode;
    }

    public void setGoodsCode(String goodsCode) {
        this.goodsCode = goodsCode;
    }

    public String getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(String retailPrice) {
        this.retailPrice = retailPrice;
    }

    public String getSpecialPrice() {
        return specialPrice;
    }

    public void setSpecialPrice(String specialPrice) {
        this.specialPrice = specialPrice;
    }

    public String getMemPrice() {
        return memPrice;
    }

    public void setMemPrice(String memPrice) {
        this.memPrice = memPrice;
    }

    public String getMemSpecPrice() {
        return memSpecPrice;
    }

    public void setMemSpecPrice(String memSpecPrice) {
        this.memSpecPrice = memSpecPrice;
    }

    public String getLabelName() {
        return labelName;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName;
    }
}
