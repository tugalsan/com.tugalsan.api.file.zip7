package com.tugalsan.api.file.zip7.server;

import module sevenzipjbinding;
import java.io.*;
import java.nio.file.*;

public class PrintCountOfItems {

//    public static void main(String[] args) {
//        System.out.println("zip: " + getCountOfItems(Path.of("D:\\zip\\a.zip")));
//        System.out.println("7z : " + getCountOfItems(Path.of("D:\\zip\\a.7z")));
//    }
    public static Integer getCountOfItems(Path compressedFile) {
        RandomAccessFile randomAccessFile = null;
        IInArchive inArchive = null;
        Integer count = null;
        try {
            randomAccessFile = new RandomAccessFile(compressedFile.toAbsolutePath().toString(), "r");
            inArchive = SevenZip.openInArchive(null, // autodetect archive type
                    new RandomAccessFileInStream(randomAccessFile));
            count = inArchive.getNumberOfItems();
        } catch (FileNotFoundException | SevenZipException e) {
            System.err.println("Error occurs: " + e);
        } finally {
            if (inArchive != null) {
                try {
                    inArchive.close();
                } catch (SevenZipException e) {
                    System.err.println("Error closing archive: " + e);
                }
            }
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    System.err.println("Error closing file: " + e);
                }
            }
        }
        return count;
    }
}
