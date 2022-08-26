package com.moria.lib.printer.network;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.moria.lib.printer.network.interfaces.NetPortPrintListener;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetPortPrintService {
    private static final int ConnectFinish = 0;
    private static final int PrintFinish = 1;
    private static final int CloseFinish = 2;
    private NetPortPrintListener serviceListener;
    private Map<String, Socket> map;
    private PrintHandler handler;

    public NetPortPrintService() {
        map = new HashMap<>();
        handler = new PrintHandler(Looper.getMainLooper());
    }

    // 网络打印机 打开网络打印机
    public void open(String ip) {
        boolean connectFlag = false;
        Socket sock = map.get(ip);
        if (sock == null) {
            sock = new Socket();
        }
        try {
            //sock.getOutputStream()返回不为空说明有输出流，此时才能向网口打印机传输数据
            if (!sock.isConnected() || !sock.isClosed() || sock.getOutputStream() == null) {
                InetAddress mIPAddress =
                        Inet4Address.getByName(ip);
                SocketAddress remoteAddr = new InetSocketAddress(
                        mIPAddress,
                        9100);

                sock.connect(remoteAddr, 4000);
                if (sock.getOutputStream() != null) {
                    map.put(ip, sock);
                    connectFlag = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Message msg = new Message();
            msg.what = ConnectFinish;
            Bundle bundle = new Bundle();
            bundle.putBoolean("connect", connectFlag);
            bundle.putString("ip", ip);
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
    }

    // 网络打印机 关闭
    public void close(String ip) {
        Socket sock = map.get(ip);
        if (sock != null) {
            close(ip, sock);
        }
    }

    private void close(String ip, Socket sock) {
        try {
            if (sock.getOutputStream() != null) {
                sock.getOutputStream().flush();
            }
            sock.shutdownOutput();
            sock.close();
            map.remove(ip);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Message msg = new Message();
            msg.what = CloseFinish;
            Bundle bundle = new Bundle();
            bundle.putString("ip", ip);
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
    }

    // 网络打印机 关闭
    public void closeAll() {
        for (String sock : map.keySet()) {
            close(sock);
        }
    }

    public void print(byte[] data, String ip) {
        Socket sock = map.get(ip);
        if (sock != null) {
            print(sock, data);
        } else {
            open(ip);
            print(map.get(ip), data);
        }
    }

    private void print(Socket sock, byte[] data) {
        boolean printSuccess = false;
        try {
            if (sock != null && sock.isConnected() && !sock.isOutputShutdown()) {
                OutputStream outputStream = sock.getOutputStream();
                outputStream.write(data);
                outputStream.flush();
                printSuccess = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Message msg = new Message();
            msg.what = PrintFinish;
            Bundle bundle = new Bundle();
            bundle.putBoolean("print", printSuccess);
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
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
                    break;
                case PrintFinish:
                    if (serviceListener != null) {
                        serviceListener.printFinish(bundle.getBoolean("print"));
                    }
                    break;
                case CloseFinish:
                    if (serviceListener != null) {
                        serviceListener.closeFinish(bundle.getString("ip"));
                    }
                    break;
            }
        }

        public void setServiceListener(NetPortPrintListener serviceListener) {
            this.serviceListener = serviceListener;
        }
    }

    public boolean isConnect(String ip) {
        if (TextUtils.isEmpty(ip))
            return false;
        Socket sock = map.get(ip);
        if (sock == null)
            return false;
        OutputStream outputStream = null;
        try {
            outputStream = sock.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        if (!sock.isConnected() || sock.isOutputShutdown() || sock.isClosed() || outputStream == null) {
            return false;
        }
        try {
            sock.sendUrgentData(0xFF);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

}
