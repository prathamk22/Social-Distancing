package com.example.socialdistancing;

import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{

    ListView listView;
    TextView textView;
    DatabaseReference reference;
    int RSSI=-200;
    String deviceName="";
    ArrayList<String> mDeviceList;
    ToneGenerator toneGen1;
    FusedLocationProviderClient mFusedLocationClient;
    BluetoothAdapter adapter;
    Handler handler;
    Runnable runnable;
    protected static final String TAG = "RangingActivity";
    private boolean beep = false;
    int time = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listView);
        textView = findViewById(R.id.textView);
        mDeviceList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        adapter = BluetoothAdapter.getDefaultAdapter();

        adapter.startDiscovery();

        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.startDiscovery();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                adapter.startDiscovery();
            }
        });

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (beep){
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 150);
                    time++;
                }else{
                    time = 0;
                    RSSI=-200;
                }

                if (time==20){
                    beep=false;
                }


                handler.postDelayed(this, 500);
            }
        };
        handler.postDelayed(runnable, 500);

    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
                textView.setText("Started");
            }

            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                adapter.startDiscovery();
            }

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                textView.setText("Got devices");
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);

                RSSI = Math.max(rssi,RSSI);

                textView.setText(RSSI +"\n" + deviceName);
                String name  = device.getName();
                if (RSSI<0 && RSSI>=-55){
                    deviceName = name;
                    Toast.makeText(context, "Beep ", Toast.LENGTH_SHORT).show();
                    beep = true;
                    if (deviceName==null)
                        beep=  false;
                }
                else
                    beep = false;
                mDeviceList.add(device.getName() + "\n" + device.getAddress() + "\n" + String.valueOf(rssi));
                Log.i("BT", device.getName() + "\n" + device.getAddress());
                listView.setAdapter(new ArrayAdapter<String>(context,
                        android.R.layout.simple_list_item_1, mDeviceList));
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

}
