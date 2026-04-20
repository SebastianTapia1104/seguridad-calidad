package com.duoc.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record FacturaGenerarRequest(
		Long visitaId,
		LocalDateTime fecha,
		String motivo,
		Long mascotaId,
		Long veterinarioId,
		@NotEmpty @Valid List<LineaFacturaDto> lineas) {

	public boolean usaVisitaExistente() {
		return visitaId != null;
	}
}
