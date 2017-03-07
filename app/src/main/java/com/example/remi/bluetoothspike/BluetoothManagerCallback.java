package com.example.remi.bluetoothspike;

import android.bluetooth.BluetoothDevice;

import java.util.Set;

/**
 * Created by remi on 07/03/2017.
 */

public abstract class BluetoothManagerCallback {

    public void scanResult(Set<BluetoothDevice> devices) {}
}
