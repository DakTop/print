package com.moria.lib.printer.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.text.TextUtils;

import com.moria.lib.printer.PrintManager;

/**
 * Author  moria
 * Date    2020/9/10
 * Time    10:14
 * <p>
 * 监听USB设备插拔事件
 */
public class UsbAttachDetachReceiver extends BroadcastReceiver {

    private boolean isRegister = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || TextUtils.isEmpty(intent.getAction())) {
            return;
        }
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (!PrinterDeviceHelper.isPrintDevice(device)) {//是否是打印设备
            return;
        }
        String action = intent.getAction();
        switch (action) {
            case UsbManager.ACTION_USB_DEVICE_ATTACHED://插入
            case UsbManager.ACTION_USB_DEVICE_DETACHED://拔出
                PrintManager.getInstance().asyncRefreshAllDevice();
                break;
        }
    }

    public void register(Context context) {
        if (!isRegister) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            context.registerReceiver(this, intentFilter);
            isRegister = true;
        }
    }

    public void unregister(Context context) {
        if (isRegister) {
            context.unregisterReceiver(this);
            isRegister = false;
        }
    }
}
