package com.lab240.utils;

import android.content.Context;

import androidx.annotation.Nullable;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MQTT {

    public interface MessageCallback{
        void handle(String topic, MqttMessage msg);
    }

    public MQTT(String server, String name, String pass) {
        this.server = server;
        this.name = name;
        this.pass = pass;
    }

    public void connect(Context context, @Nullable IMqttActionListener listener){
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(context, server,
                        clientId);
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println("msg "+message.toString());
                if(listeners.containsKey(topic))
                    for(MessageCallback cb : listeners.get(topic)){
                        cb.handle(topic, message);
                    }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        IMqttToken token = null;
        try {
             token = client.connect(getMqttConnectionOption());
        } catch (MqttException e) {
            e.printStackTrace();
            if(listener != null)
                listener.onFailure(token, e);
            return;
        }
        token.setActionCallback(listener);
    }

    private MqttConnectOptions getMqttConnectionOption() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setUserName(name);
        mqttConnectOptions.setPassword(pass != null ? pass.toCharArray() : null);
        return mqttConnectOptions;
    }

    public void addListener(String topic, MessageCallback cb){
        if(!listeners.containsKey(topic))
            listeners.put(topic, new ArrayList<>());
        listeners.get(topic).add(cb);
    }

    public void removeListener(String topic, MessageCallback cb){
        if(!listeners.containsKey(topic))
            return;
        listeners.get(topic).remove(cb);
    }

    public void subscribe(String topic, int qos, @Nullable IMqttActionListener listener){
        if(client == null)
            throw new RuntimeException("No connection");

        IMqttToken subToken = null;
        try {
            subToken = client.subscribe(topic, qos);
        } catch (MqttException e) {
            e.printStackTrace();
            if(listener != null)
                listener.onFailure(subToken, e);
            return;
        }
        subToken.setActionCallback(listener);
    }

    public void subscribe(String topic, int qos){
        subscribe(topic, qos, null);
    }

    public void unsubscribe(String topic, @Nullable IMqttActionListener listener){
        if(client == null)
            throw new RuntimeException("No connection");

        IMqttToken subToken = null;
        try {
            subToken = client.unsubscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
            if(listener != null)
                listener.onFailure(subToken, e);
            return;
        }
        subToken.setActionCallback(listener);
    }

    public void unsubscribe(String topic){
        unsubscribe(topic, null);
    }

    private final String server, name, pass;
    private MqttAndroidClient client;
    private final Map<String, Collection<MessageCallback>> listeners = new HashMap<>();

}
