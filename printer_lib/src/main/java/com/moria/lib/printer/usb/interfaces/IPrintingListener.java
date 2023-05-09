package com.moria.lib.printer.usb.interfaces;

/**
 * Author   tanglifang
 * Date     2018/6/4
 * Time     下午5:30
 * DESC     打印机的状态监听
 */

public interface IPrintingListener {

    /**
     * 授权进行中
     */
    void connecting();

    /**
     * 有权限连接成功, 然后开始打印
     */
    void connectSuccess();

    /**
     * 无权限, 连接失败
     *
     * @param msg
     */
    void connectFailure(String msg);

    /**
     * 打印成功
     */
    void printSuccess();

    /**
     * 打印失败, e.g. 打印的信息过长
     *
     * @param msg
     */
    void printFailure(String msg);

}
