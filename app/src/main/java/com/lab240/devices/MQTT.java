package com.lab240.devices;

import android.content.Context;

import androidx.annotation.Nullable;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class MQTT {

    public interface MessageCallback{
        void handle(String topic, MqttMessage msg);
    }

    public interface LostConnectionCallback {
        void connectionLost(Throwable cause);
    }

    public MQTT(String server, String name, String pass) {
        this.server = server;
        this.name = name;
        this.pass = pass;
    }

    private final Set<LostConnectionCallback> lostConnectionCallbacks = new HashSet<>();

    public void addOnConnectionLostCallback(LostConnectionCallback lcc){
        lostConnectionCallbacks.add(lcc);
    }

    public void removeOnConnectionLostCallback(LostConnectionCallback lcc){
        lostConnectionCallbacks.remove(lcc);
    }

    public void disconnect() throws MqttException {
        if(client != null) client.disconnect();
    }

    public synchronized void connect(Context context, @Nullable IMqttActionListener listener){
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(context, server,
                        clientId);
        client.setCallback(new MqttCallback() {

            @Override
            public void connectionLost(Throwable cause) {
                for(LostConnectionCallback clc : lostConnectionCallbacks){
                    clc.connectionLost(cause);
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                synchronized (listeners) {
                    try {
                        for (MessageCallback cb : listeners.get(topic)) {
                            cb.handle(topic, message);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setAutomaticReconnect(false);
        mqttConnectOptions.setUserName(name);
        mqttConnectOptions.setPassword(pass != null ? pass.toCharArray() : null);
        return mqttConnectOptions;
    }

    public void addListener(String topic, MessageCallback cb){
        synchronized (listeners) {
            listeners.put(topic, cb);
        }
    }

    public void addListeners(String topic, Collection<MessageCallback> cb){
        synchronized (listeners) {
            listeners.putAll(topic, cb);
        }
    }

    public void removeListener(String topic, MessageCallback cb){
        synchronized (listeners) {
            listeners.remove(topic, cb);
        }
    }

    public void removeListeners(String topic, Collection<MessageCallback> cb){
        synchronized (listeners) {
            listeners.get(topic).removeAll(cb);
        }
    }

    public void send(String topic, String msg, int qos) {
        send(topic, msg, qos, null);
    }

    public void send(String topic, String msg, int qos, @Nullable IMqttActionListener listener) {
        if(client == null || !client.isConnected()) {
            throw new RuntimeException("No connection");
        }
        IMqttToken subToken = null;
        try {
            subToken = client.publish(topic, msg.getBytes(), qos, false);
        } catch (MqttException e) {
            e.printStackTrace();
            if(listener != null)
                listener.onFailure(subToken, e);
            return;
        }
        subToken.setActionCallback(listener);
    }

    private final Map<String, Set<Integer>> subs = new HashMap<>();

    public void subscribe(String topic, int qos, int key){
        subscribe(topic, qos, key, null);
    }

    public void subscribe(String topic, int qos, int key, @Nullable IMqttActionListener listener){
        if(client == null || !client.isConnected()) {
            throw new RuntimeException("No connection");
        }

        IMqttToken subToken = null;
        try {
            subToken = client.subscribe(topic, qos);
        } catch (MqttException e) {
            e.printStackTrace();
            if(listener != null)
                listener.onFailure(subToken, e);
            return;
        }
        subToken.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                if(listener != null) listener.onSuccess(asyncActionToken);
                if(!subs.containsKey(topic))
                    subs.put(topic, new TreeSet<>());
                subs.get(topic).add(key);
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                if(listener != null) listener.onFailure(asyncActionToken, exception);
            }
        });
    }

    public void unsubscribe(String topic, int key, @Nullable IMqttActionListener listener){
        if(!subs.containsKey(topic)){
            return;
        }else{
            subs.get(topic).remove(key);
        }
        if(!subs.get(topic).isEmpty())
            return;
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

    public void unsubscribe(String topic, int key){
        unsubscribe(topic, key,null);
    }

    public Set<String> getSubscriptions(){
        return subs.keySet();
    }

    public Set<String> getSubscriptions(int key){
        Set<String> res = new HashSet<>();
        for(Map.Entry<String, Set<Integer>> i : subs.entrySet()){
            if(i.getValue().contains(key))
                res.add(i.getKey());
        }
        return res;
    }

    private final String server;
    private final String name;

    public String getServer() {
        return server;
    }

    public String getName() {
        return name;
    }

    public String getPass() {
        return pass;
    }

    public boolean isConnected(){
        return client != null && client.isConnected();
    }

    private final String pass;
    private MqttAndroidClient client;
    private final Multimap<String, MessageCallback> listeners = ArrayListMultimap.create();

}
