package com.example.demo.bean;

import com.example.demo.merkle.MerkleTree;
import com.example.demo.merkle.TreeNode;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Block implements Serializable {
    private static final long serialVersionUID=1L;

    public int id;              //区块索引号（区块高度）
    public String content;      //内容
    public String hash;         //区块的哈希
//    public int nonce;           // 工作量证明
    public String preHash;      //上一个区块的hash

    public String timeStamp;      //时间戳

    public String rootHash;       //merkleTree根节点的哈希

    public String getRootHash() {
        List<Block> list = Notebook.getInstance().getList();
        ArrayList<String> contens = new ArrayList<>();
        for (Block block:list){
            contens.add(block.content);
        }
        contens.add(this.content);
        return new MerkleTree(contens).getRoot().getHash();
    }


    public Block() {
    }

    public Block(int id, String content, String hash, String preHash) {
        this.id = id;
        this.content = content;
        this.hash = hash;
        this.preHash = preHash;
        this.timeStamp=getTime();
        this.rootHash=getRootHash();
    }

    private String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sd = sdf.format(new Date().getTime());
        return sd;
    }

}
