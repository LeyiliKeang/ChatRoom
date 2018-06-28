package com.lk;

import java.io.Serializable;

public class ClientInfo implements Serializable {
    private static final long serialVersionUID = 2002877994932656979L;

    private String name;
    private String ip;
    private int port;
    private String msg;

    public ClientInfo(String name, String ip, int port, String msg) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.msg = msg;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getMsg() {
        return msg;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
