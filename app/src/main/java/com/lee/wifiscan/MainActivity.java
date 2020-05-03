package com.lee.wifiscan;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION = 1000;
    private final int UPDATE_UI_REQUEST_CODE = 1024;
    private TextView mCurrConnTV;   // 显示当前所连WiFi信息的控件
    private TextView mScanResultTV;    // 显示WiFi扫描结果的控件
    private StringBuffer mCurrConnStr;  // 暂存当前所连WiFi信息的字符串
    private StringBuffer mScanResultStr;    // 暂存WiFi扫描结果的字符串
    private WifiManager mWifiManager;   // 调用WiFi各种API的对象
    private Timer mTimer;   // 启动定时任务的对象
    private final int SAMPLE_RATE = 2000; // 采样周期，以毫秒为单位
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == UPDATE_UI_REQUEST_CODE) {
                updateUI();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCurrConnTV = findViewById(R.id.connected_wifi_info_tv);
        mScanResultTV = findViewById(R.id.scan_results_info_tv);

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        getLocationAccessPermission();  // 先获取位置权限

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                scanWifi();
                mHandler.sendEmptyMessage(UPDATE_UI_REQUEST_CODE);
            }
        }, 0, SAMPLE_RATE); // 立即执行任务，每隔2000ms执行一次WiFi扫描的任务
        // 扫描周期不能太快，WiFi扫描所有的AP需要一定时间
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTimer.cancel();    // 取消定时任务
    }

    /**
     * 增加开启位置权限功能，以适应Android 6.0及以上的版本
     */
    private void getLocationAccessPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION);
        }
    }

    public void scanWifi() {
        // 如果WiFi未打开，先打开WiFi
        if (!mWifiManager.isWifiEnabled())
            mWifiManager.setWifiEnabled(true);

        // 开始扫描WiFi
        mWifiManager.startScan();
        // 获取并保存当前所连WiFi信息
        mCurrConnStr = new StringBuffer();
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        mCurrConnStr.append("SSID: ").append(wifiInfo.getSSID()).append("\n");
        mCurrConnStr.append("MAC Address: ").append(wifiInfo.getBSSID()).append("\n");
        mCurrConnStr.append("Signal Strength(dBm): ").append(wifiInfo.getRssi()).append("\n");
        mCurrConnStr.append("speed: ").append(wifiInfo.getLinkSpeed()).append(" ").append(WifiInfo.LINK_SPEED_UNITS);

        // 获取并保存WiFi扫描结果
        mScanResultStr = new StringBuffer();
        List<ScanResult> scanResults = mWifiManager.getScanResults();
        for (ScanResult sr : scanResults) {
            mScanResultStr.append("SSID: ").append(sr.SSID).append("\n");
            mScanResultStr.append("MAC Address: ").append(sr.BSSID).append("\n");
            mScanResultStr.append("Signal Strength(dBm): ").append(sr.level).append("\n\n");
        }
    }

    private void updateUI() {
        mCurrConnTV.setText(mCurrConnStr);
        mScanResultTV.setText(mScanResultStr);
    }
}