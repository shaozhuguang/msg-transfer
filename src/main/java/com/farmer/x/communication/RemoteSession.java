package com.farmer.x.communication;

import com.farmer.x.communication.callback.CallBackBarrier;
import com.farmer.x.communication.callback.CallBackDataListener;
import com.farmer.x.communication.connection.Connection;
import com.farmer.x.communication.connection.listener.ReplyListener;
import com.farmer.x.communication.message.LoadMessage;
import com.farmer.x.communication.node.RemoteNode;
import org.apache.commons.codec.binary.Hex;

import java.util.concurrent.TimeUnit;


/**
 * 远端Session
 *
 * @author shaozhuguang
 * @create 2019/4/11
 * @since 1.0.0
 */

public class RemoteSession {

    /**
     * 本地节点ID
     */
    private String localId;

    /**
     * 远端节点
     */
    private RemoteNode remoteNode;

    /**
     * 远端连接
     */
    private Connection connection;

    /**
     * 对应远端节点消息的处理器
     * 该处理器若为NULL，则使用当前节点默认处理器
     */
    private MessageExecutor messageExecutor;

    /**
     * 初始化标识
     */
    private boolean isInit;

    /**
     * 构造器
     * @param localId
     *     本地节点ID
     * @param connection
     *     对应连接
     */
    public RemoteSession(String localId, Connection connection) {
        this(localId, connection, null);
    }

    /**
     * 构造器
     * @param localId
     *     本地节点ID
     * @param connection
     *     对应连接
     * @param messageExecutor
     *     对应远端消息处理器
     */
    public RemoteSession(String localId, Connection connection, MessageExecutor messageExecutor) {
        this.localId = localId;
        this.connection = connection;
        this.messageExecutor = messageExecutor;
        this.remoteNode = connection.remoteNode();
    }

    public void init() {
        connection.initSession(this);
        isInit = true;
    }

    public void initExecutor(MessageExecutor messageExecutor) {
        this.messageExecutor = messageExecutor;
    }

    /**
     * 同步请求
     * 该请求会阻塞原线程
     *
     * @param loadMessage
     *     要请求的负载消息
     * @return
     *     应答，直到有消息应答或出现异常
     * @throws Exception
     */
    public byte[] request(LoadMessage loadMessage) throws Exception {
        checkInit();
        return this.connection.request(this.localId, loadMessage, null).getCallBackData();
    }

    /**
     * 同步请求
     * 该请求会阻塞原线程
     *
     * @param loadMessage
     *     要请求的负载消息
     * @param time
     *     请求的最长等待时间
     * @param timeUnit
     *     请求的最长等待单位
     * @return
     *     应答，直到有消息或时间截止或出现异常
     * @throws Exception
     */
    public byte[] request(LoadMessage loadMessage, long time, TimeUnit timeUnit) throws Exception {
        checkInit();
        return this.connection.request(this.localId, loadMessage, null).getCallBackData(time, timeUnit);
    }

    /**
     * 自定义应答处理器
     * 该请求不会阻塞原线程
     *
     * @param loadMessage
     *     要请求的负载消息
     * @param replyListener
     *     自定义接收应答处理器
     * @return
     *     消息的Key（该Key会绑定唯一的ReplyListener，调用 {@link RemoteSession#removeReplyListener(String)} 使用）
     */
    public String request(LoadMessage loadMessage, ReplyListener replyListener) {
        checkInit();
        return this.connection.request(this.localId, replyListener, loadMessage);
    }

    /**
     * 异步请求
     * 不会阻塞调用线程
     *
     * @param loadMessage
     *     要发送的负载消息
     * @return
     *     应答，需要调用者从Listener中获取结果
     */
    public CallBackDataListener asyncRequest(LoadMessage loadMessage) {
        return asyncRequest(loadMessage, null);
    }

    /**
     * 异步请求
     * 不会阻塞调用线程
     *
     * @param loadMessage
     *     要请求的负载消息
     * @param callBackBarrier
     *     回调栅栏（用于多个请求时进行统一阻拦）
     * @return
     *     应答，需要调用者从Listener中获取结果
     */
    public CallBackDataListener asyncRequest(LoadMessage loadMessage, CallBackBarrier callBackBarrier) {
        checkInit();
        return this.connection.request(this.localId, loadMessage, callBackBarrier);
    }

    /**
     * 应答
     *
     * @param key
     *     请求消息的Key
     * @param loadMessage
     *     需要应答的负载消息
     */
    public void reply(String key, LoadMessage loadMessage) {
        this.connection.reply(this.localId, key, loadMessage);
    }

    /**
     * 移除应答监听器
     *
     * @param listenerKey
     *     应答监听器的Key，参考：{@link RemoteSession#request(LoadMessage, ReplyListener)}
     */
    public void removeReplyListener(String listenerKey) {
        this.connection.removeReplyListener(listenerKey);
    }


    public void closeAll() {
        this.connection.closeAll();
    }

    public void closeReceiver() {
        this.connection.closeReceiver();
    }

    public void closeSender() {
        this.connection.closeSender();
    }

    /**
     * 返回本地节点ID
     *
     * @return
     */
    public String localId() {
        return localId;
    }


    public boolean isInit() {
        return isInit;
    }

    /**
     * 返回远端对应的SessionID
     *
     * @return
     */
    public String remoteSessionId() {
        return Hex.encodeHexString(remoteNode.toString().getBytes());
    }

    /**
     * 返回远端对应执行器
     *
     * @return
     */
    public MessageExecutor messageExecutor() {
        return this.messageExecutor;
    }

    /**
     * 返回对应远端节点
     *
     * @return
     */
    public RemoteNode remoteNode() {
        return remoteNode;
    }

    private void checkInit() {
        if (!isInit) {
            throw new IllegalStateException("Please Init State First !!!");
        }
    }
}