package com.tugalsan.api.file.zip7.server;


import com.tugalsan.api.function.client.maythrowexceptions.checked.TGS_FuncMTCUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;

public class TS_FileZip7Utils {

    public static void compress(CharSequence name, File... files) {
        TGS_FuncMTCUtils.run(() -> {
            try ( var out = new SevenZOutputFile(new File(name.toString()))) {
                Arrays.stream(files).forEachOrdered(file -> addToArchiveCompression(out, file, "."));
            }
        });
    }

    public static void decompress(CharSequence in, File destination) {
        TGS_FuncMTCUtils.run(() -> {
            var sevenZFile = new SevenZFile(new File(in.toString()));
            SevenZArchiveEntry entry;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                var curfile = new File(destination, entry.getName());
                var parent = curfile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                var content = new byte[(int) entry.getSize()];
                sevenZFile.read(content, 0, content.length);
                try ( var out = new FileOutputStream(curfile)) {
                    out.write(content);
                }
            }
        });
    }

    private static void addToArchiveCompression(SevenZOutputFile out, File file, CharSequence dir) {
        TGS_FuncMTCUtils.run(() -> {
            var name = dir + File.separator + file.getName();
            if (file.isFile()) {
                var entry = out.createArchiveEntry(file, name);
                out.putArchiveEntry(entry);
                try ( var in = new FileInputStream(file);) {
                    var b = new byte[1024];
                    int count;
                    while ((count = in.read(b)) > 0) {
                        out.write(b, 0, count);
                    }
                }
                out.closeArchiveEntry();
            } else if (file.isDirectory()) {
                var children = file.listFiles();
                if (children != null) {
                    for (var child : children) {
                        addToArchiveCompression(out, child, name);
                    }
                }
            } else {
                System.out.println(file.getName() + " is not supported");
            }
        });
    }
}
