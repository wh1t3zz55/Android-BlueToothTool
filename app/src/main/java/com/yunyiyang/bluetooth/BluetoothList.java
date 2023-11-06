package com.yunyiyang.bluetooth;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
public class BluetoothList extends Activity {
    private static final String TAG = "BluetoothList";
    private BluetoothAdapter btAdapter;//蓝牙是入口
    private ListAdaptor listAdaptor;
    private ListView listView;
    private Timer scanDelayTimer;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_list);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        listAdaptor = new ListAdaptor(this, btAdapter.getBondedDevices());
        listView = findViewById(R.id.listView);
        listView.setAdapter(listAdaptor);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice d = (BluetoothDevice)adapterView.getAdapter().getItem(i);
                Intent intent = new Intent();
                intent.putExtra("address", d.getAddress());
                Log.d(TAG, "onItemClick: address="+d.getAddress());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        scanDelayTimer = new Timer();
        scanDelayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                //延迟两秒之后再开始搜索
                if (!btAdapter.isDiscovering()) {
                    //搜索蓝牙设备
                    boolean b = btAdapter.startDiscovery();
                    Log.d(TAG, "Timer: startDiscovery b="+b);
                }
            }
        },2000);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            Log.d(TAG, "onCreate: reuqest permission");
        } else {

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 注册Receiver来获取蓝牙设备相关的结果
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND); // 用BroadcastReceiver来取得搜索结果
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(searchDevices, intent);

        Log.d(TAG, "onStart: ");
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(searchDevices);
        scanDelayTimer.cancel();
        if (btAdapter.isDiscovering()) {
            //搜索蓝牙设备
            btAdapter.cancelDiscovery();
        }
        Log.d(TAG, "onStop: ");
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
            if (!btAdapter.isDiscovering()) {
                //搜索蓝牙设备
                btAdapter.startDiscovery();
            }
        }
    }

    private BroadcastReceiver searchDevices = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: is called");
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) { //found device
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                listAdaptor.addDevice(device);
                @SuppressLint("MissingPermission") String str = device.getName() + "|" + device.getAddress();
                Log.d(TAG, "onReceive: "+str);

            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                Toast.makeText(getBaseContext(), "正在扫描", Toast.LENGTH_SHORT).show();
            } else if (action
                    .equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                Toast.makeText(getBaseContext(), "扫描完成", Toast.LENGTH_SHORT).show();
            }
        }
    };

    class ListAdaptor extends BaseAdapter {
        Context context;
        List<BluetoothDevice> deviceList;
        public ListAdaptor(Context context, Set<BluetoothDevice> devices){
            this.context = context;
            this.deviceList = new ArrayList<>();
            for(BluetoothDevice d:devices){
                this.deviceList.add(d);
            }
        }

        public void addDevice(BluetoothDevice device) {
            if (!deviceList.contains(device)) {
                deviceList.add(device);
                notifyDataSetChanged();
            }
        }
        @Override
        public int getCount() {
            return deviceList.size();
        }

        @Override
        public Object getItem(int i) {
            return deviceList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v = LayoutInflater.from(context).inflate(R.layout.list_item, null);
            if(deviceList==null)return null;
            BluetoothDevice d = deviceList.get(i);
            if(d==null)return null;
            TextView name = v.findViewById(R.id.tvBTName);
            TextView address = v.findViewById(R.id.tvBTAddress);
            name.setText(d.getName());
            address.setText(d.getAddress());
            return v;
        }
    }
}
