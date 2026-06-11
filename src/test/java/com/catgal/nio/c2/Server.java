package com.catgal.nio.c1.c2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Server {

    private static void split(ByteBuffer source) {
        //简单的枚举算法 本质是滑动窗口s
        source.flip();
        for (int i = 0; i< source.limit(); i++) {
            //找到一条完整消息
            if (source.get(i) == '\n') {
                int len = i - source.position() + 1;
                ByteBuffer buffer = ByteBuffer.allocate(len);
                for (int j=0; j<len; j++) {
                    buffer.put(source.get());
                }
            }
        }
        source.compact();


    }

    public static void main(String[] args) throws IOException {

        Selector selector = Selector.open();
        //使用nio理解阻塞模式
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        //1. 创建服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false); //设置为非阻塞 影响的是accept (客户端的监听)
        ssc.bind(new InetSocketAddress(8081));
        //2.建立 selector与 channel的关系
        SelectionKey sscKey = ssc.register(selector, 0, null);
        sscKey.interestOps(SelectionKey.OP_ACCEPT);


        List<SocketChannel> channels = new ArrayList<SocketChannel>();
        while (true) {
            /*System.out.println("connecting");
            SocketChannel accept = ssc.accept();
            accept.configureBlocking(false);//设置为非阻塞 影响的是channel的read方法 非阻塞 会无意义的空转
            System.out.println("accept");
            channels.add(accept);
            for (SocketChannel channel : channels) {
                channel.read(buffer);
                buffer.flip();
                buffer.clear();*/

            selector.select();
            //这个是事件发生后加进来的
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                //每次调用要删key 防止不对
                iterator.remove();
                if (key.isAcceptable()) {
                    System.out.println("key:"+key);
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel client = server.accept();
                    ByteBuffer scBuffer = ByteBuffer.allocate(16);
                    //将一个byteBuffer 关联到selector的附件 延长生命周期 针对性的局部
                    SelectionKey scKey = client.register(selector, 0, scBuffer);
                    client.configureBlocking(false);
                    scKey.interestOps(SelectionKey.OP_READ);
                    System.out.println(client);
                } else if (key.isReadable()) {
                    try {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer scBuffer = (ByteBuffer) key.attachment();
                        int read = client.read(scBuffer);
                        if (read == -1){
                            //正常断开 也要取消 防止ssc空转
                            key.cancel();
                        } else {
                            split(scBuffer);
                            if (scBuffer.position() == scBuffer.limit()) {
                                //说明 读的容量到达上限
                                ByteBuffer newByteBuffer = ByteBuffer.allocate(scBuffer.capacity() * 2);
                                scBuffer.flip();
                                newByteBuffer.put(scBuffer);
                                key.attach(newByteBuffer);
                            }
                        }

                    } catch (IOException e) {
                        //异常导致客户端断开 需要canceL
                        e.printStackTrace();
                        key.cancel();
                    }

                }
            }
        }
    }



}
