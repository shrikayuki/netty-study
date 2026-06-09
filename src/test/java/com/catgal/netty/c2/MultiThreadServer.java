package com.catgal.netty.c2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MultiThreadServer {
    public static void main(String[] args) throws IOException {
        Thread.currentThread().setName("boss");
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        Selector selector = Selector.open();
        SelectionKey bossKey = ssc.register(selector, 0, null);
        bossKey.interestOps(SelectionKey.OP_ACCEPT);
        ssc.bind(new InetSocketAddress(8082));
        Worker worker = new Worker("危宇杰");
        while (true) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    //关联
                    System.out.println("before register...");
                    worker.register(sc);
                    System.out.println("after register...");
                }
            }
        }


    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
     static class Worker implements Runnable {
        private Thread thread;
        private Selector selector;
        private String workerName;
        private ConcurrentLinkedQueue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();

        private volatile boolean isRunning;

        public Worker(String workerName) {
            this.workerName = workerName;
        }

        public void register(SocketChannel sc) throws IOException {
            if (!isRunning) {
                synchronized (this) {
                    if (!isRunning) {
                        thread = new Thread(this);
                        thread.setName(workerName);
                        thread.start();
                        selector = Selector.open();
                    }
                }
            }
            taskQueue.add(() -> {
                try {
                    sc.register(selector, SelectionKey.OP_READ, null);
                } catch (ClosedChannelException e) {
                    throw new RuntimeException(e);
                }
            });
            selector.wakeup();

        }

        @Override
        public void run() {
            while (true) {
                try {
                    selector.select();
                    Runnable task = taskQueue.poll();
                    if (task != null) {
                        task.run();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                try {
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (key.isReadable()) {
                            SocketChannel sc = (SocketChannel) key.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            sc.read(buffer);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("空");
                }
            }
        }
    }
}
