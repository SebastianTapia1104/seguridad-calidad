package com.duoc.backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.duoc.backend.entities.GeneroMascota;
import com.duoc.backend.entities.Mascota;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {

	@Query("""
			SELECT m FROM Mascota m
			WHERE (:especie IS NULL OR LOWER(m.especie) = LOWER(:especie))
			  AND (:edad IS NULL OR m.edad = :edad)
			  AND (:ubicacion IS NULL OR LOWER(m.ubicacion) LIKE LOWER(CONCAT('%', :ubicacion, '%')))
			  AND (:genero IS NULL OR m.genero = :genero)
			""")
	List<Mascota> buscar(
			@Param("especie") String especie,
			@Param("edad") Integer edad,
			@Param("ubicacion") String ubicacion,
			@Param("genero") GeneroMascota genero);
}
