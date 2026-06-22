package com.catgal.netty.c3;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class EventLoopClient {
    public static void main(String[] args) throws InterruptedException {
        // 创建一个NIO事件循环组，用于处理网络事件（连接、读写等）
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();

        // 1. 创建客户端启动器（Bootstrap是Netty客户端启动引导类）
        ChannelFuture channelFuture = new Bootstrap()
                // 2. 添加事件循环组到启动器
                .group(eventExecutors)
                // 3. 指定客户端使用的Channel类型为NioSocketChannel（基于NIO的Socket通道）
                .channel(NioSocketChannel.class)
                // 4. 添加处理器（Handler）初始化器
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel sc) throws Exception {
                        // 向管道（Pipeline）中添加字符串编码器，用于将字符串编码为ByteBuf发送
                        sc.pipeline().addLast(new StringEncoder());
                    }
                })
                // 连接到指定的服务器地址和端口（异步连接，不会阻塞）
                .connect(new InetSocketAddress("localhost", 8080));

        // 方式一：主线程同步阻塞，等待连接建立完成
        // channelFuture.sync();  // 阻塞当前线程直到连接完成
        // Channel channel = channelFuture.channel();
        // channel.writeAndFlush("hello world");

        // 方式二：使用监听器异步处理连接成功事件（非阻塞）
        channelFuture.addListener(future -> {
            // 连接成功后，获取Channel对象
            Channel channel = channelFuture.channel();
            // 发送第一条消息（异步操作）
            channel.writeAndFlush("hello world");
        });

        // 同步等待连接建立完成，并获取Channel对象
        // 注意：这里会阻塞主线程，直到连接成功
        Channel channel = channelFuture.sync().channel();

        // 创建一个独立的输入线程，用于接收用户控制台输入
        new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (true) {
                String str = sc.nextLine();  // 阻塞等待用户输入
                if (str.equals("exit")) {
                    // 用户输入exit时，关闭Channel连接
                    // 注意：close()是异步操作，不会立即关闭
                    channel.close();
                    break;  // 退出循环，结束输入线程
                }
            }
        }, "input").start();

        // ========== 关闭处理部分 ==========

        // 获取Channel的关闭Future对象（用于监听连接关闭事件）
        ChannelFuture closeFuture = channel.closeFuture();

        // 方式一：同步等待关闭完成（阻塞当前线程直到连接真正关闭）
        closeFuture.sync();
        System.out.println("处理关闭之后的");  // 这行会在连接关闭后才执行

        // 方式二：异步监听关闭事件（不会阻塞，通过回调处理关闭逻辑）
        // 注意：由于上面已经调用了sync()，下面的代码实际上不会在当前流程中执行
        // 实际使用时，通常只选择其中一种方式
        closeFuture.addListeners((ChannelFutureListener) cf -> {
            // 当连接成功关闭后，会回调这个监听器
            System.out.println("处理关闭之后的");
            // 优雅地关闭事件循环组，释放资源
            eventExecutors.shutdownGracefully();
        });

        // 注意：由于上面已经调用了closeFuture.sync()，主线程会阻塞直到Channel关闭
        // 因此下面的异步监听器代码可能在关闭后才注册，实际效果可能不符合预期
        // 建议在实际代码中只使用同步方式或异步方式之一
    }
}