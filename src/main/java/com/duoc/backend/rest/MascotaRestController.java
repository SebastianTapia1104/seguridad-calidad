package com.duoc.backend.rest;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.backend.dto.MascotaDto;
import com.duoc.backend.dto.MascotaFormDto;
import com.duoc.backend.entities.GeneroMascota;
import com.duoc.backend.services.MascotaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaRestController {

	private final MascotaService mascotaService;

	public MascotaRestController(MascotaService mascotaService) {
		this.mascotaService = mascotaService;
	}

	@GetMapping("/catalogo")
	public ResponseEntity<List<MascotaDto>> catalogo() {
		return ResponseEntity.ok(mascotaService.catalogo());
	}

	@GetMapping("/buscar")
	public ResponseEntity<List<MascotaDto>> buscar(
			@RequestParam(required = false) String especie,
			@RequestParam(required = false) Integer edad,
			@RequestParam(required = false) String ubicacion,
			@RequestParam(required = false) GeneroMascota genero) {
		return ResponseEntity.ok(mascotaService.buscar(especie, edad, ubicacion, genero));
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN','VETERINARIO','RECEPCIONISTA')")
	public ResponseEntity<MascotaDto> crear(@Valid @RequestBody MascotaFormDto dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(mascotaService.crear(dto));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','VETERINARIO','RECEPCIONISTA')")
	public ResponseEntity<MascotaDto> actualizar(@PathVariable Long id, @Valid @RequestBody MascotaFormDto dto) {
		return ResponseEntity.ok(mascotaService.actualizar(id, dto));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','VETERINARIO','RECEPCIONISTA')")
	public ResponseEntity<Map<String, String>> eliminar(@PathVariable Long id) {
		mascotaService.eliminar(id);
		return ResponseEntity.ok(Map.of("mensaje", "Mascota eliminada"));
	}
}
