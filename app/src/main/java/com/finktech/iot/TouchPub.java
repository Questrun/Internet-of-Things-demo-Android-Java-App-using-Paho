package com.finktech.iot;

import java.util.concurrent.Callable;
import org.eclipse.paho.client.mqttv3.*;


public class TouchPub implements Callable<Void> {
    private final IMqttClient client;
    private final String stat;
    private final int id;

    public TouchPub(IMqttClient client, int id, String stat) {
        this.client = client;
        this.id=id;
        this.stat=stat;
    }

    @Override
    public Void call() throws Exception {
        if ( !client.isConnected()) {
            return null;
        }
        MqttMessage msg = sendStatus();
        msg.setId(id);
        msg.setQos(2);
        msg.setRetained(true);
        client.publish("stat/t1touchwahcantt/POWER",msg);
        return null;//(stat.equals("OFF"))?"ON":"OFF";
    }

    private MqttMessage sendStatus()
    {
        String x=(stat.equals("OFF"))?"ON":"OFF";
        byte[] payload = String.valueOf(x).getBytes();
        return new MqttMessage(payload);
    }
}
