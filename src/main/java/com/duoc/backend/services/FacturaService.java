package com.duoc.backend.services;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.duoc.backend.dto.FacturaGenerarRequest;
import com.duoc.backend.dto.LineaFacturaDto;
import com.duoc.backend.entities.DetalleFactura;
import com.duoc.backend.entities.Factura;
import com.duoc.backend.entities.Mascota;
import com.duoc.backend.entities.RolUsuario;
import com.duoc.backend.entities.Usuario;
import com.duoc.backend.entities.Visita;
import com.duoc.backend.repositories.FacturaRepository;
import com.duoc.backend.repositories.MascotaRepository;
import com.duoc.backend.repositories.UsuarioRepository;
import com.duoc.backend.repositories.VisitaRepository;

@Service
public class FacturaService {

	private final FacturaRepository facturaRepository;
	private final VisitaRepository visitaRepository;
	private final MascotaRepository mascotaRepository;
	private final UsuarioRepository usuarioRepository;

	public FacturaService(
			FacturaRepository facturaRepository,
			VisitaRepository visitaRepository,
			MascotaRepository mascotaRepository,
			UsuarioRepository usuarioRepository) {
		this.facturaRepository = facturaRepository;
		this.visitaRepository = visitaRepository;
		this.mascotaRepository = mascotaRepository;
		this.usuarioRepository = usuarioRepository;
	}

	@Transactional
	public Factura generar(FacturaGenerarRequest request) {
		List<LineaFacturaDto> lineas = request.lineas();
		if (lineas.isEmpty()) {
			throw new IllegalArgumentException("Debe incluir al menos una línea de detalle");
		}
		BigDecimal total = lineas.stream()
				.map(LineaFacturaDto::monto)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		Visita visita;
		if (request.usaVisitaExistente()) {
			visita = visitaRepository.findById(request.visitaId())
					.orElseThrow(() -> new IllegalArgumentException("Visita no encontrada: " + request.visitaId()));
			if (facturaRepository.existsByVisita_Id(visita.getId())) {
				throw new IllegalArgumentException("Ya existe factura para esta visita");
			}
		} else {
			if (request.fecha() == null || request.motivo() == null || request.motivo().isBlank()
					|| request.mascotaId() == null || request.veterinarioId() == null) {
				throw new IllegalArgumentException(
						"Para nueva visita se requiere fecha, motivo, mascotaId y veterinarioId");
			}
			Mascota mascota = mascotaRepository.findById(request.mascotaId())
					.orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada"));
			Usuario veterinario = usuarioRepository.findById(request.veterinarioId())
					.orElseThrow(() -> new IllegalArgumentException("Veterinario no encontrado"));
			if (veterinario.getRol() != RolUsuario.VETERINARIO && veterinario.getRol() != RolUsuario.ADMIN) {
				throw new IllegalArgumentException("El usuario no es un veterinario válido");
			}
			visita = visitaRepository.save(new Visita(request.fecha(), request.motivo(), mascota, veterinario));
		}

		Factura factura = new Factura(visita, total, Instant.now());
		for (LineaFacturaDto linea : lineas) {
			factura.agregarDetalle(new DetalleFactura(linea.concepto(), linea.monto()));
		}
		return facturaRepository.save(factura);
	}

	@Transactional(readOnly = true)
	public Factura obtenerParaPdf(Long id) {
		return facturaRepository.findByIdConRelaciones(id)
				.orElseThrow(() -> new IllegalArgumentException("Factura no encontrada: " + id));
	}
}
