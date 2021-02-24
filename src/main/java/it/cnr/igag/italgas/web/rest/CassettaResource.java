package it.cnr.igag.italgas.web.rest;

import com.codahale.metrics.annotation.Timed;

import it.cnr.igag.italgas.domain.Campione;
import it.cnr.igag.italgas.domain.Cassetta;
import it.cnr.igag.italgas.domain.enumeration.TipoCampione;
import it.cnr.igag.italgas.repository.CampioneRepository;
import it.cnr.igag.italgas.service.CampioneService;
import it.cnr.igag.italgas.service.CassettaService;
import it.cnr.igag.italgas.service.ExcelExportService;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * REST controller for managing Cassetta.
 */
@RestController
@RequestMapping({"/api", "/api_basic"})
public class CassettaResource {

    private final Logger log = LoggerFactory.getLogger(CassettaResource.class);
        
    @Inject
    private CassettaService cassettaService;
    
    @Inject
    private CampioneService campioneService;
    
    @Inject
    private CampioneRepository campioneRepository;
    
    @Inject
    private ExcelExportService excelExportService;
    
    /**
     * POST  /cassettas : Create a new cassetta.
     *
     * @param cassetta the cassetta to create
     * @return the ResponseEntity with status 201 (Created) and with body the new cassetta, or with status 400 (Bad Request) if the cassetta has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/cassettas",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Cassetta> createCassetta(@Valid @RequestBody Cassetta cassetta) throws URISyntaxException {
        log.debug("REST request to save Cassetta : {}", cassetta);
        if (cassetta.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("cassetta", "idexists", "A new cassetta cannot already have an ID")).body(null);
        }
        
        //salvo i blob perché non vengono inseriti nel db quando si tratta di una nuova entity
        byte[] blobFotoContenitore = cassetta.getFotoContenitore();
        byte[] blobFotoContenuto = cassetta.getFotoContenuto();
        byte[] blobCertificatoPdf = cassetta.getCertificatoPdf();
        
        Cassetta result = cassettaService.save(cassetta);
        
        // risalvo l'entity con i blob
        result.setFotoContenitore(blobFotoContenitore);
        result.setFotoContenuto(blobFotoContenuto);
        result.setCertificatoPdf(blobCertificatoPdf);
        result = cassettaService.save(result);
        
        // annullo i blob nel risultato inviato al client
        result = cassettaService.resetBlobs(result);
        
        return ResponseEntity.created(new URI("/api/cassettas/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("cassetta", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /cassettas : Updates an existing cassetta.
     *
     * @param cassetta the cassetta to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated cassetta,
     * or with status 400 (Bad Request) if the cassetta is not valid,
     * or with status 500 (Internal Server Error) if the cassetta couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/cassettas",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Cassetta> updateCassetta(@Valid @RequestBody Cassetta cassetta) throws URISyntaxException {
        log.debug("REST request to update Cassetta : {}", cassetta);
        if (cassetta.getId() == null) {
            return createCassetta(cassetta);
        }
        Cassetta result = cassettaService.save(cassetta);
        
        // annullo i blob nel risultato inviato al client
        result = cassettaService.resetBlobs(result);
        
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("cassetta", cassetta.getId().toString()))
            .body(result);
    }

    /**
     * GET  /cassettas : get all the cassettas.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of cassettas in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/cassettas",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Cassetta>> getAllCassettas(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Cassettas");
        Page<Cassetta> page = cassettaService.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/cassettas");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/consegnas/{idConsegna}/cassettas",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Cassetta>> getCassettasByConsegna(@PathVariable Long idConsegna, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Cassettas by Consegna {}", idConsegna);
        Page<Cassetta> page = cassettaService.findByConsegna(pageable, idConsegna); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/consegnas/" + idConsegna + "/cassettas");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /cassettas/:id : get the "id" cassetta.
     *
     * @param id the id of the cassetta to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the cassetta, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/cassettas/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Cassetta> getCassetta(@PathVariable Long id) {
        log.debug("REST request to get Cassetta : {}", id);
        Cassetta cassetta = cassettaService.findOne(id, true);
        return Optional.ofNullable(cassetta)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    // GET certificatoPdf
    @RequestMapping(value = "/cassettas/{id}/cert",
            method = RequestMethod.GET)
    @Timed
    public ResponseEntity<byte[]> getCertificatoPdf(@PathVariable Long id) {
        log.debug("REST request to get certificatoPdf for Cassetta : {}", id);
        Cassetta cassetta = cassettaService.findOne(id, false);

        if(cassetta == null || cassetta.getCertificatoPdf() == null)
        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        
        HttpHeaders headers = new HttpHeaders();
	    headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
	    headers.add("Content-disposition", "attachment; filename=" + cassetta.getCodiceCassetta() + ".pdf");
	    headers.add("Pragma", "no-cache");
	    headers.add("Expires", "0");
	    
	    return new ResponseEntity<byte[]>(cassetta.getCertificatoPdf(), headers, HttpStatus.OK);
    }
    
    // GET fotoContenitore
    @RequestMapping(value = "/cassettas/{id}/fotocontenitore",
            method = RequestMethod.GET)
    @Timed
    public ResponseEntity<byte[]> getFotoContenitore(@PathVariable Long id) {
        log.debug("REST request to get fotoContenitore for Cassetta : {}", id);
        Cassetta cassetta = cassettaService.findOne(id, false);

        if(cassetta == null || cassetta.getFotoContenitore() == null)
        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        
        HttpHeaders headers = new HttpHeaders();
	    headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
	    headers.add("Content-disposition", "inline; filename=" + cassetta.getCodiceCassetta() +	"_contenitore" +
	    		"." + MediaType.parseMediaType(cassetta.getFotoContenitoreContentType()).getSubtype());
	    headers.setContentType(MediaType.parseMediaType(cassetta.getFotoContenitoreContentType()));
	    headers.add("Pragma", "no-cache");
	    headers.add("Expires", "0");
	    
	    return new ResponseEntity<byte[]>(cassetta.getFotoContenitore(), headers, HttpStatus.OK);
    }
    
    // GET fotoContenuto
    @RequestMapping(value = "/cassettas/{id}/fotocontenuto",
            method = RequestMethod.GET)
    @Timed
    public ResponseEntity<byte[]> getFotoContenuto(@PathVariable Long id) {
        log.debug("REST request to get fotoContenuto for Cassetta : {}", id);
        Cassetta cassetta = cassettaService.findOne(id, false);

        if(cassetta == null || cassetta.getFotoContenuto() == null)
        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        
        HttpHeaders headers = new HttpHeaders();
	    headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
	    headers.add("Content-disposition", "inline; filename=" + cassetta.getCodiceCassetta() +	"_contenuto" +
	    		"." + MediaType.parseMediaType(cassetta.getFotoContenutoContentType()).getSubtype());
	    headers.setContentType(MediaType.parseMediaType(cassetta.getFotoContenutoContentType()));
	    headers.add("Pragma", "no-cache");
	    headers.add("Expires", "0");
	    
	    return new ResponseEntity<byte[]>(cassetta.getFotoContenuto(), headers, HttpStatus.OK);
    }
    
    // genera e invia un modello di certificato da protocollare
    @RequestMapping(value = "/cassettas/{id}/modellocertificato",
            method = RequestMethod.GET)
    @Timed
    public ResponseEntity<byte[]> getModelloProtocolloAccettazione(@PathVariable Long id) {
        log.debug("REST request to get Modello Certificato for Cassetta : {}", id);
        Cassetta cassetta = cassettaService.findOne(id, false);

        if(cassetta == null)
        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        
        byte[] pdfReport = cassettaService.generateModelloCertificato(id);
        
        HttpHeaders headers = new HttpHeaders();
	    headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
	    headers.add("Content-disposition", "attachment; filename=Rapporto_Prova_" + cassetta.getCodiceCassetta() + ".pdf");
	    headers.add("Pragma", "no-cache");
	    headers.add("Expires", "0");
	    
	    return new ResponseEntity<byte[]>(pdfReport, headers, HttpStatus.OK);
    }
    
    // esportazione in excel + pdf
    @RequestMapping(value = "/cassettas/{id}/export",
            method = RequestMethod.GET)
    @Timed
    public ResponseEntity<byte[]> exportCassetta(@PathVariable Long id) throws IOException {
        log.debug("REST request to export Cassetta : {}", id);
        Cassetta cassetta = cassettaService.findOne(id, false);

        if(cassetta == null)
        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        
        //esporto file excel
        byte[] excelBa = excelExportService.exportData(id);
        
        // esporto file pdf
        byte[] pdfReportBa = cassettaService.generateModelloCertificato(id);
        
        // esporto rapporti cb
        List<Campione> campioneList = campioneRepository.findByCassetta(cassetta);
        Campione campioneCB_A = null;
		Campione campioneCB_B = null;
        for(Campione c : campioneList) {
			if(c.getTipoCampione() == TipoCampione.CB_A) {
				campioneCB_A = c;
			}
			if(c.getTipoCampione() == TipoCampione.CB_B) {
				campioneCB_B = c;
			}
		}
        byte[] pdfReportCbABa = null;
        byte[] pdfReportCbBBa = null;
        if(campioneCB_A != null)
        	pdfReportCbABa = campioneService.generateRapportoCb(campioneCB_A.getId());
        if(campioneCB_B != null)
        	pdfReportCbBBa = campioneService.generateRapportoCb(campioneCB_B.getId());
        
        // creo file zip
		// bisogna controllare che i valori non siano null !!!
        String fileName = "_" + cassetta.getCodiceIstat().getComune().replace(" ", "") + "_MsLink_" + cassetta.getRifGeografo()
		+ "_OdS_" + cassetta.getOdl() + "_ID_" + cassetta.getId();
        
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ZipOutputStream zos = new ZipOutputStream(baos)) {

			ZipEntry entry = new ZipEntry(fileName + ".xlsm");

			zos.putNextEntry(entry);
			zos.write(excelBa);
			zos.closeEntry();
			
			ZipEntry entry2 = new ZipEntry(fileName + ".pdf");
			
			zos.putNextEntry(entry2);
			zos.write(pdfReportBa);
			zos.closeEntry();

			if(pdfReportCbABa != null) {
				ZipEntry entry3 = new ZipEntry(fileName + "_CB_A.pdf");
				
				zos.putNextEntry(entry3);
				zos.write(pdfReportCbABa);
				zos.closeEntry();
			}
			
			if(pdfReportCbBBa != null) {
				ZipEntry entry4 = new ZipEntry(fileName + "_CB_B.pdf");
				
				zos.putNextEntry(entry4);
				zos.write(pdfReportCbBBa);
				zos.closeEntry();
			}
			

		} 
        
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
	    headers.add("Content-disposition", "attachment; filename=" + fileName + ".zip");
	    headers.add("Pragma", "no-cache");
	    headers.add("Expires", "0");
	    
	    return new ResponseEntity<byte[]>(baos.toByteArray(), headers, HttpStatus.OK);
	    
	    //return new ResponseEntity<>("Ok", HttpStatus.ACCEPTED);
    }

    /**
     * DELETE  /cassettas/:id : delete the "id" cassetta.
     *
     * @param id the id of the cassetta to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/cassettas/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteCassetta(@PathVariable Long id) {
        log.debug("REST request to delete Cassetta : {}", id);
        cassettaService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("cassetta", id.toString())).build();
    }

    /**
     * SEARCH  /_search/cassettas?query=:query : search for the cassetta corresponding
     * to the query.
     *
     * @param query the query of the cassetta search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/cassettas",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Cassetta>> searchCassettas(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Cassettas for query {}", query);
        Page<Cassetta> page = cassettaService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/cassettas");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/_search/consegnas/{idConsegna}/cassettas",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Cassetta>> searchCassettasByConsegna(@PathVariable Long idConsegna, @RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Cassettas by Consegna for query {}", query);
        Page<Cassetta> page = cassettaService.searchByConsegna(idConsegna, query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/consegnas/" + idConsegna + "/cassettas");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
