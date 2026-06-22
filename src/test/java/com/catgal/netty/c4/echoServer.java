package com.catgal.netty.c4;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

public class echoServer {
    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();

                            // ✅ Inbound：处理收到的数据
                            pipeline.addLast("inbound1", new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                    ByteBuf buf = (ByteBuf) msg;
                                    String content = buf.toString(CharsetUtil.UTF_8);
                                    System.out.println("【Inbound】收到消息: " + content);

                                    // 继续传递到下一个 inbound
                                    ctx.fireChannelRead(msg);
                                }
                            });

                            // ✅ Inbound：真正的业务处理
                            pipeline.addLast("inbound2", new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                    System.out.println("【Inbound】处理业务逻辑，准备返回...");

                                    // 写数据（触发 Outbound）
                                    ctx.writeAndFlush(msg);
                                }
                            });

                            // ✅ Outbound：处理要发送的数据
                            pipeline.addLast("outbound1", new ChannelOutboundHandlerAdapter() {
                                @Override
                                public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                                    ByteBuf buf = (ByteBuf) msg;
                                    String content = buf.toString(CharsetUtil.UTF_8);
                                    System.out.println("【Outbound】准备发送: " + content);

                                    // 继续传递到下一个 outbound
                                    ctx.write(msg, promise);
                                }
                            });

                            // ✅ Outbound：最后发送
                            pipeline.addLast("outbound2", new ChannelOutboundHandlerAdapter() {
                                @Override
                                public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                                    System.out.println("【Outbound】实际发送数据到网络...");
                                    ctx.write(msg, promise);
                                }
                            });
                        }
                    })
                    .bind(8080)
                    .sync()
                    .channel()
                    .closeFuture()
                    .sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}