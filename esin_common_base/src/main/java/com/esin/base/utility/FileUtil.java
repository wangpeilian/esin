package com.esin.base.utility;

import com.esin.base.exception.SystemException;
import com.esin.base.executor.IExecutorA;
import com.esin.base.zip7.LzmaAlone;
import org.mockito.internal.util.io.IOUtil;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public final class FileUtil {
    private static final Logger logger = Logger.getLogger(FileUtil.class);
    public static final String UTF8 = "UTF-8";
    public static final String GBK = "GBK";

    public static interface Handler {
        public void doHandle(String value);
    }

    public static final RuntimeException IgnoreException = new RuntimeException();

    public static void read(InputStream is, String charset, Handler handler) {
        BufferedReader reader = null;
        try {
//            final int bufferSize = 256 * 1024;
//            reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(is, bufferSize), charset), bufferSize);
            reader = new BufferedReader(new InputStreamReader(is, charset));
            String lineString = reader.readLine();
            while (lineString != null) {
                handler.doHandle(lineString);
                lineString = reader.readLine();
            }
        } catch (RuntimeException e) {
            if (!IgnoreException.equals(e)) {
                throw e;
            }
        } catch (IOException e) {
            throw new SystemException("FileUtil.read", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.error("FileUtil.read", e);
                }
            }
        }
    }

    public static void read(String filename, String charset, Handler handler) {
        if (checkExistFileAndNotEmpty(filename)) {
            try {
                read(new FileInputStream(filename), charset, handler);
            } catch (FileNotFoundException e) {
                throw new SystemException("FileUtil.read", e);
            }
        }
    }

    public static void read(String filename, String charset, final List<String> dataList) {
        read(filename, charset, new Handler() {
            @Override
            public void doHandle(String value) {
                dataList.add(value);
            }
        });
    }

    public static String read(String filename, String charset) {
        final StringBuilder sb = new StringBuilder();
        read(filename, charset, new Handler() {
            @Override
            public void doHandle(String value) {
                sb.append(value);
                sb.append("\r\n");
            }
        });
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public static void save(OutputStream os, String charset, IExecutorA<Handler> writeHandler) {
        try {
            final OutputStreamWriter osw = new OutputStreamWriter(os, charset);
            try {
                writeHandler.doExecute(new Handler() {
                    @Override
                    public void doHandle(String value) {
                        try {
                            osw.write(value);
                        } catch (RuntimeException e) {
                            if (!IgnoreException.equals(e)) {
                                throw e;
                            }
                        } catch (IOException e) {
                            throw new SystemException("FileUtil.save", e);
                        }
                    }
                });
            } finally {
                osw.flush();
                os.flush();
                if (os instanceof FileOutputStream) {
                    ((FileOutputStream) os).getFD().sync();
                }
                os.close();
                osw.close();
            }
        } catch (IOException e) {
            throw new SystemException("FileUtil.save", e);
        }
    }

    public static void save(String filename, String charset, IExecutorA<Handler> writeHandler) {
        try {
            save(new FileOutputStream(filename), charset, writeHandler);
        } catch (FileNotFoundException e) {
            throw new SystemException("FileUtil.save", e);
        }
    }

    public static void save(String filename, String charset, final List<String> dataList) {
        save(filename, charset, new IExecutorA<Handler>() {
            @Override
            public void doExecute(Handler handler) {
                for (int i = 0; i < dataList.size(); i++) {
                    handler.doHandle(dataList.get(i));
                    if (i < dataList.size() - 1) {
                        handler.doHandle("\r\n");
                    }
                }
            }
        });
    }

    public static void save(String filename, String charset, final String value) {
        save(filename, charset, Arrays.asList(value));
    }

    public static boolean checkExistFile(String filename) {
        return Utility.isNotEmpty(filename) && checkExistFile(new File(filename));
    }

    public static boolean checkExistFile(File file) {
        return file != null && file.exists();
    }

    public static boolean checkExistFileAndNotEmpty(String filename) {
        return checkExistFile(filename) && checkExistFileAndNotEmpty(new File(filename));
    }

    public static boolean checkExistFileAndNotEmpty(File file) {
        return checkExistFile(file) && file.isFile() && file.length() > 0;
    }

    public static boolean createDirs(String filename) {
        return Utility.isNotEmpty(filename) && createDirs(new File(filename));
    }

    public static boolean createDirs(File file) {
        if (file == null) {
            return false;
        }
        if (!checkExistFile(file)) {
            createDirs(file.getParentFile());
            if (!file.mkdirs()) {
                return false;
            }
        }
        if (checkExistFile(file)) {
            boolean readable = file.setReadable(true, false) && file.canRead();
            boolean writable = file.setWritable(true, false) && file.canWrite();
            if (!readable || !writable) {
                try {
                    Runtime.getRuntime().exec("cmd /c attrib -r -a -s -h "
                            + file.getAbsolutePath() + " /s /d");
                    return true;
                } catch (IOException e) {
                    logger.error("FileUtil.createDirs", e);
                    return false;
                }
            }
        }
        return checkExistFile(file);
    }

    public static boolean delete(File file) {
        if (checkExistFile(file)) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File _file : files) {
                        delete(_file);
                    }
                }
            }
            if (!file.delete()) {
                file.deleteOnExit();
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public static boolean copyTo(String origFilename, String destFilename) {
        try {
            return copyTo(new FileInputStream(origFilename), new FileOutputStream(destFilename));
        } catch (FileNotFoundException e) {
            logger.error("Utility.copyTo", e);
            return false;
        }
    }

    public static boolean copyTo(InputStream is, OutputStream os) {
        try {
            byte[] buf = new byte[4096];
            int size = -1;
            while ((size = is.read(buf)) != -1) {
                os.write(buf, 0, size);
            }
            is.close();
            os.flush();
            if (os instanceof FileOutputStream) {
                ((FileOutputStream) os).getFD().sync();
            }
            os.close();
            return true;
        } catch (IOException e) {
            logger.error("Utility.copyTo", e);
            return false;
        }
    }

    public static synchronized String generateRandomFilename() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
        }
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return dateFormat.format(new Date());
    }

    public static boolean deleteFile(String filename) {
        return checkExistFile(filename) && delete(new File(filename));
    }

    public static boolean copyFile(File origFile, String toDirName) {
        File toDir = new File(toDirName);
        if (!FileUtil.checkExistFile(toDir) || !toDir.isDirectory()) {
            return false;
        }
        File destFile = new File(toDir + File.separator + origFile.getName());
        if (FileUtil.checkExistFile(destFile) && destFile.isFile()) {
            return false;
        }
        try {
            copyTo(new FileInputStream(origFile),
                    new FileOutputStream(destFile, true));
            return true;
        } catch (IOException e) {
            logger.error(null, e);
            return false;
        }
    }

    public static boolean unzipLzmaFile(String orig, String dest) {
        try {
            if (!checkExistFileAndNotEmpty(orig)) {
                FileUtil.deleteFile(orig);
                return false;
            }
            LzmaAlone.main(new String[]{"d", orig, dest});
            if (!checkExistFileAndNotEmpty(dest)) {
                FileUtil.deleteFile(dest);
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("unzip : " + orig + " -> " + dest, e);
            FileUtil.deleteFile(dest);
            return false;
        }
    }

    public static boolean zipLzmaFile(String orig, String dest) {
        try {
            if (!checkExistFileAndNotEmpty(orig)) {
                FileUtil.deleteFile(orig);
                return false;
            }
            LzmaAlone.main(new String[]{"e", orig, dest});
            if (!checkExistFileAndNotEmpty(dest)) {
                FileUtil.deleteFile(dest);
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("unzip : " + orig + " -> " + dest, e);
            FileUtil.deleteFile(dest);
            return false;
        }
    }

    private static final char[] HEX = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    private static char[] encode(byte[] bytes) {
        final int nBytes = bytes.length;
        char[] result = new char[2 * nBytes];

        int j = 0;
        for (int i = 0; i < nBytes; i++) {
            // Char for top 4 bits
            result[j++] = HEX[(0xF0 & bytes[i]) >>> 4];
            // Bottom 4
            result[j++] = HEX[(0x0F & bytes[i])];
        }
        return result;
    }

    public static String file_md5(String filename) {
        byte[] buffer = new byte[4096];
        DigestInputStream dis = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            dis = new DigestInputStream(new FileInputStream(filename), md5);
            while (dis.read(buffer) > 0) ;
            md5 = dis.getMessageDigest();
            byte[] result = md5.digest();
            return String.valueOf(encode(result));
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new SystemException("file_md5 : " + filename, e);
        } finally {
            IOUtil.closeQuietly(dis);
        }
    }

    public static String detectedCharset(File file) {
        try {
            byte[] buf = new byte[4096];
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            final UniversalDetector universalDetector = new UniversalDetector(null);
            int numberOfBytesRead;
            while ((numberOfBytesRead = bufferedInputStream.read(buf)) > 0
                    && !universalDetector.isDone()) {
                universalDetector.handleData(buf, 0, numberOfBytesRead);
            }
            universalDetector.dataEnd();
            String encoding = universalDetector.getDetectedCharset();
            universalDetector.reset();
            bufferedInputStream.close();
            return encoding;
        } catch (IOException e) {
            logger.error("detectedCharset error. (" + file.getAbsolutePath() + ")", e);
            return FileUtil.UTF8;
        }
    }
}
