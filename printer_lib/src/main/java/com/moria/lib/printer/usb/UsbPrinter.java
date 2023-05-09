package com.moria.lib.printer.usb;

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
import com.moria.lib.printer.usb.interfaces.IPrintingListener;

import java.lang.ref.WeakReference;

/**
 * Author  moria
 * Date    2020/9/11
 * Time    11:51
 */
public class UsbPrinter {

    private UsbManager mUsbManager;
    private DeviceModel mDeviceModel;

    private NoLeakHandler mHandler;

    private static final int CONNECTSUCCESS = 0;
    private static final int PRINTSUCCESS = 1;
    private static final int PRINTFAILURE = 2;
    private static final int CONNECTFAILURE = 3;

    public UsbPrinter(Context context, DeviceModel deviceModel, IPrintingListener listener) {
        this.mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        this.mDeviceModel = deviceModel;
        mHandler = new NoLeakHandler(Looper.getMainLooper());
        mHandler.setListener(listener);
    }

    //一次最大打印约256000byte
    public void printCmd(byte[] cmds) {
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
        int allSize = 0;
        int writeLength = 1024;
        byte[] parCmds = new byte[writeLength];
        int times = 0;
        int len = cmds.length;
        for (int i = 0; i < len; i++) {
            if (times >= writeLength) {
                int size = deviceConnection.bulkTransfer(epOut, parCmds, parCmds.length, 6000);
                allSize = allSize + size;
                parCmds = new byte[writeLength];
                times = 0;
            }
            times++;
            parCmds[i % (writeLength)] = cmds[i];
        }
        if (times >= 0) {
            int size = deviceConnection.bulkTransfer(epOut, parCmds, parCmds.length, 6000); // 最大返回 16384
            if (size == writeLength) {
                allSize = allSize + times;
            }
        }
        deviceConnection.releaseInterface(mInterface);
        deviceConnection.close();
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

    private void connectFailure(String msg) {
        Message message = mHandler.obtainMessage();
        message.what = CONNECTFAILURE;
        Bundle bundle = new Bundle();
        bundle.putString("error", msg);
        message.setData(bundle);
        mHandler.sendMessage(message);
    }


    private static class NoLeakHandler extends Handler {
        private IPrintingListener mListener;

        public NoLeakHandler(Looper looper) {
            super(looper);
        }

        public void setListener(IPrintingListener mListener) {
            this.mListener = mListener;
        }

        public void release() {
            mListener = null;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mListener == null) {
                release();
                return;
            }
            switch (msg.what) {
                case CONNECTSUCCESS:
                    mListener.connectSuccess();
                    break;
                case PRINTSUCCESS:
                    mListener.printSuccess();
                    release();
                    break;
                case PRINTFAILURE:
                    mListener.printFailure(msg.getData().getString("error"));
                    release();
                    break;
                case CONNECTFAILURE:
                    mListener.connectFailure(msg.getData().getString("error"));
                    release();
                    break;
            }
        }
    }
}
