package com.example.auto_ria.mail;

import com.example.auto_ria.configurations.MailConfiguration;
import com.example.auto_ria.enums.EMail;
import com.example.auto_ria.exceptions.CustomException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class FMService {

    private JavaMailSender javaMailSender;
    private Configuration freemarkerConfig;
    private MailConfiguration mailConfig;


    public void sendEmail(String recipientEmail, EMail templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setTo(recipientEmail);

            HashMap<String, String> result = mailConfig.parser(templateName);

            helper.setSubject(result.get("subject"));

            Template template = freemarkerConfig.getTemplate(result.get("templateName"));
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, variables);

            helper.setText(html, true);

            javaMailSender.send(message);
        } catch (Exception e) {
            throw new CustomException(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }
}

