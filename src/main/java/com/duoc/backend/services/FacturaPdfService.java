package com.duoc.backend.services;

import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Service;

import com.duoc.backend.entities.DetalleFactura;
import com.duoc.backend.entities.Factura;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class FacturaPdfService {

	public byte[] generarPdf(Factura factura) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			Document document = new Document();
			PdfWriter.getInstance(document, baos);
			document.open();

			Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
			Font normal = FontFactory.getFont(FontFactory.HELVETICA, 11);

			document.add(new Paragraph("Factura veterinaria", title));
			document.add(new Paragraph(" "));
			document.add(new Paragraph("Nº factura: " + factura.getId(), normal));
			document.add(new Paragraph("Fecha emisión: " + factura.getFechaEmision(), normal));
			document.add(new Paragraph("Mascota: " + factura.getVisita().getMascota().getNombre(), normal));
			document.add(new Paragraph("Visita: " + factura.getVisita().getFecha() + " — " + factura.getVisita().getMotivo(),
					normal));
			document.add(new Paragraph("Veterinario: " + factura.getVisita().getVeterinario().getUsername(), normal));
			document.add(new Paragraph(" "));
			document.add(new Paragraph("Detalle:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));

			for (DetalleFactura d : factura.getDetalles()) {
				document.add(new Paragraph("- " + d.getConcepto() + " … " + d.getMonto(), normal));
			}
			document.add(new Paragraph(" "));
			document.add(new Paragraph("TOTAL: " + factura.getTotal(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));

			document.close();
			return baos.toByteArray();
		} catch (Exception e) {
			throw new IllegalStateException("No se pudo generar el PDF", e);
		}
	}
}
