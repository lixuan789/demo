package com.example.demo.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.bean.Block;
import com.example.demo.bean.MessageBean;
import com.example.demo.bean.Notebook;
import com.example.demo.bean.Transaction;
import com.example.demo.merkle.MerkleTree;
import com.example.demo.pbft.VoteEnum;
import com.example.demo.pbft.VoteInfo;
import com.example.demo.utils.PBFTUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;

//服务端
public class MyServer extends WebSocketServer {


    //服务器端口
    private int port;


    public MyServer(int port) {
        super(new InetSocketAddress(port));
        this.port = port;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("webSocket服务器_" + port + "_打开了连接");
        conn.send("服务器成功创建连接");//发送消息
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("webSocket服务器_" + port + "_关闭了连接");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("webSocket服务器_" + port + "_收到了消息:" + message);
        try {
            if ("同步数据".equals(message)) {//此方法是同步数据
                // 获取本地的区块链数据
                Notebook notebook = Notebook.getInstance();
                ArrayList<Block> list = notebook.showlist();
                // 发送给连接到本服务器的所有客户端
                ObjectMapper objectMapper = new ObjectMapper();
                String blockChainData = objectMapper.writeValueAsString(list);

                MessageBean messageBean = new MessageBean(1, blockChainData);
                String msg = objectMapper.writeValueAsString(messageBean);
                // 广播消息
                broadcast(msg);
            }else {
                //收到入库的消息则不再发送
                if ("客户端开始区块入库啦".equals(message)){
                    return;
                }

                //收到的不是JSON化数据，则说明仍处在双方建立连接的过程中。目前连接已经建立完毕，发起投票
                if (!message.startsWith("{")){
                    VoteInfo vi=PBFTUtils.creteVoteInfo(VoteEnum.PREPREPARE);
                    String viString = JSON.toJSONString(vi);
                    MessageBean messageBean = new MessageBean(0, viString);//发送类型为0的消息
                    conn.send(JSON.toJSONString(messageBean));
                    System.out.println("服务端发送到客户端的pbft消息："+viString);
                    return;
                }

                //如果是JSON化数据，则表明已经进入到了PBFT投票阶段
                JSONObject json = JSON.parseObject(message);
                if (!json.containsKey("code")){
                    System.out.println("服务端收到非json化数据");
                }
                int code=json.getIntValue("code");
                if (code==VoteEnum.PREPARE.getCode()){
                    //校验hash
                    VoteInfo voteInfo = JSON.parseObject(message, VoteInfo.class);

                    MerkleTree tree = new MerkleTree(voteInfo.getList());
                    if (!voteInfo.getHash().equals(tree.getRoot().getHash())){
                        return;//非法JSON化数据
                    }
                    //校验成功，发送下一个状态的数据
                    VoteInfo vi = PBFTUtils.creteVoteInfo(VoteEnum.COMMTT);
                    String viString = JSON.toJSONString(vi);
                    //类型为0
                    MessageBean messageBean = new MessageBean(0, viString);
                    conn.send(JSON.toJSONString(messageBean));
                    System.out.println("服务端发送到客户端的pbft消息："+viString);
                }
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }


    //onError方法调用完毕会触发onClose方法
    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.out.println("webSocket服务器_" + port + "_发生了错误");
    }

    @Override
    public void onStart() {
        System.out.println("webSocket服务器_" + port + "_启动成功");
    }

    @Override
    public void start() {
        super.start();
    }

    /*public void startServer(){
        new Thread(this).start();
    }*/
}
