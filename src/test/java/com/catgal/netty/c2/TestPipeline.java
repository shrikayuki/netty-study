package com.catgal.netty.c2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;

import java.nio.charset.StandardCharsets;

/**
 * Netty Pipeline 学习笔记
 * 重点：理解 Inbound 和 Outbound Handler 的执行顺序
 */
public class TestPipeline {

    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                // 添加日志处理器，方便看连接和消息
                .handler(new LoggingHandler())
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();

                        // ============ Inbound Handler（入站：读数据） ============
                        // 顺序：h1 -> h2 -> h3
                        pipeline.addLast("h1", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                // 将 msg 转为字符串（msg 通常是 ByteBuf）
                                String content = msgToString(msg);
                                System.out.println("【h1】收到消息: " + content);

                                // 传递给下一个 inbound handler（必须调用！）
                                ctx.fireChannelRead(msg);
                            }
                        });

                        pipeline.addLast("h2", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                String content = msgToString(msg);
                                System.out.println("【h2】收到消息: " + content);

                                // 传递给下一个 inbound handler
                                ctx.fireChannelRead(msg);
                            }
                        });

                        pipeline.addLast("h3", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                String content = msgToString(msg);
                                System.out.println("【h3】收到消息: " + content);

                                // 如果这是最后一个 inbound handler，可以不调用 fireChannelRead
                                // 但通常我们会在最后处理业务逻辑

                                // 回复客户端（通过 Outbound Handler 写回）
                                String response = "已收到消息: " + content;
                                ByteBuf responseBuf = Unpooled.copiedBuffer(response, StandardCharsets.UTF_8);
                                ctx.writeAndFlush(responseBuf);

                                System.out.println("【h3】已回复客户端");
                            }
                        });

                        // ============ Outbound Handler（出站：写数据） ============
                        // 注意：Outbound Handler 的执行顺序是从后往前（h4 -> h5）
                        // 因为 h3 是最后一个 Inbound，它的 ctx.writeAndFlush 会从尾向前找 Outbound

                        pipeline.addLast("h4", new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                                System.out.println("【h4】出站处理（写数据前）");
                                // 可以修改 msg 或做日志
                                ctx.write(msg, promise);  // 传递给下一个 outbound
                            }
                        });

                        pipeline.addLast("h5", new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                                System.out.println("【h5】出站处理（写数据后，靠近网络层）");
                                // 实际写出的数据可能被编码
                                ctx.write(msg, promise);
                            }
                        });

                        // 打印 Pipeline 结构，方便理解
                        System.out.println("========== Pipeline 结构 ==========");
                        System.out.println(pipeline);
                        System.out.println("====================================");
                    }
                })
                .bind(8080);  // 绑定端口（你忘了写）
    }

    /**
     * 辅助方法：将 ByteBuf 转为字符串
     */
    private static String msgToString(Object msg) {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            // 注意：不要修改 readerIndex，否则会影响后续 handler
            // 这里只读不移动索引
            return buf.toString(StandardCharsets.UTF_8);
        }
        return msg.toString();
    }
}