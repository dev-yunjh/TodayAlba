package com.example.todayalba.user.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class VerificationMailSender {

    private final JavaMailSender mailSender;
    private final String from;

    public VerificationMailSender(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendCode(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("[TodayAlba] 회원가입 인증 코드");
        message.setText("인증 코드: " + code + "\n5분 안에 입력해 주세요.");

        mailSender.send(message);
    }
}