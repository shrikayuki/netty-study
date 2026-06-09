package com.catgal.netty.c1;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class TestFileChannelTransferTo {
    public static void main(String[] args) {
        try (
                FileChannel from = new FileInputStream("target/from.txt").getChannel();
                FileChannel to = new FileOutputStream("target/to.txt").getChannel();
                ){
            long size = from.size();
            for (long i= size ;i > 0 ;i--){
                i -= from.transferTo(size - i, size, to);
            }
        }catch (Exception e){

        }
    }
}
