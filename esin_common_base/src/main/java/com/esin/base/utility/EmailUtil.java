package com.esin.base.utility;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import java.util.List;

public class EmailUtil {
    private static final Logger logger = Logger.getLogger(EmailUtil.class);

    public static void send(final String server, final int port,
                            final boolean sslOnConnect,
                            final String username, final String password,
                            final String fromAddress, final List<String> toAddressList,
                            final String subject, final String content) {
        if (Utility.isEmpty(content)) {
            logger.warn("send mail error with empty content. (subject: " + subject + ")");
            return;
        }
        ThreadUtil.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Email email = new SimpleEmail();
                    email.setHostName(server);
                    if (port > 0) {
                        if (sslOnConnect) {
                            email.setSslSmtpPort(String.valueOf(port));
                        } else {
                            email.setSmtpPort(port);
                        }
                    }
                    email.setSSLOnConnect(sslOnConnect);
                    email.setAuthenticator(new DefaultAuthenticator(username, password));
                    email.setFrom(fromAddress);
                    for (String toAddress : toAddressList) {
                        email.addTo(toAddress);
                    }
                    email.setSubject(subject);
                    email.setMsg(content);
                    email.send();
                    logger.info("Email send successful.");
                } catch (EmailException e) {
                    logger.error("Email send error.", e);
                }
            }
        });
    }
}
