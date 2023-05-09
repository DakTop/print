package com.moria.lib.printer.cmd;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.moria.lib.printer.cmd.EscCmdConstants.ESC;
import static com.moria.lib.printer.cmd.EscCmdConstants.FS;
import static com.moria.lib.printer.cmd.EscCmdConstants.GS;
import static com.moria.lib.printer.cmd.EscCmdConstants.LF;
import static com.moria.lib.printer.cmd.EscCmdConstants.SP;

/**
 * Author  moria
 * Date    2020/9/11
 * Time    16:31
 */
public class EscCmd {
    //打印的字节数组
    public List<Byte> byteList = new ArrayList<>();
    private int totalWith = 48;

    public EscCmd() {
        initPrinter();
    }


    //初始化打印机
    public void initPrinter() {
        byteList.add(ESC);
        byteList.add((byte) 64);
    }

    public byte[] build() {
        byte[] bytes = new byte[byteList.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = byteList.get(i);
        }
        return bytes;
    }

    //进纸并全部切割
    public void feedPaperCutAll() {
        byteList.add(GS);
        byteList.add((byte) 86);
        byteList.add((byte) 66);
        byteList.add((byte) 1);
    }

    /**
     * 换行
     */
    public void addLineBreak() {
        byteList.add(LF);
    }

    /**
     * 打印空行
     *
     * @param lines 行数
     */
    public void addPrintEmptyLine(int lines) {
        byteList.add(ESC);
        byteList.add((byte) 100);
        byteList.add((byte) lines);
    }

    public void addTextBothSides(String left, String right) {
        int leftLen = chineseLength(left);
        int rightLen = chineseLength(right);
        int space = 4;
        if ((leftLen + rightLen) < totalWith) {
            space = totalWith - leftLen - rightLen;
        }
        addTxt(left);
        addTextSpace(space);
        addTxt(right);
        addLineBreak();
    }

    public void addTextThreeEqually(String text1, String text2, String text3) {
        int equallyWith = totalWith / 3;
        //打印第一个文本
        addTxt(text1);
        int text1Len = chineseLength(text1);
        int space1 = equallyWith - text1Len;
        if (space1 < 0) {
            addLineBreak();//需要换行
            addTextSpace(equallyWith);
        } else {
            addTextSpace(space1);
        }
        //打印第二个文本
        int text2Len = chineseLength(text2);
        int space2 = equallyWith - text2Len;
        if (space2 < 0) {
            addTxt(text2);
            addLineBreak();//需要换行
            addTextSpace(equallyWith * 2);
        } else {
            int o = space2 % 2;
            int leafSpace = space2 / 2;
            addTextSpace(leafSpace + o);
            addTxt(text2);
            addTextSpace(leafSpace);
        }
        //打印第三个文本
        int text3Len = chineseLength(text3);
        int space3 = equallyWith - text3Len;
        if (space3 > 0) {
            addTextSpace(space3);
        }
        addTxt(text3);
        addLineBreak();
    }

    public void addTextFourEqually(String text1, String text2, String text3, String text4) {
        int equallyWith = totalWith / 4;
        //打印第一个文本
        addTxt(text1);
        int text1Len = chineseLength(text1);
        int space1 = equallyWith - text1Len;
        if (space1 < 0) {
            addLineBreak();//需要换行
            addTextSpace(equallyWith);
        } else {
            addTextSpace(space1);
        }
        //打印第二个文本
        int text2Len = chineseLength(text2);
        int space2 = equallyWith - text2Len;
        if (space2 < 0) {
            addTxt(text2);
            addLineBreak();//需要换行
            addTextSpace(equallyWith * 2);
        } else {
            int o = space2 % 2;
            int leafSpace = space2 / 2;
            addTextSpace(leafSpace + o);
            addTxt(text2);
            addTextSpace(leafSpace);
        }

        //打印第三个文本
        int text3Len = chineseLength(text3);
        int space3 = equallyWith - text3Len;
        if (space3 < 0) {
            addTxt(text3);
            addLineBreak();//需要换行
            addTextSpace(equallyWith * 3);
        } else {
            int o = space3 % 2;
            int leafSpace = space3 / 2;
            addTextSpace(leafSpace + o);
            addTxt(text3);
            addTextSpace(leafSpace);
        }
        //打印第四个文本
        int text4Len = chineseLength(text4);
        int space4 = equallyWith - text4Len;
        if (space4 > 0) {
            addTextSpace(space4);
        }
        addTxt(text4);
        addLineBreak();
    }

