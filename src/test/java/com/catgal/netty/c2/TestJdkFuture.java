package com.catgal.netty.c2;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.*;


public class TestJdkFuture {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(2);

        Future<Integer> futures = service.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                System.out.println("执行计算");
                Thread.sleep(1000);
                return 50;
            }
        });

        System.out.println("等待结果");
        System.out.println("结果是"+futures.get()); //get()是阻塞

        for (int i =0; i < 100; i++) {
            System.out.println("真红妈妈是我老婆");
        }



    }
}
