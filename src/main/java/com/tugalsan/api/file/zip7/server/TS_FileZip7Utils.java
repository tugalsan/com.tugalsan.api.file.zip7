package com.tugalsan.api.file.zip7.server;

import com.tugalsan.api.union.client.TGS_UnionExcuseVoid;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;

public class TS_FileZip7Utils {

    public static TGS_UnionExcuseVoid compress(CharSequence name, File... files) {
        try (var out = new SevenZOutputFile(new File(name.toString()))) {
            for (var file : files) {
                var u = addToArchiveCompression(out, file, ".");
                if (u.isExcuse()) {
                    return u;
                }
            }
            return TGS_UnionExcuseVoid.ofVoid();
        } catch (IOException ex) {
            return TGS_UnionExcuseVoid.ofExcuse(ex);
        }
    }

    public static TGS_UnionExcuseVoid decompress(CharSequence in, File destination) {
        try {
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
                try (var out = new FileOutputStream(curfile)) {
                    out.write(content);
                }
            }
            return TGS_UnionExcuseVoid.ofVoid();
        } catch (IOException ex) {
            return TGS_UnionExcuseVoid.ofExcuse(ex);
        }
    }

    private static TGS_UnionExcuseVoid addToArchiveCompression(SevenZOutputFile out, File file, CharSequence dir) {
        try {
            var name = dir + File.separator + file.getName();
            if (file.isFile()) {
                var entry = out.createArchiveEntry(file, name);
                out.putArchiveEntry(entry);
                try (var in = new FileInputStream(file);) {
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
            return TGS_UnionExcuseVoid.ofVoid();
        } catch (IOException ex) {
            return TGS_UnionExcuseVoid.ofExcuse(ex);
        }
    }
}
