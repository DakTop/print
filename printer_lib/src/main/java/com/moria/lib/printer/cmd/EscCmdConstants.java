package com.moria.lib.printer.cmd;

/**
 * Author  moria
 * Date    2020/9/11
 * Time    16:31
 */
public class EscCmdConstants {
    /**
     * 下面是十进制指令，一般是配合使用，
     * 例如：FS ! n  表示设置汉字字符模式 在写指令时先写入28(FS)，在写入33(!)，最后写入n(n=4 选择倍宽，n=8 选择倍高，n=128 选择下划线，n=0 取消倍宽、倍高、下划线。)
     *
     * 有些变量名称在官方文档中是特殊字符，不能在这里定义，即在注释中写明
     * !：33
     */
    //打印指令
    public static final byte LF = 10;//打印并换行（水平定位）
    public static final byte CR = 13;//打印并回车
    public static final byte HT = 9;//横向列表，水平定位
    public static final byte ESC = 27;//换码
    public static final byte FS = 28;//文本分隔符
    public static final byte GS = 29;//组分隔符
    public static final byte DLE = 16;//数据连接换码
    public static final byte EOT = 4;//传输结束
    public static final byte ENQ = 5;//询问字符
    public static final byte SP = 32;//空格
    public static final byte FF = 12;//走纸控制（打印并回到标准模式（在页模式下） ）
    public static final byte CAN = 24;//作废（页模式下取消打印数据 ）
}
