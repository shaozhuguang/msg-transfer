package com.farmer.x.communication.pow;

import java.util.LinkedList;

public class LedgerChain {

    private String name;

    private LinkedList<LedgerBlock> blocks = new LinkedList<>();

    public LedgerChain(String name) {
        this.name = name;
    }

    public synchronized boolean addBlock(LedgerBlock ledgerBlock) {

        if (blocks.isEmpty()) {
            System.out.printf("[%s]开始添加创世区块至链中...... \r\n", name);
            // 创世区块
            if (ledgerBlock.getHeight() == 0) {
                // 创世区块高度为0
                blocks.addLast(ledgerBlock);
                System.out.printf("[%s]添加创世区块成功，来源于[%s] \r\n", name, ledgerBlock.getLocal());
                return true;
            }
        } else {
            LedgerBlock lastBlock = blocks.getLast();

            if (ledgerBlock.getHeight() == lastBlock.getHeight() + 1) {
                System.out.printf("[%s]开始添加区块至链中...... \r\n", name);
                blocks.addLast(ledgerBlock);
                System.out.printf("[%s]添加区块成功，来源于[%s]，区块高度[%s] \r\n", name, ledgerBlock.getLocal(), ledgerBlock.getHeight());
                return true;
            } else {
                // TODO 暂不处理，理论上应该放入缓冲池
                System.err.printf("[%s]添加该区块失败，来源于[%s]，区块高度[%s] \r\n", name, ledgerBlock.getLocal(), ledgerBlock.getHeight());
                return false;
            }
        }

        return false;
    }

    public LinkedList<LedgerBlock> getBlocks() {
        return blocks;
    }

    public String getName() {
        return name;
    }

    public LedgerBlock lastLedgerBlock() {
        if (blocks.isEmpty()) {
            return null;
        }
        return blocks.getLast();
    }
}
