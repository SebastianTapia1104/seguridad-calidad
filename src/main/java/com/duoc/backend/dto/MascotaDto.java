package com.duoc.backend.dto;

import com.duoc.backend.entities.EstadoAdopcion;
import com.duoc.backend.entities.GeneroMascota;
import com.duoc.backend.entities.Mascota;

public record MascotaDto(
		Long id,
		String nombre,
		String especie,
		String raza,
		Integer edad,
		GeneroMascota genero,
		String ubicacion,
		EstadoAdopcion estadoAdopcion,
		String fotoUrl) {

	public static MascotaDto from(Mascota m) {
		return new MascotaDto(
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

	public Mascota toEntity() {
		return new Mascota(nombre, especie, raza, edad, genero, ubicacion, estadoAdopcion, fotoUrl);
	}
}
