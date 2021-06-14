package com.finktech.iot;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistable;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button touchbtn= findViewById(R.id.button);
        try {
            MqttClientPersistence mqttClientPersistence = new MemoryPersistence();
            MqttClient publisher = new MqttClient("tcp://broker.hivemq.com:1883", "GoodBoy",mqttClientPersistence);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            publisher.connect(options);
            MqttClient subscriber = new MqttClient("tcp://broker.hivemq.com:1883", "GoodBoy",mqttClientPersistence);
            String jx=touchSub(subscriber);
            String stat=(jx.equals("ON"))?jx:"OFF";
            TextView tx = findViewById(R.id.text);
            tx.setText(stat);

            touchbtn.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v) {
                new TouchPub(publisher,0,stat);
                    MqttClient subscriber;
                    String jx="";
                    try {
                        subscriber = new MqttClient("tcp://broker.hivemq.com:1883", "GoodBoy",mqttClientPersistence);
                        jx=touchSub(subscriber);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                String stat=(jx.equals("ON"))?jx:"OFF";
                TextView tx = findViewById(R.id.text);
                tx.setText(jx);
                }
            });
            Log.d("F","Running");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    private String touchSub(MqttClient client){
        AtomicReference<String> status= new AtomicReference<>("");
        if ( !client.isConnected()) {
            return status.get();
        }
        CountDownLatch receivedSignal = new CountDownLatch(10);
        try {
            client.subscribe("cmnd/t1touchwahcantt/POWER", (topic, msg) -> {
                byte[] payload = msg.getPayload();
                receivedSignal.countDown();
                status.set(new String(payload));
            });
            receivedSignal.await(1, TimeUnit.MINUTES);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return status.get();
    }
}