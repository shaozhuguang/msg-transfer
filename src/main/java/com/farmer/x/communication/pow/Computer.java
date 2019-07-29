package com.farmer.x.communication.pow;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Random;

public class Computer implements Runnable, AutoCloseable {

    private boolean work = true;

    private Random sleepRandom;

    private Random hashRandom;

    private String localNode;

    private Speaker speaker;

    private LedgerChain ledgerChain;

    public Computer(Speaker speaker, LedgerChain ledgerChain, String localNode) {
        this.speaker = speaker;
        this.ledgerChain = ledgerChain;
        this.localNode = localNode;
    }

    @Override
    public void run() {

        try {
            // 延时5秒再进行计算，等待网络状态正常
            Thread.sleep(5000);
        } catch (Exception e) {
            System.err.println(e);
        }
        // 设置休眠随机数
        sleepRandom = new Random(Thread.currentThread().getId() + System.nanoTime());
        // 设置填充随机数
        hashRandom = new Random(Thread.currentThread().getId() + System.nanoTime());

        System.out.printf("%s 开始启动计算模块...... \r\n", localNode);
        while (work) {
            // 用try包裹，无论何种异常都不会退出循环
            try {
                // 获取前置区块
                LedgerBlock lastLedgerBlock = ledgerChain.lastLedgerBlock();

                LedgerBlock computeBlock;

                int nextBlockHeight = 0;

                if (lastLedgerBlock == null) {
                    // 处理创世区块
                    computeBlock = computer("", nextBlockHeight);
                } else {
                    nextBlockHeight = lastLedgerBlock.getHeight() + 1;
                    computeBlock = computer(lastLedgerBlock.getHash(), nextBlockHeight);
                }

                if (computeBlock != null) {
                    System.out.printf("%s 计算得到符合的区块，区块高度 = %s \r\n", localNode, computeBlock.getHeight());
                    if (ledgerChain.addBlock(computeBlock)) {
                        // 加入成功的话，再选择将其广播,不关心是否广播成功
                        System.out.printf("%s 广播区块，区块高度 = %s \r\n", localNode, computeBlock.getHeight());
                        speaker.broadcast(computeBlock);
                    }
                } else {
                    System.out.printf("%s 计算未得到符合的区块，区块高度 = %s，继续计算！ \r\n", localNode, nextBlockHeight);
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    public void setWork(boolean work) {
        this.work = work;
    }

    private LedgerBlock computer(String preHash, int blockHeight) {

        // 随机休眠1~5秒，再进行计算
        int sleepSecond = 1000 + sleepRandom.nextInt(4000);
        try {
            Thread.sleep(sleepSecond);
            // 生成随机数
            byte[] randomBytes = new byte[256];

            hashRandom.nextBytes(randomBytes);

            String randomHash = DigestUtils.sha256Hex(randomBytes);

            // 判断第一个字符是不是a-f
            if (rule(randomHash)) {
                return new LedgerBlock(preHash, blockHeight, localNode);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return null;
    }


    @Override
    public void close() throws Exception {
        setWork(false);
    }

    private boolean rule(String hash) {
        if (hash.charAt(0) < 52) {
            return true;
        }
        return false;
    }
}
