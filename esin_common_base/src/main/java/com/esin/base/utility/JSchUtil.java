package com.esin.base.utility;

import com.jcabi.ssh.Shell;
import com.jcabi.ssh.SshByPassword;

import java.io.IOException;
import java.util.List;

public class JSchUtil {

    public static void sample(String ip, int port, String username, String password, List<String> commandList) {
        try {
            Shell shell = new SshByPassword(ip, port, username, password);
            String stdout = new Shell.Plain(shell).exec(commandList.get(0));
            System.out.println(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
