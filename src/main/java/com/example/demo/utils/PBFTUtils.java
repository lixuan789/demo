package com.example.demo.utils;

import com.example.demo.bean.Block;
import com.example.demo.bean.Node;
import com.example.demo.bean.Notebook;
import com.example.demo.mapper.NodeMapper;
import com.example.demo.merkle.MerkleTree;
import com.example.demo.pbft.VoteEnum;
import com.example.demo.pbft.VoteInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class PBFTUtils {

    @Autowired
    private NodeMapper nodeMapper;

    public static VoteInfo creteVoteInfo(VoteEnum ve) {
        VoteInfo vi=new VoteInfo();

        vi.setCode(ve.getCode());

        //获取MerkleTree的根节点
        ArrayList<Block> list = Notebook.getInstance().getList();
        ArrayList<String> contens = new ArrayList<>();
        for (Block block:list){
            contens.add(block.content);
        }
        vi.setList(contens);
        String hash = new MerkleTree(contens).getRoot().getHash();
        vi.setHash(hash);
        return vi;
    }

    //已经在连接的节点的个数
    public double getLeastNodeCount() {
        List<Node> list = nodeMapper.getOnlineNode();
        return list.size();
    }

    //PBFT消息节点最少确认个数计算
    public double getConnecttedNodeCount() {
        List<Node> list=nodeMapper.getCommitNode();
        return list.size();
    }
}
