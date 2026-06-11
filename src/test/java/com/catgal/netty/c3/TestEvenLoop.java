package com.catgal.netty.c3;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.TimeUnit;

public class TestEvenLoop {
    public static void main(String[] args) {
        // 1. 创建事件循环组
        EventLoopGroup group = new NioEventLoopGroup(); //处理io 事件 普通任务 定时任务
        //异步执行普通任务
        group.next().submit(() -> {
            try {
                System.out.println("普通任务开始");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("ok");
            System.out.println("普通任务结束");
        });

        //执行定时任务
        group.next().scheduleAtFixedRate(() -> {
            try {
                System.out.println("定时任务");
                System.out.println(System.currentTimeMillis() + ": ok");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 10,1, TimeUnit.SECONDS);

    }
}
