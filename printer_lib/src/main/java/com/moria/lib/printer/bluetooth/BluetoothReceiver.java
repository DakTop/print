package com.moria.lib.printer.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.moria.lib.printer.bluetooth.interfaces.BluetoothBondListener;
import com.moria.lib.printer.bluetooth.interfaces.BluetoothFindDeviceListener;
import com.moria.lib.printer.bluetooth.interfaces.BluetoothScanListener;

/**
 * 蓝牙广播接收器
 * <p>
 * Author  moria
 * Date    2020/10/15
 * Time    16:17
 */
public class BluetoothReceiver extends BroadcastReceiver {

    private BluetoothScanListener scanListener;
    private BluetoothBondListener bondListener;
    private BluetoothFindDeviceListener findDeviceListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle bundle = intent.getExtras();
        if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {//开关模式变化广播
            int previous_state = bundle.getInt(BluetoothAdapter.EXTRA_PREVIOUS_STATE, 0);//前一个状态
            int state = bundle.getInt(BluetoothAdapter.EXTRA_STATE, 0);//当前状态
            Log.i("状态改变", previous_state + "_" + state);
            if (state == BluetoothAdapter.STATE_ON) {//打开
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (!mBluetoothAdapter.isDiscovering()) {//是否正在扫描中
                    mBluetoothAdapter.startDiscovery();
                }
            } else if (state == BluetoothAdapter.STATE_OFF) {//

            }
        } else if (action == BluetoothAdapter.ACTION_DISCOVERY_STARTED) {//开始扫描
            Log.i("开始扫描", "。。。");
        } else if (action == BluetoothAdapter.ACTION_DISCOVERY_FINISHED) {//完成扫描
            Log.i("完成扫描", "。。。");
            if (scanListener != null) {
                scanListener.onScanFinishListener();
            }
        } else if (action == BluetoothDevice.ACTION_FOUND) {//寻找到设备
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (findDeviceListener != null) {
                findDeviceListener.onFindDeviceListener(device);
            }
        } else if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {//绑定状态改变
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            switch (device.getBondState()) {
                case BluetoothDevice.BOND_BONDING://正在配对...
                    Log.i("配对", "正在配对。。。");
                    break;
                case BluetoothDevice.BOND_BONDED://配对完成
                    Log.i("配对", "配对完成。。。");
                    if (bondListener != null) {
                        bondListener.onBondFinishListener(true);
                    }
                    break;
                case BluetoothDevice.BOND_NONE://取消配对
                    Log.i("配对", "配对失败。。。");
                    if (bondListener != null) {
                        bondListener.onBondFinishListener(false);
                    }
                default:
                    break;
            }
        }
    }

    /**
     * 注册蓝牙功能的广播接收器
     */
    public void registerBluetooth(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        //开关模式变化广播
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //扫描模式变化广播
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        //本地的蓝牙适配器改变了自己的名称
        intentFilter.addAction(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED);
        //开始扫描
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        //完成扫描
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //寻找到设备
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        //绑定状态改变
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        //配对请求
        intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        context.getApplicationContext().registerReceiver(this, intentFilter);
    }

    public void setScanListener(BluetoothScanListener scanListener) {
        this.scanListener = scanListener;
    }

    public void setBondListener(BluetoothBondListener bondListener) {
        this.bondListener = bondListener;
    }

    public void setFindDeviceListener(BluetoothFindDeviceListener findDeviceListener) {
        this.findDeviceListener = findDeviceListener;
    }
}