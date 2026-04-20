package com.duoc.backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LineaFacturaDto(
		@NotBlank String concepto,
		@NotNull BigDecimal monto) {
}
