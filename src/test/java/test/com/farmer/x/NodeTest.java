package test.com.farmer.x;

import com.farmer.x.communication.MessageExecutor;
import com.farmer.x.communication.RemoteSession;
import com.farmer.x.communication.connection.listener.ReplyListener;
import com.farmer.x.communication.manager.RemoteSessionManager;
import com.farmer.x.communication.message.LoadMessage;
import com.farmer.x.communication.node.LocalNode;
import com.farmer.x.communication.node.RemoteNode;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NodeTest {

    private String ADDRESS = "127.0.0.1";

    int CLIENT_PORT = 10002;

    int SERVER_PORT = 10012;

    RemoteSessionManager server;

    RemoteSessionManager client;

    @Test
    public void sampleTest() {
        autoInit();
        // Client 连接远端
        RemoteNode remoteNode = new RemoteNode(ADDRESS, SERVER_PORT);
        RemoteSession remoteSession = client.newSession(remoteNode);
        remoteSession.init();

        try {
            byte[] response = remoteSession.request(() -> "I am client".getBytes());
            System.out.printf("Client receive reply = %s \r\n", new String(response));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void customReplyListener() {
        manualInit();
        ReplyListener printListener = new ReplyListener() {

            private int index = 0;

            @Override
            public void reply(byte[] reply) {
                System.out.printf("Client receive reply index = %s, content = %s \r\n", index, new String(reply));
                index++;
            }
        };

        // Client 连接远端
        RemoteNode remoteNode = new RemoteNode(ADDRESS, SERVER_PORT);
        RemoteSession remoteSession = client.newSession(remoteNode);
        remoteSession.init();

        try {
            String listenerKey = remoteSession.request(() -> "I am client".getBytes(), printListener);
            System.out.printf("Listener Key = %s \r\n", listenerKey);
            // 等待5秒后remove，这样应该只能接收到部分消息
            Thread.sleep(5000);
            remoteSession.removeReplyListener(listenerKey);
            // 防止线程提前退出
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void autoInit() {
        client = new RemoteSessionManager(new LocalNode(ADDRESS, CLIENT_PORT, new MyAutoMessageExecutor()));
        server = new RemoteSessionManager(new LocalNode(ADDRESS, SERVER_PORT, new MyAutoMessageExecutor()));
    }

    private void manualInit() {
        client = new RemoteSessionManager(new LocalNode(ADDRESS, CLIENT_PORT, new MyAutoMessageExecutor()));
        server = new RemoteSessionManager(new LocalNode(ADDRESS, SERVER_PORT, new MyManualMessageExecutor()));
    }

    public static class MyAutoMessageExecutor implements MessageExecutor {

        @Override
        public byte[] receive(String key, byte[] data, RemoteSession session) {
            System.out.printf("Server Receive Msg = %s \r\n", new String(data));
            return "This is Server Reply !!!".getBytes();
        }

        @Override
        public REPLY replyType() {
            return REPLY.AUTO;
        }
    }

    public static class MyManualMessageExecutor implements MessageExecutor {

        ExecutorService singleThread = Executors.newSingleThreadExecutor();

        @Override
        public byte[] receive(String key, byte[] data, RemoteSession session) {
            System.out.printf("Server Receive Msg = %s \r\n", new String(data));
            asyncReply(key, session);
            return null;
        }

        @Override
        public REPLY replyType() {
            return REPLY.MANUAL;
        }

        private void asyncReply(String key, RemoteSession session) {
            singleThread.execute(() -> {
                // 手动持续发送10次应答
                for (int i = 0; i < 10; i++) {
                    String reply = String.format("This is %s Server !!!", i);
                    session.reply(key, new MyLoadMessage(reply));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        public static class MyLoadMessage implements LoadMessage {

            private String msg;

            public MyLoadMessage(String msg) {
                this.msg = msg;
            }

            @Override
            public byte[] toBytes() {
                return msg.getBytes();
            }
        }
    }
}
