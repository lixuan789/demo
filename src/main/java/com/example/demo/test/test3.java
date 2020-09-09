package com.example.demo.test;

import com.example.demo.websocket.MyServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class test3 {
    public static void main(String[] args) {
        ServerSocket s = null;
        try {
            s = new ServerSocket(0);
            System.out.println("listening on port: " + s.getLocalPort());
            System.out.println("listening on port: " + s.getLocalPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
