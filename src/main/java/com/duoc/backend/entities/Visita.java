package com.duoc.backend.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "visitas")
public class Visita {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private LocalDateTime fecha;

	@Column(nullable = false, length = 500)
	private String motivo;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "mascota_id")
	private Mascota mascota;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "veterinario_id")
	private Usuario veterinario;

	protected Visita() {
	}

	public Visita(LocalDateTime fecha, String motivo, Mascota mascota, Usuario veterinario) {
		this.fecha = fecha;
		this.motivo = motivo;
		this.mascota = mascota;
		this.veterinario = veterinario;
	}

	public Long getId() {
		return id;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public String getMotivo() {
		return motivo;
	}

	public Mascota getMascota() {
		return mascota;
	}

	public Usuario getVeterinario() {
		return veterinario;
	}
}
