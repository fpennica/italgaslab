package it.cnr.igag.italgas.repository;

import it.cnr.igag.italgas.domain.Campione;
import it.cnr.igag.italgas.domain.Cassetta;
import it.cnr.igag.italgas.domain.Laboratorio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Campione entity.
 */
@SuppressWarnings("unused")
public interface CampioneRepository extends JpaRepository<Campione,Long> {

	@Query("select campione from Campione campione where campione.cassetta.consegna.laboratorio = ?1")
	Page<Campione> findByLaboratorio(Pageable pageable, Laboratorio laboratorio);

	Page<Campione> findByCassetta(Pageable pageable, Cassetta cassetta);
	
	List<Campione> findByCassetta(Cassetta cassetta);

	@Query("SELECT COUNT(c) FROM Campione c WHERE c.cassetta.consegna.laboratorio=?1")
	Long countByLaboratorio(Laboratorio laboratorio);
}
