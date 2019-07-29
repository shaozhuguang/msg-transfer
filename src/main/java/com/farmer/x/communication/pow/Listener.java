package com.farmer.x.communication.pow;

import com.farmer.x.communication.MessageExecutor;
import com.farmer.x.communication.RemoteSession;

import java.nio.charset.StandardCharsets;

public class Listener implements MessageExecutor {

    private static final String SUCCESS = "SUCCESS";

    private static final String FAIL = "FAIL";

    private LedgerChain ledgerChain;

    public Listener(LedgerChain ledgerChain) {
        this.ledgerChain = ledgerChain;
    }

    @Override
    public byte[] receive(String key, byte[] data, RemoteSession session) {
        try {
            LedgerBlock ledgerBlock = LedgerBlock.convert(data);
            System.out.printf("接收到%s发送的区块，区块高度 = %s \r\n", ledgerBlock.getLocal(), ledgerBlock.getHeight());
            if (this.ledgerChain.addBlock(ledgerBlock)) {
                return SUCCESS.getBytes(StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            // 打印日志，不处理
            System.err.println(e);
        }
        return FAIL.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public REPLY replyType() {
        return REPLY.AUTO;
    }
}
