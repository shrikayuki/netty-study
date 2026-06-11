package com.catgal.nio.c1;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@Slf4j
public class TestByteBuffer {

    public static void main(String[] args) {

        //FileChannel
        //1. 输入输出流
        try (FileChannel channel = new FileInputStream("target/data.txt").getChannel()) {
            ByteBuffer buffer = ByteBuffer.allocate(10);
            while (true) {
                int len = channel.read(buffer);
                if (len == -1) {
                    break;
                }
                //切换为读模式
                buffer.flip();
                while (buffer.hasRemaining()) {
                    System.out.println("实际字节:"+(char)(buffer.get()));
                }
                buffer.clear();
            }

        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
