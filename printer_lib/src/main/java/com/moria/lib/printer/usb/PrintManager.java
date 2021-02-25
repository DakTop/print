package com.moria.lib.printer.usb;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.WorkerThread;

import com.moria.lib.printer.bean.DeviceModel;
import com.moria.lib.printer.usb.interfaces.IPrintingListener;
import com.moria.lib.printer.usb.interfaces.IRequestOncePermissionFinish;
import com.moria.lib.printer.usb.interfaces.IUsbDeviceRefreshListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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
    private AtomicBoolean lockObject = new AtomicBoolean(false);
    private boolean isWait = false;
    private PrinterUsbService usbService;
    private Context mContext;
    private CopyOnWriteArrayList<IUsbDeviceRefreshListener> usbDeviceRefreshListeners = new CopyOnWriteArrayList<>();

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
        new Thread() {
            @Override
            public void run() {
                super.run();
                waitRefreshList("refreshAllDevice");
                // 设置开始访问
                if (!lockObject.compareAndSet(false, true)) {
                    return; // 已经正在刷新
                }
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
                        if (lockObject.compareAndSet(true, false)) {
                            if (isWait) {
                                lockObject.notifyAll(); // 如果有等待的就刷新
                                isWait = false;
                            }
                        }
                    }
                }, PrinterUsbService.DEFAULT_TIME_OUT);
            }
        }.start();
    }

    /**
     * 打印
     *
     * @param deviceModel
     * @param cmd
     * @param listener
     */
    public void print(final DeviceModel deviceModel, final byte[] cmd, final IPrintingListener listener) {
        new Thread() {
            @Override
            public void run() {
                super.run();
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
        }.start();
    }

    public List<DeviceModel> getPrintDevice() {
        return usbService.getAllDeviceModel();
    }

    @WorkerThread
    private synchronized void waitRefreshList(String caller) {
        if (lockObject.compareAndSet(true, true)) {
            try {
                isWait = true;
                lockObject.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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

}
