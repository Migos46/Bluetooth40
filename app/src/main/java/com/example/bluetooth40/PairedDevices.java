package com.example.bluetooth40;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class PairedDevices extends AppCompatActivity {

    public ArrayList<String> deviceNameArray = new ArrayList<String>();
    public static final String DATA = "Device DATA";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paired_devices);

        final ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, deviceNameArray);
        final ListView listView = (ListView) findViewById(R.id.arrayPairedDevices);
        listView.setClickable(true);
        listView.setAdapter(adapter);


        //TODO: Get the intent for Paired Devices and show it on screen
        if(getIntent().getStringArrayExtra(MainActivity.MESSAGE) == null){
            Intent intent = getIntent();
            ArrayList deviceNameArray = intent.getStringArrayListExtra(MainActivity.MESSAGE);
            adapter.addAll(deviceNameArray);
        }else {
            //TODO: Get the intent for no Devices paired
            Intent noDevicesIntent = getIntent();
            String noDevices = noDevicesIntent.getStringExtra(MainActivity.MESSAGE_NODEVICES);
            TextView textView = (TextView) (findViewById(R.id.noDevicesText));
            textView.setText(noDevices);
        }
        //TODO: Receive info from a item clicked in the list
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parentAdapter, View view,
                                    int position, long id) {
                adapter.getItem(position);
                sendID(listView.getItemAtPosition(position).toString());
            }
        });
    }

    //TODO: Intent to send the Data to the MainActivity to the main activity
    public void sendID(String deviceData){
        Intent  intentData = new Intent(this, MainActivity.class);
        intentData.putExtra(DATA, deviceData);
        startActivity(intentData);
    }
}