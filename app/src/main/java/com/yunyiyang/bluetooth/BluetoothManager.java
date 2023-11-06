package com.yunyiyang.bluetooth;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothManager {
    private static final String BT_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private static final String TAG = "BluetoothManager";
    private BluetoothAdapter mBluetoothAdapter;//蓝牙是入口
    private BluetoothDevice mBluetoothDevice;//将要连接的指定设备
    private BluetoothSocket mBluethoothSocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private String address;
    onBluetoothListener listener;
    //单例模式
    private static BluetoothManager instance;

    public BluetoothManager() {
        //获取蓝牙适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static BluetoothManager getInstance() {
        if (instance == null) {
            instance = new BluetoothManager();
        }
        return instance;
    }

    public void setOnBluetoothListener(onBluetoothListener listener) {
        this.listener = listener;
    }

    @SuppressLint("MissingPermission")
    public boolean connect(String address){
        if (address!=null&&!address.isEmpty()) mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);
        else mBluetoothDevice = null;

        if (mBluetoothDevice != null) {//如果发现了指定的设备
            try {
                if(mBluethoothSocket!=null){
                    mBluethoothSocket.close();
                }
                mBluethoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(BT_UUID));
                //开始交换数据
                mBluethoothSocket.connect();//连接
                //获取输出流
                outputStream = mBluethoothSocket.getOutputStream();
                inputStream = mBluethoothSocket.getInputStream();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true){
                            try {
                                byte[] data = new byte[1000];
                                int len = readAvailable(data);
                                String str;
                                str = new String(data, 0, len);
                                Log.d(TAG, "run: data="+str);
                                Message msg = new Message();
                                msg.obj = str;
                                msg.what = WHAT_RECV_MSG;
                                handler.sendMessage(msg);
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                this.address = address;
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    private static final int WHAT_RECV_MSG = 1;
    Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case WHAT_RECV_MSG:
                    String str = (String) msg.obj;
                    if(listener!=null)listener.onReceived(str);
                    break;
            }
        }
    };

    public String getAddress() {
        return address;
    }

    @SuppressLint("MissingPermission")
    public String getDeviceName() {
        if (mBluetoothDevice != null) {
            return mBluetoothDevice.getName();
        }
        return null;
    }

    public void disconnect() {
        if(mBluethoothSocket!=null) {
            try {
                mBluethoothSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean isConnected() {
        if (mBluethoothSocket != null) {
            return mBluethoothSocket.isConnected();
        }
        return false;
    }

    public void send(String str) {
        Log.d(TAG, "btSend: str="+str);
        if(str.isEmpty())return;
        try {
            if (outputStream != null) {
                outputStream.write(str.getBytes());//写入数据
                outputStream.flush();
                Log.d(TAG,"BT Send="+ str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private int readAvailable(byte[] bytes) throws IOException,InterruptedException{
        int i = 0,a;
        int ch = inputStream.read();
        if(ch < 0)return ch;
        bytes[i] = (byte) ch;
        i++;
        Thread.sleep(10);
        a = inputStream.available()+i;
        while (a > i) {
            for (; i < a; i++) {
                byte b = (byte) inputStream.read();
                bytes[i] = b;
            }
            Thread.sleep(10);
            a = inputStream.available() + i;
        }
//        Log.d(TAG, "readAvailable: size="+i);
        return i;
    }

    public interface onBluetoothListener{
        void onReceived(String data);
    }
}
