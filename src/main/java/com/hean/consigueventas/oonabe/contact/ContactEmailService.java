package com.hean.consigueventas.oonabe.contact;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class ContactEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Value("${app.mail.to}")
    private String mailTo;

    public ContactEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarSolicitud(ContactEmailRequest request) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(mailFrom);
        message.setTo(mailTo);

        message.setSubject("Nueva solicitud de información - Círculo Oona");

        String tipo;

        if (request.profesional()) {
            tipo = "Soy profesional del bienestar";
        } else {
            tipo = "Interesado en información de Círculo Oona";
        }

        String contenido = """
                Nueva solicitud recibida desde Oona.

                Email:
                %s

                Tipo:
                %s

                El usuario ha solicitado recibir información sobre Círculo Oona.
                """.formatted(
                request.email(),
                tipo
        );

        message.setText(contenido);

        mailSender.send(message);
    }
}