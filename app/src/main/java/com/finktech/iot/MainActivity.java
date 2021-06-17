package com.finktech.iot;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


public class MainActivity extends AppCompatActivity {
    private static volatile String status1="OFF";
    private static volatile String status2="OFF";
    private static volatile String status3="OFF";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String clientID="mqtt"+String.valueOf(Math.random()).substring(2,8);
        String brokerURL="tcp://broker.hivemq.com:1883";
        Log.d("DEBUGGING LOG","client ID: "+clientID);
        String[] topic={"stat/t1touchwahcantt/POWER","cmnd/t1touchwahcantt/POWER","stat/gasvalve/POWER","cmnd/gasvalve/POWER","stat/watervalve/POWER","cmnd/watervalve/POWER"};
        int qos=0;
        SwitchMaterial switchMaterial = findViewById(R.id.input1);
        SwitchMaterial switchMaterial1 = findViewById(R.id.input2);
        SwitchMaterial switchMaterial2 = findViewById(R.id.input3);
        Handler mainHandler= new Handler(this.getMainLooper());
            IMqttClient mqttClient = connect(brokerURL,clientID);
            if(mqttClient!=null)
            {
                Log.d("Debugging Log", "Mqtt Client Connected");
                switchMaterial.setOnTouchListener(new View.OnTouchListener(){
                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        switchMaterial.setClickable(false);
                        String stat=(switchMaterial.isChecked())?"OFF":"ON";
                        if(mqttClient!=null) {
                            new MainPub(mqttClient, 0, stat, topic[1], qos);
                            Thread t = new Thread(() -> {
                                Log.d("Debugging Log", "Subscribing to " + topic[0]);
                                mainHandler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        status1 = (mainSub(mqttClient, topic[0])=="ON")?"ON":"OFF";
                                        updateComponents();
                                    }
                                });
                            });
                            t.start();
                        }
                        return false;
                    }
                });
        /*switchMaterial.setOnCheckedChangeListener((buttonView, isChecked) -> {

        });*/
                switchMaterial1.setOnTouchListener(new View.OnTouchListener(){
                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        switchMaterial1.setClickable(false);
                        String stat=(switchMaterial1.isChecked())?"OFF":"ON";
                        if(mqttClient!=null) {
                            Log.d("Debugging Log","SENDING/PUBLISHSING: "+((stat.equals("OFF"))?"ON":"OFF"));
                            new MainPub(mqttClient, 0, stat, topic[3], qos);
                            Thread t = new Thread(() -> {
                                Log.d("Debugging Log", "Subscribing to " + topic[2]);
                                status2 = (mainSub(mqttClient, topic[2])=="ON")?"ON":"OFF";
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateComponents();
                                    }
                                });
                            });
                            t.start();
                        }
                        return false;
                    }
                });
                switchMaterial2.setOnTouchListener(new View.OnTouchListener(){
                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        switchMaterial2.setClickable(false);
                        String stat=(switchMaterial2.isChecked())?"OFF":"ON";
                        if(mqttClient!=null) {
                            Log.d("Debugging Log","SENDING/PUBLISHSING: "+((stat.equals("OFF"))?"ON":"OFF"));
                            new MainPub(mqttClient, 0, stat, topic[5], qos);
                            Thread t = new Thread(() -> {
                                Log.d("Debugging Log", "Subscribing to " + topic[4]);
                                status3 = (mainSub(mqttClient, topic[4])=="ON")?"ON":"OFF";
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateComponents();
                                    }
                                });
                            });
                            t.start();
                        }
                        return false;
                    }
                });
                Thread t = new Thread(() -> {
                    status1 = (mainSub(mqttClient, topic[0]).equals("ON"))?"ON":"OFF";
                    status2 = (mainSub(mqttClient, topic[2]).equals("ON"))?"ON":"OFF";
                    status3 = (mainSub(mqttClient, topic[4]).equals("ON"))?"ON":"OFF";
                    mainHandler.post(this::updateComponents);
                });
                t.start();
            }
            else{
                Log.d("Debugging Log", "Mqtt Client Failed to initiate ");
            }
    }
    private IMqttClient connect(String brokerURL,String clientID) {
        IMqttClient mqttClient = null;
        try {
            MqttClientPersistence mqttClientPersistence = new MemoryPersistence();
            mqttClient = new MqttClient(brokerURL, clientID, mqttClientPersistence);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            mqttClient.connect(options);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return mqttClient;
    }
    private String mainSub(IMqttClient client, String topicc){
        AtomicReference<String> status= new AtomicReference<>("");
        if ( !client.isConnected()) {
            return status.get();
        }
        CountDownLatch receivedSignal = new CountDownLatch(10);
        try {
            client.subscribe(topicc, (topic, msg) -> {
                byte[] payload = msg.getPayload();
                receivedSignal.countDown();
                status.set(new String(payload));
            });
            receivedSignal.await(1, TimeUnit.MINUTES);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        Log.d("Debugging Log", "RECIEVING SUBSCRIPTION: " + status.get());
        return status.get();
    }
    private void updateComponents(){
        SwitchMaterial switchMaterial = findViewById(R.id.input1);
        SwitchMaterial switchMaterial1 = findViewById(R.id.input2);
        SwitchMaterial switchMaterial2 = findViewById(R.id.input3);
        switchMaterial.setChecked(status1.equals("ON"));
        switchMaterial1.setChecked(status2.equals("ON"));
        switchMaterial2.setChecked(status3.equals("ON"));
    }
}