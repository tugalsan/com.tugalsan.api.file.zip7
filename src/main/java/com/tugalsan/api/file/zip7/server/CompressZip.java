package com.tugalsan.api.file.zip7.server;

import com.tugalsan.api.bytes.client.TGS_ByteArrayUtils;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.Date;
import net.sf.sevenzipjbinding.IOutCreateArchiveZip;
import net.sf.sevenzipjbinding.IOutCreateCallback;
import net.sf.sevenzipjbinding.IOutItemZip;
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

public class CompressZip {

    static class ZipCallback implements IOutCreateCallback<IOutItemZip> {

        private final String outFilename;
        private final byte[] inBytes;

        private ZipCallback(byte[] inBytes, String outFilename) {
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
        public IOutItemZip getItemInformation(int index, OutItemFactory<IOutItemZip> outItemFactory) {
            var outItem = outItemFactory.createOutItem();
            outItem.setDataSize((long) inBytes.length);
            outItem.setPropertyPath(outFilename);
            outItem.setPropertyCreationTime(new Date());
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
//        IntStream.range(0, 10000000).forEachOrdered(i -> sb.append(CompressZip.class.getSimpleName()));
//        compressZip(sb.toString(), "aligel.txt", Path.of("D:\\zip\\c.zip"));
//    }
    public static TGS_UnionExcuseVoid compressZip(String inText, String inFilename, Path outFile) {
        var inBytes = TGS_ByteArrayUtils.toByteArray(inText);
        return compressZip(inBytes, inFilename, outFile);
    }

    public static TGS_UnionExcuseVoid compressZip(byte[] inBytes, String inFilename, Path outFile) {
        RandomAccessFile raf = null;
        IOutCreateArchiveZip outArchive = null;
        var errors = new StringJoiner(" | ");
        try {
            raf = new RandomAccessFile(outFile.toAbsolutePath().toString(), "rw");
            outArchive = SevenZip.openOutArchiveZip();
            outArchive.setLevel(5);
            outArchive.createArchive(new RandomAccessFileOutStream(raf), 1, new ZipCallback(inBytes, inFilename));
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
