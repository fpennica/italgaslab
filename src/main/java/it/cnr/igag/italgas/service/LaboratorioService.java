package it.cnr.igag.italgas.service;

import it.cnr.igag.italgas.domain.Laboratorio;
import it.cnr.igag.italgas.domain.Operatore;
import it.cnr.igag.italgas.domain.User;
import it.cnr.igag.italgas.repository.LaboratorioRepository;
import it.cnr.igag.italgas.repository.OperatoreRepository;
import it.cnr.igag.italgas.repository.UserRepository;
import it.cnr.igag.italgas.repository.search.LaboratorioSearchRepository;
import it.cnr.igag.italgas.security.SecurityUtils;

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
 * Service Implementation for managing Laboratorio.
 */
@Service
@Transactional
public class LaboratorioService {

    private final Logger log = LoggerFactory.getLogger(LaboratorioService.class);
    
    @Inject
    private LaboratorioRepository laboratorioRepository;
    
    @Inject
    private LaboratorioSearchRepository laboratorioSearchRepository;
    
    @Inject
    private UserRepository userRepository;
    
    @Inject
    private OperatoreRepository operatoreRepository;
    
    /**
     * Save a laboratorio.
     * 
     * @param laboratorio the entity to save
     * @return the persisted entity
     */
    public Laboratorio save(Laboratorio laboratorio) {
        log.debug("Request to save Laboratorio : {}", laboratorio);
        Laboratorio result = laboratorioRepository.save(laboratorio);
        laboratorioSearchRepository.save(result);
        return result;
    }

    /**
     *  Get all the laboratorios.
     *  
     *  @return the list of entities
     */
    @Transactional(readOnly = true) 
    public List<Laboratorio> findAll() {
        log.debug("Request to get all Laboratorios");
        List<Laboratorio> result = laboratorioRepository.findAll();
        return result;
    }

    /**
     *  Get one laboratorio by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true) 
    public Laboratorio findOne(Long id) {
        log.debug("Request to get Laboratorio : {}", id);
        Laboratorio laboratorio = laboratorioRepository.findOne(id);
        
        if(laboratorio == null)
        	return null;
        
        // get current user
        User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        // get operatore
        Operatore op = operatoreRepository.findByUser(user);
        
        // gli amministratori accedono a tutte le cassette e tutti i campioni
        if(SecurityUtils.isCurrentUserInRole("ROLE_ADMIN") ||
        		(laboratorio.getId() == op.getLaboratorio().getId())) {
        	return laboratorio; 
        }
        
        return null;
        
    }

    /**
     *  Delete the  laboratorio by id.
     *  
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Laboratorio : {}", id);
        laboratorioRepository.delete(id);
        laboratorioSearchRepository.delete(id);
    }

    /**
     * Search for the laboratorio corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<Laboratorio> search(String query) {
        log.debug("Request to search Laboratorios for query {}", query);
        return StreamSupport
            .stream(laboratorioSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .collect(Collectors.toList());
    }
}
