package com.touchmediaproductions.pneumocheck.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Class responsible for compression
 */
public class CompressedFilesHelper {

    /**
     * Compresses a given file saves it to zipFile
     *
     * @param srcFile given file source file
     * @param zipFile compressed file will be saved to this given file
     * @throws IOException
     */
    public static void zipFile(File srcFile, File zipFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(srcFile);
             ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            zos.putNextEntry(new ZipEntry(srcFile.getName()));
            int len;
            byte[] buffer = new byte[1024];
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            zos.closeEntry();
        }
    }
}
