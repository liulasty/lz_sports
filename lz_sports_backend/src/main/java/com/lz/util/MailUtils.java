package com.lz.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mail Utils
 */
public class MailUtils {
    private static final Logger log = LoggerFactory.getLogger(MailUtils.class);

    // Placeholder
    public static void sendMail(String to, String content, String title) {
        log.info("Sending mail to: {}, title: {}, content: {}", to, title, content);
    }
}
