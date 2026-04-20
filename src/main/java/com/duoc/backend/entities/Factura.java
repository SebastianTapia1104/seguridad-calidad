package com.duoc.backend.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "facturas")
public class Factura {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "visita_id", unique = true, nullable = false)
	private Visita visita;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal total;

	@Column(nullable = false)
	private Instant fechaEmision;

	@OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DetalleFactura> detalles = new ArrayList<>();

	protected Factura() {
	}

	public Factura(Visita visita, BigDecimal total, Instant fechaEmision) {
		this.visita = visita;
		this.total = total;
		this.fechaEmision = fechaEmision;
	}

	public Long getId() {
		return id;
	}

	public Visita getVisita() {
		return visita;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public Instant getFechaEmision() {
		return fechaEmision;
	}

	public List<DetalleFactura> getDetalles() {
		return detalles;
	}

	public void agregarDetalle(DetalleFactura detalle) {
		detalles.add(detalle);
		detalle.setFactura(this);
	}
}
