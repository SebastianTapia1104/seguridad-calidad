package com.duoc.backend.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.duoc.backend.dto.MascotaDto;
import com.duoc.backend.entities.GeneroMascota;
import com.duoc.backend.services.MascotaService;

@Controller
public class PortalController {

	private final MascotaService mascotaService;

	public PortalController(MascotaService mascotaService) {
		this.mascotaService = mascotaService;
	}

	@GetMapping("/")
	public String index(Model model) {
		model.addAttribute("mascotas", mascotaService.catalogo());
		return "index";
	}

	@GetMapping("/buscar")
	public String buscar(
			@RequestParam(required = false) String especie,
			@RequestParam(required = false) Integer edad,
			@RequestParam(required = false) String ubicacion,
			@RequestParam(required = false) String genero,
			Model model) {
		GeneroMascota g = null;
		if (genero != null && !genero.isBlank()) {
			try {
				g = GeneroMascota.valueOf(genero);
			} catch (IllegalArgumentException ignored) {
				model.addAttribute("errorGenero", "Género inválido");
			}
		}
		List<MascotaDto> resultados = mascotaService.buscar(especie, edad, ubicacion, g);
		model.addAttribute("mascotas", resultados);
		model.addAttribute("especie", especie);
		model.addAttribute("edad", edad);
		model.addAttribute("ubicacion", ubicacion);
		model.addAttribute("genero", genero);
		return "buscar";
	}
}
