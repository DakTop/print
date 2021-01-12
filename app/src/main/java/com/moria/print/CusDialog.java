package com.moria.print;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

/**
 * Author  moria
 * Date    2020/10/15
 * Time    15:20
 */
public class CusDialog extends Dialog implements View.OnClickListener {
    private Context context;
    private OnItemClickListener onItemClickListener;

    public CusDialog(@NonNull Context context, OnItemClickListener onItemClickListener) {
        super(context);
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(context).inflate(R.layout.view_dialog, null);
        setContentView(view);
        view.findViewById(R.id.cancelBond).setOnClickListener(this);
        view.findViewById(R.id.connectDevice).setOnClickListener(this);
        view.findViewById(R.id.cancelConnectDevice).setOnClickListener(this);
        view.findViewById(R.id.print).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancelBond:
                onItemClickListener.onCancelBond();
                break;
            case R.id.connectDevice:
                onItemClickListener.onConnectDevice();
                break;
            case R.id.cancelConnectDevice:
                onItemClickListener.onCancelConnectDevice();
                break;
            case R.id.print:
                onItemClickListener.onPrint();
                break;
        }
        this.dismiss();
    }

    public interface OnItemClickListener {
        void onCancelBond();

        void onConnectDevice();

        void onCancelConnectDevice();

        void onPrint();
    }
}
