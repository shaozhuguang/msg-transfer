package com.farmer.x.communication.connection.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 心跳接收触发器
 * @author shaozhuguang
 * @create 2019/4/15
 * @since 1.0.0
 */
@ChannelHandler.Sharable
public class HeartBeatReceiverTrigger extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeartBeatReceiverTrigger.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 服务端只会接收心跳数据后应答，而不会主动应答
        if (evt instanceof IdleStateEvent) {
            IdleState idleState = ((IdleStateEvent) evt).state();
            // 读请求超时表示很久没有收到客户端请求
            if (idleState.equals(IdleState.READER_IDLE)) {
                // 长时间未收到客户端请求，则关闭连接
                LOGGER.debug("Long Time UnReceive HeartBeat Request, Close Connection !!!");
                ctx.close();
            }
        } else {
            // 非空闲状态事件，由其他Handler处理
            super.userEventTriggered(ctx, evt);
        }
    }
}