package com.lk.thread;

import com.lk.frame.PrivateChatFrame;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class ClientPrivateReceiveThread extends Thread {
    private Socket s;
    private PrivateChatFrame frame;
    private JTextArea ta;
    private HashMap<String, PrivateChatFrame> stringChatFrameHashMap;

    public ClientPrivateReceiveThread(Socket s, PrivateChatFrame frame, HashMap<String, PrivateChatFrame> stringChatFrameHashMap) {
        this.s = s;
        this.frame = frame;
        this.ta = frame.getChatTextArea();
        this.stringChatFrameHashMap = stringChatFrameHashMap;
    }

    @Override
    public void run() {
        stringChatFrameHashMap.put(frame.getTitle(), frame);
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(s.getInputStream());
            String text;
            while ((text = dis.readUTF()) != null) {
                final String msg = text;
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (!frame.isVisible()) {
                            frame.setVisible(true);
                        }
                        Date date = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        String time = sdf.format(date);
                        ta.append("Received " + time + "\n" + msg + "\n");
                        ta.setSelectionStart(ta.getText().length());
                    }
                });
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (EOFException e) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Date date = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    String time = sdf.format(date);
                    ta.append(frame.getTitle() + "已退出私聊 " + time + "\n");
                    ta.setSelectionStart(ta.getText().length());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (dis != null) {
                    dis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
