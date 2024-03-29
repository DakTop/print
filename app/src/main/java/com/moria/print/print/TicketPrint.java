package com.moria.print.print;


import android.graphics.fonts.Font;
import android.text.TextUtils;

import com.moria.lib.printer.cmd.EscCmd;
import com.moria.lib.printer.cmd.HORIZONTAL;

/**
 * Author  moria
 * Date    2020/9/11
 * Time    16:39
 */
public class TicketPrint {
    public static byte[] testCmd() {
        EscCmd escCmd = new EscCmd();
        escCmd.setAlignment(HORIZONTAL.CENTER);
        escCmd.setFontDouble();
        escCmd.addTxtBreak("盘点单"); // 打印文字
        escCmd.addPrintEmptyLine(1);
        escCmd.setFontDefault();
        escCmd.setAlignment(HORIZONTAL.LEFT);
        escCmd.addStrokeLine();

        for (int i = 0; i < 10; i++) {
            escCmd.addTxtBreak("审核人名称:发顺丰");
            escCmd.addTxtBreak("审核日期:热热热应该是");
            escCmd.addTxtBreak("备注:分公司的公司大股东是否");
            escCmd.addStrokeLine();
            escCmd.addTextThreeEqually("商品/优惠", "数量", "单价");
            escCmd.addTextThreeEqually("一二三四五六七八九十一二三四五六七八九十一二三四五六七八九十一二三四五六七八九十", "6.565", "6.565");
//
            escCmd.setFontDouble();
            escCmd.setFontDoubleHeight();
            escCmd.addTxtBreak("审核日期:是33333");
            escCmd.setFontDoubleWith();
            escCmd.addTxtBreak("审核日期:该是33333");
            escCmd.setFontDouble();
            escCmd.addTxtBreak("审核日期:热热应该是33333");
            escCmd.setFontDefault();
            escCmd.addTxtBreak("审核日333");
            escCmd.addTextThreeEqually("嗯嗯", "5", "6.565");
            escCmd.addTextThreeEqually("发送", "5", "6.565");
            escCmd.addTextThreeEqually("电风扇胜多负少", "5", "6.565");
            escCmd.addTextThreeEqually("的说法", "5", "6.565");
            escCmd.addStrokeLine();
            escCmd.setAlignment(HORIZONTAL.RIGHT);
            escCmd.addTxtBreak("商品数量:0");
            escCmd.addTxtBreak("盈亏金额:0");
            escCmd.addTextBothSides("总计", "￥4564646");
            escCmd.addStrokeLine();
            escCmd.addTextFourEqually("商品", "单价", "数量", "金额");
            escCmd.addTextFourEqually("大师课教案", "23.3", "3", "75865");
            escCmd.addTextFourEqually("无容器", "3.265", "9999", "3633");
            escCmd.addTextFourEqually("fasd.m.ckjl", "0.26", "4457", "5814");
        }
        escCmd.addTxtBreak("打完了");
        escCmd.feedPaperCutAll();
        escCmd.openCashBox();
        return escCmd.build();
    }


    /**
     * 打印检测
     *
     * @return
     */
    public static byte[] buildTextPrintData() {
        EscCmd escCmd = new EscCmd();
        escCmd.addTxtBreak("打印中心打印检测");
        escCmd.setFontDefault();
        escCmd.addStrokeLine();
        escCmd.addPrintEmptyLine(2);
        escCmd.feedPaperCutAll();
        return escCmd.build();
    }

}
