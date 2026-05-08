package com.duoc.backend.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.duoc.backend.dto.MascotaFormDto;
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
		MascotaFormDto form;
        if (edit != null) {
            Mascota mascotaDB = mascotaRepository.findById(edit)
                    .orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada"));
            form = MascotaFormDto.from(mascotaDB);
        } else {
            form = MascotaFormDto.from(Mascota.formularioVacio()); 
        }
		model.addAttribute("form", form);
		model.addAttribute("generos", GeneroMascota.values());
		model.addAttribute("estados", EstadoAdopcion.values());
		return "admin/mascotas";
	}

	@PostMapping("/guardar")
	public String guardar(@ModelAttribute("form") MascotaFormDto formDto, BindingResult result, RedirectAttributes ra) {
		if (result.hasErrors()) {
            return "admin/mascotas";
        }
		Mascota mascota;
        boolean nueva = formDto.id() == null; 
        if (nueva) {
            mascota = formDto.toNuevaEntidad();
        } else {
            mascota = mascotaRepository.findById(formDto.id()).orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada"));
            mascota = formDto.aplicar(mascota);
        }
        mascotaRepository.save(mascota);
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
