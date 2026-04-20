package com.duoc.backend.config;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.duoc.backend.entities.EstadoAdopcion;
import com.duoc.backend.entities.GeneroMascota;
import com.duoc.backend.entities.Mascota;
import com.duoc.backend.entities.RolUsuario;
import com.duoc.backend.entities.Usuario;
import com.duoc.backend.entities.Visita;
import com.duoc.backend.repositories.MascotaRepository;
import com.duoc.backend.repositories.UsuarioRepository;
import com.duoc.backend.repositories.VisitaRepository;

@Configuration
public class DataSeeder {

	@Bean
	CommandLineRunner datosIniciales(
			UsuarioRepository usuarioRepository,
			MascotaRepository mascotaRepository,
			VisitaRepository visitaRepository,
			PasswordEncoder passwordEncoder) {
		return args -> {
			if (usuarioRepository.count() == 0) {
				usuarioRepository.save(new Usuario("admin", passwordEncoder.encode("admin123"), RolUsuario.ADMIN));
				usuarioRepository.save(new Usuario("vet", passwordEncoder.encode("vet123"), RolUsuario.VETERINARIO));
				usuarioRepository.save(new Usuario("recep", passwordEncoder.encode("recep123"), RolUsuario.RECEPCIONISTA));
			}
			if (mascotaRepository.count() == 0) {
				mascotaRepository.save(new Mascota(
						"Luna", "Perro", "Mestizo", 3, GeneroMascota.HEMBRA, "Santiago", EstadoAdopcion.DISPONIBLE,
						"https://placehold.co/400x300?text=Luna"));
				mascotaRepository.save(new Mascota(
						"Michi", "Gato", "Siamés", 2, GeneroMascota.MACHO, "Valparaíso", EstadoAdopcion.DISPONIBLE,
						"https://placehold.co/400x300?text=Michi"));
				mascotaRepository.save(new Mascota(
						"Rocky", "Perro", "Labrador", 5, GeneroMascota.MACHO, "Concepción", EstadoAdopcion.EN_TRAMITE,
						"https://placehold.co/400x300?text=Rocky"));
			}
			// Crear visitas de prueba para facturación
			if (visitaRepository.count() == 0) {
				Usuario veterinario = usuarioRepository.findByUsername("vet").orElse(null);
				Mascota luna = mascotaRepository.findAll().stream()
						.filter(m -> "Luna".equals(m.getNombre())).findFirst().orElse(null);
				Mascota michi = mascotaRepository.findAll().stream()
						.filter(m -> "Michi".equals(m.getNombre())).findFirst().orElse(null);
				
				if (veterinario != null && luna != null) {
					visitaRepository.save(new Visita(
							LocalDateTime.now().minusDays(2),
							"Consulta general y vacunación",
							luna,
							veterinario));
				}
				if (veterinario != null && michi != null) {
					visitaRepository.save(new Visita(
							LocalDateTime.now().minusDays(5),
							"Chequeo de rutina",
							michi,
							veterinario));
				}
			}
		};
	}
}
