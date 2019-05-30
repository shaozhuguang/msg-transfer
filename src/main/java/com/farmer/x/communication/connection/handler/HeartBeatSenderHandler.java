package com.farmer.x.communication.connection.handler;

import com.farmer.x.communication.message.HeartBeatMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 心跳发送Handler
 * @author shaozhuguang
 * @create 2019/4/15
 * @since 1.0.0
 */
@ChannelHandler.Sharable
public class HeartBeatSenderHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeartBeatSenderHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 判断收到的消息
        if (HeartBeatMessage.isHeartBeat(msg)) {
            // 假设收到的消息是字符串，并且是心跳消息，说明由服务端发送了心跳信息
            // 此处不需要进行消息反馈，只需要打印日志即可
            LOGGER.debug("Receive HeartBeat Response Message -> {}", msg.toString());
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 出现异常直接关闭连接
        LOGGER.error(cause.getMessage());
        ctx.close();
    }
}