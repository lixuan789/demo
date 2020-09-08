package com.example.demo.bean;


import com.example.demo.crypto.SM2;
import com.example.demo.crypto.SM2KeyPair;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.util.Date;

public class Transaction {

    // 付款方公钥
    public String senderPublicKey;
    // 收款方公钥
    //public String receiverPublicKey;
    // 内容
    public String content;
    // 数字签名
    public String signaturedData;

    //密文
//    public String encode;


    public long timeStamp;

    public String userName;//登录的用户名




    public Transaction() {
    }

    public Transaction( String content,String userName) {
        this.content = content;
        this.userName=userName;
        init();
    }

    private void init() {
        SM2 sm2 = new SM2();
        String mypub = this.getClass().getClassLoader().getResource("mypub.txt").getPath();//获取文件路径
        String mypri = this.getClass().getClassLoader().getResource("mypri.txt").getPath();//获取文件路径
        ECPoint publicKey = sm2.importPublicKey(mypub.substring(1));
        BigInteger privateKey = sm2.importPrivateKey(mypri.substring(1));
        byte buffer[] = publicKey.getEncoded(true);
        String strPublicKey = Base64.encodeBase64String(buffer);
        this.senderPublicKey=strPublicKey;
//        this.encode=SM2.getHexString(sm2.encrypt(content,publicKey));
        this.signaturedData=sm2.sign(content, "Heartbeats", new SM2KeyPair(publicKey, privateKey)).toString();
        this.timeStamp=new Date().getTime();
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSenderPublicKey() {
        return senderPublicKey;
    }

    public void setSenderPublicKey(String senderPublicKey) {
        this.senderPublicKey = senderPublicKey;
    }

    public String getSignaturedData() {
        return signaturedData;
    }

    public void setSignaturedData(String signaturedData) {
        this.signaturedData = signaturedData;
    }


    @Override
    public String toString() {
        return "Transaction{" +
                "senderPublicKey='" + senderPublicKey + '\'' +
                ", content='" + content + '\'' +
                ", signaturedData='" + signaturedData + '\'' +
                ", timeStamp=" + timeStamp +
                ", userName='" + userName + '\'' +
                '}';
    }

    // 校验交易的方法，非对称加密
    public boolean verify() {
        SM2 sm2 = new SM2();
        String mypub = this.getClass().getClassLoader().getResource("mypub.txt").getPath();//获取文件路径
        String mypri = this.getClass().getClassLoader().getResource("mypri.txt").getPath();//获取文件路径
        ECPoint publicKey = sm2.importPublicKey(mypub.substring(1));
        BigInteger privateKey = sm2.importPrivateKey(mypri.substring(1));
        String IDA = "Heartbeats";
        SM2.Signature signature = sm2.sign(content, IDA, new SM2KeyPair(publicKey, privateKey));
        return sm2.verify(content, signature, IDA, publicKey);
//        PublicKey publicKey = RSAUtils.getPublicKeyFromString("RSA", senderPublicKey);
//        return RSAUtils.verifyDataJS("SHA256withRSA", publicKey, content, signaturedData);
    }
}
