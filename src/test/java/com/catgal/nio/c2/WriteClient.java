package com.catgal.nio.c1.c2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class WriteClient {
    public static void main(String[] args) throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost", 8080));

        int total = 0;
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (true) {
            int read = sc.read(buffer);;
            System.out.println("read:" + read);
            total += read;
            buffer.clear();
            System.out.println("total : " + total);
        }



    }
}
