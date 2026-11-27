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
        message.setText("인증 코드: " + code + "\n인증번호는 발급 후 2분 30초 동안 유효합니다.");

        mailSender.send(message);
    }
}
