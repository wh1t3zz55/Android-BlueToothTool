package com.yunyiyang.bluetooth;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private BluetoothManager btManager;

    private EditText etMsg;
    private TextView tvRecord;

    private ScrollView scrollView;
    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private boolean bluetoothPermissionDenied = false;//记录蓝牙授权有没有被用户拒绝
    private boolean locationPermissionDenied = false;//记录位置授权有没有被用户拒绝
    private AlertDialog dialog;
    private boolean havePermission = false;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 检查蓝牙和位置权限
        checkBluetoothPermissions();
        checkLocationPermissions();
        checkPermission();

        //关联
        etMsg = findViewById(R.id.editTextTextPersonName);
        tvRecord = findViewById(R.id.textView);
        scrollView=findViewById(R.id.scrollView2);
        Button timeButton = findViewById(R.id.button4);

        if (btManager != null) {
            if (btManager.isConnected()) {
                setTitle("蓝牙连接到：" + btManager.getDeviceName());
            } else {
                setTitle("蓝牙未连接");
            }
            btManager.setOnBluetoothListener(new BluetoothManager.onBluetoothListener() {
                @Override
                public void onReceived(String data) {
                    tvRecord.append(data);
                    int scrollAmount = tvRecord.getLayout().getLineTop(tvRecord.getLineCount())
                            - scrollView.getHeight();
                    if (scrollAmount > 0) {
                        scrollView.scrollTo(0, scrollAmount);
                    }else {
                        scrollView.scrollTo(0, 0);
                    }
                    //Toast.makeText(getBaseContext(),"收到蓝牙消息！",Toast.LENGTH_LONG).show();
                }
            });
        }

        //发送数据的按钮事件
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //发送数据
                btManager.send(etMsg.getText().toString());
            }
        });
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,MainActivity2.class));
            }
        });
        //授时按钮
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 获取当前系统时间
                Date currentTime = new Date();
                // 定义日期时间格式
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                // 格式化日期时间并加上大写'R'
                String formattedTime = dateFormat.format(currentTime) + "R";
                // 打印或使用格式化后的时间
                Log.d("Formatted Time", formattedTime);
                //发送数据到蓝牙串口
                btManager.send(formattedTime);
            }
        });
    }
    private void checkBluetoothPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // 检查蓝牙权限是否已授予
            if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                // 请求蓝牙权限
                requestPermissions(new String[]{
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH_CONNECT
                }, BLUETOOTH_PERMISSION_REQUEST_CODE);
            } else {
                // 蓝牙权限已授予，继续进行蓝牙操作
                initializeBluetooth();
            }
        } else {
            // 在低于Android 6.0的设备上不需要运行时权限
            initializeBluetooth();
        }
    }

    private void checkLocationPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // 检查位置权限是否已授予
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                // 请求位置权限
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                // 位置权限已授予，可以执行与位置相关的操作
                Toast.makeText(this, "位置权限已授权", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 在低于Android 6.0的设备上不需要运行时权限
            Toast.makeText(this, "安卓6.0以下不需要权限", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 蓝牙权限已授予，继续进行蓝牙操作
                initializeBluetooth();
            } else {
                // 蓝牙权限被拒绝，记录状态
                bluetoothPermissionDenied = true;
                Toast.makeText(this, "蓝牙权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 位置权限已授予，可以执行与位置相关的操作
            } else {
                // 位置权限被拒绝，记录状态
                locationPermissionDenied = true;
                // 位置权限被拒绝，根据需要进行处理
                Toast.makeText(this, "位置权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeBluetooth(){
        //蓝牙设置
        btManager = BluetoothManager.getInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu,menu);//创建菜单
        return super.onCreateOptionsMenu(menu);
    }

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //在这里接收，所选择的蓝牙设备
                    if (result.getResultCode() == RESULT_OK) {
                        String address = result.getData().getStringExtra("address");
                        Log.d(TAG, "onActivityResult: address="+address);
                        boolean b = btManager.connect(address);
                        if (b) {
                            Toast.makeText(getBaseContext(), "蓝牙连接成功！", Toast.LENGTH_SHORT).show();
                            setTitle("蓝牙连接到：" + btManager.getDeviceName());
                        } else {
                            Toast.makeText(getBaseContext(), "蓝牙连接失败！", Toast.LENGTH_SHORT).show();
                            setTitle("蓝牙未连接");
                        }
                    }
                }
            }
    );

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.item_conn) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                // 请求蓝牙权限
                requestPermissions(new String[]{
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH_CONNECT
                }, BLUETOOTH_PERMISSION_REQUEST_CODE);
            }else {
                launcher.launch(new Intent(MainActivity.this, BluetoothList.class));
            }
        } else if (itemId == R.id.item_disconn) {
            btManager.disconnect();
            Toast.makeText(getBaseContext(), "蓝牙已断开", Toast.LENGTH_SHORT).show();
            setTitle("蓝牙未连接");
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onResume() {
        super.onResume();

        // 在 onResume 中检查是否有权限被拒绝，如果有，重新请求权限
        if (bluetoothPermissionDenied || locationPermissionDenied) {
            checkBluetoothPermissions();
            checkLocationPermissions();
            bluetoothPermissionDenied = false;
            locationPermissionDenied = false;
        }
    }

    //动态权限申请
    private void checkPermission() {
        //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
        if (Build.VERSION.SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
                if (dialog != null) {
                    dialog.dismiss();
                    dialog = null;
                }
                dialog = new AlertDialog.Builder(this)
                        .setTitle("提示")//设置标题
                        .setMessage("版本升级需要下载安装包，请打开文件权限！")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                startActivity(intent);
                            }
                        }).create();
                dialog.show();
            } else {
                havePermission = true;
                Log.i("PermissionLog", "Android 11以上，当前已有权限");
                // 使用一个包含服务器 URL 的字符串数组
                String[] updateUrls = {"http://118.31.39.96:2777/"};
                // 异步执行网络请求
                new ServerJsonTask().execute(updateUrls);
            }
        } else {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    if (dialog != null) {
                        dialog.dismiss();
                        dialog = null;
                    }
                    dialog = new AlertDialog.Builder(this)
                            .setTitle("提示")//设置标题
                            .setMessage("版本升级需要下载安装包，请打开文件权限！")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                                }
                            }).create();
                    dialog.show();
                } else {
                    havePermission = true;
                    Log.i("PermissionLog", "Android 6.0以上，11以下，当前已有权限");
                    // 使用一个包含服务器 URL 的字符串数组
                    String[] updateUrls = {"http://118.31.39.96:2777/"};
                    // 异步执行网络请求
                    new ServerJsonTask().execute(updateUrls);
                }
            } else {
                havePermission = true;
                Log.i("PermissionLog", "Android 6.0以下，已获取权限");
                // 使用一个包含服务器 URL 的字符串数组
                String[] updateUrls = {"http://118.31.39.96:2777/"};
                // 异步执行网络请求
                new ServerJsonTask().execute(updateUrls);
            }
        }
    }

    private class ServerJsonTask extends AsyncTask<String, Void, JSONObject> {
        private Update updater;  // 声明 updater 变量

        @Override
        protected JSONObject doInBackground(String... urls) {
            if (urls.length > 0) {
                String url = urls[0];
                updater = new Update(MainActivity.this, new String[]{url});
                return updater.GetServerJson();
            }
            return null;
        }

        @Override
        protected void onPostExecute(@Nullable JSONObject serverJson) {
            // 在这里处理服务器响应
            if (serverJson != null) {
                int serverVersionCode = serverJson.optInt("BlueToothVersionCode", 0);
                int currentVersionCode = updater.getVersionCode();
                if (serverVersionCode > currentVersionCode) {
                    // 需要更新，显示更新对话框

                    updater.showDialogUpdate();
                } else {
                    // 当前版本已是最新
                    Toast.makeText(MainActivity.this, "当前已是最新版本", Toast.LENGTH_SHORT).show();
                }
            } else {
                // 获取服务器版本信息失败
                Toast.makeText(MainActivity.this, "获取服务器版本信息失败", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
