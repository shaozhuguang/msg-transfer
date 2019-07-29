package com.farmer.x.communication.pow;

import com.farmer.x.communication.MessageExecutor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PowBooter {

    static final String LEDGER_NAME = "JDChain";

    static final String[] NODES = new String[]{"127.0.0.1:9090", "127.0.0.1:9091", "127.0.0.1:9092"};

    public static void main(String[] args) {

        List<LedgerChain> ledgerChains = new ArrayList<>();

        for (final String nodeAddr : NODES) {

            String localNode = nodeAddr;

            List<String> remotes = new ArrayList<>();

            for (final String node : NODES) {
                if (!node.equals(localNode)) {
                    remotes.add(node);
                }
            }

            String[] remoteNodes = new String[remotes.size()];
            // 使用单独线程
            ledgerChains.add(nodeStart(localNode, remotes.toArray(remoteNodes)));
        }

        // 定时检查
        check(ledgerChains);
    }

    private static LedgerChain nodeStart(final String localNode, String[] remotes) {
        // 创建一条链的对象
        LedgerChain ledgerChain = new LedgerChain(LEDGER_NAME + "_" + localNode);

        MessageExecutor ledgerExecutor = new Listener(ledgerChain);

        Speaker speaker = new Speaker(localNode, remotes, ledgerExecutor);

        // 启动speaker
        Executors.newSingleThreadExecutor().execute(speaker);

        Computer computer = new Computer(speaker, ledgerChain, localNode);
        // 启动computer
        Executors.newSingleThreadExecutor().execute(computer);

        return ledgerChain;
    }

    private static void check(List<LedgerChain> ledgerChains) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        // 每分钟检查一次
        executorService.scheduleAtFixedRate(() -> {

            LedgerChain firstChain = ledgerChains.get(0);

            String firstChainName = firstChain.getName();

            LinkedList<LedgerBlock> blocks = firstChain.getBlocks();

            for (LedgerChain otherChain : ledgerChains) {
                if (otherChain == firstChain) {
                    continue;
                }

                String otherChainName = otherChain.getName();

                // 判断哪个链区块更高
                LinkedList<LedgerBlock> otherBlocks = otherChain.getBlocks();

                // 开始判断
                int index = 0;
                while (index < blocks.size() && index < otherBlocks.size()) {
                    // 只检查已经成功的区块
                    LedgerBlock block = blocks.get(index),
                            otherBlock = otherBlocks.get(index);

                    if (block.getHash().equals(otherBlock.getHash()) &&
                            block.getHeight() == otherBlock.getHeight()) {
                        System.out.printf("[%s] <-> [%s]，检查区块高度[%s]成功，来源于[%s] \r\n",
                                firstChainName, otherChainName, block.getHeight(), block.getLocal());
                    } else {
                        System.err.printf("[%s] <-> [%s]，检查区块高度[%s]失败，区块Hash为[%s][%s] \r\n",
                                firstChainName, otherChainName,
                                index,
                                block.getHash(), otherBlock.getHash());
                    }
                    index++;
                }
            }

        }, 1, 1, TimeUnit.MINUTES);
    }
}
