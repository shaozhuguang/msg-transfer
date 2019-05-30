package com.farmer.x.communication.connection;


import com.farmer.x.communication.callback.CallBackLauncher;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步执行器接口
 * @author shaozhuguang
 * @create 2019/4/17
 * @since 1.0.0
 */

public interface AsyncExecutor {

    /**
     * 初始化运行线程
     * @return
     */
    ThreadPoolExecutor initRunThread();

    /**
     * 启动完成后返回调度执行器
     * @return
     * @throws InterruptedException
     */
    CallBackLauncher waitBooted() throws InterruptedException;
}