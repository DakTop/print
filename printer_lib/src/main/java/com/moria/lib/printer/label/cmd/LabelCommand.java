package com.moria.lib.printer.label.cmd;


import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 * Author  moria
 * Date    2020/9/11
 * Time    16:31
 */
@SuppressWarnings("all")
public class LabelCommand {

    private static final String DEBUG_TAG = "LabelCommand";
    private Vector<Byte> Command = null;

    public LabelCommand() {
        this.Command = new Vector();
    }

    /**
     * 根据标签的大小初始化指令
     *
     * @param width  标签的宽度, 单位㎜
     * @param height 标签的高度, 单位㎜
     * @param gap    标签之间的间隙, 单位㎜
     */
    public LabelCommand(int width, int height, int gap) {
        this.Command = new Vector(4096, 1024);
        this.addSize(width, height);
        this.addGap(gap);
    }

    private void addStrToCommand(String str) {
        byte[] bs = null;
        if (!str.equals("")) {
            try {
                bs = str.getBytes("GB2312");
            } catch (UnsupportedEncodingException var4) {
                var4.printStackTrace();
            }

            for (int i = 0; i < bs.length; ++i) {
                this.Command.add(Byte.valueOf(bs[i]));
            }
        }

    }

    /**
     * 设置标签纸之间的间隙
     *
     * @param gap 标签之间的间隙, 单位㎜
     */
    public void addGap(float gap) {
        String str = "GAP " + gap + " mm," + 0 + " mm" + "\r\n";
        this.addStrToCommand(str);
    }

    /**
     * 设置标签的宽度和高度
     *
     * @param width  标签的宽度, 单位㎜
     * @param height 标签的高度, 单位㎜
     */
    public void addSize(int width, int height) {
        String str = "SIZE " + width + " mm," + height + " mm" + "\r\n";
        this.addStrToCommand(str);
    }

    /**
     * 设置密度, 不影响实际效果
     *
     * @param density
     */
    public void addDensity(DENSITY density) {
        String str = "DENSITY " + density.getValue() + "\r\n";
        this.addStrToCommand(str);
    }

    /**
     * 设置打印的方向
     *
     * @param direction {@link DIRECTION}
     * @param mirror
     */
    public void addDirection(DIRECTION direction, MIRROR mirror) {
        String str = "DIRECTION " + direction.getValue() + ',' + mirror.getValue() + "\r\n";
        this.addStrToCommand(str);
    }

    /**
     * 设置原点坐标
     *
     * @param x
     * @param y
     */
    public void addReference(int x, int y) {
        String str = "REFERENCE " + x + "," + y + "\r\n";
        this.addStrToCommand(str);
    }

    public void addCls() {
        String str = "CLS\r\n";
        this.addStrToCommand(str);
    }

    /**
     * 调整标签纸的位置, 使第一个标签纸空白打印出去
     */
    public void addHome() {
        String str = "HOME\r\n";
        this.addStrToCommand(str);
    }

    /**
     * 设置打印的份数1,1代表打印一份
     *
     * @param m 打印张数
     * @param n 每张标签需重复打印的张数
     */
    public void addPrint(int m, int n) {
        String str = "PRINT " + m + "," + n + "\r\n";
        this.addStrToCommand(str);
    }

    public void addPrint(int m) {
        String str = "PRINT " + m + "\r\n";
        this.addStrToCommand(str);
    }

    /**
     * 绘制文本
     *
     * @param x        绘制的横坐标, 1mm相当于8个坐标
     * @param y        绘制的纵坐标
     * @param font     绘制文本选择的字体
     * @param rotation 绘制文本旋转的角度
     * @param Xscal    水平放大比例
     * @param Yscal    竖直放大比例
     * @param text     绘制文本的内容
     */
    public void addText(int x, int y, FONTTYPE font, ROTATION rotation, FONTMUL Xscal, FONTMUL Yscal, String text) {
        String str = "TEXT " + x + "," + y + "," + "\"" + font.getValue() + "\"" + "," + rotation.getValue() + "," + Xscal
                .getValue() + "," + Yscal.getValue() + "," + "\"" + text + "\"" + "\r\n";
        this.addStrToCommand(str);
    }

    /**
     * 绘制文本
     *
     * @param x    绘制的横坐标, 1mm相当于8个坐标
     * @param y    绘制的纵坐标
     * @param text 绘制文本的内容
     *             font 默认中文
     *             rotation 绘制文本旋转的角度(默认不旋转)
     *             Xscal 水平放大比例(默认不放大)
     *             Yscal 竖直放大比例(默认不放大)
     */
    public void addTextBase(int x, int y, String text) {
        String str = "TEXT " + x + "," + y + "," + "\""
                + FONTTYPE.SIMPLIFIED_CHINESE.getValue() + "\"" + ","
                + ROTATION.ROTATION_0.getValue() + ","
                + FONTMUL.MUL_1.getValue() + ","
                + FONTMUL.MUL_1.getValue() + ","
                + "\"" + text + "\"" + "\r\n";
        this.addStrToCommand(str);
    }

