package com.catgal.nio.c2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class Client {
    public static void main(String[] args) throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("127.0.0.1", 8082));
        sc.write(Charset.defaultCharset().encode("hello\nworld\n"));
        System.out.println("waiting");
        System.in.read();

    }
}
