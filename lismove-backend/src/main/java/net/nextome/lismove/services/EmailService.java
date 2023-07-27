package net.nextome.lismove.services;

import net.nextome.lismove.exceptions.LismoveException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.*;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private TemplateEngine templateEngine;

    public void send(String to, String subject, String template, Context ctx) {
        try {
            MimeMessage msg = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setFrom("Lismove");
            helper.setSubject(subject);
            helper.setText(templateEngine.process("email/" + template + ".html", ctx),true);
            helper.addInline("logo", new ClassPathResource("static/img/logo.png"), "image/png");
            javaMailSender.send(msg);
        } catch (MessagingException e) {
            throw new LismoveException("Error in sending email", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
