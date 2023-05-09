package com.moria.lib.printer.usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.moria.lib.printer.bean.DeviceModel;
import com.moria.lib.printer.usb.interfaces.IRequestOncePermissionFinish;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Author  moria
 * Date    2020/9/10
 * Time    10:29
 * <p>
 * usb打印设备服务
 */
public class PrinterUsbService {
    private static final String ACTION_ONE_USB_PERMISSION = "com.android.example.ONE_USB_PERMISSION";
    private static int REQUEST_ONE_DEVICE_PERMISSION = 1002;
    private final String TAG = "usb打印设备服务";
    //
    private List<DeviceModel> mAllDeviceModels = new ArrayList<>();
    private UsbManager mUsbManager;
    private Context mContext;
    private IRequestOncePermissionFinish requestOncePermissionFinish;

    public PrinterUsbService(Context context) {
        mContext = context.getApplicationContext();
        this.mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_ONE_USB_PERMISSION);
        mContext.registerReceiver(mUsbPermissionReceiver, intentFilter);
    }

    /**
     * 异步获取当前连接的UsbPrinter
     *
     * @param devicesCallback 获取返回的Printers
     */
    protected void initAllDeviceModels(final DevicesCallback devicesCallback) {
        mAllDeviceModels.clear();
        HashMap<String, UsbDevice> deviceHashMap = mUsbManager.getDeviceList();
        Iterator<UsbDevice> usbDeviceIterator = deviceHashMap.values().iterator();
        Log.i(TAG, "usb设备数= " + deviceHashMap.values().size());
        while (usbDeviceIterator.hasNext()) {
            UsbDevice usbDevice = usbDeviceIterator.next();
            UsbInterface usbInterface = PrinterDeviceHelper.getUsbInterface(usbDevice);
            if (usbInterface == null) {//接口为空则不是打印机
                continue;
            }
            Log.e(TAG, "打印机详情：" + usbDevice.toString());
            mAllDeviceModels.add(new DeviceModel(usbDevice, usbInterface));
        }
        if (devicesCallback != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    devicesCallback.onCallback(mAllDeviceModels);
                }
            });
        }
    }

    /**
     * 单次请求设备权限
     *
     * @param deviceModel
     * @param onRequestOncePermissionFinish
     */
    protected void requestPermission(DeviceModel deviceModel, IRequestOncePermissionFinish onRequestOncePermissionFinish) {
        this.requestOncePermissionFinish = onRequestOncePermissionFinish;
        Intent intent = new Intent(ACTION_ONE_USB_PERMISSION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, REQUEST_ONE_DEVICE_PERMISSION, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mUsbManager.requestPermission(deviceModel.getUsbDevice(), pendingIntent);
    }

    private BroadcastReceiver mUsbPermissionReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (null == action || action.length() == 0) {
                return;
            }
            if (TextUtils.equals(action, ACTION_ONE_USB_PERMISSION)) {//单次为某个设备请求权限
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (requestOncePermissionFinish == null)
                    return;
                if (mUsbManager.hasPermission(device)) {
                    requestOncePermissionFinish.requestOncePermissionFinish(true);
                } else {
                    requestOncePermissionFinish.requestOncePermissionFinish(false);
                }
                requestOncePermissionFinish = null;
            }
        }
    };

    protected void release() {
        if (mUsbPermissionReceiver != null) {
            mContext.unregisterReceiver(mUsbPermissionReceiver);
            mUsbPermissionReceiver = null;
        }
        requestOncePermissionFinish = null;
    }

    public String getSerialNumberWithPermission(UsbDevice usbDevice) {
        if (usbDevice == null)
            return "";
        UsbDeviceConnection usbDeviceConnection = mUsbManager.openDevice(usbDevice);
        String serialNumber = usbDeviceConnection.getSerial();
        usbDeviceConnection.close();
        return serialNumber;
    }

    protected List<DeviceModel> getAllDeviceModel() {
        return mAllDeviceModels;
    }


    protected interface DevicesCallback {

        /**
         * @param deviceModelList 已经获取open权限的设备列表
         */
        void onCallback(List<DeviceModel> deviceModelList);

    }

    protected boolean hasPermission(UsbDevice device) {
        return mUsbManager.hasPermission(device);
    }
}
