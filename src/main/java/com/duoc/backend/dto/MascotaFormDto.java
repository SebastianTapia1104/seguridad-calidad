package com.duoc.backend.dto;

import com.duoc.backend.entities.EstadoAdopcion;
import com.duoc.backend.entities.GeneroMascota;
import com.duoc.backend.entities.Mascota;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MascotaFormDto(
		Long id,
		@NotBlank String nombre,
		@NotBlank String especie,
		String raza,
		@NotNull @Min(0) Integer edad,
		@NotNull GeneroMascota genero,
		@NotBlank String ubicacion,
		@NotNull EstadoAdopcion estadoAdopcion,
		String fotoUrl) {

	public Mascota aplicar(Mascota existente) {
		existente.setNombre(nombre);
		existente.setEspecie(especie);
		existente.setRaza(raza);
		existente.setEdad(edad);
		existente.setGenero(genero);
		existente.setUbicacion(ubicacion);
		existente.setEstadoAdopcion(estadoAdopcion);
		existente.setFotoUrl(fotoUrl);
		return existente;
	}

	public Mascota toNuevaEntidad() {
		return new Mascota(nombre, especie, raza, edad, genero, ubicacion, estadoAdopcion, fotoUrl);
	}

	public static MascotaFormDto from(Mascota m) {
		return new MascotaFormDto(
				m.getId(),
				m.getNombre(),
				m.getEspecie(),
				m.getRaza(),
				m.getEdad(),
				m.getGenero(),
				m.getUbicacion(),
				m.getEstadoAdopcion(),
				m.getFotoUrl());
	}
}
