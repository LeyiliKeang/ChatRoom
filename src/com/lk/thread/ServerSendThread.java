package com.lk.thread;

import com.lk.ClientInfo;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class ServerSendThread extends Thread {
    private ObjectOutputStream oos;
    private HashMap<String, ObjectOutputStream> stringObjectOutputStreamHashMap;
    private ClientInfo clientInfo;

    public ServerSendThread(ObjectOutputStream oos, HashMap<String, ObjectOutputStream> stringObjectOutputStreamHashMap, ClientInfo clientInfo) {
        this.oos = oos;
        this.stringObjectOutputStreamHashMap = stringObjectOutputStreamHashMap;
        this.clientInfo = clientInfo;
    }

    @Override
    public void run() {
        for (String name : stringObjectOutputStreamHashMap.keySet()) {
            ObjectOutputStream objectOutputStream = stringObjectOutputStreamHashMap.get(name);
            if (objectOutputStream != oos) {//将该对象流对应的客户端的消息转发给其他的客户端
                try {
                    objectOutputStream.writeObject(clientInfo);
                    objectOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
