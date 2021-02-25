package com.moria.lib.printer.bean;

import android.bluetooth.BluetoothDevice;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.os.Build;

/**
 * Author  moria
 * Date    2020/9/10
 * Time    9:59
 */
public class DeviceModel {
    private int deviceType = 0;//设备类型：0：usb打印机，1：蓝牙打印机，2：网口打印机
    //
    private UsbDevice usbDevice;
    private UsbInterface usbInterface;
    //
    private String deviceName;//设备的名称,在标准实现中，这是设备文件的路径
    private String productName;//设备的产品名称（可能为空）
    private String manufacturerName;//设备的制造商名称（可能为空）
    private int vendorId;//设备的供应商ID
    private int productId;//设备的产品id
    private String serialNumber;//设备的序列号,如果当前android系统版本大于等于10.0并且没有设备访问权限，则会抛出异常。
    //蓝牙设备信息
    private BluetoothDevice bluetoothDevice;

    //网口打印机信息
    private String ip;

    public DeviceModel(UsbDevice usbDevice, UsbInterface usbInterface) {
        deviceType = 0;
        this.usbDevice = usbDevice;
        this.usbInterface = usbInterface;
        if (usbDevice != null) {
            deviceName = usbDevice.getDeviceName();
            productName = usbDevice.getProductName();
            manufacturerName = usbDevice.getManufacturerName();
            vendorId = usbDevice.getVendorId();
            productId = usbDevice.getProductId();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                serialNumber = usbDevice.getSerialNumber();
            }
        }
    }

    //蓝牙打印机
    public DeviceModel(BluetoothDevice bluetoothDevice) {
        deviceType = 1;
        this.bluetoothDevice = bluetoothDevice;
        if (bluetoothDevice != null) {
            deviceName = bluetoothDevice.getName();
        }
    }

    //网口打印机
    public DeviceModel(String ip, String name) {
        deviceType = 2;
        this.ip = ip;
        deviceName = name;
    }

    public DeviceModel() {
    }

    public UsbDevice getUsbDevice() {
        return usbDevice;
    }

    public void setUsbDevice(UsbDevice usbDevice) {
        this.usbDevice = usbDevice;
    }

    public UsbInterface getUsbInterface() {
        return usbInterface;
    }

    public void setUsbInterface(UsbInterface usbInterface) {
        this.usbInterface = usbInterface;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public int getVendorId() {
        return vendorId;
    }

    public void setVendorId(int vendorId) {
        this.vendorId = vendorId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
