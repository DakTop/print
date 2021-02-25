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

    private NetPortPrintService printService = null;
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

    public void init(final List<DeviceModel> list, final NetPortPrintListener serviceListener) {
        if (list == null)
            return;
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                printService.setServiceListener(serviceListener);
                List<String> ipList = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    ipList.add(list.get(i).getIp());
                }
                printService.init(ipList);
            }
        });
    }

    private NetPortPrintManger() {
        printService = new NetPortPrintService();
        threadPool = Executors.newFixedThreadPool(1);
    }

    /**
     * 连接网口打印机
     *
     * @param deviceModel
     */
    public void connect(final DeviceModel deviceModel, final NetPortPrintListener serviceListener) {
        if (deviceModel == null) {
            return;
        }
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                printService.setServiceListener(serviceListener);
                printService.open(deviceModel.getIp());
            }
        });
    }

    /**
     * 调用所有网口打印机打印
     *
     * @param data
     */
    public void print(final byte[] data, final NetPortPrintListener serviceListener) {
        if (data == null) {
            return;
        }
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                printService.setServiceListener(serviceListener);
                printService.print(data);
            }
        });
    }

    /**
     * 指定网口打印机打印
     *
     * @param data
     */
    public void print(final byte[] data, final DeviceModel deviceModel, final NetPortPrintListener serviceListener) {
        if (data == null || deviceModel == null) {
            return;
        }
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                printService.setServiceListener(serviceListener);
                printService.print(data, deviceModel.getIp());
            }
        });

    }

    /**
     * 关闭指定网口打印机
     *
     * @param deviceModel
     */
    public void close(final DeviceModel deviceModel, final NetPortPrintListener serviceListener) {
        if (deviceModel == null) {
            return;
        }
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                printService.setServiceListener(serviceListener);
                printService.close(deviceModel.getIp());
            }
        });
    }

    /**
     * 关闭所有网口打印机
     */
    public void closeAll(final NetPortPrintListener serviceListener) {
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                printService.setServiceListener(serviceListener);
                printService.closeAll();
            }
        });
    }

    public boolean isConnect(DeviceModel deviceModel) {
        return deviceModel != null && printService.isConnect(deviceModel.getIp());
    }

}
