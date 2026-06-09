package com.catgal.netty.c1;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class TestByteBufferExam {

    public static void main(String[] args) {
        //粘包半包问题拆解
        ByteBuffer source = ByteBuffer.allocate(32);
        source.put("Hello,world\nI'm zhangsan\nHo".getBytes());
        split(source);
        source.put("w are you?\n".getBytes());
        split(source);



    }

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
}
