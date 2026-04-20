package com.duoc.backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.duoc.backend.entities.Factura;

import jakarta.mail.internet.MimeMessage;

@Service
public class FacturaMailService {

	private static final Logger log = LoggerFactory.getLogger(FacturaMailService.class);

	private final JavaMailSender mailSender;

	@Value("${app.mail.from}")
	private String from;

	@Value("${app.mail.cliente-default}")
	private String clienteDefault;

	public FacturaMailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void enviarFacturaPdf(Factura factura, byte[] pdf) {
		String destino = clienteDefault;
		String asunto = "Factura #" + factura.getId() + " — " + factura.getVisita().getMascota().getNombre();
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom(from);
			helper.setTo(destino);
			helper.setSubject(asunto);
			helper.setText("Adjuntamos la factura de la visita realizada.", false);
			helper.addAttachment("factura-" + factura.getId() + ".pdf", new ByteArrayResource(pdf),
					"application/pdf");
			mailSender.send(message);
			log.info("Correo de factura {} enviado (simulado o real) a {}", factura.getId(), destino);
		} catch (Exception e) {
			log.warn("No se pudo enviar correo (SMTP no disponible o error). Factura {}: {}", factura.getId(),
					e.getMessage());
			log.debug("Detalle envío correo", e);
		}
	}
}
