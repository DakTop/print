package com.moria.lib.printer.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import com.moria.lib.printer.usb.PrintManager;
import com.moria.lib.printer.bean.DeviceModel;
import com.moria.lib.printer.bluetooth.interfaces.BluetoothBondListener;
import com.moria.lib.printer.bluetooth.interfaces.BluetoothConnectFinishListener;
import com.moria.lib.printer.bluetooth.interfaces.BluetoothConnectListener;
import com.moria.lib.printer.bluetooth.interfaces.BluetoothFindDeviceListener;
import com.moria.lib.printer.bluetooth.interfaces.BluetoothScanListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author  moria
 * Date    2020/10/16
 * Time    10:36
 */
public class BluetoothPrinterManager implements BluetoothScanListener, BluetoothBondListener, BluetoothFindDeviceListener {
    private static BluetoothPrinterManager instance;
    private BluetoothPrinterService bluetoothService;
    private List<DeviceModel> deviceList = new ArrayList<>();//蓝牙设备搜索结果集合
    private List<DeviceModel> bondedDeviceList = new ArrayList<>();//蓝牙设备已配对集合
    private Map<String, BluetoothSocket> connectDevice = new HashMap<>();//已连接成功设备
    private BluetoothReceiver bluetoothReceiver;

    private BluetoothScanListener scanListener;
    private BluetoothBondListener bondListener;

    public static BluetoothPrinterManager getInstance() {
        if (instance == null) {
            synchronized (PrintManager.class) {
                if (instance == null) {
                    instance = new BluetoothPrinterManager();
                }
            }
        }
        return instance;
    }

    private BluetoothPrinterManager() {
        bluetoothService = new BluetoothPrinterService();
        bluetoothReceiver = new BluetoothReceiver();
    }

    public void init(Context context) {
        bluetoothReceiver.registerBluetooth(context);
        bluetoothReceiver.setScanListener(this);
        bluetoothReceiver.setBondListener(this);
        bluetoothReceiver.setFindDeviceListener(this);
    }

    /**
     * 打开蓝牙，并扫描
     */
    public void openOrScanBluetooth(BluetoothScanListener scanListener) {
        this.scanListener = scanListener;
        bluetoothService.openOrScanBluetooth();
    }


    @Override
    public void onFindDeviceListener(BluetoothDevice device) {
        //将扫描出来的蓝牙设备添加到集合
        if (device == null)
            return;
        int deviceClass = device.getBluetoothClass().getDeviceClass();//设备类型 如：音频、手机、电脑、音箱
        int majorDeviceClass = device.getBluetoothClass().getMajorDeviceClass();//具体的设备类型 如：音频设备又分为音箱、耳机、麦克风
        //蓝牙打印机为1536
        if (deviceClass == 1664 && majorDeviceClass == 1536) {
            for (int i = 0; i < deviceList.size(); i++) {
                if (deviceList.get(i).getBluetoothDevice().getAddress().equals(device.getAddress())) {
                    return;
                }
            }
            deviceList.add(obtainBluetoothDevice(device));
        }
    }

    @Override
    public void onBondFinishListener(boolean isBonded) {
        if (bondListener != null) {
            bondListener.onBondFinishListener(isBonded);
        }
    }

    @Override
    public void onScanFinishListener() {
        if (scanListener != null) {
            scanListener.onScanFinishListener();
        }
    }

    public void print(final byte[] bytes) {
        for (final String key : connectDevice.keySet()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        connectDevice.get(key).getOutputStream().write(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public List<DeviceModel> getDeviceList() {
        return deviceList;
    }

    public boolean isConnect(DeviceModel device) {
        BluetoothSocket socket = connectDevice.get(device.getBluetoothDevice().getAddress());
        return socket != null && socket.isConnected();
    }

    public boolean isConnect(String macAddress) {
        BluetoothSocket socket = connectDevice.get(macAddress);
        return socket != null && socket.isConnected();
    }

    /**
     * 蓝牙设备是否正在绑定
     */
    public boolean isBonding(BluetoothDevice device) {
        return bluetoothService.isBonding(device);
    }

    /**
     * 蓝牙设备是否已经绑定
     */
    public boolean isBonded(BluetoothDevice device) {
        return bluetoothService.isBonded(device);
    }

    /**
     * 蓝牙设备是否正在绑定
     */
    public boolean isBonding(String address) {
        return isBonding(obtainDevice(address));
    }

    /**
     * 蓝牙设备是否已经绑定
     */
    public boolean isBonded(String address) {
        return isBonded(obtainDevice(address));
    }

    /**
     * 蓝牙配对
     */
    public void bondDevice(BluetoothDevice device, BluetoothBondListener bondListener) {
        this.bondListener = bondListener;
        bluetoothService.bondDevice(device);
    }

    /**
     * 蓝牙配对
     */
    public void bondDevice(String address, BluetoothBondListener bondListener) {
        bondDevice(obtainDevice(address), bondListener);
    }


    /**
     * 取消配对（取消配对成功与失败通过广播返回 也就是配对失败）
     *
     * @param device
     */
    public void cancelBondDevice(BluetoothDevice device, BluetoothBondListener bondListener) {
        this.bondListener = bondListener;
        closeConnectDevice(device);
        bluetoothService.cancelBondDevice(device);
    }

    /**
     * 取消配对（取消配对成功与失败通过广播返回 也就是配对失败）
     */
    public void cancelBondDevice(String address, BluetoothBondListener bondListener) {
        cancelBondDevice(obtainDevice(address), bondListener);
    }

    /**
     * 连接设备，回调监听方法是在子线程中进行的
     *
     * @param device
     * @param connectListener
     */
    public void connectDevice(final BluetoothDevice device, final BluetoothConnectListener connectListener) {
        bluetoothService.cancelDiscovery();
        bluetoothService.connectDevice(device, new BluetoothConnectFinishListener() {
            @Override
            public void onFinishConnectListener(boolean isConnect, BluetoothSocket mSocket) {
                connectDevice.put(device.getAddress(), mSocket);
                connectListener.onFinishConnectListener(isConnect);
            }
        });
    }

    /**
     * 连接设备，回调监听方法是在子线程中进行的
     *
     * @param macAddress
     * @param connectListener
     */
    public void connectDevice(String macAddress, BluetoothConnectListener connectListener) {
        connectDevice(obtainDevice(macAddress), connectListener);
    }


    /**
     * 断开连接
     *
     * @param device
     */
    public void closeConnectDevice(BluetoothDevice device) {
        closeConnectDevice(device.getAddress());
    }

    /**
     * 断开连接
     */
    public void closeConnectDevice(String macAddress) {
        BluetoothSocket socket = connectDevice.get(macAddress);
        connectDevice.remove(macAddress);
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void destroy() {
        for (final String key : connectDevice.keySet()) {
            try {
                BluetoothSocket socket = connectDevice.get(key);
                if (socket.isConnected()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 获取已配对设备
     *
     * @return
     */
    public List<DeviceModel> getBondedList() {
        bondedDeviceList.clear();
        List<BluetoothDevice> mList = bluetoothService.getBondedList();
        if (mList != null) {
            for (int i = 0; i < mList.size(); i++) {
                bondedDeviceList.add(obtainBluetoothDevice(mList.get(i)));
            }
        }
        return bondedDeviceList;
    }

    private DeviceModel obtainBluetoothDevice(BluetoothDevice device) {
        return new DeviceModel(device);
    }

    /**
     * 生成设备
     *
     * @param macAddress
     */
    public BluetoothDevice obtainDevice(String macAddress) {
        return bluetoothService.obtainDevice(macAddress);
    }

}
