package com.example.remi.bluetoothspike;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by remi on 07/03/2017.
 */

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private List<BluetoothDevice> mDevices = new ArrayList<>();
    private DeviceViewHolderClick mDeviceViewHolderClick;

    public static interface DeviceViewHolderClick {
        void onDevice(BluetoothDevice device);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextViewName;
        public TextView mTextViewIdentifier;

        public ViewHolder(View v) {
            super(v);
            mTextViewName = (TextView) v.findViewById(R.id.text_view_name);
            mTextViewIdentifier = (TextView) v.findViewById(R.id.text_view_identifier);
        }

        public void BindView(final BluetoothDevice device, final DeviceViewHolderClick deviceViewHolderClick) {
            String name = device.getName();
            mTextViewName.setText(name);
            mTextViewIdentifier.setText(device.getAddress());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deviceViewHolderClick.onDevice(device);
                }
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_recycle_view, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BluetoothDevice device = mDevices.get(position);
        holder.BindView(device, mDeviceViewHolderClick);
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public void setmDevices(List<BluetoothDevice> mDevices) {
        this.mDevices = mDevices;
        notifyDataSetChanged();
    }

    public DeviceAdapter(DeviceViewHolderClick mDeviceViewHolderClick) {
        this.mDeviceViewHolderClick = mDeviceViewHolderClick;
    }
}
