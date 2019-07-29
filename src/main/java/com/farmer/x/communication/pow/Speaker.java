package com.farmer.x.communication.pow;

import com.farmer.x.communication.MessageExecutor;
import com.farmer.x.communication.RemoteSession;
import com.farmer.x.communication.manager.RemoteSessionManager;
import com.farmer.x.communication.message.LoadMessage;
import com.farmer.x.communication.node.LocalNode;
import com.farmer.x.communication.node.RemoteNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Speaker implements Runnable, AutoCloseable {

    private boolean work = true;

    private ExecutorService broadcastThreadPool;

    private RemoteSessionManager client;

    private String localNode;

    private String[] remoteNodes;

    private List<RemoteSession> remoteSessions = new ArrayList<>();

    private BlockingQueue<LedgerBlock> ledgerBlocks = new LinkedBlockingQueue<>();

    public Speaker(String localNode, String[] remoteNodes, MessageExecutor localMessageExecutor) {
        this.localNode = localNode;
        client = new RemoteSessionManager(localNode(localNode, localMessageExecutor));
        this.remoteNodes = remoteNodes;
    }

    @Override
    public void run() {
        try {
            // 延时3s，等待其他节点正常
            Thread.sleep(3000);
        } catch (Exception e) {
            System.err.println(e);
        }

        // 连接远端节点
        connectRemoteSessions(remoteNodes);

        // 初始化线程池
        broadcastThreadPool = Executors.newFixedThreadPool(remoteNodes.length);
        while (work) {
            try {
                LedgerBlock ledgerBlock = ledgerBlocks.take();

                System.out.printf("%s节点从队列中获取广播区块，区块高度 = %s \r\n",
                        localNode, ledgerBlock.getHeight());
                // 广播当前节点至其他节点，暂不关心该节点是否写入账本成功
                for (RemoteSession remoteSession : remoteSessions) {
                    Runnable runnable = new BroadcastRunner(ledgerBlock, remoteSession);
                    // 线程池广播，防止阻塞主线程
                    broadcastThreadPool.execute(runnable);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private LocalNode localNode(String localNode, MessageExecutor localMessageExecutor) {
        String[] localNodes = localNode.split(":");
        if (localNodes.length == 2) {
            String localIpAddr = localNodes[0];

            int localPort = Integer.parseInt(localNodes[1]);

            return new LocalNode(localIpAddr, localPort, localMessageExecutor);
        } else {
            throw new IllegalStateException("Local Node Config is Error !!!");
        }
    }

    private void connectRemoteSessions(String[] remoteNodes) {

        for (String remoteNode : remoteNodes) {

            String[] remotes = remoteNode.split(":");

            if (remotes.length == 2) {

                String remoteIpAddr = remotes[0];

                int remotePort = Integer.parseInt(remotes[1]);

                RemoteNode remote = new RemoteNode(remoteIpAddr, remotePort);

                RemoteSession remoteSession = client.newSession(remote).init();
                System.out.printf("%s 连接到节点 %s，成功 \r\n", localNode, remoteNode);
                remoteSessions.add(remoteSession);

            } else {
                throw new IllegalStateException("Remote Node Config is Error !!!");
            }
        }
    }

    public void broadcast(LedgerBlock ledgerBlock) {
        try {
            ledgerBlocks.put(ledgerBlock);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setWork(boolean work) {
        this.work = work;
    }

    @Override
    public void close() throws Exception {
        setWork(false);
        // 关闭连接
        if (remoteSessions != null) {
            for (RemoteSession remoteSession : remoteSessions) {
                try {
                    remoteSession.closeAll();
                } catch (Exception e) {
                    System.err.println(e);
                }
            }
        }
        // 关闭线程池
        broadcastThreadPool.shutdown();
    }

    private static class BroadcastRunner implements Runnable {

        private LoadMessage loadMessage;

        private RemoteSession remoteSession;

        public BroadcastRunner(LoadMessage loadMessage, RemoteSession remoteSession) {
            this.loadMessage = loadMessage;
            this.remoteSession = remoteSession;
        }

        @Override
        public void run() {
            try {
                remoteSession.request(loadMessage);
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }
}
