package com.farmer.x.communication.message;

/**
 * 负载消息
 * 该接口用于应用实现
 * @author shaozhuguang
 * @create 2019/4/11
 * @since 1.0.0
 */

public interface LoadMessage {

    /**
     * 将负载消息转换为字节数组
     * @return
     */
    byte[] toBytes();
}