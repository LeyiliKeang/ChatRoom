package com.lk.thread;

import com.lk.ClientInfo;
import com.lk.frame.ServerFrame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ServerReceiveThread extends Thread {
    private Socket s;
    private ObjectOutputStream oos;
    private JTextArea ta;
    private DefaultTableModel defaultTableModel;
    private HashMap<String, ObjectOutputStream> stringObjectOutputStreamHashMap;//存储用户名和对象流，不存储Socket对象的原因是因为对象流不能多次获取，不然会出现java.io.StreamCorruptedException
    private ArrayList<ClientInfo> clientInfoArrayList;
    private String name;
    private ClientInfo firstClientInfo;
    private ClientInfo clientInfo;

    public ServerReceiveThread(Socket s, ServerFrame frame) {
        this.s = s;
        this.ta = frame.getChatTextArea();
        this.defaultTableModel = frame.getDefaultTableModel();
        this.stringObjectOutputStreamHashMap = frame.getStringObjectOutputStreamHashMap();
        this.clientInfoArrayList = frame.getClientInfoArrayList();
    }

    @Override
    public void run() {
        try {
            oos = new ObjectOutputStream(s.getOutputStream());//获取对象输出流
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());//获取对象输入流
            while (true) {
                clientInfo = (ClientInfo) ois.readObject();
                name = clientInfo.getName();
                if (stringObjectOutputStreamHashMap.containsKey(name)) {//判断hashmap中是否已经存在该用户名，如果存在即切断现在的用户连接
                    if (clientInfo.getMsg() == null) {//消息字段为null，为登录信息，但是hashmap存在该用户名，这里就不允许相同的用户名进行登陆
                        oos.writeObject(new ClientInfo(null, null, 0, null));
                        oos.flush();
                    } else {//如果消息字段不为空，就进行转发
                        new ServerSendThread(oos, stringObjectOutputStreamHashMap, clientInfo).start();
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                Date date = new Date();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                String time = sdf.format(date);
                                ta.append(name + " " + time + "\n" + clientInfo.getMsg() + "\n");
                                ta.setSelectionStart(ta.getText().length());
                            }
                        });
                    }
                } else {//如果hashmap不包含该用户
                    for (ClientInfo c : clientInfoArrayList) {//将现在已经连接上的用户信息发给新连接的用户
                        oos.writeObject(c);
                        oos.flush();
                    }
                    firstClientInfo = clientInfo;//存储第一个ClientInfo对象，因为这个是消息为null的
                    new ServerSendThread(oos, stringObjectOutputStreamHashMap, firstClientInfo).start();//服务器将该消息发送给已经连接上的用户，客户端根据消息字段是否为null进行判断是添加到列表还是显示在聊天消息中
                    clientInfoArrayList.add(firstClientInfo);//将信息添加到List中
                    stringObjectOutputStreamHashMap.put(name, oos);//将用户名和相应的对象流存储到hashmap中
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            defaultTableModel.addRow(new Object[]{name, clientInfo.getIp(), clientInfo.getPort()});
                            Date date = new Date();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                            String time = sdf.format(date);
                            ta.append(name + "连入服务器 " + time + "\n");
                            ta.setSelectionStart(ta.getText().length());
                        }
                    });
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Date date = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    String time = sdf.format(date);
                    ta.append(name + "已退出聊天 " + time + "\n");
                    ta.setSelectionStart(ta.getText().length());
                }
            });
            new ServerSendThread(oos, stringObjectOutputStreamHashMap, firstClientInfo).start();
            stringObjectOutputStreamHashMap.remove(name);
            clientInfoArrayList.remove(firstClientInfo);
            for (int row = 0; row < defaultTableModel.getRowCount(); row++) {
                final int r = row;
                if (defaultTableModel.getValueAt(r, 0).equals(name)) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            defaultTableModel.removeRow(r);
                        }
                    });
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
