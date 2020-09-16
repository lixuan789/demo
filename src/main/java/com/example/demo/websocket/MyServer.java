package com.example.demo.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.bean.Block;
import com.example.demo.bean.MessageBean;
import com.example.demo.bean.Notebook;
import com.example.demo.bean.Transaction;
import com.example.demo.mapper.NodeMapper;
import com.example.demo.merkle.MerkleTree;
import com.example.demo.pbft.VoteEnum;
import com.example.demo.pbft.VoteInfo;
import com.example.demo.utils.PBFTUtils;
import com.example.demo.utils.WebUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

//服务端
public class MyServer extends WebSocketServer {
    private final static Logger logger= LoggerFactory.getLogger(MyServer.class);
    private static String ip= WebUtils.getHostIp();
    //服务器端口
    private int port;

    private List<WebSocket> localSockets=new ArrayList<WebSocket>();

    public List<WebSocket> getLocalSockets() {
        return localSockets;
    }

    public void setLocalSockets(List<WebSocket> localSockets) {
        this.localSockets = localSockets;
    }
    //客户端进行确认的数量
    /*private int commit;

    public int getCommit() {
        return commit;
    }

    public void setCommit(int commit) {
        this.commit = commit;
    }*/

    public MyServer(int port) {
        super(new InetSocketAddress(port));
        this.port = port;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        logger.info("服务器:" + ip+":"+port + "打开了连接");
//        nodeMapper.updateState(ip,String.valueOf(port),1);
        localSockets.add(conn);
        conn.send("服务器成功创建");//发送消息
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        logger.info("服务器:" + ip+":"+port + "关闭了连接");
        localSockets.remove(conn);
//        nodeMapper.updateState(ip,String.valueOf(port),0);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        logger.info("服务器:" + ip+":"+port + "收到了消息："+message);
        Notebook notebook = Notebook.getInstance();
        if ("同步数据".equals(message)) {//此方法是同步数据
            // 获取本地的区块链数据
            List<Block> list = notebook.showlist();
            // 发送给连接到本服务器的所有客户端
            String blockChainData=JSON.toJSONString(list);
            MessageBean messageBean = new MessageBean(1, blockChainData);
            String msg = JSON.toJSONString(messageBean);
            // 广播消息
            broadcast(msg);
        }else {
            //收到入库的消息则不再发送
            if ("共识".equals(message)){
                conn.send("请求达成共识");
                return;
            }
            //收到的不是JSON化数据，则说明仍处在双方建立连接的过程中。目前连接已经建立完毕，发起投票
            if (!message.startsWith("{")){
                VoteInfo vi=PBFTUtils.creteVoteInfo(VoteEnum.PREPREPARE);
                String viString = JSON.toJSONString(vi);
                MessageBean messageBean = new MessageBean(0, viString);//发送类型为0的消息
                conn.send(JSON.toJSONString(messageBean));
                logger.info("服务端发送到客户端的pbft消息:PREPREPARE状态");
                return;
            }

            //如果是JSON化数据，则表明已经进入到了PBFT投票阶段
            JSONObject json = JSON.parseObject(message);
            if (!json.containsKey("code")){
                logger.info("服务端收到非json化数据");
            }
            int code=json.getIntValue("code");
            if (code==VoteEnum.PREPARE.getCode()){
                //校验hash
                VoteInfo voteInfo = JSON.parseObject(message, VoteInfo.class);

                List<Block> list = notebook.getList();
                ArrayList<String> contens = new ArrayList<>();
                for (Block block: list){
                    contens.add(block.content);
                }
                MerkleTree tree = new MerkleTree(contens);
                if (!voteInfo.getHash().equals(tree.getRoot().getHash())){
                    System.out.println("MerkleTree根节点hash值校验失败，请先同步");
                    return;//非法JSON化数据
                }
                //校验成功，发送下一个状态的数据
                VoteInfo vi = PBFTUtils.creteVoteInfo(VoteEnum.COMMTT);
                String viString = JSON.toJSONString(vi);
                //类型为0
                MessageBean messageBean = new MessageBean(0, viString);
                conn.send(JSON.toJSONString(messageBean));
                logger.info("服务端发送到客户端的pbft消息："+viString);
            }
        }
    }


    //onError方法调用完毕会触发onClose方法
    @Override
    public void onError(WebSocket conn, Exception ex) {
        localSockets.remove(conn);
        logger.info("服务器" +ip+":"+ port + "发生错误");
    }

    @Override
    public void onStart() {
        logger.info("服务器" +ip+":"+ port + "启动成功");
    }

    @Override
    public void start() {
        super.start();
    }

    /*public void startServer(){
        new Thread(this).start();
    }*/
}