    /**
     * 打印文本
     *
     * @param str 需要打印的txt
     */
    public void addTxt(String str) {
        addStrToCommand(str);
    }

    /**
     * 打印文并换行
     *
     * @param str 需要打印的txt
     */
    public void addTxtBreak(String str) {
        addStrToCommand(str);
        addLineBreak();
    }

    /**
     * 添加虚线
     */
    public void addStrokeLine() {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < totalWith; i++) {
            line.append("-");
        }
        addTxt(line.toString());
        addLineBreak();
    }

    private void addStrToCommand(String str) {
        byte[] bs = null;
        if (!TextUtils.isEmpty(str)) {
            try {
                bs = str.getBytes("GB2312");
            } catch (UnsupportedEncodingException var4) {
                var4.printStackTrace();
            }
            if (bs != null) {
                for (byte b : bs) {
                    byteList.add(b);
                }
            }
        }
    }

    /**
     * 添加空格
     *
     * @param count 空格数
     */
    public void addTextSpace(int count) {
        for (int i = 0; i < count; i++) {
            byteList.add(SP);
        }
    }

    /**
     * 设置对齐方式
     *
     * @param just left center right
     */
    public void setAlignment(HORIZONTAL just) {
        byteList.add(ESC);
        byteList.add((byte) 97);
        byteList.add(just.getValue());
    }

    /**
     * 默认字体大小
     */
    public void setFontDefault() {
        byteList.add(GS);
        byteList.add((byte) 33);
        byteList.add((byte) 0);
    }

    /**
     * 倍高
     */
    public void setFontDoubleHeight() {
        byteList.add(GS);
        byteList.add((byte) 33);
        byteList.add((byte) 1);
    }

    /**
     * 倍宽
     */
    public void setFontDoubleWith() {
        byteList.add(GS);
        byteList.add((byte) 33);
        byteList.add((byte) 16);
    }

    /**
     * 双倍大
     */
    public void setFontDouble() {
        byteList.add(GS);
        byteList.add((byte) 33);
        byteList.add((byte) 17);
    }

    public void setBold() {
        byte temp = 8;
        byteList.add(ESC);
        byteList.add((byte) 33);
        byteList.add(temp);
    }

    public void cleanBold() {
        byte temp = 0;
        byteList.add(ESC);
        byteList.add((byte) 33);
        byteList.add(temp);
    }


    public void openCashBox() {
        byteList.add(ESC);
        byteList.add((byte) 112);
        byteList.add((byte) 1);
        byteList.add((byte) 255);
        byteList.add((byte) 255);
    }

    /**
     * 计算字符串的长度，包含中英文数字，每个中文字符计为2位
     *
     * @param value
     * @return
     */
    public int chineseLength(String value) {
        int valueLength = 0;
        String chinese = "[\u0391-\uFFE5]";
        /* 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1 */
        for (int i = 0; i < value.length(); i++) {
            /* 获取一个字符 */
            String temp = value.substring(i, i + 1);
            /* 判断是否为中文字符 */
            if (temp.matches(chinese)) {
                /* 中文字符长度为2 */
                valueLength += 2;
            } else {
                /* 其他字符长度为1 */
                valueLength += 1;
            }
        }
        return valueLength;
    }

    public void setTotalWith(int totalWith) {
        this.totalWith = totalWith;
    }
}
