package com.moria.lib.printer.bean;

import com.moria.lib.printer.usb.interfaces.IPrintingListener;

public class NoPermissionDevice {
    public DeviceModel deviceModel;
    public byte[] cmd;
    public IPrintingListener listener;

    public NoPermissionDevice(DeviceModel deviceModel, byte[] cmd, IPrintingListener listener) {
        this.deviceModel = deviceModel;
        this.cmd = cmd;
        this.listener = listener;
    }
}
