package com.tugalsan.api.file.zip7.server;

import com.tugalsan.api.bytes.client.TGS_ByteArrayUtils;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.Date;
import net.sf.sevenzipjbinding.IOutCreateArchive7z;
import net.sf.sevenzipjbinding.IOutCreateCallback;
import net.sf.sevenzipjbinding.IOutItem7z;
import net.sf.sevenzipjbinding.ISequentialInStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.OutItemFactory;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import net.sf.sevenzipjbinding.util.ByteArrayStream;
import com.tugalsan.api.os.server.TS_OsPlatformUtils;
import com.tugalsan.api.union.client.TGS_UnionExcuseVoid;
import java.io.FileNotFoundException;
import java.util.StringJoiner;

public class CompressZip7 {

    static class Zip7Callback implements IOutCreateCallback<IOutItem7z> {

        private final String outFilename;
        private final byte[] inBytes;

        private Zip7Callback(byte[] inBytes, String outFilename) {
            this.outFilename = outFilename;
            this.inBytes = inBytes;
        }

        @Override
        public void setOperationResult(boolean operationResultOk) {
            // called for each archive item
        }

        @Override
        public void setTotal(long total) {
            // Track operation progress here
        }

        @Override
        public void setCompleted(long complete) {
            // Track operation progress here
        }

        @Override
        public IOutItem7z getItemInformation(int index, OutItemFactory<IOutItem7z> outItemFactory) {
            var outItem = outItemFactory.createOutItem();
            outItem.setDataSize((long) inBytes.length);
            outItem.setPropertyPath(outFilename);
            outItem.setPropertyLastModificationTime(new Date());
            if (TS_OsPlatformUtils.isLinux()) {
                outItem.setPropertyAttributes(0x81808000);
            }
            return outItem;
        }

        @Override
        public ISequentialInStream getStream(int index) {
            return new ByteArrayStream(inBytes, true);
        }
    }

//    public static void main(String[] args) {
//        var sb = new StringBuilder();
//        IntStream.range(0, 10000000).forEachOrdered(i -> sb.append(CompressZip7.class.getSimpleName()));
//        compress7z(sb.toString(),"aligel.txt",  Path.of("D:\\zip\\c.7z"));
//    }
    public static void compress7z(CharSequence inText, CharSequence inFilename, Path outFile) {
        compress7z(TGS_ByteArrayUtils.toByteArray(inText), inFilename, outFile);
    }

    public static TGS_UnionExcuseVoid compress7z(byte[] inBytes, CharSequence inFilename, Path outFile) {
        RandomAccessFile raf = null;
        IOutCreateArchive7z outArchive = null;
        var errors = new StringJoiner(" | ");
        try {
            raf = new RandomAccessFile(outFile.toAbsolutePath().toString(), "rw");
            outArchive = SevenZip.openOutArchive7z();
            outArchive.setLevel(5);
            outArchive.createArchive(new RandomAccessFileOutStream(raf), 1, new Zip7Callback(inBytes, inFilename.toString()));
        } catch (SevenZipException | FileNotFoundException e) {
            errors.add("Error closing archive: " + e);
        } finally {
            if (outArchive != null) {
                try {
                    outArchive.close();
                } catch (IOException e) {
                    errors.add("Error closing archive: " + e);
                }
            }
            if (raf != null) {
                try {
                    raf.close();
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
