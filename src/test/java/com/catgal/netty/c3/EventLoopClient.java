package com.catgal.netty.c3;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public class EventLoopClient {
    public static void main(String[] args) throws InterruptedException {
        // 1. 启动器
        ChannelFuture channelFuture = new Bootstrap()
                // 2. 添加 EventLoop
                .group(new NioEventLoopGroup())
                // 3. 添加客户端的 channel 的实现
                .channel(NioSocketChannel.class)
                // 4. 添加处理器
                .handler(new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    protected void initChannel(NioSocketChannel sc) throws Exception {
                        sc.pipeline().addLast(new StringEncoder());
                    }
                })
                .connect(new InetSocketAddress("localhost", 8080));

        //主线程同步阻塞
        /*channelFuture.sync();
        Channel channel = channelFuture.channel();
        channel.writeAndFlush("hello world");*/


        channelFuture.addListener(future -> {
            Channel channel = channelFuture.channel();
            channel.writeAndFlush("hello world");
        });





    }
}
