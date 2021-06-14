package com.finktech.iot;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.paho.client.mqttv3.*;

public class TouchSub  implements Callable<String> {

    private final IMqttClient client;

    public TouchSub(IMqttClient client) {
        this.client = client;
    }

    @Override
    public String call() {

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
