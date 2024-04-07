package com.esin.base.utility;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

public final class CompressUtil {

    public static boolean unzip(String zipFilename, String unzipDir, String charset) {
        if (!unzipDir.endsWith("/")) {
            unzipDir += "/";
        }
        try {
            ZipFile zipFile = new ZipFile(zipFilename, charset);
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                InputStream content = zipFile.getInputStream(entry);
                FileUtil.copyTo(content, new FileOutputStream(unzipDir + entry.getName()));
                System.out.println("unzip : " + entry.getName());
            }
            zipFile.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void un7zip(String inputFile, String destDirPath) throws IOException {
        File srcFile = new File(inputFile);
        SevenZFile zIn = new SevenZFile(srcFile);
        SevenZArchiveEntry entry = null;
        while ((entry = zIn.getNextEntry()) != null) {
            if (!entry.isDirectory()) {
                File file = new File(destDirPath, entry.getName());
                OutputStream out = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(out);
                int len = -1;
                byte[] buf = new byte[1024];
                while ((len = zIn.read(buf)) != -1) {
                    bos.write(buf, 0, len);
                }
                bos.close();
                out.close();
                System.out.println("un7zip : " + entry.getName());
            }
        }
    }

}
