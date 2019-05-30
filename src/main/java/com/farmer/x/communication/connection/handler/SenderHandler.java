package com.farmer.x.communication.connection.handler;

import com.farmer.x.communication.message.SessionMessage;
import com.farmer.x.communication.node.LocalNode;
import com.farmer.x.communication.node.RemoteNode;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Sender对应Handler
 * @author shaozhuguang
 * @create 2019/4/16
 * @since 1.0.0
 */
@ChannelHandler.Sharable
public class SenderHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SenderHandler.class);

    /**
     * 本地session信息
     */
    private SessionMessage sessionMessage;

    /**
     * 本地节点
     */
    private LocalNode localNode;

    /**
     * 远端节点
     */
    private RemoteNode remoteNode;

    public SenderHandler(LocalNode localNode, RemoteNode remoteNode, SessionMessage sessionMessage) {
        this.localNode = localNode;
        this.remoteNode = remoteNode;
        this.sessionMessage = sessionMessage;
    }

    /**
     * 连接远端节点成功时触发
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 发送本机信息（包括IP、端口等）至对端
        LOGGER.debug("{} Connect {} Success, Send Local Node Information !!! \r\n", this.localNode, this.remoteNode);
        ctx.writeAndFlush(sessionMessage.toTransferByteBuf());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}