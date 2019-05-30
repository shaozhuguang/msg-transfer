package com.farmer.x.communication.connection.handler;

import com.farmer.x.communication.message.HeartBeatMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 心跳发送触发器
 * @author shaozhuguang
 * @create 2019/4/15
 * @since 1.0.0
 */
@ChannelHandler.Sharable
public class HeartBeatSenderTrigger extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeartBeatSenderTrigger.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        // 心跳事件（状态空闲事件）
        if (evt instanceof IdleStateEvent) {
            IdleState idleState = ((IdleStateEvent) evt).state();
            if (idleState.equals(IdleState.READER_IDLE)) {
                // Sender读超时，表示在指定时间内未收到Receiver的应答
                // 此时关闭连接，自动调用重连机制，进行重连操作
                LOGGER.debug("Long Time UnReceive HeartBeat Response, Close Connection !!!");
                ctx.close();
            } else if (idleState == IdleState.WRITER_IDLE) {
                // Sender写超时，表示很长时间没有发送消息了，需要发送消息至Receiver
                LOGGER.debug("Read TimeOut Trigger, Send HeartBeat Request !!!");
                HeartBeatMessage.write(ctx);
            }
            // TODO 还有一种情况是读写超时，该情况暂不处理
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}