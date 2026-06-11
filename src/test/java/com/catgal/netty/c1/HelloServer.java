package com.catgal.netty.c1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

public class HelloServer {
    public static void main(String[] args) {
        // 创建并配置Netty服务端启动引导程序
        new ServerBootstrap()
                // 设置线程组：NioEventLoopGroup默认线程数为CPU核心数*2，处理连接和IO事件
                .group(new NioEventLoopGroup())
                // 指定使用NIO模式的ServerSocketChannel作为服务端通道
                .channel(NioServerSocketChannel.class)
                // 设置子通道（客户端连接）的处理器
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // 获取客户端的通道管道（Pipeline），用于添加处理器
                        ch.pipeline()
                                // 添加字符串解码器：将ByteBuf自动解码为字符串（默认UTF-8）
                                .addLast(new StringDecoder())
                                // 添加自定义的入站处理器
                                .addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        // 打印接收到的消息（此时msg已解码为String类型）
                                        System.out.println(msg);
                                        // 注意：未调用ctx.fireChannelRead(msg)，消息不会继续向后传递
                                    }
                                });
                    }
                })
                // 绑定指定端口并启动服务（异步操作，此处未阻塞等待）
                .bind(8080);
        // 问题：未添加await()或sync()，main线程可能立即退出导致服务终止（建议添加sync()阻塞）
    }
}
