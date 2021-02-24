package it.cnr.igag.italgas.repository;

import it.cnr.igag.italgas.domain.Consegna;
import it.cnr.igag.italgas.domain.Laboratorio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for the Consegna entity.
 */
public interface ConsegnaRepository extends JpaRepository<Consegna,Long> {

	Page<Consegna> findByLaboratorio(Pageable pageable, Laboratorio laboratorio);
	
	List<Consegna> findByLaboratorio(Laboratorio laboratorio);
	
	Consegna findByLaboratorioAndDataConsegna(Laboratorio laboratorio, LocalDate date);
	
	Long countByLaboratorio(Laboratorio laboratorio);
}
