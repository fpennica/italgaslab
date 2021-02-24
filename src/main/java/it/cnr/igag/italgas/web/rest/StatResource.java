package it.cnr.igag.italgas.web.rest;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;

import it.cnr.igag.italgas.domain.Consegna;
import it.cnr.igag.italgas.service.ConsegnaService;
import it.cnr.igag.italgas.service.StatService;
import it.cnr.igag.italgas.web.rest.dto.ConsegnaStatoCassetteDTO;
import it.cnr.igag.italgas.web.rest.dto.LaboratorioStatoCassetteDTO;
import it.cnr.igag.italgas.web.rest.dto.ProgettoBasicStatsDTO;

@RestController
@RequestMapping({"/api", "/api_basic"})
public class StatResource {
	
	private final Logger log = LoggerFactory.getLogger(StatResource.class);
	
	@Inject
    private StatService statService;
	
	@Inject
    private ConsegnaService consegnaService;
	
	@RequestMapping(value = "/stats/consegna/{id}/cassette",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<ConsegnaStatoCassetteDTO> getConsegnaStatoCassette(@PathVariable Long id) {
		Consegna consegna = consegnaService.findOne(id, true);
		
		if(consegna == null)
        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		
		ConsegnaStatoCassetteDTO dto = statService.getConsegnaStatoCassette(consegna);
		
		return new ResponseEntity<>(dto, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/stats/laboratorio/cassette",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<LaboratorioStatoCassetteDTO> getLaboratorioStatoCassette() {
				
		LaboratorioStatoCassetteDTO dto = statService.getLaboratorioStatoCassette();
		
		return new ResponseEntity<>(dto, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/stats/progetto/basicstats",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<ProgettoBasicStatsDTO> getProgettoBasicStats() {
				
		ProgettoBasicStatsDTO dto = statService.getProgettoBasicStats();
		
		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

}
