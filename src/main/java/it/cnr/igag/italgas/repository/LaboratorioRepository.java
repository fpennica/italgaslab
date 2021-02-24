package it.cnr.igag.italgas.repository;

import it.cnr.igag.italgas.domain.Laboratorio;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Laboratorio entity.
 */
@SuppressWarnings("unused")
public interface LaboratorioRepository extends JpaRepository<Laboratorio,Long> {
	List<Laboratorio> findAllByOrderByIstituto();
}
