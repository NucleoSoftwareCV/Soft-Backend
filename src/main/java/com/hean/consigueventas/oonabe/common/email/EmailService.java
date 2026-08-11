package com.hean.consigueventas.oonabe.common.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final boolean smtpConfigured;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String fromAddress,
            @Value("${spring.mail.host:}") String mailHost) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.smtpConfigured = mailHost != null && !mailHost.isBlank();
    }

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        if (!smtpConfigured) {
            // Sin SMTP configurado (ej. entorno local sin credenciales todavia):
            // no bloqueamos el flujo, solo dejamos constancia en el log.
            logger.warn("SMTP no configurado; no se envio el email de recuperacion a {}. Enlace: {}", toEmail, resetLink);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Recupera tu contraseña de Oona");
        message.setText("""
                Hola,

                Recibimos una solicitud para restablecer tu contraseña de Oona.

                Si fuiste tú, usa este enlace para elegir una nueva contraseña (válido por 1 hora):
                %s

                Si no solicitaste esto, puedes ignorar este mensaje.

                — El equipo de Oona
                """.formatted(resetLink));

        mailSender.send(message);
    }
}
