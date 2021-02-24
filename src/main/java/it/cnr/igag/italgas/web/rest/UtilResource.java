package it.cnr.igag.italgas.web.rest;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;

import it.cnr.igag.italgas.security.AuthoritiesConstants;
import it.cnr.igag.italgas.service.ExcelImportService;

@RestController
@RequestMapping({"/api", "/api_basic"})
public class UtilResource {

private final Logger log = LoggerFactory.getLogger(StatResource.class);
	
	@Inject
    private ExcelImportService importService;
	
	@RequestMapping(value = "/util/folderimport",
	        method = RequestMethod.POST,
	        produces = MediaType.TEXT_PLAIN_VALUE)
    @Timed
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "')")
    public ResponseEntity<String> importFiles(@RequestParam String path, @RequestParam String root, @RequestParam String logPath) {
        log.info("REST request to import files from folder: {}", path);
        
        System.setProperty("importLogDest", logPath);
        
        importService.importData(path, root);
        
        return new ResponseEntity<>("Request accepted, performing import.", HttpStatus.ACCEPTED);
    }
	
}
