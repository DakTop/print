package com.moria.print;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.moria.lib.printer.bluetooth.BluetoothPrinterManager;
import com.moria.lib.printer.bluetooth.interfaces.BluetoothBondListener;
import com.moria.lib.printer.bluetooth.interfaces.BluetoothConnectListener;
import com.moria.lib.printer.bluetooth.interfaces.BluetoothScanListener;
import com.moria.lib.printer.label.cmd.LabelCommand;
import com.moria.lib.printer.network.NetPortPrintManger;
import com.moria.lib.printer.network.interfaces.NetPortPrintListener;
import com.moria.print.print.TicketPrint;
import com.moria.lib.printer.usb.PrintManager;
import com.moria.lib.printer.usb.adapter.PrintingListenerAdapter;
import com.moria.lib.printer.bean.DeviceModel;
import com.moria.lib.printer.label.template.LabelModel;
import com.moria.lib.printer.usb.interfaces.IUsbDeviceRefreshListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DeviceAdapter.OnItemClickListener {

    private RecyclerView deviceList;
    private DeviceAdapter adapter;
    private boolean labelPrint = false;
    //
    private BluetoothPrinterManager bluetoothPrinter;
    private RecyclerView bluetoothDeviceList;
    private BluetoothDeviceAdapter bluetoothAdapter;
    private RecyclerView bondedBluetoothDeviceList;
    private BluetoothDeviceAdapter bondedBluetoothAdapter;

    private DeviceModel deviceModel_221;
    private DeviceModel deviceModel_212;
    private DeviceModel deviceModel_137;
    private List<DeviceModel> netPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //usb打印机
        initUsb();
        //蓝牙
//        initBluetooth();
        //网口打印机
//        initNetPort();
    }

    public void initUsb() {
        deviceList = findViewById(R.id.deviceList);
        deviceList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DeviceAdapter(this);
        adapter.setOnItemClickListener(this);
        deviceList.setAdapter(adapter);
        PrintManager.getInstance().init(this);
        PrintManager.getInstance().registerUsbDeviceListener(new IUsbDeviceRefreshListener() {
            @Override
            public void onCallback() {
                adapter.refreshData(PrintManager.getInstance().getPrintDevice());
            }
        });
        refreshUsb(null);
    }

    public void refreshUsb(View view) {
        PrintManager.getInstance().asyncRefreshAllDevice();
    }

    public void toPrint(View view) {
        labelPrint = true;
        onItemClickListener(adapter.getData().get(0), 0);
//        labelPrint = false;
//        onItemClickListener(adapter.getData().get(1), 1);
    }

    @Override
    public void onItemClickListener(DeviceModel deviceModel, int p) {
        byte[] cmds = new byte[0];
        if (labelPrint) {
            List<LabelPrintEntity> productList = new ArrayList<>();

            for (int i = 0; i < 1; i++) {
                LabelPrintEntity productInfo = new LabelPrintEntity();
                productInfo.setLabelName("单八桂单八桂");
                productInfo.setGoodsName("娃哈哈娃娃哈哈娃娃");
                productInfo.setGoodsFrom("云南云南云南");
                productInfo.setGoodsUnit("瓶");
                productInfo.setGoodsType("规格");
                productInfo.setGoodsBrand("品牌");
                productInfo.setGoodsCode("5658462364218");
                productInfo.setRetailPrice("199.66");
                productInfo.setMemPrice("11.11");
                productInfo.setSpecialPrice("36.22");
                productInfo.setMemSpecPrice("30.22");
                productList.add(productInfo);
            }
            LabelModel labelModel = new LabelPrintDefaultModel(productList);
            labelModel.setDirection(LabelCommand.DIRECTION.BACKWARD);
            cmds = labelModel.getPrintBytes();
        } else {
            cmds = TicketPrint.buildTextPrintData();
        }
        for (int i = 0; i < 1; i++) {
            PrintManager.getInstance().print(deviceModel, cmds, new PrintingListenerAdapter() {
                @Override
                public void connectSuccess() {
                    super.connectSuccess();
                    Log.i("打印", "连接成功");
                }

                @Override
                public void connectFailure(String msg) {
                    super.connectFailure(msg);
                    Log.i("打印", "连接失败");
                }

                @Override
                public void printSuccess() {
                    super.printSuccess();
                    Log.i("打印", "打印成功");
                    Toast.makeText(MainActivity.this, "打印成功", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void printFailure(String msg) {
                    super.printFailure(msg);
                    Log.i("打印", "打印失败");
                    Toast.makeText(MainActivity.this, "打印失败", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    public void initNetPort() {
        netPort = new ArrayList<>();
        deviceModel_212 = new DeviceModel();
        deviceModel_212.setIp("192.168.5.212");
        netPort.add(deviceModel_212);
        deviceModel_221 = new DeviceModel();
        deviceModel_221.setIp("192.168.5.221");
        netPort.add(deviceModel_221);
        deviceModel_137 = new DeviceModel();
        deviceModel_137.setIp("192.168.5.137");
        netPort.add(deviceModel_137);
    }

    /**
     * 打印网口打印机
     *
     * @param view
     */
    public void printNetPrint(View view) {
        for (int i = 0; i < 1; i++) {
            NetPortPrintManger.getInstance().printData(deviceModel_221, TicketPrint.testCmd(), new NetPortPrintListener() {
                @Override
                public void connectFinish(boolean isConnect, String ip) {
                    Log.i("网口连接", ip + "——" + isConnect);
                }

                @Override
                public void printFinish(boolean isSuccess, String ip) {
                    Log.i("网口打印", ip + "——" + isSuccess);
                }
            });
        }
    }

    public void initBluetooth() {
        //
        bluetoothPrinter = BluetoothPrinterManager.getInstance();
        bluetoothPrinter.init(this);
        //已配对过的蓝牙设备
        bondedBluetoothDeviceList = findViewById(R.id.bondedBluetoothDeviceList);
        bondedBluetoothDeviceList.setLayoutManager(new LinearLayoutManager(this));
        bondedBluetoothAdapter = new BluetoothDeviceAdapter(this);
        bondedBluetoothDeviceList.setAdapter(bondedBluetoothAdapter);
        bondedBluetoothAdapter.setOnItemClickListener(new BluetoothDeviceAdapter.OnItemClickListener() {
            @Override
            public void onItemClickListener(final DeviceModel deviceModel, int p) {
                CusDialog cusDialog = new CusDialog(MainActivity.this, new CusDialog.OnItemClickListener() {
                    @Override
                    public void onCancelBond() {
                        bluetoothPrinter.cancelBondDevice(deviceModel.getBluetoothDevice(), new BluetoothBondListener() {
                            @Override
                            public void onBondFinishListener(boolean isBonded) {
                                refreshBondedList();
                            }
                        });
                    }

                    @Override
                    public void onConnectDevice() {
                        bluetoothPrinter.connectDevice(deviceModel.getBluetoothDevice(), new BluetoothConnectListener() {
                            @Override
                            public void onFinishConnectListener(boolean isConnect) {
                                if (isConnect) {
                                    Log.i("连接", "连接成功");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            refreshBondedList();
                                        }
                                    });
                                } else {
                                    Log.i("连接", "连接失败");
                                }

                            }
                        });
                    }

                    @Override
                    public void onCancelConnectDevice() {
                        bluetoothPrinter.closeConnectDevice(deviceModel.getBluetoothDevice());
                        refreshBondedList();
                    }

                    @Override
                    public void onPrint() {
                        bluetoothPrinter.print(TicketPrint.testCmd());
                    }
                });
                cusDialog.show();
            }
        });
        refreshBondedList();
        //
        bluetoothDeviceList = findViewById(R.id.bluetoothDeviceList);
        bluetoothDeviceList.setLayoutManager(new LinearLayoutManager(this));
        bluetoothAdapter = new BluetoothDeviceAdapter(this);
        bluetoothDeviceList.setAdapter(bluetoothAdapter);
        bluetoothAdapter.setOnItemClickListener(new BluetoothDeviceAdapter.OnItemClickListener() {
            @Override
            public void onItemClickListener(DeviceModel deviceModel, int p) {
//                bluetoothPrinter.bondDevice(deviceModel, new BluetoothBondListener() {
//                    @Override
//                    public void onBondFinishListener(boolean isBonded) {
//                        refreshBondedList();
//                    }
//                });
                Log.i("物理地址：", "" + deviceModel.getBluetoothDevice().getAddress());
                bluetoothPrinter.connectDevice(deviceModel.getBluetoothDevice().getAddress(), new BluetoothConnectListener() {
                    @Override
                    public void onFinishConnectListener(boolean isConnect) {
                        Log.i("连接结果：", "" + isConnect);
                    }
                });
            }
        });
    }


    /**
     * 打开蓝牙
     *
     * @param view
     */
    public void openBluetooth(View view) {
        bluetoothPrinter.openOrScanBluetooth(new BluetoothScanListener() {
            @Override
            public void onScanFinishListener() {
                bluetoothAdapter.refreshData(null);
                refreshBondedList();
                refreshList();
                Toast.makeText(MainActivity.this, "扫描完成", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 获取蓝牙列表
     *
     * @param view
     */
    public void getBluetoothDevice(View view) {

    }


    public void refreshBondedList() {
        bondedBluetoothAdapter.refreshData(bluetoothPrinter.getBondedList());
    }

    public void refreshList() {
        bluetoothAdapter.refreshData(bluetoothPrinter.getDeviceList());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PrintManager.getInstance().unRegisterAllUsbDeviceListener();
    }
}