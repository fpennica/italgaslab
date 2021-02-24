package it.cnr.igag.italgas.web.rest;

import com.codahale.metrics.annotation.Timed;

import it.cnr.igag.italgas.domain.Campione;
import it.cnr.igag.italgas.domain.Pesata;
import it.cnr.igag.italgas.service.CampioneService;
import it.cnr.igag.italgas.service.PesataService;
import it.cnr.igag.italgas.web.rest.errors.CustomParameterizedException;
import it.cnr.igag.italgas.web.rest.util.HeaderUtil;
import it.cnr.igag.italgas.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Pesata.
 */
@RestController
@RequestMapping({"/api", "/api_basic"})
public class PesataResource {

    private final Logger log = LoggerFactory.getLogger(PesataResource.class);
        
    @Inject
    private PesataService pesataService;
    
    @Inject
    private CampioneService campioneService;
    
    /**
     * POST  /pesatas : Create a new pesata.
     *
     * @param pesata the pesata to create
     * @return the ResponseEntity with status 201 (Created) and with body the new pesata, or with status 400 (Bad Request) if the pesata has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/pesatas",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Pesata> createPesata(@RequestBody Pesata pesata) throws URISyntaxException {
        log.debug("REST request to save Pesata : {}", pesata);
        if (pesata.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("pesata", "idexists", "A new pesata cannot already have an ID")).body(null);
        }
        Pesata result = pesataService.save(pesata);
        return ResponseEntity.created(new URI("/api/pesatas/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("pesata", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /pesatas : Updates an existing pesata.
     *
     * @param pesata the pesata to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated pesata,
     * or with status 400 (Bad Request) if the pesata is not valid,
     * or with status 500 (Internal Server Error) if the pesata couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/pesatas",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Pesata> updatePesata(@RequestBody Pesata pesata) throws URISyntaxException {
        log.debug("REST request to update Pesata : {}", pesata);
        if (pesata.getId() == null) {
            return createPesata(pesata);
        }
        Pesata result = pesataService.save(pesata);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("pesata", pesata.getId().toString()))
            .body(result);
    }

    /**
     * GET  /pesatas : get all the pesatas.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of pesatas in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/pesatas",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Pesata>> getAllPesatas(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Pesatas");
        Page<Pesata> page = pesataService.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/pesatas");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/campiones/{idCampione}/pesatas",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Pesata> getPesatasByCampione(@PathVariable Long idCampione, 
    		@RequestParam(value = "binder", required = false, defaultValue = "false") Boolean binder)
        throws URISyntaxException {
        log.debug("REST request to get Pesatas by Campione");
        
        if(binder) {
        	return pesataService.findByCampione(idCampione, true);
        }
        
        return pesataService.findByCampione(idCampione); 
    }
    
    @RequestMapping(value = "/campiones/{idCampione}/pesatasWithChart",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Pesata> getPesatasByCampione(@PathVariable Long idCampione, @RequestParam String generateChart, 
    		@RequestParam(value = "cb", required = false, defaultValue = "false") Boolean cb, 
    		@RequestParam(value = "binder", required = false, defaultValue = "false") Boolean binder)
        throws URISyntaxException {
        log.debug("REST request to get Pesatas by Campione");
        //log.debug("############################ {} {}", binder, binder instanceof Boolean);
        
        if(generateChart != null && generateChart.equalsIgnoreCase("true")) {
        	Campione campione = campioneService.findOne(idCampione, false);
        	if(campione != null && campione.getId() != null) {
        		try {
        			byte[] byteArray = null;
        			if(cb) {
        				if(binder) {
        					log.debug("############################ BINDER");
        					byteArray = campioneService.generateCurvaGranulometricaCb(campione, true);
        				} else {
        					log.debug("############################ USURA");
        					byteArray = campioneService.generateCurvaGranulometricaCb(campione, false);
        				}
        			} else {
        				byteArray = campioneService.generateCurvaGranulometrica(campione);
        			}
        			
        			if(byteArray != null) {
        				if(binder) {
        					campione.setCurvaBinder(byteArray);
            				campione.setCurvaBinderContentType("image/png");
        				} else {
	        				campione.setCurva(byteArray);
	        				campione.setCurvaContentType("image/png");
        				}
        				campioneService.save(campione);
        			}
        		} catch(CustomParameterizedException e) {
        			if(e.getMessage().equals("sumTrattError") || e.getMessage().equals("pesataListEmptyError")) {
        				campione.setCurva(null);
        				campione.setCurvaContentType(null);
        				campioneService.save(campione);
        			}
        			throw e;
        		}
//        		if(campione == null) {
//        			throw new CustomParameterizedException("La sommatoria dei trattenuti è 0, controllare i dati inseriti!", "");
//        		}
        	}
        }
        
        return pesataService.findByCampione(idCampione); 
    }
    
    /**
     * GET  /pesatas/:id : get the "id" pesata.
     *
     * @param id the id of the pesata to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the pesata, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/pesatas/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Pesata> getPesata(@PathVariable Long id) {
        log.debug("REST request to get Pesata : {}", id);
        Pesata pesata = pesataService.findOne(id);
        return Optional.ofNullable(pesata)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /pesatas/:id : delete the "id" pesata.
     *
     * @param id the id of the pesata to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/pesatas/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deletePesata(@PathVariable Long id) {
        log.debug("REST request to delete Pesata : {}", id);
        pesataService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("pesata", id.toString())).build();
    }

    /**
     * SEARCH  /_search/pesatas?query=:query : search for the pesata corresponding
     * to the query.
     *
     * @param query the query of the pesata search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/pesatas",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Pesata>> searchPesatas(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Pesatas for query {}", query);
        Page<Pesata> page = pesataService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/pesatas");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
