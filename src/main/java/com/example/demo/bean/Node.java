package com.example.demo.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Node {

    private Integer id;

    private String ip;//ip地址

    private String port; //端口

    private Integer state; //在线状态，0为离线，1为在线

    private String hostName;//主机名字

    private Integer commit;//PBFT中进入commit状态的节点，0为不是，1为是

}
