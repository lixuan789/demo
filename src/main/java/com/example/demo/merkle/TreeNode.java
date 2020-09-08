package com.example.demo.merkle;
/**
 * @Auther: GK
 * @Date: 2020/7/14 15:09
 * @Description:
 */

import com.example.demo.crypto.SHACoder;

/**
 * @program: SM2Java
 *
 * @description: 1
 *
 * @author: GaoKun
 *
 * @create: 2020-07-14 15:09
 **/
public class TreeNode {

    //二叉树左孩子
    private TreeNode left;
    //二叉树右孩子
    private TreeNode right;
    //二叉树中叶子节点中的数据
    private String data;
    //二叉树中叶子节点数据对应的哈希值
    private String hash;
    //节点名称
    private String name;

    public TreeNode(){

    }
    public TreeNode(String data){
        this.data = data;
        this.hash = SHACoder.encodeSHA256Hex(data);
        this.name = "[节点："+ data +"]";
    }

    public TreeNode getLeft() {
        return left;
    }

    public void setLeft(TreeNode left) {
        this.left = left;
    }

    public TreeNode getRight() {
        return right;
    }

    public void setRight(TreeNode right) {
        this.right = right;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
