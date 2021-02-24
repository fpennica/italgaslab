package it.cnr.igag.italgas.web.rest;

import com.codahale.metrics.annotation.Timed;

import it.cnr.igag.italgas.domain.Consegna;
import it.cnr.igag.italgas.service.ConsegnaService;
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
import javax.validation.Valid;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Consegna.
 */
@RestController
@RequestMapping({"/api", "/api_basic"})
public class ConsegnaResource {

    private final Logger log = LoggerFactory.getLogger(ConsegnaResource.class);
        
    @Inject
    private ConsegnaService consegnaService;
    
    /**
     * POST  /consegnas : Create a new consegna.
     *
     * @param consegna the consegna to create
     * @return the ResponseEntity with status 201 (Created) and with body the new consegna, or with status 400 (Bad Request) if the consegna has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/consegnas",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Consegna> createConsegna(@Valid @RequestBody Consegna consegna) throws URISyntaxException {
        log.debug("REST request to save Consegna : {}", consegna);
        if (consegna.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("consegna", "idexists", "A new consegna cannot already have an ID")).body(null);
        }
        
        //salvo i blob perché non vengono inseriti nel db quando si tratta di una nuova entity
        byte[] blobProtocolloAccettazione = consegna.getProtocolloAccettazione();
        
        Consegna result = consegnaService.save(consegna);
        
        // risalvo l'entity con i blob
        result.setProtocolloAccettazione(blobProtocolloAccettazione);
        result = consegnaService.save(result);
        
        // annullo i blob nel risultato inviato al client
        result = consegnaService.resetBlobs(result);
        
        return ResponseEntity.created(new URI("/api/consegnas/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("consegna", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /consegnas : Updates an existing consegna.
     *
     * @param consegna the consegna to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated consegna,
     * or with status 400 (Bad Request) if the consegna is not valid,
     * or with status 500 (Internal Server Error) if the consegna couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/consegnas",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Consegna> updateConsegna(@Valid @RequestBody Consegna consegna) throws URISyntaxException {
        log.debug("REST request to update Consegna : {}", consegna);
        if (consegna.getId() == null) {
            return createConsegna(consegna);
        }
        Consegna result = consegnaService.save(consegna);
        
        // annullo i blob nel risultato inviato al client
        result = consegnaService.resetBlobs(result);
        
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("consegna", consegna.getId().toString()))
            .body(result);
    }

    /**
     * GET  /consegnas : get all the consegnas.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of consegnas in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/consegnas",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Consegna>> getAllConsegnas(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Consegnas");
        Page<Consegna> page = consegnaService.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/consegnas");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /consegnas/:id : get the "id" consegna.
     *
     * @param id the id of the consegna to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the consegna, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/consegnas/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Consegna> getConsegna(@PathVariable Long id) {
        log.debug("REST request to get Consegna : {}", id);
        Consegna consegna = consegnaService.findOne(id, true);
        return Optional.ofNullable(consegna)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    // GET protocolloAccettazione
    @RequestMapping(value = "/consegnas/{id}/accettaz",
            method = RequestMethod.GET)
    @Timed
    public ResponseEntity<byte[]> getProtocolloAccettazione(@PathVariable Long id) {
        log.debug("REST request to get ProtocolloAccettazione for Consegna : {}", id);
        Consegna consegna = consegnaService.findOne(id, false);

        if(consegna == null || consegna.getProtocolloAccettazione() == null)
        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        
        HttpHeaders headers = new HttpHeaders();
	    headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
	    headers.add("Content-disposition", "attachment; filename=" + consegna.getLaboratorio().getIstituto() + "_" + consegna.getDataConsegna() + ".pdf");
	    headers.add("Pragma", "no-cache");
	    headers.add("Expires", "0");
	    
	    return new ResponseEntity<byte[]>(consegna.getProtocolloAccettazione(), headers, HttpStatus.OK);
    }
    
    
    // genera e invia un modello di bolla di accettazione da protocollare
    @RequestMapping(value = "/consegnas/{id}/modelloaccettaz",
            method = RequestMethod.GET)
    @Timed
    public ResponseEntity<byte[]> getModelloProtocolloAccettazione(@PathVariable Long id) {
        log.debug("REST request to get Modello ProtocolloAccettazione for Consegna : {}", id);
        Consegna consegna = consegnaService.findOne(id, false);

        if(consegna == null)
        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        
        byte[] pdfReport = consegnaService.generateModelloProtocolloAccettazione(id);
        
        HttpHeaders headers = new HttpHeaders();
	    headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
	    headers.add("Content-disposition", "attachment; filename=Modello_Accettazione_" + consegna.getLaboratorio().getIstituto() + "_" + consegna.getDataConsegna() + ".pdf");
	    headers.add("Pragma", "no-cache");
	    headers.add("Expires", "0");
	    
	    return new ResponseEntity<byte[]>(pdfReport, headers, HttpStatus.OK);
    }
    

    /**
     * DELETE  /consegnas/:id : delete the "id" consegna.
     *
     * @param id the id of the consegna to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/consegnas/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteConsegna(@PathVariable Long id) {
        log.debug("REST request to delete Consegna : {}", id);
        consegnaService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("consegna", id.toString())).build();
    }

    /**
     * SEARCH  /_search/consegnas?query=:query : search for the consegna corresponding
     * to the query.
     *
     * @param query the query of the consegna search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/consegnas",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Consegna>> searchConsegnas(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Consegnas for query {}", query);
        Page<Consegna> page = consegnaService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/consegnas");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
