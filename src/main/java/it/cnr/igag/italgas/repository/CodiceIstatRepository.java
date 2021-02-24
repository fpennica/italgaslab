package it.cnr.igag.italgas.repository;

import it.cnr.igag.italgas.domain.CodiceIstat;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the CodiceIstat entity.
 */
public interface CodiceIstatRepository extends JpaRepository<CodiceIstat,Long> {
	
	List<CodiceIstat> findByRegioneIgnoreCaseContainingAndProvinciaIgnoreCaseContainingAndComuneIgnoreCaseContaining(String regione, String provincia, String comune);
	
	List<CodiceIstat> findByRegioneIgnoreCaseContainingAndProvinciaIgnoreCaseContainingAndComuneIgnoreCase(String regione, String provincia, String comune);
	
	List<CodiceIstat> findByComuneIgnoreCaseContaining(String comune);
	
}
