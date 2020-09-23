package com.example.demo.controller;

import com.alibaba.fastjson.JSON;
import com.example.demo.bean.Block;
import com.example.demo.bean.Notebook;
import com.example.demo.bean.SearchVo;
import com.example.demo.bean.Transaction;
import com.example.result.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class SearchController {

    private final static Logger logger = LoggerFactory.getLogger(BlockController.class);

    private Notebook notebook = Notebook.getInstance();

    /**
     * 根据用户名、区块高度、hash搜索区块
     * @param content
     * @return
     */
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public R addGenesis(@RequestBody String content) {

        Map map = JSON.parseObject(content, Map.class);
        String search = (String) map.get("content"); //搜索条件
        System.out.println(search);
        List<Block> blockList = notebook.getList();  //本地json文件

        if (search==null||search.length()==0){
            return R.error().message("请输入查询条件").data("list",blockList);
        }

        if (blockList.size()==0){
            return R.error().message("区块链为空");
        }

        ArrayList<Block> res = new ArrayList<>(); //返回结果
        int num = getNum(search);
        if (num>0&&num<=blockList.size()){//根据id查询
            res.add(blockList.get(num-1));
            return R.ok().message("查询成功").data("list",res);
        }

        for (Block block:blockList){
            String hash = block.hash;
            String userName = block.transaction.userName;

            if (search.equals(hash)){
                res.add(block);
                return R.ok().message("查询成功").data("list",res);
            }

            if (search.equals(userName)){
                res.add(block);
            }

        }
        if (res.size()>0){
            return R.ok().message("查询成功").data("list",res);
        }else {
            return R.ok().message("找不到");
        }

    }

    /**
     * 获取区块详情
     * @param id
     * @return
     */
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public R detail(Integer id){
        System.out.println(id);
        List<Block> blockList = notebook.getList();
        Block block = blockList.get(id-1);
        ArrayList<Block> res = new ArrayList<>();
        res.add(block);
        return R.ok().message("区块详情").data("item",res);
    }

    private int getNum(String str){
        int res=0;
        try {
            res = Integer.parseInt(str);
        }catch (NumberFormatException e){
            res=-1;
        }finally {
            return res;
        }
    }

}
