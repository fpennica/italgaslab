package it.cnr.igag.italgas.web.rest;

import com.codahale.metrics.annotation.Timed;

import it.cnr.igag.italgas.domain.Operatore;
import it.cnr.igag.italgas.service.OperatoreService;
import it.cnr.igag.italgas.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Operatore.
 */
@RestController
@RequestMapping({"/api", "/api_basic"})
public class OperatoreResource {

    private final Logger log = LoggerFactory.getLogger(OperatoreResource.class);
        
    @Inject
    private OperatoreService operatoreService;
    
    /**
     * POST  /operatores : Create a new operatore.
     *
     * @param operatore the operatore to create
     * @return the ResponseEntity with status 201 (Created) and with body the new operatore, or with status 400 (Bad Request) if the operatore has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/operatores",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Operatore> createOperatore(@RequestBody Operatore operatore) throws URISyntaxException {
        log.debug("REST request to save Operatore : {}", operatore);
        if (operatore.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("operatore", "idexists", "A new operatore cannot already have an ID")).body(null);
        }
        Operatore result = operatoreService.save(operatore);
        return ResponseEntity.created(new URI("/api/operatores/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("operatore", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /operatores : Updates an existing operatore.
     *
     * @param operatore the operatore to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated operatore,
     * or with status 400 (Bad Request) if the operatore is not valid,
     * or with status 500 (Internal Server Error) if the operatore couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/operatores",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Operatore> updateOperatore(@RequestBody Operatore operatore) throws URISyntaxException {
        log.debug("REST request to update Operatore : {}", operatore);
        if (operatore.getId() == null) {
            return createOperatore(operatore);
        }
        Operatore result = operatoreService.save(operatore);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("operatore", operatore.getId().toString()))
            .body(result);
    }

    /**
     * GET  /operatores : get all the operatores.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of operatores in body
     */
    @RequestMapping(value = "/operatores",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Operatore> getAllOperatores() {
        log.debug("REST request to get all Operatores");
        return operatoreService.findAll();
    }
    
    @RequestMapping(value = "/laboratorios/current/operatores",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Operatore> getOperatoresByCurrentLaboratorio() {
        log.debug("REST request to get Operatores by current Laboratorio");
        return operatoreService.findByCurrentLaboratorio();
    }
    
    @RequestMapping(value = "/laboratorios/{id}/operatores",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Operatore> getOperatoresByLaboratorio(@PathVariable Long id) {
        log.debug("REST request to get Operatores by Laboratorio with id: {}", id);
        return operatoreService.findByLaboratorio(id);
    }

    /**
     * GET  /operatores/:id : get the "id" operatore.
     *
     * @param id the id of the operatore to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the operatore, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/operatores/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Operatore> getOperatore(@PathVariable Long id) {
        log.debug("REST request to get Operatore : {}", id);
        Operatore operatore = operatoreService.findOne(id);
        return Optional.ofNullable(operatore)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /operatores/:id : delete the "id" operatore.
     *
     * @param id the id of the operatore to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/operatores/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteOperatore(@PathVariable Long id) {
        log.debug("REST request to delete Operatore : {}", id);
        operatoreService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("operatore", id.toString())).build();
    }

    /**
     * SEARCH  /_search/operatores?query=:query : search for the operatore corresponding
     * to the query.
     *
     * @param query the query of the operatore search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/operatores",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Operatore> searchOperatores(@RequestParam String query) {
        log.debug("REST request to search Operatores for query {}", query);
        return operatoreService.search(query);
    }


}
