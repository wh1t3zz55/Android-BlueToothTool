package com.yunyiyang.bluetooth;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;



import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private BluetoothManager btManager;

    private EditText etMsg;
    private TextView tvRecord;

    private ScrollView scrollView;
    private Button timeButton;

    //private final ScrollView svResult = (ScrollView) findViewById(R.id.scrollView2);

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //关联
        etMsg = findViewById(R.id.editTextTextPersonName);
        tvRecord = findViewById(R.id.textView);
        scrollView=findViewById(R.id.scrollView2);
        //蓝牙设置
        btManager = BluetoothManager.getInstance();
        timeButton = findViewById(R.id.button4);
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
            launcher.launch(new Intent(MainActivity.this, BluetoothList.class));
        } else if (itemId == R.id.item_disconn) {
            btManager.disconnect();
            Toast.makeText(getBaseContext(), "蓝牙已断开", Toast.LENGTH_SHORT).show();
            setTitle("蓝牙未连接");
        }
        return super.onOptionsItemSelected(item);
    }


}
