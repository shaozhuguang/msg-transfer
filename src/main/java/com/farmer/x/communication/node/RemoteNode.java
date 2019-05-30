package com.farmer.x.communication.node;

/**
 * 节点信息
 * @author shaozhuguang
 * @create 2019/4/11
 * @since 1.0.0
 * @date 2019-04-19 09:28
 */

public class RemoteNode {

    /**
     * 监听端口
     */
    private int port;

    /**
     * 当前节点域名
     */
    private String hostName;

    public RemoteNode(String hostName, int port) {
        this.port = port;
        this.hostName = hostName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * 通过hostName+port形式作为判断节点的唯一标识
     * @return
     */
    @Override
    public int hashCode() {
        return (hostName + ":" + port).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof RemoteNode) {
            RemoteNode other = (RemoteNode) obj;
            if (this.hashCode() == other.hashCode()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return this.hostName + ":" + this.port;
    }
}