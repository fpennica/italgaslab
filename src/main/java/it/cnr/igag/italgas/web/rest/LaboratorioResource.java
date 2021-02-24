package it.cnr.igag.italgas.web.rest;

import com.codahale.metrics.annotation.Timed;
import it.cnr.igag.italgas.domain.Laboratorio;
import it.cnr.igag.italgas.service.LaboratorioService;
import it.cnr.igag.italgas.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Laboratorio.
 */
@RestController
@RequestMapping({"/api", "/api_basic"})
public class LaboratorioResource {

    private final Logger log = LoggerFactory.getLogger(LaboratorioResource.class);
        
    @Inject
    private LaboratorioService laboratorioService;
    
    /**
     * POST  /laboratorios : Create a new laboratorio.
     *
     * @param laboratorio the laboratorio to create
     * @return the ResponseEntity with status 201 (Created) and with body the new laboratorio, or with status 400 (Bad Request) if the laboratorio has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/laboratorios",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Laboratorio> createLaboratorio(@Valid @RequestBody Laboratorio laboratorio) throws URISyntaxException {
        log.debug("REST request to save Laboratorio : {}", laboratorio);
        if (laboratorio.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("laboratorio", "idexists", "A new laboratorio cannot already have an ID")).body(null);
        }
        Laboratorio result = laboratorioService.save(laboratorio);
        return ResponseEntity.created(new URI("/api/laboratorios/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("laboratorio", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /laboratorios : Updates an existing laboratorio.
     *
     * @param laboratorio the laboratorio to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated laboratorio,
     * or with status 400 (Bad Request) if the laboratorio is not valid,
     * or with status 500 (Internal Server Error) if the laboratorio couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/laboratorios",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Laboratorio> updateLaboratorio(@Valid @RequestBody Laboratorio laboratorio) throws URISyntaxException {
        log.debug("REST request to update Laboratorio : {}", laboratorio);
        if (laboratorio.getId() == null) {
            return createLaboratorio(laboratorio);
        }
        Laboratorio result = laboratorioService.save(laboratorio);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("laboratorio", laboratorio.getId().toString()))
            .body(result);
    }

    /**
     * GET  /laboratorios : get all the laboratorios.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of laboratorios in body
     */
    @RequestMapping(value = "/laboratorios",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Laboratorio> getAllLaboratorios() {
        log.debug("REST request to get all Laboratorios");
        return laboratorioService.findAll();
    }

    /**
     * GET  /laboratorios/:id : get the "id" laboratorio.
     *
     * @param id the id of the laboratorio to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the laboratorio, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/laboratorios/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Laboratorio> getLaboratorio(@PathVariable Long id) {
        log.debug("REST request to get Laboratorio : {}", id);
        Laboratorio laboratorio = laboratorioService.findOne(id);
        return Optional.ofNullable(laboratorio)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /laboratorios/:id : delete the "id" laboratorio.
     *
     * @param id the id of the laboratorio to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/laboratorios/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteLaboratorio(@PathVariable Long id) {
        log.debug("REST request to delete Laboratorio : {}", id);
        laboratorioService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("laboratorio", id.toString())).build();
    }

    /**
     * SEARCH  /_search/laboratorios?query=:query : search for the laboratorio corresponding
     * to the query.
     *
     * @param query the query of the laboratorio search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/laboratorios",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Laboratorio> searchLaboratorios(@RequestParam String query) {
        log.debug("REST request to search Laboratorios for query {}", query);
        return laboratorioService.search(query);
    }


}
