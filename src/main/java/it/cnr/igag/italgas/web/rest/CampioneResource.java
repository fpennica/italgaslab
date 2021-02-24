package it.cnr.igag.italgas.web.rest;

import com.codahale.metrics.annotation.Timed;
import it.cnr.igag.italgas.domain.Campione;
import it.cnr.igag.italgas.service.CampioneService;
import it.cnr.igag.italgas.web.rest.dto.CbPercGranDTO;
import it.cnr.igag.italgas.web.rest.dto.ClassificazioneGeotecnicaDTO;
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Campione.
 */
@RestController
@RequestMapping({"/api", "/api_basic"})
public class CampioneResource {

    private final Logger log = LoggerFactory.getLogger(CampioneResource.class);
        
    @Inject
    private CampioneService campioneService;
    
    /**
     * POST  /campiones : Create a new campione.
     *
     * @param campione the campione to create
     * @return the ResponseEntity with status 201 (Created) and with body the new campione, or with status 400 (Bad Request) if the campione has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/campiones",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Campione> createCampione(@Valid @RequestBody Campione campione) throws URISyntaxException {
        log.debug("REST request to save Campione : {}", campione);
        if (campione.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("campione", "idexists", "A new campione cannot already have an ID")).body(null);
        }
        
        //salvo i blob perché non vengono inseriti nel db quando si tratta di una nuova entity
        byte[] blobFotoSacchetto = campione.getFotoSacchetto();
        byte[] blobFotoCampione = campione.getFotoCampione();
        byte[] blobCurva = campione.getCurva();
        byte[] blobCurvaBinder = campione.getCurvaBinder();
        
        Campione result = campioneService.save(campione);
        
        // risalvo l'entity con i blob
        result.setFotoSacchetto(blobFotoSacchetto);
        result.setFotoCampione(blobFotoCampione);
        result.setCurva(blobCurva);
        result.setCurvaBinder(blobCurvaBinder);
        result = campioneService.save(campione);
        
        // annullo i blob nel risultato inviato al client
        result = campioneService.resetBlobs(result);
        
        return ResponseEntity.created(new URI("/api/campiones/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("campione", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /campiones : Updates an existing campione.
     *
     * @param campione the campione to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated campione,
     * or with status 400 (Bad Request) if the campione is not valid,
     * or with status 500 (Internal Server Error) if the campione couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/campiones",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Campione> updateCampione(@Valid @RequestBody Campione campione) throws URISyntaxException {
        log.debug("REST request to update Campione : {}", campione);
        if (campione.getId() == null) {
            return createCampione(campione);
        }
        
        Campione result = campioneService.save(campione);
        
        // annullo i blob nel risultato inviato al client
        result = campioneService.resetBlobs(result);
        
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("campione", campione.getId().toString()))
            .body(result);
    }

    /**
     * GET  /campiones : get all the campiones.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of campiones in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/campiones",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Campione>> getAllCampiones(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Campiones");
        Page<Campione> page = campioneService.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/campiones");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/cassettas/{idCassetta}/campiones",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Campione>> getCampionesByCassetta(@PathVariable Long idCassetta, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Campiones by cassetta");
        Page<Campione> page = campioneService.findByCassetta(pageable, idCassetta); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/cassettas/" + idCassetta + "/campiones");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/campiones/{id}/classgeotec",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<ClassificazioneGeotecnicaDTO> getClassificazioneGeotecnica (@PathVariable Long id) {
    	Campione campione = campioneService.findOne(id, true);
    	
    	if(campione == null)
        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    	
    	ClassificazioneGeotecnicaDTO dto = campioneService.generateClassificazioneGeotecnica(campione);
    	
    	if(dto != null) {
    		// salvo la classificazione
	    	if(campione.getClassificazioneGeotecnica() == null || !campione.getClassificazioneGeotecnica().equals(dto.getClassificazioneGeotecnica())) {
		    	campione.setClassificazioneGeotecnica(dto.getClassificazioneGeotecnica());
		    	campioneService.save(campione);
	    	}
    	}
    	
    	return new ResponseEntity<>(dto, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/campiones/{id}/cbusurapercgran",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<CbPercGranDTO> getCbUsuraPercGran (@PathVariable Long id) {
    	Campione campione = campioneService.findOne(id, true);
    	
    	if(campione == null)
        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    	
    	CbPercGranDTO dto = campioneService.generateCbUsuraPercGran(campione);
    	
    	return new ResponseEntity<>(dto, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/campiones/{id}/cbbinderpercgran",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<CbPercGranDTO> getCbBinderPercGran (@PathVariable Long id) {
    	Campione campione = campioneService.findOne(id, true);
    	
    	if(campione == null)
        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    	
    	CbPercGranDTO dto = campioneService.generateCbBinderPercGran(campione);
    	
    	return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    /**
     * GET  /campiones/:id : get the "id" campione.
     *
     * @param id the id of the campione to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the campione, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/campiones/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Campione> getCampione(@PathVariable Long id) {
        log.debug("REST request to get Campione : {}", id);
        Campione campione = campioneService.findOne(id, true);
        return Optional.ofNullable(campione)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    // GET fotoSacchetto
    @RequestMapping(value = "/campiones/{id}/fotosacchetto",
            method = RequestMethod.GET)
    @Timed
    public ResponseEntity<byte[]> getFotoSacchetto(@PathVariable Long id) {
        log.debug("REST request to get fotoSacchetto for Campione : {}", id);
        Campione campione = campioneService.findOne(id, false);

        if(campione == null || campione.getFotoSacchetto() == null)
        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        
        HttpHeaders headers = new HttpHeaders();
	    headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
	    headers.add("Content-disposition", "inline; filename=" + campione.getCodiceCampione() + "_sacchetto" +
	    		"." + MediaType.parseMediaType(campione.getFotoSacchettoContentType()).getSubtype());
	    headers.add("Pragma", "no-cache");
	    headers.add("Expires", "0");
	    
	    return new ResponseEntity<byte[]>(campione.getFotoSacchetto(), headers, HttpStatus.OK);
    }
    
    // GET fotoCampione
    @RequestMapping(value = "/campiones/{id}/fotocampione",
            method = RequestMethod.GET)
    @Timed
    public ResponseEntity<byte[]> getFotoCampione(@PathVariable Long id) {
        log.debug("REST request to get fotoCampione for Campione : {}", id);
        Campione campione = campioneService.findOne(id, false);

        if(campione == null || campione.getFotoCampione() == null)
        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        
        HttpHeaders headers = new HttpHeaders();
	    headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
	    headers.add("Content-disposition", "inline; filename=" + campione.getCodiceCampione() +	"_campione" +
	    		"." + MediaType.parseMediaType(campione.getFotoCampioneContentType()).getSubtype());
	    headers.setContentType(MediaType.parseMediaType(campione.getFotoCampioneContentType()));
	    headers.add("Pragma", "no-cache");
	    headers.add("Expires", "0");
	    
	    return new ResponseEntity<byte[]>(campione.getFotoCampione(), headers, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/campiones/{id}/fotoetichetta",
            method = RequestMethod.GET)
    @Timed
    public ResponseEntity<byte[]> getFotoEtichetta(@PathVariable Long id) {
        log.debug("REST request to get fotoEtichetta for Campione : {}", id);
        Campione campione = campioneService.findOne(id, false);

        if(campione == null || campione.getFotoCampione() == null)
        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        
        HttpHeaders headers = new HttpHeaders();
	    headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
	    headers.add("Content-disposition", "inline; filename=" + campione.getCodiceCampione() +	"_campione" +
	    		"." + MediaType.parseMediaType(campione.getFotoEtichettaContentType()).getSubtype());
	    headers.setContentType(MediaType.parseMediaType(campione.getFotoEtichettaContentType()));
	    headers.add("Pragma", "no-cache");
	    headers.add("Expires", "0");
	    
	    return new ResponseEntity<byte[]>(campione.getFotoEtichetta(), headers, HttpStatus.OK);
    }
    
    // GET curva
    @RequestMapping(value = "/campiones/{id}/curva",
            method = RequestMethod.GET)
    @Timed
    public ResponseEntity<byte[]> getCurva(@PathVariable Long id) {
        log.debug("REST request to get curva for Cassetta : {}", id);
        Campione campione = campioneService.findOne(id, false);

        if(campione == null || campione.getCurva() == null)
        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        
        HttpHeaders headers = new HttpHeaders();
	    headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
	    headers.add("Content-disposition", "inline; filename=" + campione.getCodiceCampione() +	"_curva" +
	    		"." + MediaType.parseMediaType(campione.getCurvaContentType()).getSubtype());
	    headers.setContentType(MediaType.parseMediaType(campione.getCurvaContentType()));
	    headers.add("Pragma", "no-cache");
	    headers.add("Expires", "0");
	    
	    return new ResponseEntity<byte[]>(campione.getCurva(), headers, HttpStatus.OK);
    }
    
    // GET curva
    @RequestMapping(value = "/campiones/{id}/curvabinder",
            method = RequestMethod.GET)
    @Timed
    public ResponseEntity<byte[]> getCurvaBinder(@PathVariable Long id) {
        log.debug("REST request to get curva binder for Cassetta : {}", id);
        Campione campione = campioneService.findOne(id, false);

        if(campione == null || campione.getCurvaBinder() == null)
        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        
        HttpHeaders headers = new HttpHeaders();
	    headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
	    headers.add("Content-disposition", "inline; filename=" + campione.getCodiceCampione() +	"_curvabinder" +
	    		"." + MediaType.parseMediaType(campione.getCurvaBinderContentType()).getSubtype());
	    headers.setContentType(MediaType.parseMediaType(campione.getCurvaBinderContentType()));
	    headers.add("Pragma", "no-cache");
	    headers.add("Expires", "0");
	    
	    return new ResponseEntity<byte[]>(campione.getCurvaBinder(), headers, HttpStatus.OK);
    }

    /**
     * DELETE  /campiones/:id : delete the "id" campione.
     *
     * @param id the id of the campione to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/campiones/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteCampione(@PathVariable Long id) {
        log.debug("REST request to delete Campione : {}", id);
        campioneService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("campione", id.toString())).build();
    }

    @RequestMapping(value = "/campiones/{id}/rapportocb",
            method = RequestMethod.GET)
    @Timed
    public ResponseEntity<byte[]> getRapportoCb(@PathVariable Long id) {
    	
    	Campione campione = campioneService.findOne(id, false);
    	
    	if(campione == null)
    		return null;
    	
    	byte[] pdfReport = campioneService.generateRapportoCb(id);
        
        HttpHeaders headers = new HttpHeaders();
	    headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
	    headers.add("Content-disposition", "attachment; filename=Rapporto_Prova_" + campione.getCodiceCampione() + ".pdf");
	    headers.add("Pragma", "no-cache");
	    headers.add("Expires", "0");
	    
	    return new ResponseEntity<byte[]>(pdfReport, headers, HttpStatus.OK);
    	
    }
    
    /**
     * SEARCH  /_search/campiones?query=:query : search for the campione corresponding
     * to the query.
     *
     * @param query the query of the campione search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/campiones",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Campione>> searchCampiones(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Campiones for query {}", query);
        Page<Campione> page = campioneService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/campiones");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
