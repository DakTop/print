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
import android.os.Build;
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
import java.util.Timer;
import java.util.TimerTask;

/**
 * Author  moria
 * Date    2020/9/10
 * Time    10:29
 * <p>
 * usb打印设备服务
 */
public class PrinterUsbService {
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final String ACTION_ONE_USB_PERMISSION = "com.android.example.ONE_USB_PERMISSION";

    private final String TAG = "usb打印设备服务";
    public static final int DEFAULT_TIME_OUT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 60 * 60 * 1000 : 3 * 1000;
    private static final String PRINTER_INTERFACE = "PRINTER_INTERFACE";

    private static final int STD_USB_REQUEST_GET_DESCRIPTOR = 0x06;
    private static final int LIB_USB_DT_STRING = 0x03;
    private static int REQUEST_DEVICE_PERMISSION = 1001;
    private static int REQUEST_ONE_DEVICE_PERMISSION = 1002;
    //
    private List<DeviceModel> mAllDeviceModels = new ArrayList<>();
    private List<DeviceModel> mDeviceModels = new ArrayList<>();
    private List<DeviceModel> mDeviceWaitList = new ArrayList<>();
    private DevicesCallback mDevicesCallback = null;
    private UsbManager mUsbManager;
    private Context mContext;
    private boolean isReceiverRegister = false;
    private Timer mTimer;//计算超时时间
    private IRequestOncePermissionFinish requestOncePermissionFinish;

    public PrinterUsbService(Context context) {
        mContext = context.getApplicationContext();
        this.mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    }

    /**
     * 异步获取当前连接的UsbPrinter
     *
     * @param devicesCallback 获取返回的Printers, 超过timeOutInMs仍没有获取权限的设备会用第二个参数返回
     * @param timeOutInMs     等待获取权限的超时时间, 0使用默认的超时配置
     */
    protected void initAllDeviceModels(DevicesCallback devicesCallback, int timeOutInMs) {
        if (timeOutInMs == 0) {
            timeOutInMs = DEFAULT_TIME_OUT;
        }
        mDevicesCallback = devicesCallback;
        mDeviceModels.clear();
        mDeviceWaitList.clear();
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                //大于6.0系统的权限需要用户同意
                mDeviceModels.add(new DeviceModel(usbDevice, usbInterface));
            } else if (mUsbManager.hasPermission(usbDevice)) {
                addDeviceModelWithPermission(usbDevice, usbInterface);
            } else {
                // 添加进wait list
                mDeviceWaitList.add(new DeviceModel(usbDevice, usbInterface));
            }
        }
        if (!isReceiverRegister) {
            mTimer = new Timer();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_USB_PERMISSION);
            intentFilter.addAction(ACTION_ONE_USB_PERMISSION);
            mContext.registerReceiver(mUsbPermissionReceiver, intentFilter);
            mTimer.schedule(new TimeOutTask(), timeOutInMs); //timeOutInMs时间后返回结果
            isReceiverRegister = true;
        }
        checkWaitDevice();
    }

    /**
     * 单次请求设备权限
     *
     * @param deviceModel
     * @param onRequestOncePermissionFinish
     */
    protected void requestPermission(DeviceModel deviceModel, IRequestOncePermissionFinish onRequestOncePermissionFinish) {
        if (mUsbManager.hasPermission(deviceModel.getUsbDevice())) {
            onRequestOncePermissionFinish.requestOncePermissionFinish(true);
        } else {
            this.requestOncePermissionFinish = onRequestOncePermissionFinish;
            Intent intent = new Intent(ACTION_ONE_USB_PERMISSION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, REQUEST_ONE_DEVICE_PERMISSION, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mUsbManager.requestPermission(deviceModel.getUsbDevice(), pendingIntent);
        }
    }

    private void checkWaitDevice() {
        if (mDeviceWaitList.size() > 0) {
            //请求权限
            DeviceModel deviceModel = mDeviceWaitList.get(0);
            Intent intent = new Intent(ACTION_USB_PERMISSION);
            intent.putExtra(PRINTER_INTERFACE, deviceModel.getUsbInterface());
            mUsbManager.requestPermission(deviceModel.getUsbDevice(), PendingIntent.getBroadcast(mContext, REQUEST_DEVICE_PERMISSION, intent, PendingIntent.FLAG_ONE_SHOT));
        } else {
            callBack();
            release();
        }
    }

    private BroadcastReceiver mUsbPermissionReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (null == action || action.length() == 0) {
                return;
            }
            if (TextUtils.equals(action, ACTION_USB_PERMISSION)) {//初始化请求权限
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                for (int i = 0; i < mDeviceWaitList.size(); i++) {
                    DeviceModel waitModel = mDeviceWaitList.get(i);
                    if (TextUtils.equals(device.getDeviceName(), waitModel.getUsbDevice().getDeviceName())) {
                        mDeviceWaitList.remove(i);
                        break;
                    }
                }
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    UsbInterface usbInterface = intent.getParcelableExtra(PRINTER_INTERFACE);
                    addDeviceModelWithPermission(device, usbInterface);
                } else {
                    //拒绝了权限请求
                }
                checkWaitDevice();
            } else if (TextUtils.equals(action, ACTION_ONE_USB_PERMISSION)) {//单次为某个设备请求权限
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


    class TimeOutTask extends TimerTask {
        @Override
        public void run() {
            callBack();
            release();
            cancel();
        }
    }

    private void callBack() {
        mAllDeviceModels.clear();
        mAllDeviceModels.addAll(mDeviceModels);
        mAllDeviceModels.addAll(mDeviceWaitList);
        if (mDevicesCallback != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mDevicesCallback.onCallback(mDeviceModels, mDeviceWaitList);
                    mDevicesCallback = null;
                }
            });
        }
    }

    protected void release() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (isReceiverRegister && mUsbPermissionReceiver != null) {
            mContext.unregisterReceiver(mUsbPermissionReceiver);
            mUsbPermissionReceiver = null;
            isReceiverRegister = false;
        }
        requestOncePermissionFinish = null;
    }

    private void addDeviceModelWithPermission(UsbDevice usbDevice, UsbInterface usbInterface) {
        String serialNumber = getSerialNumberWithPermission(usbDevice);
        DeviceModel deviceModel = new DeviceModel(usbDevice, usbInterface);
        deviceModel.setSerialNumber(serialNumber);
        mDeviceModels.add(deviceModel);

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
         * @param deviceWaitList  超时, 没有获取open权限的设备列表
         */
        void onCallback(List<DeviceModel> deviceModelList, List<DeviceModel> deviceWaitList);

    }
}
