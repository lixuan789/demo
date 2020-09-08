package com.example.demo.merkle;/**
 * @Auther: GK
 * @Date: 2020/7/14 15:17
 * @Description:
 */


import com.example.demo.crypto.SHACoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @program: SM2Java
 *
 * @description: 1
 *
 * @author: GaoKun
 *
 * @create: 2020-07-14 15:17
 **/
public class MerkleTree {

    //节点列表
    private List<TreeNode> list;
    //根节点
    private TreeNode root;

    public MerkleTree(List<String> contents){
        createMerkleTree(contents);
    }

    private void createMerkleTree(List<String> contents){
        //输入为空则不进行任何处理
        if(contents == null || contents.size()==0){
            return;
        }
        list = new ArrayList<TreeNode>();
        //根据数据创建叶子节点
        List<TreeNode> leafList = createLeafList(contents);
        //创建父节点
        List<TreeNode> parents = createParentList(leafList);
        //循环创建各级父节点直至根节点
        while(parents.size() > 1){
            List<TreeNode> temp = createParentList(parents);
            list.addAll(temp);
            parents = temp;
        }
        root = parents.get(0);
    }

    //创建父节点列表
    private List<TreeNode> createParentList(List<TreeNode> leafList){
        List<TreeNode> parents = new ArrayList<TreeNode>();
        //空验证
        if(leafList == null || leafList.size() ==0){
            return parents;
        }
        int length = leafList.size();
        for(int i =0;i<length-1;i= i+2){
            TreeNode parent = createParentNode(leafList.get(i),leafList.get(i+1));
            parents.add(parent);
        }
        //奇数个节点的时候，单独处理最后一个节点
        if(length%2 != 0){
            TreeNode parent = createParentNode(leafList.get(length-1),null);
            parents.add(parent);
        }
        return parents;
    }
    //创建父节点
    private TreeNode createParentNode(TreeNode left,TreeNode right){
        TreeNode parent = new TreeNode();
        parent.setLeft(left);
        parent.setRight(right);
        //如果right为空，则父节点的hash值为left的hash值
        String hash = left.getHash();
        if(right != null){
            hash = SHACoder.encodeSHA256Hex(left.getHash() + right.getHash());
        }
        parent.setData(hash);
        parent.setHash(hash);
        if(right != null){
            parent.setName("(" + left.getName() + "和" + right.getName() + "的父节点)");
        }else{
            parent.setName("(继承节点{" + left.getName() + "}成为父节点)");
        }

        return parent;
    }
    //构建叶子节点列表
    private List<TreeNode> createLeafList(List<String> contents){
        List<TreeNode> leafList = new ArrayList<TreeNode>();
        //空验证
        if(contents == null || contents.size() ==0){
            return leafList;
        }
        for(String content : contents){
            TreeNode node = new TreeNode(content);
            leafList.add(node);
        }
        return leafList;
    }

    //遍历树
    public void traverseTreeNodes(){
        Collections.reverse(list);
        TreeNode root  = list.get(0);
        traverseTreeNodes(root);
    }
    public void traverseTreeNodes(TreeNode node){
        System.out.println(node.getName());
        if(node.getLeft() != null){
            traverseTreeNodes(node.getLeft());
        }
        if(node.getRight() != null){
            traverseTreeNodes(node.getRight());
        }
    }

    public List<TreeNode> getList() {
        if(list == null){
            return list;
        }
        Collections.reverse(list);
        return list;
    }

    public void setList(List<TreeNode> list) {
        this.list = list;
    }

    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }
}