    /**
     * 绘制一维码, 相比
     *
     * @see LabelCommand#add1DBarcodeNarrowWidth
     * 将narrow和width参数写死
     */
    public void add1DBarcode(int x, int y, BARCODETYPE type, int height, READABEL readable, ROTATION rotation, String content) {
        byte narrow = 2;
        byte width = 2;
        String str = "BARCODE " + x + "," + y + "," + "\"" + type.getValue() + "\"" + "," + height + "," + readable
                .getValue() + "," + rotation.getValue() + "," + narrow + "," + width + "," + "\"" + content + "\"" + "\r\n";
        this.addStrToCommand(str);
    }

    /**
     * 绘制一维码
     *
     * @param x        绘制的横坐标, 1mm相当于8个坐标
     * @param y        绘制的纵坐标
     * @param type     绘制的一维码的类型, 实际用128
     * @param height   绘制一维码的高度
     * @param readable 是否在一维码的下面明文显示一维码的内容
     * @param rotation 一维码旋转的角度
     * @param content  一维码表示的含义
     * @param narrow   白条的宽度, 实际效果会跟随白条和黑条最宽的效果, 比如2,3的效果和3,3一致
     * @param width    黑条的宽度
     */
    public void add1DBarcodeNarrowWidth(int x, int y, BARCODETYPE type, int height, READABEL readable, ROTATION rotation, String content, byte narrow, byte width) {
        String str = "BARCODE " + x + "," + y + "," + "\"" + type.getValue() + "\"" + "," + height + "," + readable
                .getValue() + "," + rotation.getValue() + "," + narrow + "," + width + "," + "\"" + content + "\"" + "\r\n";
        this.addStrToCommand(str);
    }

    public Vector<Byte> getCommand() {
        return this.Command;
    }

    public void addTear(ENABLE enable) {
        String str = "SET TEAR " + enable.getValue() + "\r\n";
        this.addStrToCommand(str);
    }

    public static enum BARCODETYPE {
        CODE128("128"), CODE128M("128M"), EAN128("EAN128"), ITF25("25"), ITF25C("25C"), CODE39("39"), CODE39C("39C"), CODE39S("39S"), CODE93("93"), EAN13("EAN13"), EAN13_2("EAN13+2"), EAN13_5("EAN13+5"), EAN8("EAN8"), EAN8_2("EAN8+2"), EAN8_5("EAN8+5"), CODABAR("CODA"), POST("POST"), UPCA("UPCA"), UPCA_2("UPCA+2"), UPCA_5("UPCA+5"), UPCE("UPCE13"), UPCE_2("UPCE13+2"), UPCE_5("UPCE13+5"), CPOST("CPOST"), MSI("MSI"), MSIC("MSIC"), PLESSEY("PLESSEY"), ITF14("ITF14"), EAN14("EAN14");

        private final String value;

        private BARCODETYPE(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    public static enum BITMAP_MODE {
        OVERWRITE(0), OR(1), XOR(2);

        private final int value;

        private BITMAP_MODE(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public static enum CODEPAGE {
        PC437(437), PC850(850), PC852(852), PC860(860), PC863(863), PC865(865), WPC1250(1250), WPC1252(1252), WPC1253(1253), WPC1254(1254);

        private final int value;

        private CODEPAGE(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public static enum DENSITY {
        DNESITY0(0), DNESITY1(1), DNESITY2(2), DNESITY3(3), DNESITY4(4), DNESITY5(5), DNESITY6(6), DNESITY7(7), DNESITY8(8), DNESITY9(9), DNESITY10(10), DNESITY11(11), DNESITY12(12), DNESITY13(13), DNESITY14(14), DNESITY15(15);

        private final int value;

        private DENSITY(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public static enum DIRECTION {
        FORWARD(0), BACKWARD(1);

        private final int value;

        private DIRECTION(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public static enum EEC {
        LEVEL_L("L"), LEVEL_M("M"), LEVEL_Q("Q"), LEVEL_H("H");

        private final String value;

        private EEC(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    public static enum FONTMUL {
        MUL_1(1), MUL_2(2), MUL_3(3), MUL_4(4), MUL_5(5), MUL_6(6), MUL_7(7), MUL_8(8), MUL_9(9), MUL_10(10);

        private final int value;

        private FONTMUL(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public static enum FONTTYPE {
        FONT_1("1"), FONT_2("2"), FONT_3("3"), FONT_4("4"), FONT_5("5"), FONT_6("6"), FONT_7("7"), FONT_8("8"), SIMPLIFIED_CHINESE("TSS24.BF2"), TRADITIONAL_CHINESE("TST24.BF2"), KOREAN("K");

        private final String value;

        private FONTTYPE(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    public static enum FOOT {
        F2(0), F5(1);

        private final int value;

        private FOOT(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public static enum MIRROR {
        NORMAL(0), MIRROR(1);

        private final int value;

        private MIRROR(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public static enum READABEL {
        DISABLE(0), EANBEL(1);

        private final int value;

        private READABEL(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public static enum ROTATION {
        ROTATION_0(0), ROTATION_90(90), ROTATION_180(180), ROTATION_270(270);

        private final int value;

        private ROTATION(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public static enum SPEED {
        SPEED1DIV5(1.5F), SPEED2(2.0F), SPEED3(3.0F), SPEED4(4.0F);

        private final float value;

        private SPEED(float value) {
            this.value = value;
        }

        public float getValue() {
            return this.value;
        }
    }

    public static enum ENABLE {
        OFF(0), ON(1);

        private final int value;

        private ENABLE(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte) this.value;
        }
    }
}
