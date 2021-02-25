package com.moria.lib.printer.network.interfaces;

public interface NetPortPrintListener {
    void connectFinish(boolean isConnect, String ip);

    void printFinish(boolean isSuccess);

    void closeFinish(String ip);

}
