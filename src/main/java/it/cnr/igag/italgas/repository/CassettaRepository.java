package it.cnr.igag.italgas.repository;

import it.cnr.igag.italgas.domain.Cassetta;
import it.cnr.igag.italgas.domain.Consegna;
import it.cnr.igag.italgas.domain.Laboratorio;
import it.cnr.igag.italgas.domain.Operatore;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Cassetta entity.
 */
@SuppressWarnings("unused")
public interface CassettaRepository extends JpaRepository<Cassetta,Long> {

	@Query("select cassetta from Cassetta cassetta where cassetta.consegna.laboratorio = ?1")
	Page<Cassetta> findByLaboratorio(Pageable pageable, Laboratorio laboratorio);
	
	@Query("select cassetta from Cassetta cassetta where cassetta.consegna.laboratorio = ?1")
	List<Cassetta> findByLaboratorio(Laboratorio laboratorio);

	//@Query("select cassetta from Cassetta cassetta where cassetta.consegna = ?1")
	Page<Cassetta> findByConsegna(Pageable pageable, Consegna consegna);
	
	List<Cassetta> findByConsegnaOrderByRifGeografo(Consegna consegna);

	//@Query("select cassetta from Cassetta cassetta where cassetta.consegna = ?1 and cassetta.consegna.laboratorio = ?2")
	//Page<Cassetta> findByConsegnaAndLaboratorio(Pageable pageable, Consegna consegna, Laboratorio laboratorio);

//	@Query("select cassetta from Cassetta cassetta where cassetta.laboratorio.operatore.user.login = ?#{principal.username}")
//	Page<Cassetta> findByUserIsCurrentUser(Pageable pageable);
	
	@Query("SELECT COUNT(c) FROM Cassetta c WHERE c.consegna.laboratorio=?1")
	Long countByLaboratorio(Laboratorio laboratorio);
}
