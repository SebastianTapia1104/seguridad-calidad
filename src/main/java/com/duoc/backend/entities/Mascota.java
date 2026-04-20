package com.duoc.backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mascotas")
public class Mascota {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String nombre;

	@Column(nullable = false, length = 80)
	private String especie;

	@Column(length = 80)
	private String raza;

	@Column(nullable = false)
	private Integer edad;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private GeneroMascota genero;

	@Column(nullable = false, length = 160)
	private String ubicacion;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 24)
	private EstadoAdopcion estadoAdopcion;

	@Column(length = 500)
	private String fotoUrl;

	protected Mascota() {
	}

	/** Para formularios MVC (Thymeleaf). */
	public static Mascota formularioVacio() {
		Mascota m = new Mascota();
		m.setNombre("");
		m.setEspecie("");
		m.setRaza("");
		m.setEdad(0);
		m.setGenero(GeneroMascota.MACHO);
		m.setUbicacion("");
		m.setEstadoAdopcion(EstadoAdopcion.DISPONIBLE);
		m.setFotoUrl("");
		return m;
	}

	public Mascota(String nombre, String especie, String raza, Integer edad, GeneroMascota genero,
			String ubicacion, EstadoAdopcion estadoAdopcion, String fotoUrl) {
		this.nombre = nombre;
		this.especie = especie;
		this.raza = raza;
		this.edad = edad;
		this.genero = genero;
		this.ubicacion = ubicacion;
		this.estadoAdopcion = estadoAdopcion;
		this.fotoUrl = fotoUrl;
	}

	public Long getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getEspecie() {
		return especie;
	}

	public void setEspecie(String especie) {
		this.especie = especie;
	}

	public String getRaza() {
		return raza;
	}

	public void setRaza(String raza) {
		this.raza = raza;
	}

	public Integer getEdad() {
		return edad;
	}

	public void setEdad(Integer edad) {
		this.edad = edad;
	}

	public GeneroMascota getGenero() {
		return genero;
	}

	public void setGenero(GeneroMascota genero) {
		this.genero = genero;
	}

	public String getUbicacion() {
		return ubicacion;
	}

	public void setUbicacion(String ubicacion) {
		this.ubicacion = ubicacion;
	}

	public EstadoAdopcion getEstadoAdopcion() {
		return estadoAdopcion;
	}

	public void setEstadoAdopcion(EstadoAdopcion estadoAdopcion) {
		this.estadoAdopcion = estadoAdopcion;
	}

	public String getFotoUrl() {
		return fotoUrl;
	}

	public void setFotoUrl(String fotoUrl) {
		this.fotoUrl = fotoUrl;
	}
}
