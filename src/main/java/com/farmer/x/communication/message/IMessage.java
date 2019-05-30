package com.farmer.x.communication.message;

import io.netty.buffer.ByteBuf;

/**
 * 消息接口
 * @author shaozhuguang
 * @create 2019/4/16
 * @since 1.0.0
 */

public interface IMessage {

    /**
     * 消息转换为字符串
     * @return
     */
    String toTransfer();

    /**
     * 消息转换为ByteBuf
     * @return
     */
    ByteBuf toTransferByteBuf();
}