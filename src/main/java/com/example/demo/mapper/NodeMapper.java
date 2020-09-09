package com.example.demo.mapper;

import com.example.demo.bean.Node;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NodeMapper {

    /**
     * 查询所有可用节点
     * @return
     */
    public List<Node> getAll();

    /**
     * 插入节点
     * @param node
     * @return
     */
    public int insertNode(Node node);

    /**
     * 通过ip和端口获取节点
     * @param ip
     * @param port
     * @return
     */
    public Node getNodeByIpAndPort(@Param("ip") String ip,@Param("port") String port);

    /**
     * 更新节点的状态
     * @param ip
     * @param port
     * @param state
     */
    void updateState(@Param("ip") String ip, @Param("port") String port,@Param("state") int state);

    /**
     * 获取在线的节点
     * @return
     */
    List<Node> getOnlineNode();


    /**
     * 更新节点的commit状态
     * @param ip
     * @param port
     * @param commit
     */
    void updateCommit(@Param("ip") String ip, @Param("port") String port, @Param("commit") int commit);

    /**
     * 获取commit状态为1的节点
     * @return
     */
    List<Node> getCommitNode();
}
