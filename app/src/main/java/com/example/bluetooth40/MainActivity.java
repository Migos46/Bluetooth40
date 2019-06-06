package com.example.bluetooth40;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    /**
     * TODO: Global Variables
     * Global variables/Constants
     */
    Button btnConnect;
    Button btnTakePic;
    TextView showDataFromBlueT;
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    public static final String TAG = "Exception";
    static final int REQUEST_ENABLE_BT = 1;
    static BluetoothAdapter bluetoothAdapter;
    public BluetoothSocket mGlobalSocket;
    static Handler handler;
    byte[] mArrayTest;
    int[] mIntArrayTest;
    int bitWise = 0xFF;
    public byte click = 0;

    String MAC_Address = "";
    public ArrayList<String> deviceNameArray = new ArrayList<String>();
    public static final String MESSAGE = "Devices Paired";
    public static final String MESSAGE_NODEVICES = "No Devices Paired";
    boolean notDevices;
    String noDevices = "No devices found.\nIf you know that it should be devices paired go back to the main screen and turn on bluetooth.\nOtherwise, pair a new device.";

    public interface MessageConstants {
        int MESSAGE_READ = 0;
        int MESSAGE_WRITE = 1;
        int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }

    /**
     * End of global variables/Constants
     */
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO: Message receive
        handler = new Handler(){
            public void handleMessage (Message mArray){
                mArrayTest = new byte[mArray.arg1];
                if (mArray.what == MessageConstants.MESSAGE_READ){
                    mArrayTest = (byte[]) mArray.obj;
                    btnTakePic.setEnabled(true);
                }
            }
        };


        //OnClickListener
        //You can also declare the click event handler programmatically rather than in an XML layout.
        Button btn = findViewById(R.id.btnONOFF);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableDisableBT();
            }
        });

        //OnClickListener
        //You can also declare the click event handler programmatically rather than in an XML layout.
        Button btnQ = findViewById(R.id.btnQuery);
        btnQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter.isEnabled()) {
                    deviceNameArray.clear();
                    queringBTdevices();
                    launchPairedDevicesActivity(deviceNameArray, noDevices);
                } else {
                    Context context = getApplicationContext();
                    CharSequence text = "Please Enable the bluetooth by clicking the (ON / OFF) button";
                    Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

        //OnClickListener
        //You can also declare the click event handler programmatically rather than in an XML layout.
        btnConnect = findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MAC_Address == null) {
                    Context context = getApplicationContext();
                    CharSequence text = "Please select a paired BT device from the list in Query Devices Paired";
                    Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    try {
                        connectToDevice();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //OnClickListener
        showDataFromBlueT = findViewById(R.id.showData);
        btnTakePic = findViewById(R.id.btnTakePic);
        btnTakePic.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              byteToInt();
                                              showDataFromBlueT.setText(String.valueOf(mArrayTest));
                                          }
                                      });

        //TODO: Get the intent for Paired Devices and show it on screen
        Intent intentData = getIntent();
        String deviceData = intentData.getStringExtra(PairedDevices.DATA);
        MAC_Address = deviceData;
        //TODO: Log.i statement
        String TAG = "From PairedDevices:  ";
        Log.i(TAG, "Data: " + MAC_Address);
    }
    /**
     * TODO: Launch Paired devices Activity
     */
    public void byteToInt(){
        btnTakePic.setEnabled(false);
        mIntArrayTest = new int[mArrayTest.length];
        for(int i = 0; i < mArrayTest.length; i++) {
            mIntArrayTest[i] = (mArrayTest[i] & bitWise);
        }
        for(int j=9800; j<mArrayTest.length;j++){
            //TODO: Log.i statement
            String TAG = "mArrayTest:  ";
            Log.i(TAG, "Position: "+ j + "= " + mIntArrayTest[j]);
        }
    }
    /**
     * TODO: Connect to the HC-05 BT device.
     */
    public void connectToDevice() throws IOException {
        if(click == 0){
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(MAC_Address);
            ConnectThread connectThread = new ConnectThread(device);
            connectThread.start();
            click++;
            //TODO: Log.i statement
            String TAG = "Connect to HC-05";
            Log.i(TAG, "ON: " + click);
            btnConnect.setText("Turn ON InputStream");
        }
        else{
            ConnectedThread connectedThread = new ConnectedThread(mGlobalSocket);
            connectedThread.start();
            click = 0;
            //TODO: Log.i statement
            String TAG = "inputStream";
            Log.i(TAG, "ON: " + click);
        }
    }
    /**
     * TODO: Launch Paired devices Activity
     */
    public void launchPairedDevicesActivity(ArrayList NameArray, String noDevices) {
        if (!notDevices) {
            Intent intent = new Intent(this, PairedDevices.class);
            intent.putStringArrayListExtra(MESSAGE, NameArray);
            startActivity(intent);
        } else {
            Intent noDevicesIntent = new Intent(this, PairedDevices.class);
            noDevicesIntent.putExtra(MESSAGE_NODEVICES, noDevices);
            startActivity(noDevicesIntent);
        }
    }

    /**
     * TODO: Enable + disable BT adapter
     * Get the BluetoothAdapter. This returns a BluetoothAdapter that represents the device's own Bluetooth adapter (the Bluetooth radio).
     * There's one Bluetooth adapter for the entire system, and your application can interact with it using this object.
     */
    public void enableDisableBT() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
        }
    }

    /**
     * TODO: Quering and showing already paired devices.
     * Quering BT devices
     */
    public void queringBTdevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            notDevices = false;
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                deviceNameArray.add("Name: " + device.getName());
                deviceNameArray.add(device.getAddress());
            }
        } else //deviceNameArray.add("No devices Found.");
            notDevices = true;
    }

    //TODO: Connect Thread
    public class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
            mGlobalSocket = tmp;
        }

        @Override
        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                Log.e(TAG, "Could not connect the client socket", connectException);
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            //manageMyConnectedSocket(mmSocket);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }


    //TODO: Connected Thread
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream
        private byte[] mmTempBuffer; // Buffer to make the final array before sending.
        private short lastArrayIndex;

        public ConnectedThread(BluetoothSocket socket) throws IOException {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
            mmTempBuffer = new byte[9840];
            mmBuffer = new byte[9840];
            int numBytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.

                    numBytes = mmInStream.read(mmTempBuffer, 0, mmTempBuffer.length);
                    // Send the obtained bytes to the UI activity.

                    for(int i = lastArrayIndex; i <= (lastArrayIndex+numBytes-1); i++){
                        mmBuffer[i] = mmTempBuffer[i-lastArrayIndex];
                    }
                    lastArrayIndex += numBytes;
                    Message readMsg = handler.obtainMessage(
                            MessageConstants.MESSAGE_READ, lastArrayIndex, -1,
                            mmBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }
        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}