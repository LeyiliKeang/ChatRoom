package com.lk.thread;

import com.lk.ClientInfo;
import com.lk.frame.ClientFrame;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ClientReceiveThread extends Thread {
    private Socket s;
    private JTextArea chatTextArea;
    private JTextArea sendTextArea;
    private ArrayList<ClientInfo> clientInfoArrayList;
    private DefaultListModel defaultListModel;

    public ClientReceiveThread(Socket s, ClientFrame frame) {
        this.s = s;
        this.chatTextArea = frame.getChatTextArea();
        this.sendTextArea = frame.getSendTextArea();
        this.clientInfoArrayList = frame.getClientInfoArrayList();
        this.defaultListModel = frame.getDefaultListModel();
    }

    @Override
    public void run() {
        try {
            final ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            while (true) {
                final ClientInfo clientInfo = (ClientInfo) ois.readObject();
                if (clientInfo.getName() == null) {
                    JOptionPane.showMessageDialog(chatTextArea, "用户名被使用", "小小提示框", JOptionPane.WARNING_MESSAGE);
                    s.close();
                    sendTextArea.setEnabled(false);
                    break;
                } else {
                    if (clientInfo.getMsg() == null) {
                        if (clientInfoArrayList.contains(clientInfo)) {
                            clientInfoArrayList.remove(clientInfo);
                            EventQueue.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    Date date = new Date();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                    String time = sdf.format(date);
                                    chatTextArea.append(clientInfo.getName() + "离开了聊天室 " + time + "\n");
                                    chatTextArea.setSelectionStart(chatTextArea.getText().length());
                                    defaultListModel.removeElement(clientInfo.getName());
                                }
                            });
                        } else {
                            clientInfoArrayList.add(clientInfo);
                            EventQueue.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    defaultListModel.addElement(clientInfo.getName());
                                }
                            });
                        }
                    } else {
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                Date date = new Date();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                String time = sdf.format(date);
                                chatTextArea.append(clientInfo.getName() + " " + time + "\n" + clientInfo.getMsg() + "\n");
                                chatTextArea.setSelectionStart(chatTextArea.getText().length());
                            }
                        });
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(chatTextArea, "服务器已关闭", "小小提示框", JOptionPane.WARNING_MESSAGE);
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    clientInfoArrayList.clear();
                    defaultListModel.removeAllElements();
                    sendTextArea.setEnabled(false);
                }
            });
        } catch (EOFException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(chatTextArea, "服务器已关闭", "小小提示框", JOptionPane.WARNING_MESSAGE);
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    clientInfoArrayList.clear();
                    defaultListModel.removeAllElements();
                    sendTextArea.setEnabled(false);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
