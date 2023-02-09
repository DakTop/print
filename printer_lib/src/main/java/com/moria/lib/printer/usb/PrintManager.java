package com.moria.lib.printer.usb;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.WorkerThread;

import com.moria.lib.printer.bean.DeviceModel;
import com.moria.lib.printer.usb.interfaces.IPrintingListener;
import com.moria.lib.printer.usb.interfaces.IRequestOncePermissionFinish;
import com.moria.lib.printer.usb.interfaces.IUsbDeviceRefreshListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Author  moria
 * Date    2020/9/10
 * Time    8:59
 */
public class PrintManager {

    private static PrintManager instance;
    private AtomicBoolean isInit = new AtomicBoolean(false);
    private UsbAttachDetachReceiver mUsbReceiver;
    private boolean isWait = false;
    private PrinterUsbService usbService;
    private Context mContext;
    private CopyOnWriteArrayList<IUsbDeviceRefreshListener> usbDeviceRefreshListeners = new CopyOnWriteArrayList<>();
    private ExecutorService threadPool;

    public static PrintManager getInstance() {
        if (instance == null) {
            synchronized (PrintManager.class) {
                if (instance == null) {
                    instance = new PrintManager();
                }
            }
        }
        return instance;
    }

    private PrintManager() {
        threadPool = Executors.newFixedThreadPool(1);
    }

    /**
     * 全局初始化, 通过全局初始化UsbDeviceManager监听usb设备的插拔, 并自动更新设备列表
     */
    public void init(Context context) {
        mContext = context;
        // 仅当第一次调用的时候初始化, 如果多次调用且未返回前不用再次初始化
        if (isInit.compareAndSet(false, false)) {
            mUsbReceiver = new UsbAttachDetachReceiver();
            mUsbReceiver.register(context);//注册usb插拔广播接收器
            isInit.compareAndSet(false, true);
        }
    }

    /**
     * 异步刷新所有的usb设备列表
     */
    public void asyncRefreshAllDevice() {
        if (isWait) {
            return;
        }
        isWait = true;
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                if (usbService == null) {
                    usbService = new PrinterUsbService(mContext);
                } else {
                    usbService.release();
                }
                usbService.initAllDeviceModels(new PrinterUsbService.DevicesCallback() {
                    @Override
                    public void onCallback(List<DeviceModel> deviceModelList, List<DeviceModel> deviceWaitList) {
                        notifyUsbDeviceListener();
                        // 设置访问结束
                        isWait = false;
                    }
                }, PrinterUsbService.DEFAULT_TIME_OUT);
            }
        });
    }

    /**
     * 打印
     *
     * @param deviceModel
     * @param cmd
     * @param listener
     */
    public void print(final DeviceModel deviceModel, final byte[] cmd, final IPrintingListener listener) {
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                final UsbPrinter usbPrinter = new UsbPrinter(mContext, deviceModel, listener);
                usbService.requestPermission(deviceModel, new IRequestOncePermissionFinish() {
                    @Override
                    public void requestOncePermissionFinish(boolean success) {
                        if (success) {
                            if (TextUtils.isEmpty(deviceModel.getSerialNumber())) {
                                String val = usbService.getSerialNumberWithPermission(deviceModel.getUsbDevice());
                                deviceModel.setSerialNumber(val);
                            }
                            usbPrinter.printCmd(cmd);
                        }
                    }
                });
            }
        });
    }

    public List<DeviceModel> getPrintDevice() {
        return usbService.getAllDeviceModel();
    }

    /**
     * 退出应用时销毁占用的资源
     */
    public void destroy() {
        if (mUsbReceiver != null) {
            mUsbReceiver.unregister(mContext);
        }
        if (usbService != null) {
            usbService.release();
        }
        unRegisterAllUsbDeviceListener();
    }

    private void notifyUsbDeviceListener() {
        for (IUsbDeviceRefreshListener listener : usbDeviceRefreshListeners) {
            listener.onCallback();
        }
    }

    public void registerUsbDeviceListener(IUsbDeviceRefreshListener listener) {
        usbDeviceRefreshListeners.add(listener);
    }

    public void unRegisterUsbDeviceListener(IUsbDeviceRefreshListener listener) {
        usbDeviceRefreshListeners.remove(listener);
    }

    public void unRegisterAllUsbDeviceListener() {
        usbDeviceRefreshListeners.clear();
    }

    public boolean hasPermission(DeviceModel deviceModel) {
        if (usbService == null || deviceModel == null || deviceModel.getUsbDevice() == null) {
            return false;
        }
        return usbService.hasPermission(deviceModel.getUsbDevice());
    }

    public void requestPermission(final DeviceModel deviceModel, final IRequestOncePermissionFinish listener) {
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                usbService.requestPermission(deviceModel, listener);
            }
        });

    }
}
