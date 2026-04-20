package com.duoc.backend.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.duoc.backend.dto.MascotaDto;
import com.duoc.backend.dto.MascotaFormDto;
import com.duoc.backend.entities.GeneroMascota;
import com.duoc.backend.entities.Mascota;
import com.duoc.backend.repositories.MascotaRepository;

@Service
public class MascotaService {

	private final MascotaRepository mascotaRepository;

	public MascotaService(MascotaRepository mascotaRepository) {
		this.mascotaRepository = mascotaRepository;
	}

	@Transactional(readOnly = true)
	public List<MascotaDto> catalogo() {
		return mascotaRepository.findAll().stream().map(MascotaDto::from).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<MascotaDto> buscar(String especie, Integer edad, String ubicacion, GeneroMascota genero) {
		String e = blankToNull(especie);
		String u = blankToNull(ubicacion);
		return mascotaRepository.buscar(e, edad, u, genero).stream().map(MascotaDto::from).collect(Collectors.toList());
	}

	@Transactional
	public MascotaDto crear(MascotaFormDto dto) {
		Mascota guardada = mascotaRepository.save(dto.toNuevaEntidad());
		return MascotaDto.from(guardada);
	}

	@Transactional
	public MascotaDto actualizar(Long id, MascotaFormDto dto) {
		Mascota existente = mascotaRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada: " + id));
		return MascotaDto.from(mascotaRepository.save(dto.aplicar(existente)));
	}

	@Transactional
	public void eliminar(Long id) {
		if (!mascotaRepository.existsById(id)) {
			throw new IllegalArgumentException("Mascota no encontrada: " + id);
		}
		mascotaRepository.deleteById(id);
	}

	private static String blankToNull(String s) {
		return s == null || s.isBlank() ? null : s.trim();
	}
}
