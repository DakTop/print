package com.moria.lib.printer.helper;

import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.moria.lib.printer.bean.DeviceModel;
import com.moria.lib.printer.interfaces.IPrintingListener;

import java.lang.ref.WeakReference;

/**
 * Author  moria
 * Date    2020/9/11
 * Time    11:51
 */
public class UsbPrinter {

    private UsbManager mUsbManager;
    private DeviceModel mDeviceModel;
    private static IPrintingListener mListener;
    private Context mContext;
    private NoLeakHandler mHandler;

    private static final int CONNECTSUCCESS = 0;
    private static final int PRINTING = 1;
    private static final int PRINTSUCCESS = 2;
    private static final int PRINTFAILURE = 3;
    private static final int CONNECTFAILURE = 4;
    private static final int CONNECTING = 5;

    public UsbPrinter(Context context, DeviceModel deviceModel, IPrintingListener listener) {
        this.mListener = listener;
        this.mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        this.mDeviceModel = deviceModel;
        this.mContext = context;
        mHandler = new NoLeakHandler(mContext, Looper.getMainLooper());
    }

    //一次最大打印约256000byte
    public void printCmd(byte[] cmds) {
        mHandler.sendEmptyMessage(CONNECTING);
        UsbDevice myUsbDevice = mDeviceModel.getUsbDevice();
        if (myUsbDevice == null) {
            connectFailure("未找到设备");
            return;
        }
        UsbInterface mInterface = mDeviceModel.getUsbInterface();
        if (mInterface == null) {
            connectFailure("未找到设备");
            return;
        }
        UsbEndpoint epOut = null;
        for (int i = 0; i < mInterface.getEndpointCount(); i++) {
            UsbEndpoint ep = mInterface.getEndpoint(i);
            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                    epOut = ep;
                    break;
                }
            }
        }

        UsbDeviceConnection deviceConnection = mUsbManager.openDevice(myUsbDevice);
        if (deviceConnection == null) {
            connectFailure("连接失败");
            return;
        }
        if (!deviceConnection.claimInterface(mInterface, true)) {
            connectFailure("连接失败");
            return;
        }
        mHandler.sendEmptyMessage(CONNECTSUCCESS);
        mHandler.sendEmptyMessage(PRINTING);
        int allSize = 0;
        byte[] parCmds = new byte[1024 * 10];
        int times = 0;
        int len = cmds.length;
        for (int i = 0; i < len; i++) {
            if (times >= 1024 * 10) {
                int size = deviceConnection.bulkTransfer(epOut, parCmds, parCmds.length, 2000);
                allSize = allSize + size;
                parCmds = new byte[1024 * 10];
                times = 0;
            }
            times++;
            parCmds[i % (1024 * 10)] = cmds[i];
        }
        if (times >= 0) {
            int size = deviceConnection.bulkTransfer(epOut, parCmds, parCmds.length, 2000); // 最大返回 16384
            if (size == 1024 * 10) {
                allSize = allSize + times;
            }

        }

        if (mListener != null) {
            if (allSize == cmds.length) {
                mHandler.sendEmptyMessage(PRINTSUCCESS);
            } else {
                Message message = mHandler.obtainMessage();
                message.what = PRINTFAILURE;
                Bundle bundle = new Bundle();
                bundle.putString("error", "打印失败");
                message.setData(bundle);
                mHandler.sendMessage(message);
            }
        }
//        deviceConnection.releaseInterface(mInterface);
        deviceConnection.close();
    }

    private void connectFailure(String msg) {
        Message message = mHandler.obtainMessage();
        message.what = CONNECTFAILURE;
        Bundle bundle = new Bundle();
        bundle.putString("error", msg);
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    public static void release(NoLeakHandler handler) {
        if (mListener != null) {
            mListener = null;
        }
        handler.removeCallbacksAndMessages(null);
    }

    private static class NoLeakHandler extends Handler {

        private WeakReference<Context> mContext;

        public NoLeakHandler(Context context, Looper looper) {
            super(looper);
            mContext = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mListener == null) {
                release(this);
                return;
            }
            switch (msg.what) {
                case CONNECTSUCCESS:
                    mListener.connectSuccess();
                    break;
                case PRINTING:
                    mListener.printing();
                    break;
                case PRINTSUCCESS:
                    mListener.printSuccess();
                    release(this);
                    break;
                case PRINTFAILURE:
                    mListener.printFailure(msg.getData().getString("error"));
                    release(this);
                    break;
                case CONNECTFAILURE:
                    mListener.connectFailure(msg.getData().getString("error"));
                    release(this);
                    break;
                case CONNECTING:
                    mListener.connecting();
                    break;

            }
        }
    }
}
