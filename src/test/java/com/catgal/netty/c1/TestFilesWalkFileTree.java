package com.catgal.netty.c1;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

public class TestFilesWalkFileTree {

    public static void main(String[] args) throws IOException {
        AtomicInteger oggCount = new AtomicInteger();
        Files.walkFileTree(Paths.get("C:\\galgame"), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith("ogg")) {
                    oggCount.incrementAndGet();
                }
                return super.visitFile(file, attrs);
            }
        });
        System.out.println(oggCount.get());
    }




    private static void getAllFileAndDir() throws IOException {
        //匿名类只能访问final变量
        AtomicInteger dirCount = new AtomicInteger();
        AtomicInteger fileCount = new AtomicInteger();
        Files.walkFileTree(Paths.get("C:\\galgame"), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println("dir: " + dir);
                dirCount.incrementAndGet();
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println("file: " + file);
                fileCount.incrementAndGet();
                return super.visitFile(file, attrs);
            }

        });

        System.out.println("dirCount: " + dirCount.get());
        System.out.println("fileCount: " + fileCount.get());

    }
}
