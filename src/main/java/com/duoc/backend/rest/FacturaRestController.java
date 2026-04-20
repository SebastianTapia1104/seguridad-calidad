package com.duoc.backend.rest;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.backend.dto.FacturaGenerarRequest;
import com.duoc.backend.entities.Factura;
import com.duoc.backend.services.FacturaMailService;
import com.duoc.backend.services.FacturaPdfService;
import com.duoc.backend.services.FacturaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/facturas")
@PreAuthorize("isAuthenticated()")
public class FacturaRestController {

	private final FacturaService facturaService;
	private final FacturaPdfService facturaPdfService;
	private final FacturaMailService facturaMailService;

	public FacturaRestController(
			FacturaService facturaService,
			FacturaPdfService facturaPdfService,
			FacturaMailService facturaMailService) {
		this.facturaService = facturaService;
		this.facturaPdfService = facturaPdfService;
		this.facturaMailService = facturaMailService;
	}

	@PostMapping("/generar")
	public ResponseEntity<Map<String, Object>> generar(@Valid @RequestBody FacturaGenerarRequest request) {
		Factura f = facturaService.generar(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
				"id", f.getId(),
				"total", f.getTotal(),
				"fechaEmision", f.getFechaEmision()));
	}

	@GetMapping("/{id}/imprimir")
	public ResponseEntity<byte[]> imprimir(@PathVariable Long id) {
		Factura factura = facturaService.obtenerParaPdf(id);
		byte[] pdf = facturaPdfService.generarPdf(factura);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=factura-" + id + ".pdf")
				.contentType(MediaType.APPLICATION_PDF)
				.body(pdf);
	}

	@PostMapping("/{id}/enviar")
	public ResponseEntity<Map<String, String>> enviar(@PathVariable Long id) {
		Factura factura = facturaService.obtenerParaPdf(id);
		byte[] pdf = facturaPdfService.generarPdf(factura);
		facturaMailService.enviarFacturaPdf(factura, pdf);
		return ResponseEntity.ok(Map.of("mensaje", "Solicitud de envío procesada (revisa logs si SMTP no está disponible)"));
	}
}
