package com.yunyiyang.bluetooth;


import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class MainActivity2 extends AppCompatActivity {
    private BluetoothManager btManager;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //蓝牙设置
        btManager = BluetoothManager.getInstance();
        if (btManager.isConnected()) {
            setTitle("蓝牙连接到：" + btManager.getDeviceName());
        } else {
            setTitle("蓝牙未连接");
        }

        /*findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btManager.send("1");
            }
        });*/
        /*findViewById(R.id.buttonhou).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btManager.send("2");
            }
        });
        findViewById(R.id.buttonyou).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btManager.send("4");
            }
        });
        findViewById(R.id.buttonzuo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btManager.send("3");
            }
        });
        findViewById(R.id.buttonting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btManager.send("0");
            }
        });
        findViewById(R.id.buttonzuoyi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btManager.send("5");
            }
        });
        findViewById(R.id.buttonyouyi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btManager.send("6");
            }
        });*/

        Button btn = findViewById(R.id.button2);
        Button btnhou = findViewById(R.id.buttonhou);
        Button btnyou = findViewById(R.id.buttonyou);
        Button btnzuo = findViewById(R.id.buttonzuo);
        Button btnting = findViewById(R.id.buttonting);
        Button btnzuoyi = findViewById(R.id.buttonzuoyi);
        Button btnyouyi = findViewById(R.id.buttonyouyi);

        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    btManager.send("1");
                    btn.setBackgroundColor(Color.RED);
                }else if(event.getAction() == MotionEvent.ACTION_UP){

                    btManager.send("0");
                    btn.setBackgroundColor(Color.BLUE);
                }
                return false;
            }
        });

        btnyou.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){

                    btManager.send("4");
                    btnyou.setBackgroundColor(Color.RED);
                }else if(event.getAction() == MotionEvent.ACTION_UP){

                    btManager.send("0");
                    btnyou.setBackgroundColor(Color.BLUE);
                }
                return false;
            }
        });
        btnzuo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){

                    btManager.send("3");
                    btnzuo.setBackgroundColor(Color.RED);
                }else if(event.getAction() == MotionEvent.ACTION_UP){

                    btManager.send("0");
                    btnzuo.setBackgroundColor(Color.BLUE);
                }
                return false;
            }
        });
        btnhou.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){

                    btManager.send("2");
                    btnhou.setBackgroundColor(Color.RED);
                }else if(event.getAction() == MotionEvent.ACTION_UP){

                    btManager.send("0");
                    btnhou.setBackgroundColor(Color.BLUE);
                }
                return false;
            }
        });
        btnting.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){

                    btManager.send("0");
                    btnting.setBackgroundColor(Color.RED);
                }else if(event.getAction() == MotionEvent.ACTION_UP){

                    btManager.send("0");
                    btnting.setBackgroundColor(Color.BLUE);
                }
                return false;
            }
        });
        btnzuoyi.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){

                    btManager.send("5");
                    btnzuoyi.setBackgroundColor(Color.RED);
                }else if(event.getAction() == MotionEvent.ACTION_UP){

                    btManager.send("0");
                    btnzuoyi.setBackgroundColor(Color.BLUE);
                }
                return false;
            }
        });
        btnyouyi.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){

                    btManager.send("6");
                    btnyouyi.setBackgroundColor(Color.RED);
                }else if(event.getAction() == MotionEvent.ACTION_UP){

                    btManager.send("0");
                    btnyouyi.setBackgroundColor(Color.BLUE);
                }
                return false;
            }
        });





    }


    /*private View.OnTouchListener buttonListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) { // 按下
                btManager.send("1");
            } else if (action == MotionEvent.ACTION_UP) { // 松开
                btManager.send("0");
            }
            return false;

        }
    };*/

















}