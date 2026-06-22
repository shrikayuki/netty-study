package com.catgal.netty.c4;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import java.util.Scanner;

public class echoClient {
    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap client = new Bootstrap();
            client.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ch.pipeline().addLast(new EchoClientHandler());
                        }
                    });

            // 连接服务器
            Channel channel = client.connect("127.0.0.1", 8080).sync().channel();
            System.out.println("已连接到服务器！");
            System.out.println("请输入要发送的消息（输入 'quit' 退出）：");

            // 控制台输入
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String input = scanner.nextLine();

                if ("quit".equalsIgnoreCase(input)) {
                    break;
                }

                // 发送消息
                channel.writeAndFlush(Unpooled.copiedBuffer(input, CharsetUtil.UTF_8));
            }

            // 关闭连接
            channel.close().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}

class EchoClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String response = ((ByteBuf) msg).toString(CharsetUtil.UTF_8);
        System.out.println("服务端回声: " + response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}