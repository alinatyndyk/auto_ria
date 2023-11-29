package com.example.auto_ria.configurations;

import com.example.auto_ria.enums.EMail;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@AllArgsConstructor
public class MailConfiguration {

    public HashMap<String, String> parser(EMail templateName) {

        HashMap<EMail, HashMap<String, String>> emails = new HashMap<>();

        emails.put(EMail.WELCOME, new HashMap<>() {{
            put("subject", "Welcome to our platform!");
            put("templateName", "welcome.ftl");
        }});

        emails.put(EMail.REGISTER, new HashMap<>() {{
            put("subject", "Please register your account!");
            put("templateName", "register.ftl");
        }});

        emails.put(EMail.PREMIUM_END, new HashMap<>() {{
            put("subject", "Premium end!");
            put("templateName", "premium-end.ftl");
        }});

        emails.put(EMail.REGISTER_KEY, new HashMap<>() {{
            put("subject", "Register key");
            put("templateName", "register-key.ftl");
        }});

        emails.put(EMail.PAYMENT_FAILED, new HashMap<>() {{
            put("subject", "Subscription suspended");
            put("templateName", "payment-failed.ftl");
        }});

        emails.put(EMail.PLATFORM_LEAVE, new HashMap<>() {{
            put("subject", "We are sorry to see you leave the platform...");
            put("templateName", "platform-leave.ftl");
        }});

        emails.put(EMail.YOUR_ACCOUNT_BANNED, new HashMap<>() {{
            put("subject", "Your account has been banned permanently");
            put("templateName", "your-account-banned.ftl");
        }});

        emails.put(EMail.CAR_BEING_CHECKED, new HashMap<>() {{
            put("subject", "Your announcement is being checked by AutoRia Team");
            put("templateName", "car-being-checked.ftl");
        }});

        emails.put(EMail.CAR_BEING_BANNED, new HashMap<>() {{
            put("subject", "Your announcement is banned from the platform");
            put("templateName", "car-being-banned.ftl");
        }});

        emails.put(EMail.CAR_BEING_ACTIVATED, new HashMap<>() {{
            put("subject", "Your announcement is activated");
            put("templateName", "car-being-activated.ftl");
        }});

        emails.put(EMail.CHECK_ANNOUNCEMENT, new HashMap<>() {{
            put("subject", "New announcement need to be checked");
            put("templateName", "check-announcement.ftl");
        }});

        emails.put(EMail.FORGOT_PASSWORD, new HashMap<>() {{
            put("subject", "Password restore");
            put("templateName", "forgot-password.ftl");
        }});

        return emails.get(templateName);

    }
}
