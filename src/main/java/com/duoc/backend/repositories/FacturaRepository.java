package com.duoc.backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.duoc.backend.entities.Factura;

public interface FacturaRepository extends JpaRepository<Factura, Long> {

	@Query("""
			SELECT DISTINCT f FROM Factura f
			JOIN FETCH f.visita v
			JOIN FETCH v.mascota
			JOIN FETCH v.veterinario
			LEFT JOIN FETCH f.detalles
			WHERE f.id = :id
			""")
	Optional<Factura> findByIdConRelaciones(@Param("id") Long id);

	boolean existsByVisita_Id(Long visitaId);
}
