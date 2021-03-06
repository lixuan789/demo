package com.example.demo.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.bean.*;
import com.example.demo.controller.BlockController;
import com.example.demo.mapper.NodeMapper;
import com.example.demo.merkle.MerkleTree;
import com.example.demo.pbft.VoteEnum;
import com.example.demo.pbft.VoteInfo;
import com.example.demo.utils.PBFTUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

//客户端
public class MyClient extends WebSocketClient {
    private final static Logger logger= LoggerFactory.getLogger(MyClient.class);

    private NodeMapper nodeMapper;

    private String name;

    private Boolean flag;//hash校验是否不通过

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MyClient(URI serverUri, String name, NodeMapper nodeMapper) {
        super(serverUri);
        this.name = name;
        this.nodeMapper=nodeMapper;
    }

    @Override
    public void connect() {
        super.connect();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        String[] split = name.split(":");
        String ip=split[0];
        String port=split[1];
        logger.info("客户端__" + name + "__打开了连接");
        nodeMapper.updateState(ip,port,1);//能连接，则确保在线状态为1
        this.send("客户端成功创建");
    }

    @Override
    public void onMessage(String message) {
        logger.info("客户端__" + name + "__收到了消息:"+message);
        String[] split = name.split(":");
        String ip=split[0];
        String port=split[1];
        /*Node commit = nodeMapper.isCommit(ip, port);
        if (commit!=null){//已经确认过了直接跳过
            return;
        }*/

        if ("请求达成共识".equals(message)){
            this.send("客户端成功创建");
            return;
        }
        //如果收到的不是JSON化数据，则说明不是PBFT阶段
        if (!message.startsWith("{")){
            return;
        }

        MessageBean messageBean = JSON.parseObject(message, MessageBean.class);
        Notebook notebook = Notebook.getInstance();
        // 判断消息类型
        if (messageBean.type==0){

            //如果收到的是JSON化数据，则说明是PBFT阶段，进入到投票阶段
            JSONObject json = JSON.parseObject(messageBean.msg);
            if (!json.containsKey("code")){
                logger.info("客户端收到非JSON化数据");
            }
            int code=json.getIntValue("code");
            if (code==VoteEnum.PREPREPARE.getCode()){
                //校验hash
                VoteInfo voteInfo = JSON.parseObject(messageBean.msg, VoteInfo.class);
                //获取本地数据
                List<Block> list = notebook.getList();
                ArrayList<String> contens = new ArrayList<>();
                for (Block block: list){
                    contens.add(block.transaction.content);
                }
                MerkleTree tree = new MerkleTree(contens);
                if (!voteInfo.getHash().equals(tree.getRoot().getHash())){
                    this.flag=true;//校验不通过
                    System.out.println("MerkleTree跟节点hash校验错误，请同步");
                    logger.info("MerkleTree跟节点hash校验错误，请同步");
//                    nodeMapper.updateCommit(ip,port,1);
                    return;
                }

                this.flag=false;
                //校验成功，发送下一阶段状态的数据
                VoteInfo vi = PBFTUtils.creteVoteInfo(VoteEnum.PREPARE);
                this.send(JSON.toJSONString(vi));
                logger.info("客户端发送到服务端PBFT消息："+JSON.toJSONString(vi));
            }

            if (code==VoteEnum.COMMTT.getCode()){
                //校验hash
                VoteInfo voteInfo = JSON.parseObject(messageBean.msg, VoteInfo.class);

                List<Block> list = notebook.getList();
                ArrayList<String> contens = new ArrayList<>();
                for (Block block: list){
                    contens.add(block.transaction.content);
                }
                MerkleTree tree = new MerkleTree(contens);
                if (!voteInfo.getHash().equals(tree.getRoot().getHash())){
                    System.out.println("MerkleTree跟节点hash校验错误，请同步");
                    return;
                }
                //校验成功，进入commit状态

                logger.info(name+"确认通过");
                nodeMapper.updateCommit(ip,port,1);
//                this.send("达成共识");
                /*if (getConnecttedNodeCount()>=(getLeastNodeCount()*2)/3.0){
                    this.send("客户端开始区块入库啦");
                }*/
            }
        }else if (messageBean.type == 1) {
            // 收到的是区块链数据
            ArrayList<Block> newList = (ArrayList<Block>) JSON.parseArray(messageBean.msg, Block.class);
            // 和本地的区块链进行比较,如果对方的数据比较新,就用对方的数据替换本地的数据
            notebook.compareData(newList);

        } else if (messageBean.type == 2&&!flag) {
            //收到的是交易信息
            Transaction transaction = JSON.parseObject(messageBean.msg, Transaction.class);
            if (transaction.verify()) {
                notebook.addNote(messageBean.msg);//加到区块链
            }
        }
    }

    //已经在连接的节点的个数
    /*private double getLeastNodeCount() {
        List<Node> list = nodeMapper.getOnlineNode();
        return list.size();
    }*/

    //PBFT消息节点最少确认个数计算
    /*private double getConnecttedNodeCount() {
        return 1;//还有自己
    }*/

    @Override
    public void onClose(int code, String reason, boolean remote) {
        String[] split = name.split(":");
        String ip=split[0];
        String port=split[1];
        nodeMapper.updateState(ip,port,0);
        nodeMapper.updateCommit(ip,port,0);
        logger.info("客户端" +name + "关闭了连接");
    }

    @Override
    public void onError(Exception ex) {
        logger.info("客户端" + name + "发生错误");
    }

}
