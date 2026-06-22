package com.catgal.netty.c2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;

/**
 * Netty ByteBuf 学习笔记
 * 结合 JVM 内存分布理解：ByteBuf 在堆还是堆外？
 */
public class TestByteBuf {

    public static void main(String[] args) {
        // ============ 1. 创建 ByteBuf 的几种方式 ============

        // 方式1：默认分配（池化 + 堆外内存，Netty 推荐）
        ByteBuf buf1 = ByteBufAllocator.DEFAULT.buffer();
        System.out.println("默认分配: " + buf1.getClass().getName());

        // 方式2：堆缓冲区（在 JVM 堆上）
        ByteBuf heapBuf = ByteBufAllocator.DEFAULT.heapBuffer();
        System.out.println("堆缓冲区: " + heapBuf.getClass().getName());
        // 底层是 byte[] 数组，在 JVM 堆中，受 GC 管理

        // 方式3：直接缓冲区（堆外内存，零拷贝）
        ByteBuf directBuf = ByteBufAllocator.DEFAULT.directBuffer();
        System.out.println("直接缓冲区: " + directBuf.getClass().getName());
        // 底层是操作系统直接内存，不受 JVM GC 管理（需要手动释放）

        // 方式4：非池化（每次 new，不重用）
        ByteBuf unpooledBuf = Unpooled.buffer();
        System.out.println("非池化: " + unpooledBuf.getClass().getName());

        // ============ 2. 写数据到 ByteBuf ============
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();

        // 写入字节
        buf.writeByte(65);                    // 写 1 字节
        buf.writeShort(100);                 // 写 2 字节
        buf.writeInt(99999);                 // 写 4 字节
        buf.writeLong(8888888888L);          // 写 8 字节

        // 写入字符串（按 UTF-8 编码）
        String msg = "你好，Netty!";
        buf.writeBytes(msg.getBytes(StandardCharsets.UTF_8));

        // 写入字符串的便捷方法（需要引入 Netty 的额外依赖）
        // buf.writeCharSequence("Hello", StandardCharsets.UTF_8);

        System.out.println("写入后 - readerIndex: " + buf.readerIndex() +
                ", writerIndex: " + buf.writerIndex() +
                ", capacity: " + buf.capacity());

        // ============ 3. 读数据从 ByteBuf ============
        // 必须先重置读索引（从 0 开始读）
        buf.readerIndex(0);

        byte b = buf.readByte();              // 读 1 字节
        short s = buf.readShort();            // 读 2 字节
        int i = buf.readInt();                // 读 4 字节
        long l = buf.readLong();              // 读 8 字节

        // 读取剩余字节转字符串
        int remaining = buf.readableBytes();
        byte[] bytes = new byte[remaining];
        buf.readBytes(bytes);
        String str = new String(bytes, StandardCharsets.UTF_8);

        System.out.println("读取结果:");
        System.out.println("  byte: " + b);
        System.out.println("  short: " + s);
        System.out.println("  int: " + i);
        System.out.println("  long: " + l);
        System.out.println("  string: " + str);

        // ============ 4. 引用计数（Reference Counting）============
        // Netty 的 ByteBuf 需要手动释放（除非用 try-with-resource）
        System.out.println("refCnt 引用计数: " + buf.refCnt());

        // 增加引用（用于多个地方共享）
        buf.retain();
        System.out.println("retain() 后 refCnt: " + buf.refCnt());

        // 释放引用（减 1）
        buf.release();
        System.out.println("release() 后 refCnt: " + buf.refCnt());

        // 最后一定记得释放（否则内存泄漏！）
        buf.release();
        System.out.println("最终 refCnt: " + buf.refCnt());

        // ============ 5. 用 try-finally 保证释放（推荐）============
        ByteBuf safeBuf = ByteBufAllocator.DEFAULT.buffer();
        try {
            safeBuf.writeBytes("safe".getBytes());
            System.out.println("safeBuf 内容: " + safeBuf.toString(StandardCharsets.UTF_8));
        } finally {
            safeBuf.release();  // 确保释放
        }

        // ============ 6. 零拷贝操作（slice、duplicate）============
        ByteBuf original = ByteBufAllocator.DEFAULT.buffer();
        original.writeBytes("HelloWorld".getBytes());

        // slice：共享底层数据，但独立维护读写索引
        ByteBuf slice = original.slice(0, 5);  // 只读 "Hello"
        System.out.println("slice 内容: " + slice.toString(StandardCharsets.UTF_8));

        // duplicate：完整共享底层数据
        ByteBuf duplicate = original.duplicate();
        System.out.println("duplicate 内容: " + duplicate.toString(StandardCharsets.UTF_8));

        // 修改 original，slice 和 duplicate 也会变化（共享内存）
        original.setByte(0, 'h');
        System.out.println("修改后 slice: " + slice.toString(StandardCharsets.UTF_8));
        System.out.println("修改后 duplicate: " + duplicate.toString(StandardCharsets.UTF_8));

        // 释放
        original.release();
        // 注意：slice 和 duplicate 不会增加 refCnt，需要单独释放
        // 但它们共享底层，original 释放后，slice/duplicate 仍可用（只要 refCnt > 0）
        slice.release();
        duplicate.release();
    }
}
