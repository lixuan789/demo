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


    public static VoteInfo creteVoteInfo(VoteEnum ve) {
        VoteInfo vi=new VoteInfo();

        vi.setCode(ve.getCode());

        Notebook instance = Notebook.getInstance();
        List<Block> list = instance.getList();

        ArrayList<String> contens = new ArrayList<>();
        for (Block block: list){
            contens.add(block.transaction.content);
        }
//        List<String> contens = Notebook.getContens();
        vi.setList(contens);
        String hash = new MerkleTree(contens).getRoot().getHash();
        vi.setHash(hash);
        return vi;
    }

}
