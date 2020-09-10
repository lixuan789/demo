package com.example.demo.controller;

import com.example.demo.DemoApplication;
import com.example.demo.bean.*;
import com.example.demo.crypto.SM2;
import com.example.demo.mapper.NodeMapper;
import com.example.demo.ui.Web;
import com.example.demo.utils.WebUtils;
import com.example.demo.websocket.MyClient;
import com.example.demo.websocket.MyServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.math.ec.ECPoint;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.util.*;

@RestController
public class BlockController {
    private final static Logger logger=LoggerFactory.getLogger(BlockController.class);

    private Notebook notebook = Notebook.getInstance();

    @Autowired
    private NodeMapper nodeMapper;


    private MyServer server;//服务端

    private HashSet<MyClient> clients = new HashSet<>();//服务器包含的客户端

    private HashMap<String, MyClient> map=new HashMap<>();

    private String serverport;

    /**
     * 一启动程序，启动服务器并插入数据库，最后向其他所有客户端发起连接
     */
    @PostConstruct
    public void init() throws URISyntaxException, IOException, InterruptedException {
        ServerSocket s = new ServerSocket(0);
        serverport=String.valueOf(s.getLocalPort());
        s.close();
        //1.启动服务器
        server = new MyServer(Integer.parseInt(serverport));
        server.start();


        String ip = WebUtils.getHostIp();
        String hostName = WebUtils.getHostName();

        //2.连接其他节点
        String curUrl=ip+":"+serverport;
//        List<Node> onlineNode = nodeMapper.getOnlineNode();
        List<Node> all = nodeMapper.getAll();
        for (Node n : all) {
            String url = n.getIp() + ":" + n.getPort();
            if (url.equals(curUrl)){
                continue;
            }
            logger.info(url+":发起连接");
            URI uri = new URI("ws://" + url);
            MyClient client = new MyClient(uri,url,nodeMapper);
            client.connect();
            clients.add(client);
            map.put(url,client);
        }

        //3.已存在则不插入，进行更新
        Node result = nodeMapper.getNodeByIpAndPort(ip,serverport);//通过ip获取节点
        if (result != null) {
            if (result.getState()==0){
                //已经存在，并且状态为0，状态更新为1
                logger.info("节点:"+curUrl+"的状态更新为1");
                nodeMapper.updateState(ip, serverport, 1);
            }
        }else {
            //3.不存在插入新节点
            Node node = new Node();
            node.setIp(ip);
            node.setPort(serverport);
            node.setState(1);//为在线状态
            node.setHostName(hostName);
            node.setCommit(0);
            nodeMapper.insertNode(node);
        }

    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView index(ModelAndView model) {
        model.setViewName("index.html");
        return model;
    }


    // 添加封面
    @RequestMapping(value = "/addGenesis", method = RequestMethod.POST)
    public String addGenesis(String genesis) {
        try {
            notebook.addGenesis(genesis);
            return "添加封面成功";
        } catch (Exception e) {
            return "添加封面失败:" + e.getMessage();
        }
    }

    // 添加记录
    @RequestMapping(value = "/addNote", method = RequestMethod.POST)
    public String addNote(String content, HttpSession session) throws URISyntaxException, InterruptedException {

        //创建连接新加入节点的客户端
        List<Node> all = nodeMapper.getOnlineNode();
        String ip = WebUtils.getHostIp();
        String curUrl=ip+":"+serverport;
        for (Node node:all){
            String url=node.getIp()+":"+node.getPort();
            if (url.equals(curUrl)){
                continue;
            }
            if (map.containsKey(url)){
                continue;
            }
            URI uri = new URI("ws://" + url);
            MyClient client = new MyClient(uri,url,nodeMapper);
            client.connectBlocking();
//            client.connect();
            clients.add(client);
            map.put(url,client);
        }

        //先进行共识
        for (MyClient client : clients) {
            if (client != null && client.isOpen()) {
                client.send("共识");
            }
        }
        //先进行共识
//        server.broadcast("请求达成共识");
        String userName = (String) session.getAttribute("loginUser");
//        String hostName=WebUtils.getHostName();
        Transaction transaction = new Transaction(content,userName);
        try {
            if (content.matches(".*(中汽数据).*")){
                if (getConnecttedNodeCount()>=(getLeastNodeCount()*2)/3.0){
                    if (transaction.verify()){
                        ObjectMapper objectMapper = new ObjectMapper();
                        String transactionString = objectMapper.writeValueAsString(transaction);
                        //广播交易数据
                        MessageBean messageBean = new MessageBean(2, transactionString);
                        String msg = objectMapper.writeValueAsString(messageBean);
                        server.broadcast(msg);//广播交易数据
                        notebook.addNote(transactionString);//本地存一份

                        return "添加记录成功";
                    }else {
                        return "交易数据校验失败";
                    }
                }else {
                    return "未达成共识,请先进行同步";
                }
            }else {
                return "内容需包含中汽数据";
            }
        } catch (Exception e) {
            return "添加记录失败:" + e.getMessage();
        }
    }


    //已经在连接的节点的个数
    private double getLeastNodeCount() {
        List<Node> list = nodeMapper.getOnlineNode();
        return list.size();
    }

    //PBFT消息节点最少确认个数计算
    private double getConnecttedNodeCount() {
        List<Node> list=nodeMapper.getCommitNode();
        return list.size()+1;//加上自己
    }

    // 展示记录
    @RequestMapping(value = "/showlist", method = RequestMethod.GET)
    public List<Block> showlist() {
        return notebook.showlist();
    }

    // 校验数据
    @RequestMapping(value = "/check", method = RequestMethod.GET)
    public String check() {
        String check = notebook.check();
        if (StringUtils.isEmpty(check)) {
            return "数据是安全的";
        }
        return check;
    }


    // 请求同步其他节点的区块链数据
    @RequestMapping("/syncData")
    public String syncData() throws URISyntaxException, InterruptedException {


        List<Node> all = nodeMapper.getOnlineNode();
        String ip = WebUtils.getHostIp();
        String curUrl=ip+":"+serverport;
        for (Node node:all){
            String url=node.getIp()+":"+node.getPort();
            if (url.equals(curUrl)){
                continue;
            }
            if (map.containsKey(url)){
                continue;
            }
            URI uri = new URI("ws://" + url);
            MyClient client = new MyClient(uri,url,nodeMapper);
            client.connectBlocking();
//            client.connect();
            clients.add(client);
            map.put(url,client);
        }
        for (MyClient client : clients) {
            if (!client.isOpen()) {
                if (client.getReadyState().equals(WebSocket.READYSTATE.NOT_YET_CONNECTED)) {
                    client.connect();
                } else if (client.getReadyState().equals(WebSocket.READYSTATE.CLOSING) || client.getReadyState().equals(WebSocket.READYSTATE.CLOSED)) {
                    client.reconnect();
                }
            }
            client.send("同步数据");
            /*if (client!=null&&client.isOpen()){
                client.send("同步数据");
            }*/
        }
        return "同步失败";
    }

}
