package it.cnr.igag.italgas.service;

import it.cnr.igag.italgas.domain.Laboratorio;
import it.cnr.igag.italgas.domain.Operatore;
import it.cnr.igag.italgas.repository.OperatoreRepository;
import it.cnr.igag.italgas.repository.search.OperatoreSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing Operatore.
 */
@Service
@Transactional
public class OperatoreService {

    private final Logger log = LoggerFactory.getLogger(OperatoreService.class);
    
    @Inject
    private OperatoreRepository operatoreRepository;
    
    @Inject
    private OperatoreSearchRepository operatoreSearchRepository;
    
    @Inject
    private LaboratorioService laboratorioService;
    
    /**
     * Save a operatore.
     * 
     * @param operatore the entity to save
     * @return the persisted entity
     */
    public Operatore save(Operatore operatore) {
        log.debug("Request to save Operatore : {}", operatore);
        Operatore result = operatoreRepository.save(operatore);
        operatoreSearchRepository.save(result);
        return result;
    }

    /**
     *  Get all the operatores.
     *  
     *  @return the list of entities
     */
    @Transactional(readOnly = true) 
    public List<Operatore> findAll() {
        log.debug("Request to get all Operatores");
        List<Operatore> result = operatoreRepository.findAll();
        return result;
    }
    
    @Transactional(readOnly = true) 
    public List<Operatore> findByCurrentLaboratorio() {
        log.debug("Request to get Operatores by current Laboratorio");
        
        // get current user
        //User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        // get operatore
        //Operatore op = operatoreRepository.findByUser(user);
        Operatore op = operatoreRepository.findByLogin();
        
        List<Operatore> result = operatoreRepository.findByLaboratorio(op.getLaboratorio());
        return result;
    }
    
    @Transactional(readOnly = true) 
    public List<Operatore> findByLaboratorio(Long id) {
        log.debug("Request to get Operatores by Laboratorio");
        
        Laboratorio laboratorio = laboratorioService.findOne(id);
        
        List<Operatore> result = operatoreRepository.findByLaboratorio(laboratorio);
        return result;
    }
    
    @Transactional(readOnly = true) 
    public Operatore findOneByUserId(Long idUser) {
    	log.debug("Request to get Operatore by user id : {}", idUser);
        Operatore operatore = operatoreRepository.findOneByUserId(idUser);
        return operatore;
    }

    /**
     *  Get one operatore by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true) 
    public Operatore findOne(Long id) {
        log.debug("Request to get Operatore : {}", id);
        Operatore operatore = operatoreRepository.findOne(id);
        return operatore;
    }

    /**
     *  Delete the  operatore by id.
     *  
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Operatore : {}", id);
        operatoreRepository.delete(id);
        operatoreSearchRepository.delete(id);
    }

    /**
     * Search for the operatore corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<Operatore> search(String query) {
        log.debug("Request to search Operatores for query {}", query);
        return StreamSupport
            .stream(operatoreSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
	public Operatore findByLogin() {
    	Operatore operatore = operatoreRepository.findByLogin();
        return operatore;
	}
}
