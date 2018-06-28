package com.lk.frame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class PrivateChatFrame extends JFrame {
    private JPanel contentPane;
    private JTextArea chatTextArea;
    private JButton sendButton;
    private JTextArea SendTextArea;

    public PrivateChatFrame(final Socket s, final HashMap<String, PrivateChatFrame> stringChatFrameHashMap) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        setContentPane(contentPane);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int i = JOptionPane.showConfirmDialog(contentPane, "确认退出聊天？", "小小提示框", JOptionPane.YES_NO_OPTION);
                if (i == JOptionPane.YES_OPTION) {
                    try {
                        s.close();
                        stringChatFrameHashMap.remove(getTitle());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                } else {
                    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                }
            }
        });
        pack();
        getRootPane().setDefaultButton(sendButton);
        setLocationRelativeTo(null);
        SendTextArea.requestFocus();
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!SendTextArea.getText().trim().equals("")) {
                    try {
                        DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                        dos.writeUTF(SendTextArea.getText().trim());
                        dos.flush();
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                Date date = new Date();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                String time = sdf.format(date);
                                chatTextArea.append("Sent " + time + "\n" + SendTextArea.getText() + "\n");
                                chatTextArea.setSelectionStart(chatTextArea.getText().length());
                                SendTextArea.setText("");
                                SendTextArea.requestFocus();
                            }
                        });
                    } catch (SocketException ex) {
                        JOptionPane.showMessageDialog(contentPane, "对方已经退出私聊", "小小提示框", JOptionPane.WARNING_MESSAGE);
                        SendTextArea.requestFocus();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(contentPane, "请输入要发送的消息", "小小提示框", JOptionPane.WARNING_MESSAGE);
                    SendTextArea.setText("");
                    SendTextArea.requestFocus();
                }
            }
        });
    }

    public JTextArea getChatTextArea() {
        return chatTextArea;
    }
}
