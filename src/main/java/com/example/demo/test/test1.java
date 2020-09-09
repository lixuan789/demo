package com.example.demo.test;

import com.example.demo.websocket.MyClient;
import com.example.demo.websocket.MyServer;

import java.net.URI;
import java.net.URISyntaxException;

public class test1 {

    public static void main(String[] args) throws URISyntaxException {
        URI uri = new URI("ws://locolhost:1234");
        System.out.println(uri.toString());
    }
}
