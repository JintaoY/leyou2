package com.leyou.common.utils;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程工具类
 */
public class ThreadUtils {

    // new  一个线程池    10个容量
    private static final ExecutorService ES = Executors.newFixedThreadPool(10);

    /**
     * 执行线程
     * @param runnable
     */
    public static void execute(Runnable runnable){
        ES.execute(runnable);
    }

}
