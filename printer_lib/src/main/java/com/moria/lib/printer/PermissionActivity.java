package com.moria.lib.printer;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

/**
 * Author  moria
 * Date    2020/9/10
 * Time    16:18
 */
public class PermissionActivity  extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 到这说明usb设备已经授权, 是否勾选永久授权无法判断
        Toast.makeText(this, "USB设备已授权", Toast.LENGTH_SHORT).show();
        finish();
    }
}