package com.example.demo.bean;

import lombok.Data;

/**
 * 查询区块对象
 * @author LiXxuan
 * @date 2020/9/17 13:52
 */
@Data
public class SearchVo {
    private Integer id;   //区块高度
    private String hash;  //区块hash
    private String username; //发布人
}
