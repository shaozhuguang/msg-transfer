package com.farmer.x.communication.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;

/**
 * 心跳消息
 * @author shaozhuguang
 * @create 2019/4/12
 * @since 1.0.0
 */

public class HeartBeatMessage implements IMessage {

    /**
     * 统一的心跳信息字符串
     */
    private static final String HEARTBEAT_STRING = "IAmHeartBeat";

    /**
     * 统一的心跳消息字符串对一个的ByteBuf
     */
    private static final ByteBuf HEARTBEAT_MESSAGE = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer(HEARTBEAT_STRING + "\r\n",
            CharsetUtil.UTF_8));

    /**
     * 将心跳消息写入Ctx
     * @param ctx
     */
    public static final void write(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(HEARTBEAT_MESSAGE.duplicate());
    }

    /**
     * 判断接收的消息是否为心跳消息
     *
     * @param msg
     * @return
     */
    public static final boolean isHeartBeat(Object msg) {
        return isHeartBeat(msg.toString());
    }

    /**
     * 判断接收的消息是否为心跳消息
     *
     * @param msg
     * @return
     */
    public static final boolean isHeartBeat(String msg) {
        if (HEARTBEAT_STRING.equals(msg)) {
            return true;
        }
        return false;
    }

    @Override
    public String toTransfer() {
        return HEARTBEAT_STRING;
    }

    @Override
    public ByteBuf toTransferByteBuf() {
        return HEARTBEAT_MESSAGE;
    }
}