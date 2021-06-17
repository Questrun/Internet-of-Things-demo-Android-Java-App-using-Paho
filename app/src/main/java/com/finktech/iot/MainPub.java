package com.finktech.iot;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.concurrent.Callable;


public class MainPub implements Callable<Void> {
    private final IMqttClient client;
    private final String stat;
    private final int id;
    private final String topic;
    private final int qos;

    public MainPub(IMqttClient client, int id, String stat, String topic, int qos) {
        this.client = client;
        this.id=id;
        this.stat=stat;
        this.topic=topic;
        this.qos=qos;
    }

    @Override
    public Void call() throws Exception {
        if ( !client.isConnected()) {
            return null;
        }
        MqttMessage msg = sendStatus();
        msg.setId(id);
        msg.setQos(qos);
        msg.setRetained(true);
        client.publish(topic,msg);
        return null;
    }

    private MqttMessage sendStatus()
    {
        String x=(stat.equals("OFF"))?"ON":"OFF";
        byte[] payload = String.valueOf(x).getBytes();
        return new MqttMessage(payload);
    }
}
