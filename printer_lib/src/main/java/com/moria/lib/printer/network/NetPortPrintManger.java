package com.moria.lib.printer.network;

import android.text.TextUtils;

import com.moria.lib.printer.bean.DeviceModel;
import com.moria.lib.printer.network.interfaces.NetPortPrintListener;
import com.moria.lib.printer.usb.PrintManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 网口打印机管理类
 */
public class NetPortPrintManger {

    private static NetPortPrintManger instance;

    private ExecutorService threadPool;

    public static NetPortPrintManger getInstance() {
        if (instance == null) {
            synchronized (PrintManager.class) {
                if (instance == null) {
                    instance = new NetPortPrintManger();
                }
            }
        }
        return instance;
    }

    private NetPortPrintManger() {
        threadPool = Executors.newFixedThreadPool(1);
    }

    /**
     * 连接网口打印机打印
     *
     * @param deviceModel
     */
    public void printData(final DeviceModel deviceModel, final byte[] data, final NetPortPrintListener serviceListener) {
        if (deviceModel == null) {
            return;
        }
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                NetPortPrintService printService = new NetPortPrintService();
                printService.setServiceListener(serviceListener);
                printService.print(deviceModel.getIp(), data);
            }
        });
    }

}
