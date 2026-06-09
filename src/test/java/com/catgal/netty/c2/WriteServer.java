package com.catgal.netty.c2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class WriteServer {
    public static void main(String[] args) throws IOException {
        //写优化分析
        ServerSocketChannel ssc = ServerSocketChannel.open();
        //设置非阻塞
        ssc.configureBlocking(false);

        Selector selector = Selector.open();
        SelectionKey sscKey = ssc.register(selector, SelectionKey.OP_ACCEPT);

        ssc.bind(new InetSocketAddress(8080));
        while (true) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isAcceptable()) {
                    //简化 ssc的key只有一个
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    SelectionKey scKey = sc.register(selector, SelectionKey.OP_READ);

                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 40000000; i++) {
                        sb.append("a");
                    }
                    ByteBuffer buffer = Charset.defaultCharset().encode(sb.toString());
                    //优化 防止写的时候缓冲区为空的时候 空转
                    /*while (buffer.hasRemaining()) {
                        int write = sc.write(buffer);
                        System.out.println(write);
                    }*/
                    if (buffer.hasRemaining()) {
                        //4. 关注可写事件
                        scKey.interestOps(scKey.interestOps() | SelectionKey.OP_WRITE);
                        //5. 挂入附件
                        scKey.attach(buffer);

                    }


                } else if (key.isWritable()) {
                    SocketChannel sc = (SocketChannel) key.channel();
                    sc.configureBlocking(false);
                    ByteBuffer buffer = (ByteBuffer)key.attachment();
                    int write = sc.write(buffer);
                    System.out.println("write : " + write);
                    if (!buffer.hasRemaining()) {
                        //清理buffer 附件
                        key.attach(null);
                    key.interestOps(key.interestOps() &~ SelectionKey.OP_WRITE);
                    }
                }
            }
        }
    }
}
