package com.catgal.netty.c2;


import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.Callable;

public class TestNettyFuture {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();

        EventLoop loop = group.next();

        Future<Integer> future = loop.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                System.out.println("执行计算");
                Thread.sleep(10001);
                return 10;
            }
        });

        //异步等待
        future.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future) throws Exception {
                System.out.println("结果:"+future.getNow());

            }
        });

        for (int i =0; i < 100; i++) {
            System.out.println("真红妈妈是我老婆");
        }
    }
}
