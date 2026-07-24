package com.ecommerce.user.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String sender;
    private final String verificationBaseUrl;

    public EmailService(
            JavaMailSender mailSender,
            TemplateEngine templateEngine,
            @Value("${spring.mail.username}") String sender,
            @Value("${app.verification-base-url}") String verificationBaseUrl
    ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.sender = sender;
        this.verificationBaseUrl = verificationBaseUrl;
    }

    @Async("mailTaskExecutor")
    @Retryable(
            retryFor = {MailException.class, MessagingException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 5_000)
    )
    public void sendVerificationEmail(
            String recipient,
            String otp,
            Duration validity,
            String verificationToken
    ) throws MessagingException {
        log.info("Sending verification email to {}", maskEmail(recipient));

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                message, false, StandardCharsets.UTF_8.name());
        helper.setFrom(sender);
        helper.setTo(recipient);
        helper.setSubject("Verify your Ecommerce Platform account");
        helper.setText(renderVerificationEmail(otp, validity, verificationToken), true);
        mailSender.send(message);

        log.info("Verification email sent to {}", maskEmail(recipient));
    }

    String renderVerificationEmail(String otp, Duration validity, String verificationToken) {
        String verificationUrl = UriComponentsBuilder
                .fromUriString(verificationBaseUrl)
                .queryParam("token", verificationToken)
                .build()
                .encode()
                .toUriString();

        Context context = new Context();
        context.setVariable("otp", otp);
        context.setVariable("expirationMinutes", validity.toMinutes());
        context.setVariable("verificationUrl", verificationUrl);
        return templateEngine.process("otp-verification", context);
    }

    @Recover
    public void recover(
            Exception exception,
            String recipient,
            String otp,
            Duration validity,
            String verificationToken
    ) {
        log.error(
                "Could not send verification email to {} after 3 attempts",
                maskEmail(recipient),
                exception
        );
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(email.indexOf('@'));
    }
}
