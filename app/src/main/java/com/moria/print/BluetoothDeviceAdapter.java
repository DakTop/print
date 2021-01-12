package com.moria.print;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moria.lib.printer.bean.DeviceModel;
import com.moria.lib.printer.bluetooth.BluetoothPrinterManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Author  moria
 * Date    2020/9/11
 * Time    15:00
 */
public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {
    private List<DeviceModel> list = new ArrayList<>();
    private Context context;
    private OnItemClickListener onItemClickListener;

    public BluetoothDeviceAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.device_item, null));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.refresh(list.get(position));
        holder.getName().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClickListener(list.get(position), position);
            }
        });
    }

    public void refreshData(List<DeviceModel> mList) {
        list.clear();
        if (mList != null && mList.size() > 0) {
            list.addAll(mList);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.device_name);
        }

        public void refresh(DeviceModel deviceModel) {
            if (deviceModel == null)
                return;
            name.setText(deviceModel.getDeviceName() + "：" + (BluetoothPrinterManager.getInstance().isConnect(deviceModel) ? "已连接" : "未连接"));
        }

        public TextView getName() {
            return name;
        }
    }

    public interface OnItemClickListener {
        void onItemClickListener(DeviceModel deviceModel, int p);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
