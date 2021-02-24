package it.cnr.igag.italgas.service;

import it.cnr.igag.italgas.domain.Campione;
import it.cnr.igag.italgas.domain.Operatore;
import it.cnr.igag.italgas.domain.Pesata;
import it.cnr.igag.italgas.domain.User;
import it.cnr.igag.italgas.domain.enumeration.TipoCampione;
import it.cnr.igag.italgas.repository.CampioneRepository;
import it.cnr.igag.italgas.repository.OperatoreRepository;
import it.cnr.igag.italgas.repository.PesataRepository;
import it.cnr.igag.italgas.repository.UserRepository;
import it.cnr.igag.italgas.repository.search.PesataSearchRepository;
import it.cnr.igag.italgas.security.SecurityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing Pesata.
 */
@Service
@Transactional
public class PesataService {

    private final Logger log = LoggerFactory.getLogger(PesataService.class);
    
    @Inject
    private UserRepository userRepository;
    
    @Inject
    private OperatoreRepository operatoreRepository;
    
    @Inject
    private PesataRepository pesataRepository;
    
    @Inject
    private PesataSearchRepository pesataSearchRepository;
    
    @Inject
    private CampioneRepository campioneRepository;
    
    /**
     * Save a pesata.
     * 
     * @param pesata the entity to save
     * @return the persisted entity
     */
    public Pesata save(Pesata pesata) {
        log.debug("Request to save Pesata : {}", pesata);
        Pesata result = pesataRepository.save(pesata);
        pesataSearchRepository.save(result);
        return result;
    }

    /**
     *  Get all the pesatas.
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true) 
    public Page<Pesata> findAll(Pageable pageable) {
        log.debug("Request to get all Pesatas");
        Page<Pesata> result = pesataRepository.findAll(pageable); 
        return result;
    }
    
    @Transactional(readOnly = true)
    public List<Pesata> findByCampione(Long idCampione) {
    	log.debug("Request to get all Pesatas (not paged) by Campione with id: {}", idCampione);
    	Campione campione = campioneRepository.findOne(idCampione);
    	
    	if(campione.getTipoCampione() == TipoCampione.CB_A || campione.getTipoCampione() == TipoCampione.CB_B) {
    		//caso CB usura (per il binder viene chiamato direttamente findByCampione(Long idCampione, Boolean binder) )
    		return findByCampione(idCampione, false);
    	}
    	
    	// get current user
        User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        // get operatore
        Operatore op = operatoreRepository.findByUser(user);
    	
        // gli amministratori accedono a tutte le cassette e tutti i campioni
        if(SecurityUtils.isCurrentUserInRole("ROLE_ADMIN") ||
        		(campione.getCassetta().getConsegna().getLaboratorio().getId() == op.getLaboratorio().getId())) {
        	
        	List<Pesata> result = pesataRepository.findByCampioneOrderByNumPesataAsc(campione);
        	
        	// annullo i blob
        	resetBlobs(result);
        	
        	return result;
        }
        
        return null;
    }
    
    @Transactional(readOnly = true)
    public List<Pesata> findByCampione(Long idCampione, Boolean binder) {
    	log.debug("Request to get all Pesatas (not paged) by Campione with id: {}", idCampione);
    	Campione campione = campioneRepository.findOne(idCampione);
    	
    	// get current user
        User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        // get operatore
        Operatore op = operatoreRepository.findByUser(user);
    	
        // gli amministratori accedono a tutte le cassette e tutti i campioni
        if(SecurityUtils.isCurrentUserInRole("ROLE_ADMIN") ||
        		(campione.getCassetta().getConsegna().getLaboratorio().getId() == op.getLaboratorio().getId())) {
        	
        	List<Pesata> result = new ArrayList<Pesata>();
        	if(binder) {
        		result = pesataRepository.findByCampioneAndBinderTrueOrderByNumPesataAsc(campione);
        	} else {
        		result = pesataRepository.findByCampioneAndBinderFalseOrderByNumPesataAsc(campione);
        	}
        		
        	
        	// annullo i blob
        	resetBlobs(result);
        	
        	return result;
        }
        
        return null;
    }

    /**
     *  Get one pesata by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true) 
    public Pesata findOne(Long id) {
        log.debug("Request to get Pesata : {}", id);
        Pesata pesata = pesataRepository.findOne(id);
        return pesata;
    }

    /**
     *  Delete the  pesata by id.
     *  
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Pesata : {}", id);
        pesataRepository.delete(id);
        pesataSearchRepository.delete(id);
    }

    public Page<Pesata> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Pesatas for query {}", query);
        return pesataSearchRepository.search(queryStringQuery(query), pageable);
    }
    
    private void resetBlobs(List<Pesata> list) {
    	list.forEach(pesata-> resetBlobs(pesata));
	}
    
    private void resetBlobs(Page<Pesata> page) {
		page.forEach(pesata-> resetBlobs(pesata));
	}
	
	public Pesata resetBlobs(Pesata pesata) {
		pesata.getCampione().setFotoSacchetto(null);
		pesata.getCampione().setFotoCampione(null);
		pesata.getCampione().setFotoEtichetta(null);
		pesata.getCampione().setCurva(null);
		pesata.getCampione().setCurvaBinder(null);
		pesata.getCampione().getCassetta().setFotoContenitore(null);
		pesata.getCampione().getCassetta().setFotoContenuto(null);
		pesata.getCampione().getCassetta().setCertificatoPdf(null);
		pesata.getCampione().getCassetta().getConsegna().setProtocolloAccettazione(null);
		return pesata;
	}
}
