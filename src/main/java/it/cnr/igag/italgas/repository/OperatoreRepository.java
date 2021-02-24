package it.cnr.igag.italgas.repository;

import it.cnr.igag.italgas.domain.Laboratorio;
import it.cnr.igag.italgas.domain.Operatore;
import it.cnr.igag.italgas.domain.User;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Operatore entity.
 */
@SuppressWarnings("unused")
public interface OperatoreRepository extends JpaRepository<Operatore,Long> {

	Operatore findByUser(User user);
	
	Operatore findOneByUserId(long userId);

	@Query("select operatore from Operatore operatore where operatore.user.login = ?#{principal.username}")
	Operatore findByLogin();
	
	List<Operatore> findByLaboratorio(Laboratorio laboratorio);
	
}
