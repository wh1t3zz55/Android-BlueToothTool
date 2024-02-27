package com.yunyiyang.bluetooth;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Update {
    private static JSONObject jSONObject = null;
    private static boolean hasUpdate = true;
    private static boolean NoIgnorable;
    private static int versionCode = 0;
    private static String versionName, updateLog, apkSize, apkUrl, webUrl;
    private static String[] upl;

    private Context context;  // 新增的 Context 成员变量
    private Activity activity;

    // 构造方法接收一个 Context 对象
    public Update(Context context, String[] updateUrls) {
        this.context = context;
        this.activity = activity;
        upl = updateUrls;
    }

    /**
     * 获取当前使用的软件包的版本号
     */
    public int getVersionCode() {
        try {
            //获取packagemanager的实例
            PackageManager packageManager = context.getPackageManager();
            //getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            Log.e("TAG", "版本号" + packInfo.versionCode);  //更新软件用的是版本号
            return packInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }


    /**
     * 提示版本更新的对话框
     */
    public void showDialogUpdate() {
        // hasUpdate为true且程序版本号<服务端版本号，提示用户更新
        if (NoIgnorable) { // NoIgnorable为true 就是强制更新，无“取消”按钮
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(false); // 开启强制更新，无法触摸外部关闭
            builder.setTitle("是否升级到" + versionName + "版本?").
                    // 设置提示框的图标
                    // setIcon(R.drawable.ic_launcher).
                    // 设置要显示的信息
                            setMessage("新版本大小：" + apkSize + "\n" + updateLog).
                    // 设置确定按钮
                            setPositiveButton("更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            loadNewVersionProgress(); // 程序内直接下载最新的版本程序
                        }
                    }).
                    setNeutralButton("浏览器下载", new DialogInterface.OnClickListener() {//中性按钮 应用内下载失败可用它更新
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Uri uri = Uri.parse(webUrl);
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            context.startActivity(intent);
                            // 判断 context 是否是 Activity 的实例，以避免非 Activity 类调用 finish() 方法
                            if (context instanceof Activity) {
                                ((Activity) context).finish(); // 强制更新，点击后销毁应用
                            }
                        }
                    });
            // 显示对话框
            builder.create().show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            // builder.setCancelable(false); // 非强制更新，屏蔽此行，触摸外部或退出键可关闭
            builder.setTitle("是否升级到" + versionName + "版本?").
                    setMessage("新版本大小：" + apkSize + "\n" + updateLog).
                    setPositiveButton("更新", new DialogInterface.OnClickListener() {//正按钮
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            loadNewVersionProgress(); // 下载最新的版本程序
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {//负按钮
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 不做任何处理
                        }
                    }).setNeutralButton("浏览器下载", new DialogInterface.OnClickListener() {//中性按钮
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Uri uri = Uri.parse(webUrl);
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            context.startActivity(intent);
                            // 判断 context 是否是 Activity 的实例，以避免非 Activity 类调用 finish() 方法
                            if (context instanceof Activity) {
                                // 屏蔽销毁，访问浏览器，程序不会退出
                                // ((Activity) context).finish();
                            }
                        }
                    });
            // 显示对话框
            builder.create().show();
        }
    }


    //轮询验证两个更新链接，返回有效链接
    public static String checkUrl(String[] ltl) {
        String resultUrl = null;
        for (String url : ltl) {
            resultUrl = url;
            try {
                //调用检查链接是否有效的方法
                String result = get(url);
                if (result != null && result.length() != 0) {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resultUrl;
    }

    //检查更新链接是否有效的方法
    public static String get(String url) {
        URL infoUrl = null;
        InputStream inStream = null;
        String line = "";
        try {
            infoUrl = new URL(url);
            URLConnection connection = infoUrl.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inStream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf-8"));
                StringBuilder strber = new StringBuilder();
                while ((line = reader.readLine()) != null)
                    strber.append(line + "\n");
                inStream.close();
                int start = strber.indexOf("{");
                int end = strber.indexOf("}");
                String json = strber.substring(start, end + 1);
                return json;
            }
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
        return "";
    }

    /**
     * 使用检查过的有效链接，获取服务端json数据
     */
    public static JSONObject GetServerJson() {
        URL infoUrl = null;
        InputStream inStream = null;
        String line = "";
        try {
            String uurl = checkUrl(upl);
            infoUrl = new URL(uurl);
            URLConnection connection = infoUrl.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inStream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf-8"));
                StringBuilder strber = new StringBuilder();
                while ((line = reader.readLine()) != null)
                    strber.append(line + "\n");
                inStream.close();
                int start = strber.indexOf("{");
                int end = strber.indexOf("}");
                String json = strber.substring(start, end + 1);
                if (json != null && !json.isEmpty()) {
                    try {
                        jSONObject = new JSONObject(json);
                        hasUpdate = jSONObject.getBoolean("BlueToothHasUpdate");
                        NoIgnorable = jSONObject.getBoolean("BlueToothNoIgnorable");
                        versionCode = jSONObject.getInt("BlueToothVersionCode");
                        versionName = jSONObject.getString("BlueToothVersionName");
                        updateLog = jSONObject.getString("BlueToothUpdateLog");
                        apkSize = jSONObject.getString("BlueToothApkSize");
                        apkUrl = jSONObject.getString("BlueToothApkUrl");
                        webUrl = jSONObject.getString("BlueToothWebUrl");
                        return jSONObject;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Error parsing JSON", e);
                    }
                } else {
                    Log.e(TAG, "Empty or null JSON response");
                }
            } else {
                Log.e(TAG, "HTTP response code: " + responseCode);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG, "Malformed URL", e);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IO Exception", e);
        }
        return null;
    }



    /**
     * 应用内直链升级方法，下载新版本程序
     */
    private void loadNewVersionProgress() {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ProgressDialog pd = new ProgressDialog(context);
                pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pd.setMessage("下载最新版本安装包到：" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + apkUrl.substring(apkUrl.lastIndexOf("/") + 1));
                pd.setCancelable(false);
                pd.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            File file = getFileFromServer(apkUrl, pd);
                            installApk(file);
                            pd.dismiss();  // 关闭进度条
                        } catch (Exception e) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showDownloadErrorDialog(pd);
                                }
                            });
                        }
                    }
                }).start();
            }
        });
    }


    private void showDownloadErrorDialog(final ProgressDialog pd) {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(false);
                builder.setTitle("下载失败：").setMessage("1.请检查存储权限是否开启。\n2.请检查网络连接是否正常。\n3.使用浏览器下载新版本。");
                builder.setPositiveButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // 不关闭当前Activity，可以根据需求进行其他操作
                        pd.dismiss();  // 关闭进度条
                    }
                }).setNeutralButton("浏览器下载", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Uri.parse(webUrl);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        context.startActivity(intent);
                        if (NoIgnorable) {
                            ((Activity) context).finish();
                        } else {
                            // Dismiss the progress dialog if not a forced update
                            pd.dismiss();  // 关闭进度条
                        }
                    }
                });
                builder.create().show();
            }
        });
    }





    /**
     * 安装apk
     */
    private void installApk(File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri;

        // 判断是否是Android 7.0及以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", apkFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(apkFile);
        }

        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }




    /**
     * 从服务器获取apk文件的代码
     * 传入网址uri，进度条对象即可获得一个File文件
     * （要在子线程中执行哦）
     */
    public static File getFileFromServer(String uri, ProgressDialog pd) throws Exception {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            //获取到文件的大小
            pd.setMax(conn.getContentLength());  //字节的方式显示下载进度
            InputStream is = conn.getInputStream();
            //获取直链链接最后一个“/”后文字作为文件名，下载存储到手机
            File file = new File(Environment.getExternalStorageDirectory(), apkUrl.substring(apkUrl.lastIndexOf("/", apkUrl.lastIndexOf("")) + 1));
            FileOutputStream fos = new FileOutputStream(file);
            BufferedInputStream bis = new BufferedInputStream(is);
            byte[] buffer = new byte[1024];
            int len;
            int total = 0;
            while ((len = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                total += len;
                //获取当前下载量
                pd.setProgress(total);//字节方式显示下载量
            }
            fos.close();
            bis.close();
            is.close();
            return file;
        } else {
            return null;
        }
    }







}
