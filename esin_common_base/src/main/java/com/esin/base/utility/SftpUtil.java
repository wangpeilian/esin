package com.esin.base.utility;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

import java.io.File;

public class SftpUtil {
    public static final String lineSeparator = "\n";

    public static String createConnectionString(String hostName,
                                                String username, String password, String remoteFilePath) {
        // result: "sftp://user:123456@domainname.com/test.pdf
        return "sftp://" + username + ":" + password + "@" + hostName + remoteFilePath;
    }

    public static FileSystemOptions createDefaultOptions()
            throws FileSystemException {
        // Create SFTP options
        FileSystemOptions opts = new FileSystemOptions();
        // SSH Key checking
        SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(
                opts, "no");
        // Root directory set to user home
        SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, false);
        // Timeout is count by Milliseconds
        SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, 10000);
        return opts;
    }

    public static void upload(String hostName, String username,
                              String password, String localFilePath, String remoteFilePath) {
        File f = new File(localFilePath);
        if (!f.exists())
            throw new RuntimeException("Error. Local file not found");
        StandardFileSystemManager manager = new StandardFileSystemManager();
        try {
            manager.init();
            // Create local file object
            FileObject localFile = manager.resolveFile(f.getAbsolutePath());
            // Create remote file object
            FileObject remoteFile = manager.resolveFile(
                    createConnectionString(hostName, username, password,
                            remoteFilePath), createDefaultOptions());
            // Copy local file to sftp server
            remoteFile.copyFrom(localFile, Selectors.SELECT_SELF);
            System.out.println("File upload success. " + remoteFilePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            manager.close();
        }
    }

    public static void download(String hostName, String username,
                                String password, String localFilePath, String remoteFilePath) {
        StandardFileSystemManager manager = new StandardFileSystemManager();
        try {
            manager.init();
            // Create local file object
            FileObject localFile = manager.resolveFile(localFilePath);
            // Create remote file object
            FileObject remoteFile = manager.resolveFile(
                    createConnectionString(hostName, username, password,
                            remoteFilePath), createDefaultOptions());
            // Copy local file to sftp server
            localFile.copyFrom(remoteFile, Selectors.SELECT_SELF);
            System.out.println("File download success. " + localFilePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            manager.close();
        }
    }

    public static boolean exist(String hostName, String username,
                                String password, String remoteFilePath) {
        StandardFileSystemManager manager = new StandardFileSystemManager();
        try {
            manager.init();
            // Create remote object
            FileObject remoteFile = manager.resolveFile(
                    createConnectionString(hostName, username, password,
                            remoteFilePath), createDefaultOptions());
            System.out.println("File exist: " + remoteFile.exists());
            return remoteFile.exists();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            manager.close();
        }
    }

    public static void delete(String hostName, String username,
                              String password, String remoteFilePath) {
        StandardFileSystemManager manager = new StandardFileSystemManager();
        try {
            manager.init();
            // Create remote object
            FileObject remoteFile = manager.resolveFile(
                    createConnectionString(hostName, username, password,
                            remoteFilePath), createDefaultOptions());
            if (remoteFile.exists()) {
                remoteFile.delete();
                System.out.println("Delete remote file success");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            manager.close();
        }
    }
}
