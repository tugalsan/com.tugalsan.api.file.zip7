package com.tugalsan.api.file.zip7.server;

import com.tugalsan.api.union.client.TGS_UnionExcuseVoid;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.StringJoiner;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

public class asd {

    public static TGS_UnionExcuseVoid unzipDirWithPassword(CharSequence sourceZipFile, CharSequence destinationDir, CharSequence password) {
        RandomAccessFile randomAccessFile = null;
        IInArchive inArchive = null;
        var errors = new StringJoiner(" | ");
        try {
            randomAccessFile = new RandomAccessFile(sourceZipFile.toString(), "r");
            inArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));
            var simpleInArchive = inArchive.getSimpleInterface();
            for (var item : simpleInArchive.getArchiveItems()) {
                var hash = new int[]{0};
                if (!item.isFolder()) {
                    var result = item.extractSlow(data -> {
                        try {
                            if (item.getPath().indexOf(File.separator) > 0) {
                                var path = destinationDir + File.separator + item.getPath().substring(0, item.getPath().lastIndexOf(File.separator));
                                var folderExisting = new File(path);
                                if (!folderExisting.exists()) {
                                    new File(path).mkdirs();
                                }
                            }
                            if (!new File(destinationDir + File.separator + item.getPath()).exists()) {
                                new File(destinationDir.toString()).createNewFile();
                            }
                            try (var out = new FileOutputStream(destinationDir + File.separator + item.getPath())) {
                                out.write(data);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        hash[0] |= Arrays.hashCode(data);
                        return data.length; // Return amount of proceed data
                    }, password.toString()); /// password.
                    if (result == ExtractOperationResult.OK) {
                        System.out.println(String.format("%9X | %s", hash[0], item.getPath()));
                    } else {
                        System.err.println("Error extracting item: " + result);
                    }
                }
            }
        } catch (FileNotFoundException | SevenZipException e) {
            errors.add("Error closing archive: " + e);
        } finally {
            if (inArchive != null) {
                try {
                    inArchive.close();
                } catch (SevenZipException e) {
                    errors.add("Error closing archive: " + e);
                }
            }
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    errors.add("Error closing archive: " + e);
                }
            }
        }
        if (!errors.toString().isEmpty()) {
            return TGS_UnionExcuseVoid.ofExcuse("CompressZip", "compressZip", errors.toString());
        }
        return TGS_UnionExcuseVoid.ofVoid();
    }
}
