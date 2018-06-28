package com.lk.frame;

import com.lk.ClientInfo;
import com.lk.thread.ClientPrivateReceiveThread;
import com.lk.thread.ClientReceiveThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ClientFrame extends JFrame {
    private JPanel contentPane;
    private JTextField serverIpTextField;
    private JTextField serverPortTextField;
    private JTextField nameTextField;
    private JTextField usePortTextField;
    private JButton loginButton;
    private JButton exitButton;
    private JTextArea chatTextArea;
    private JTextArea sendTextArea;
    private JButton sendButton;
    private JList userList;
    private JButton privateChatButton;
    private JScrollPane listScrollPane;

    private ServerSocket ss;//用于监听私聊的Socket
    private Socket s;//用于与服务器进行通信
    private ArrayList<ClientInfo> clientInfoArrayList = new ArrayList<ClientInfo>();//存储服务器发来的其他客户端信息
    private HashMap<String, PrivateChatFrame> stringChatFrameHashMap = new HashMap<String, PrivateChatFrame>();//存储私聊的用户名和对应的窗口
    private ObjectOutputStream oos;

    private DefaultListModel defaultListModel = new DefaultListModel();

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        new ClientFrame().setVisible(true);
    }

    public JTextArea getChatTextArea() {
        return chatTextArea;
    }

    public ArrayList<ClientInfo> getClientInfoArrayList() {
        return clientInfoArrayList;
    }

    public DefaultListModel getDefaultListModel() {
        return defaultListModel;
    }

    public JTextArea getSendTextArea() {
        return sendTextArea;
    }

    public ClientFrame() {
        setTitle("匿名聊天室");
        serverIpTextField.setText("192.168.0.24");
        serverPortTextField.setText("10024");
        sendTextArea.setEnabled(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getRootPane().setDefaultButton(sendButton);
        setContentPane(contentPane);
        pack();
        setLocationRelativeTo(null);
        userList.setModel(defaultListModel);
        listScrollPane.setViewportView(userList);
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!serverIpTextField.getText().trim().equals("") && !serverPortTextField.getText().trim().equals("") && !nameTextField.getText().trim().equals("") && !usePortTextField.getText().trim().equals("")) {
                    if (!sendTextArea.isEnabled()) {
                        sendTextArea.setEnabled(true);
                    }
                    try {
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    ss = new ServerSocket(Integer.parseInt(usePortTextField.getText().trim()));
                                    while (true) {
                                        Socket s = ss.accept();
                                        BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                                        String name = br.readLine();
                                        PrivateChatFrame frame = new PrivateChatFrame(s, stringChatFrameHashMap);
                                        frame.setTitle(name);
                                        ClientPrivateReceiveThread rt = new ClientPrivateReceiveThread(s, frame, stringChatFrameHashMap);
                                        rt.start();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                        s = new Socket(serverIpTextField.getText().trim(), Integer.parseInt(serverPortTextField.getText().trim()));//连接服务器
                        oos = new ObjectOutputStream(s.getOutputStream());//获取对象输出流
                        oos.writeObject(new ClientInfo(nameTextField.getText().trim(), s.getInetAddress().getHostAddress(), Integer.parseInt(usePortTextField.getText().trim()), null));//将自身客户端信息写入到对象中，消息为null即为登录服务器
                        oos.flush();
                        new ClientReceiveThread(s, ClientFrame.this).start();//开启线程接收服务器发来的消息
                    } catch (ConnectException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(chatTextArea, "服务器未开启", "小小提示框", JOptionPane.WARNING_MESSAGE);
                        sendTextArea.setEnabled(false);
                    } catch (EOFException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(chatTextArea, "用户名被使用", "小小提示框", JOptionPane.WARNING_MESSAGE);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!sendTextArea.getText().trim().equals("")) {
                    try {
                        oos.writeObject(new ClientInfo(nameTextField.getText().trim(), s.getInetAddress().getHostAddress(), Integer.parseInt(usePortTextField.getText().trim()), sendTextArea.getText().trim()));//将消息写入对象中
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                Date date = new Date();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                String time = sdf.format(date);
                                chatTextArea.append(nameTextField.getText().trim() + " " + time + "\n" + sendTextArea.getText() + "\n");
                                chatTextArea.setSelectionStart(chatTextArea.getText().length());
                                sendTextArea.setText("");
                                sendTextArea.requestFocus();
                            }
                        });
                    } catch (SocketException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(chatTextArea, "服务器已关闭", "小小提示框", JOptionPane.WARNING_MESSAGE);
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                clientInfoArrayList.clear();
                                defaultListModel.removeAllElements();
                                sendTextArea.setEnabled(false);
                            }
                        });
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(contentPane, "请输入要发送的消息", "小小提示框", JOptionPane.WARNING_MESSAGE);
                    sendTextArea.setText("");
                    sendTextArea.requestFocus();
                }
            }
        });
        privateChatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = (String) userList.getSelectedValue();//获取列表中被选中的用户名
                if (stringChatFrameHashMap.containsKey(name)) {//如果该用户名有相应的窗口即让其获取焦点显示出来
                    PrivateChatFrame frame = stringChatFrameHashMap.get(name);
                    frame.requestFocus();
                } else {
                    for (ClientInfo clientInfo : clientInfoArrayList) {
                        if (clientInfo.getName().equals(name)) {//没有相应的窗口就新建窗口
                            try {
                                Socket s = new Socket(clientInfo.getIp(), clientInfo.getPort());
                                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                                bw.write(nameTextField.getText().trim());
                                bw.newLine();
                                bw.flush();
                                PrivateChatFrame frame = new PrivateChatFrame(s, stringChatFrameHashMap);
                                frame.setTitle(name);
                                frame.setVisible(true);
                                ClientPrivateReceiveThread rt = new ClientPrivateReceiveThread(s, frame, stringChatFrameHashMap);//创建接收来自私聊消息的进程
                                rt.start();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }
}
