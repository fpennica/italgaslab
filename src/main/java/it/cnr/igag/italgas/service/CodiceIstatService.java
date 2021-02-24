package it.cnr.igag.italgas.service;

import it.cnr.igag.italgas.domain.CodiceIstat;
import it.cnr.igag.italgas.repository.CodiceIstatRepository;
import it.cnr.igag.italgas.repository.search.CodiceIstatSearchRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing CodiceIstat.
 */
@Service
@Transactional
public class CodiceIstatService {

    private final Logger log = LoggerFactory.getLogger(CodiceIstatService.class);
    
    @Inject
    private CodiceIstatRepository codiceIstatRepository;
    
    @Inject
    private CodiceIstatSearchRepository codiceIstatSearchRepository;
    
    /**
     * Save a codiceIstat.
     * 
     * @param codiceIstat the entity to save
     * @return the persisted entity
     */
    public CodiceIstat save(CodiceIstat codiceIstat) {
        log.debug("Request to save CodiceIstat : {}", codiceIstat);
        CodiceIstat result = codiceIstatRepository.save(codiceIstat);
        codiceIstatSearchRepository.save(result);
        return result;
    }

    /**
     *  Get all the codiceIstats.
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true) 
    public Page<CodiceIstat> findAll(Pageable pageable) {
        log.debug("Request to get all CodiceIstats");
        Page<CodiceIstat> result = codiceIstatRepository.findAll(pageable); 
        return result;
    }

    /**
     *  Get one codiceIstat by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true) 
    public CodiceIstat findOne(Long id) {
        log.debug("Request to get CodiceIstat : {}", id);
        CodiceIstat codiceIstat = codiceIstatRepository.findOne(id);
        return codiceIstat;
    }
    
    @Transactional(readOnly = true) 
    public List<CodiceIstat> findByRegioneIgnoreCaseContainingAndProvinciaIgnoreCaseContainingAndComuneIgnoreCaseContaining(String regione, String provincia, String comune) {
        log.debug("Request to get a List of CodiceIstat by Regione: {}, Provincia: {}, Comune: {}", regione, provincia, comune);
        List<CodiceIstat> codiceIstatList = codiceIstatRepository.findByRegioneIgnoreCaseContainingAndProvinciaIgnoreCaseContainingAndComuneIgnoreCaseContaining(regione, provincia, comune);
        return codiceIstatList;
    }

    @Transactional(readOnly = true) 
    public List<CodiceIstat> findByRegioneIgnoreCaseContainingAndProvinciaIgnoreCaseContainingAndComuneIgnoreCase(String regione, String provincia, String comune) {
        log.debug("Request to get a List of CodiceIstat by Regione: {}, Provincia: {}, Comune: {}", regione, provincia, comune);
        List<CodiceIstat> codiceIstatList = codiceIstatRepository.findByRegioneIgnoreCaseContainingAndProvinciaIgnoreCaseContainingAndComuneIgnoreCase(regione, provincia, comune);
        return codiceIstatList;
    }
    
    @Transactional(readOnly = true) 
    public List<CodiceIstat> findByComuneIgnoreCaseContaining(String comune) {
        List<CodiceIstat> codiceIstatList = codiceIstatRepository.findByComuneIgnoreCaseContaining(comune);
        return codiceIstatList;
    }
    
    /**
     *  Delete the  codiceIstat by id.
     *  
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete CodiceIstat : {}", id);
        codiceIstatRepository.delete(id);
        codiceIstatSearchRepository.delete(id);
    }

    /**
     * Search for the codiceIstat corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<CodiceIstat> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of CodiceIstats for query {}", query);
        
        // elimino le parentesi per evitare errori di sintassi della ricerca
        String q = query.replace("(", "");
        q = q.replace(")", "");
        
        if(Character.isDigit(q.charAt(0))) {
        	return codiceIstatSearchRepository.search(queryStringQuery(q + "*").field("codiceIstat"), pageable);
        }
        return codiceIstatSearchRepository.search(queryStringQuery(q + "*").field("comune^2").field("provincia"), pageable);
    }
    
    /*
    @Transactional(readOnly = true)
    public Iterable<CodiceIstat> searchByRegioneProvinciaComune(String comune) {
        //log.debug("Request to search for a list of CodiceIstats by Regione: {}, Provincia: {}, Comune: {}", regione, provincia, comune);
       
        QueryStringQueryBuilder qsqb = new QueryStringQueryBuilder(comune.toLowerCase());
                
        
        return (Iterable<CodiceIstat>) codiceIstatSearchRepository.search(queryStringQuery(comune.toLowerCase()));
    }
    */
}
