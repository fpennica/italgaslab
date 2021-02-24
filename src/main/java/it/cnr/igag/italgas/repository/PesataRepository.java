package it.cnr.igag.italgas.repository;

import it.cnr.igag.italgas.domain.Campione;
import it.cnr.igag.italgas.domain.Pesata;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Pesata entity.
 */
@SuppressWarnings("unused")
public interface PesataRepository extends JpaRepository<Pesata,Long> {
	
	List<Pesata> findByCampioneOrderByNumPesataAsc(Campione campione);
	
	List<Pesata> findByCampioneAndBinderTrueOrderByNumPesataAsc(Campione campione);
	
	List<Pesata> findByCampioneAndBinderFalseOrderByNumPesataAsc(Campione campione);
	
}
