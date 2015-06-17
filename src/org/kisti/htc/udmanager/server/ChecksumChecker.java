package org.kisti.htc.udmanager.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;
 
 
 
public class ChecksumChecker {
 
    public static long getFileChecksum(String path) throws IOException {
        return getFileChecksum(new File(path));
    }
 
    public static long getFileChecksum(File file) throws IOException {
        return getFileChecksum(new FileInputStream(file));
    }
 
    public static long getFileChecksum(InputStream is) throws IOException {
        // Compute CRC-32 checksum
        CheckedInputStream cis = new CheckedInputStream(is, new CRC32());
        byte[] tempBuf = new byte[1024];
        while (cis.read(tempBuf) >= 0) {
        }
        long csValue = cis.getChecksum().getValue();
 
        return csValue;
    }
 
    public static long getByteArrayChecksum(byte[] byteArray) {
        // Compute CRC-32 checksum
        Checksum checksum = new CRC32();
        checksum.update(byteArray, 0, byteArray.length);
        long csValue = checksum.getValue();
        checksum.reset();
 
        return csValue;
    }
}



