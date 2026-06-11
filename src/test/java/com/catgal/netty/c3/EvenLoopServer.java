package com.catgal.netty.c3;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class EvenLoopServer {

    public static void main(String[] args) {
        // 创建一个自定义的 EventLoopGroup（用于处理特定 handler 的异步任务）
// 注意：DefaultEventLoop 不是 NIO 实现，适合处理耗时操作，不会阻塞 I/O 线程
        EventLoopGroup group = new DefaultEventLoop();

        new ServerBootstrap()
                // 配置两个 EventLoopGroup：
                // - bossGroup: 接收客户端连接（默认线程数 = CPU核心数 * 2）
                // - workerGroup: 处理连接的 I/O 事件（指定为 2 个线程）
                .group(new NioEventLoopGroup(), new NioEventLoopGroup(2))

                // 指定使用 NIO 的 ServerSocketChannel（非阻塞模式）
                .channel(NioServerSocketChannel.class)

                // 配置 child handler（处理每个新建立的连接）
                .childHandler(new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        // ========== Handler 1：在默认的 I/O 线程中执行 ==========
                        ch.pipeline().addLast("handle1", new ChannelInboundHandlerAdapter() {

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                // 打印接收到的消息
                                // 注意：这里 msg 通常是 ByteBuf 或其它类型，直接 toString() 可能不是预期的字符串
                                System.out.println("Handle1 收到消息: " + msg.toString());

                                // 将消息传递给下一个 inbound handler
                                // 如果不调用 fireChannelRead，消息不会被后续 handler 处理
                                ctx.fireChannelRead(msg);
                            }
                        });

                        // ========== Handler 2：在自定义的 DefaultEventLoop 线程中执行 ==========
                        // 使用指定的 EventLoopGroup 来处理这个 handler
                        // 适合执行耗时操作（如数据库查询、复杂计算），不会阻塞 I/O 线程
                        ch.pipeline().addLast(group, "handle2", new ChannelInboundHandlerAdapter() {

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                // 打印接收到的消息
                                // 这个 handler 会在 group 指定的线程池中执行，而不是 worker 线程
                                System.out.println("Handle2 收到消息: " + msg.toString());

                                // 注意：这里没有调用 ctx.fireChannelRead(msg)
                                // 意味着消息传播到此结束，不会继续传递给后续 handler
                            }
                        });
                    }
                })

                // 绑定端口并启动服务器
                .bind(8080);
    }
}
