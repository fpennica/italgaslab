package it.cnr.igag.italgas.web.rest;

import com.codahale.metrics.annotation.Timed;
import it.cnr.igag.italgas.domain.CodiceIstat;
import it.cnr.igag.italgas.service.CodiceIstatService;
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
 * REST controller for managing CodiceIstat.
 */
@RestController
@RequestMapping({"/api", "/api_basic"})
public class CodiceIstatResource {

    private final Logger log = LoggerFactory.getLogger(CodiceIstatResource.class);
        
    @Inject
    private CodiceIstatService codiceIstatService;
    
    /**
     * POST  /codice-istats : Create a new codiceIstat.
     *
     * @param codiceIstat the codiceIstat to create
     * @return the ResponseEntity with status 201 (Created) and with body the new codiceIstat, or with status 400 (Bad Request) if the codiceIstat has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/codice-istats",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<CodiceIstat> createCodiceIstat(@RequestBody CodiceIstat codiceIstat) throws URISyntaxException {
        log.debug("REST request to save CodiceIstat : {}", codiceIstat);
        if (codiceIstat.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("codiceIstat", "idexists", "A new codiceIstat cannot already have an ID")).body(null);
        }
        CodiceIstat result = codiceIstatService.save(codiceIstat);
        return ResponseEntity.created(new URI("/api/codice-istats/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("codiceIstat", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /codice-istats : Updates an existing codiceIstat.
     *
     * @param codiceIstat the codiceIstat to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated codiceIstat,
     * or with status 400 (Bad Request) if the codiceIstat is not valid,
     * or with status 500 (Internal Server Error) if the codiceIstat couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/codice-istats",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<CodiceIstat> updateCodiceIstat(@RequestBody CodiceIstat codiceIstat) throws URISyntaxException {
        log.debug("REST request to update CodiceIstat : {}", codiceIstat);
        if (codiceIstat.getId() == null) {
            return createCodiceIstat(codiceIstat);
        }
        CodiceIstat result = codiceIstatService.save(codiceIstat);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("codiceIstat", codiceIstat.getId().toString()))
            .body(result);
    }

    /**
     * GET  /codice-istats : get all the codiceIstats.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of codiceIstats in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/codice-istats",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<CodiceIstat>> getAllCodiceIstats(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of CodiceIstats");
        Page<CodiceIstat> page = codiceIstatService.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/codice-istats");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /codice-istats/:id : get the "id" codiceIstat.
     *
     * @param id the id of the codiceIstat to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the codiceIstat, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/codice-istats/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<CodiceIstat> getCodiceIstat(@PathVariable Long id) {
        log.debug("REST request to get CodiceIstat : {}", id);
        CodiceIstat codiceIstat = codiceIstatService.findOne(id);
        return Optional.ofNullable(codiceIstat)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /codice-istats/:id : delete the "id" codiceIstat.
     *
     * @param id the id of the codiceIstat to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/codice-istats/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteCodiceIstat(@PathVariable Long id) {
        log.debug("REST request to delete CodiceIstat : {}", id);
        codiceIstatService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("codiceIstat", id.toString())).build();
    }

    /**
     * SEARCH  /_search/codice-istats?query=:query : search for the codiceIstat corresponding
     * to the query.
     *
     * @param query the query of the codiceIstat search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/codice-istats",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<CodiceIstat>> searchCodiceIstats(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of CodiceIstats for query {}", query);
        Page<CodiceIstat> page = codiceIstatService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/codice-istats");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
