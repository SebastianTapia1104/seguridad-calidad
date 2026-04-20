package com.duoc.backend.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.duoc.backend.entities.EstadoAdopcion;
import com.duoc.backend.entities.GeneroMascota;
import com.duoc.backend.entities.Mascota;
import com.duoc.backend.repositories.MascotaRepository;

@Controller
@RequestMapping("/admin/mascotas")
public class AdminMascotaController {

	private final MascotaRepository mascotaRepository;

	public AdminMascotaController(MascotaRepository mascotaRepository) {
		this.mascotaRepository = mascotaRepository;
	}

	@GetMapping
	public String listar(@RequestParam(required = false) Long edit, Model model) {
		model.addAttribute("mascotas", mascotaRepository.findAll());
		Mascota form;
		if (edit != null) {
			form = mascotaRepository.findById(edit)
					.orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada"));
		} else {
			form = Mascota.formularioVacio();
		}
		model.addAttribute("form", form);
		model.addAttribute("generos", GeneroMascota.values());
		model.addAttribute("estados", EstadoAdopcion.values());
		return "admin/mascotas";
	}

	@PostMapping("/guardar")
	public String guardar(@ModelAttribute("form") Mascota form, RedirectAttributes ra) {
		boolean nueva = form.getId() == null;
		mascotaRepository.save(form);
		ra.addFlashAttribute("msg", nueva ? "Mascota creada" : "Mascota actualizada");
		return "redirect:/admin/mascotas";
	}

	@PostMapping("/{id}/eliminar")
	public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
		mascotaRepository.deleteById(id);
		ra.addFlashAttribute("msg", "Mascota eliminada");
		return "redirect:/admin/mascotas";
	}
}
