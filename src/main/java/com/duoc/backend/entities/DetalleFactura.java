package com.duoc.backend.entities;

import java.math.BigDecimal;

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
@Table(name = "detalle_facturas")
public class DetalleFactura {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "factura_id")
	private Factura factura;

	@Column(nullable = false, length = 200)
	private String concepto;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal monto;

	protected DetalleFactura() {
	}

	public DetalleFactura(String concepto, BigDecimal monto) {
		this.concepto = concepto;
		this.monto = monto;
	}

	public Long getId() {
		return id;
	}

	public Factura getFactura() {
		return factura;
	}

	void setFactura(Factura factura) {
		this.factura = factura;
	}

	public String getConcepto() {
		return concepto;
	}

	public BigDecimal getMonto() {
		return monto;
	}
}
