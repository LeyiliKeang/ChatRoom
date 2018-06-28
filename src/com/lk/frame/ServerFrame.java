package com.lk.frame;

import com.lk.ClientInfo;
import com.lk.thread.ServerReceiveThread;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerFrame extends JFrame {
    private JPanel contentPane;
    private JTextField usePortTextField;
    private JButton startButton;
    private JButton stopButton;
    private JTextArea chatTextArea;
    private JTable userTable;
    private JScrollPane tableScrollPane;

    private ServerSocket ss;
    private HashMap<String, ObjectOutputStream> stringObjectOutputStreamHashMap = new HashMap<String, ObjectOutputStream>();
    private ArrayList<ClientInfo> clientInfoArrayList = new ArrayList<ClientInfo>();
    private DefaultTableModel defaultTableModel = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        new ServerFrame().setVisible(true);
    }

    public JTextArea getChatTextArea() {
        return chatTextArea;
    }

    public DefaultTableModel getDefaultTableModel() {
        return defaultTableModel;
    }

    public HashMap<String, ObjectOutputStream> getStringObjectOutputStreamHashMap() {
        return stringObjectOutputStreamHashMap;
    }

    public ArrayList<ClientInfo> getClientInfoArrayList() {
        return clientInfoArrayList;
    }

    public ServerFrame() {
        setTitle("服务器");
        usePortTextField.setText("10024");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        stopButton.setEnabled(false);
        setContentPane(contentPane);
        pack();
        setLocationRelativeTo(null);
        defaultTableModel.setColumnIdentifiers(new Object[]{"用户名", "IP地址", "监听端口"});
        userTable.getTableHeader().setReorderingAllowed(false);
        userTable.setModel(defaultTableModel);
        tableScrollPane.setViewportView(userTable);
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!usePortTextField.getText().trim().equals("")) {
                    usePortTextField.setEditable(false);
                    startButton.setEnabled(false);
                    stopButton.setEnabled(true);
                    try {
                        ss = new ServerSocket(Integer.parseInt(usePortTextField.getText().trim()));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                while (true) {
                                    Socket s = ss.accept();
                                    new ServerReceiveThread(s, ServerFrame.this).start();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                } else {
                    JOptionPane.showMessageDialog(contentPane, "请输入服务器端口号", "小小提示框", JOptionPane.WARNING_MESSAGE);
                    usePortTextField.requestFocus();
                }
            }
        });
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        usePortTextField.setEditable(true);
                        startButton.setEnabled(true);
                        stopButton.setEnabled(false);
                    }
                });
                try {
                    ss.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                for (String name : stringObjectOutputStreamHashMap.keySet()) {
                    ObjectOutputStream objectOutputStream = stringObjectOutputStreamHashMap.get(name);
                    try {
                        objectOutputStream.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }
}
