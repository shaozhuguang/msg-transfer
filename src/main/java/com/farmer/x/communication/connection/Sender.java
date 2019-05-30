package com.farmer.x.communication.connection;

import com.farmer.x.communication.connection.handler.*;
import com.farmer.x.communication.connection.handler.HeartBeatSenderHandler;
import com.farmer.x.communication.connection.handler.HeartBeatSenderTrigger;
import com.farmer.x.communication.connection.handler.SenderHandler;
import com.farmer.x.communication.connection.handler.WatchDogHandler;
import com.farmer.x.communication.message.IMessage;
import com.farmer.x.communication.message.SessionMessage;
import com.farmer.x.communication.node.LocalNode;
import com.farmer.x.communication.node.RemoteNode;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 发送器
 *
 * @author shaozhuguang
 * @create 2019/4/11
 * @since 1.0.0
 * @date 2019-04-18 15:08
 */
public class Sender extends AbstractAsyncExecutor implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);

    private final EventLoopGroup loopGroup = new NioEventLoopGroup();

    private Bootstrap bootstrap;

    private ChannelFuture channelFuture;

    /**
     * 当前节点的SessionMessage
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


    /**
     * 监听Handler（重连Handler）
     */
    private WatchDogHandler watchDogHandler;

    public Sender(LocalNode localNode, RemoteNode remoteNode, SessionMessage sessionMessage) {
        init(localNode, remoteNode, sessionMessage);
    }

    /**
     * 连接
     */
    public void connect() {
        watchDogHandler = new WatchDogHandler(this.remoteNode.getHostName(), this.remoteNode.getPort(), bootstrap);

        ChannelHandlers frontChannelHandlers = new ChannelHandlers()
                .addHandler(watchDogHandler);

        ChannelHandlers afterChannelHandlers = new ChannelHandlers()
                .addHandler(new StringDecoder())
                .addHandler(new HeartBeatSenderTrigger())
                .addHandler(new HeartBeatSenderHandler())
                .addHandler(new SenderHandler(this.localNode, this.remoteNode, this.sessionMessage));

        // 初始化watchDogHandler
        watchDogHandler.init(frontChannelHandlers.toArray(), afterChannelHandlers.toArray());

        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                          .addLast(frontChannelHandlers.toArray())
                          .addLast(new IdleStateHandler(10, 4, 0, TimeUnit.SECONDS))
                          .addLast(new LineBasedFrameDecoder(1024))
                          .addLast(afterChannelHandlers.toArray());
                    }
                });

        ThreadPoolExecutor runThread = initRunThread();

        // 单独线程进行连接，防止当前调用线程阻塞
        runThread.execute(() -> {
            try {
                // 发起连接请求
                channelFuture = bootstrap.connect(this.remoteNode.getHostName(), this.remoteNode.getPort()).sync();
                boolean isStartSuccess = channelFuture.isSuccess();
                if (isStartSuccess) {
                    // 启动成功
                    // 设置ChannelFuture对象，以便于发送的连接状态处理
                    watchDogHandler.initChannelFuture(channelFuture);
                    // 释放等待
                    super.callBackLauncher.bootSuccess();
                    // 等待客户端关闭连接
                    channelFuture.channel().closeFuture().sync();
                } else {
                    LOGGER.error("Sender start fail {} !!!", channelFuture.cause().getMessage());
                    // 启动失败
                    throw new Exception("Sender start fail :" + channelFuture.cause().getMessage() + " !!!");
                }
            } catch (Exception e) {
                super.callBackLauncher.bootFail(e);
            } finally {
                close();
            }
        });
    }

    /**
     * 初始化相关配置
     *
     * @param localNode
     *     本地节点
     * @param remoteNode
     *     远端节点
     * @param sessionMessage
     *     本地节点连接到远端节点后发送的SessionMessage
     */
    private void init(LocalNode localNode, RemoteNode remoteNode, SessionMessage sessionMessage) {
        this.localNode = localNode;
        this.remoteNode = remoteNode;

        this.sessionMessage = sessionMessage;

        this.bootstrap = new Bootstrap().group(loopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true);
    }

    @Override
    public String threadNameFormat() {
        return "sender-pool-%d";
    }

    /**
     * 发送消息
     *
     * @param message
     *     消息统一接口
     */
    public void send(IMessage message) {

        Channel channel = watchDogHandler.channelFuture().channel();

        channel.eventLoop().execute(() -> channel.writeAndFlush(message.toTransferByteBuf()));
    }

    @Override
    public void close() {
        // 因为要重连，需要仍然需要使用该LoopGroup，因此不能关闭
//        loopGroup.shutdownGracefully();
    }

    /**
     * ChannelHandler集合管理类
     */
    public static class ChannelHandlers {

        private List<ChannelHandler> channelHandlers = new ArrayList<>();

        /**
         * 添加指定的ChannelHandler
         *
         * @param channelHandler
         *     需要加入的ChannelHandler
         * @return
         */
        public ChannelHandlers addHandler(ChannelHandler channelHandler) {
            channelHandlers.add(channelHandler);
            return this;
        }

        /**
         * List集合转换为数组
         *
         * @return
         */
        public ChannelHandler[] toArray() {
            ChannelHandler[] channelHandlerArray = new ChannelHandler[channelHandlers.size()];
            return channelHandlers.toArray(channelHandlerArray);
        }
    }
}