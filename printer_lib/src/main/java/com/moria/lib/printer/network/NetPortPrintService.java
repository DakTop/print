package com.moria.lib.printer.network;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.moria.lib.printer.network.interfaces.NetPortPrintListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetPortPrintService {
    private static final int ConnectFinish = 0;
    private static final int PrintFinish = 1;
    private PrintHandler handler;

    public NetPortPrintService() {
        handler = new PrintHandler(Looper.getMainLooper());
    }

    public void print(String ip, byte[] data) {
        boolean isPrintSuccess = false;
        boolean isConnectSuccess = true;
        Socket sock = new Socket();
        OutputStream outputStream = null;
        try {
            //sock.getOutputStream()返回不为空说明有输出流，此时才能向网口打印机传输数据
            InetAddress mIPAddress =
                    Inet4Address.getByName(ip);
            SocketAddress remoteAddr = new InetSocketAddress(
                    mIPAddress,
                    9100);
//            sock.setSoTimeout(4000);
            sock.connect(remoteAddr, 1500);
            outputStream = sock.getOutputStream();
            if (outputStream != null) {
                outputStream.write(data);
                outputStream.flush();
                sock.shutdownOutput();
                isPrintSuccess = true;
            } else {
                isPrintSuccess = false;
            }
        } catch (Exception e) {
            isPrintSuccess = false;
            isConnectSuccess = false;
            e.printStackTrace();
        } finally {
            //3、关闭IO资源
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                sock.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (isConnectSuccess) {
                printFinish(ip, isPrintSuccess);
            } else {
                connectFail(ip);
            }
        }
    }

    private void connectFail(String ip) {
        Message msg = new Message();
        msg.what = ConnectFinish;
        Bundle bundle = new Bundle();
        bundle.putBoolean("connect", false);
        bundle.putString("ip", ip);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    private void printFinish(String ip, boolean printSuccess) {
        Message msg = new Message();
        msg.what = PrintFinish;
        Bundle bundle = new Bundle();
        bundle.putBoolean("print", printSuccess);
        bundle.putString("ip", ip);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public void setServiceListener(NetPortPrintListener serviceListener) {
        handler.setServiceListener(serviceListener);
    }

    private static class PrintHandler extends Handler {
        private NetPortPrintListener serviceListener;

        public PrintHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            switch (msg.what) {
                case ConnectFinish:
                    if (serviceListener != null) {
                        serviceListener.connectFinish(bundle.getBoolean("connect"), bundle.getString("ip"));
                    }
                    serviceListener = null;
                    break;
                case PrintFinish:
                    if (serviceListener != null) {
                        serviceListener.printFinish(bundle.getBoolean("print"), bundle.getString("ip"));
                    }
                    serviceListener = null;
                    break;
            }
        }

        public void setServiceListener(NetPortPrintListener serviceListener) {
            this.serviceListener = serviceListener;
        }
    }
}
