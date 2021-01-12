package com.moria.lib.printer.adapter;


import com.moria.lib.printer.interfaces.IPrintingListener;

/**
 * Author  moria
 * Date    2020/9/11
 * Time    12:04
 */
public class PrintingListenerAdapter implements IPrintingListener {
    @Override
    public void printSuccess() {

    }

    @Override
    public void printFailure(String msg) {

    }

    @Override
    public void connecting() {

    }

    @Override
    public void connectSuccess() {

    }

    @Override
    public void printing() {

    }

    @Override
    public void connectFailure(String msg) {
        printFailure(msg);
    }


}
