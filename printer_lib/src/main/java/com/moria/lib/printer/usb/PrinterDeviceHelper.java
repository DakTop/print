package com.moria.lib.printer.usb;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;

/**
 * 打印设备帮助类
 *
 * @author moria
 * @date 2018/11/27
 */
public class PrinterDeviceHelper {

    /**
     * 获取打印设备的打印接口
     */
    public static UsbInterface getUsbInterface(UsbDevice usbDevice) {
        if (usbDevice == null)
            return null;
        int usbInterfaceCount = usbDevice.getInterfaceCount();
        for (int i = 0; i < usbInterfaceCount; i++) {
            if (usbDevice.getInterface(i)
                    .getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
                return usbDevice.getInterface(i);
            }
        }
        return null;
    }

    /**
     * 检测设备是否是打印设备  true:是，false:否
     */
    public static boolean isPrintDevice(UsbDevice usbDevice) {
        return getUsbInterface(usbDevice) != null;
    }
}
