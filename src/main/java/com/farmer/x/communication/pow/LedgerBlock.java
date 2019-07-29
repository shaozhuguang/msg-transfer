package com.farmer.x.communication.pow;

import com.alibaba.fastjson.JSON;
import com.farmer.x.communication.message.LoadMessage;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;

public class LedgerBlock implements LoadMessage {

    private String hash;

    private String preHash;

    private int height;

    // 描述该区块是由谁产生的
    private String local;

    public LedgerBlock() {
    }

    public LedgerBlock(String preHash, int height, String local) {
        this.preHash = preHash;
        this.height = height;
        this.local = local;
        initHash();
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPreHash() {
        return preHash;
    }

    public void setPreHash(String preHash) {
        this.preHash = preHash;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    private void initHash() {
        // 设置当前区块Hash
        hash = DigestUtils.sha256Hex(preHash + local + height);
    }

    @Override
    public byte[] toBytes() {
        // 使用JSON序列化
        String json = JSON.toJSONString(this);

        return json.getBytes(StandardCharsets.UTF_8);
    }

    public static LedgerBlock convert(byte[] bytes) throws Exception {

        return JSON.parseObject(
                new String(bytes, StandardCharsets.UTF_8), LedgerBlock.class);
    }
}
