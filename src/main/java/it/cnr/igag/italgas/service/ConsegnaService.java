package it.cnr.igag.italgas.service;

import it.cnr.igag.italgas.domain.Cassetta;
import it.cnr.igag.italgas.domain.Consegna;
import it.cnr.igag.italgas.domain.Laboratorio;
import it.cnr.igag.italgas.domain.Operatore;
import it.cnr.igag.italgas.domain.User;
import it.cnr.igag.italgas.repository.ConsegnaRepository;
import it.cnr.igag.italgas.repository.OperatoreRepository;
import it.cnr.igag.italgas.repository.UserRepository;
import it.cnr.igag.italgas.repository.search.ConsegnaSearchRepository;
import it.cnr.igag.italgas.security.SecurityUtils;
import it.cnr.igag.italgas.web.rest.errors.CustomParameterizedException;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.FileResolver;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.LocalJasperReportsContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing Consegna.
 */
@Service
@Transactional
public class ConsegnaService {

    private final Logger log = LoggerFactory.getLogger(ConsegnaService.class);
    
    @Inject
    private UserRepository userRepository;
    
    @Inject
    private OperatoreRepository operatoreRepository;
    
    @Inject
    private ConsegnaRepository consegnaRepository;
    
    @Inject
    private ConsegnaSearchRepository consegnaSearchRepository;
    
    @Inject
    private CassettaService cassettaService;
    
    /**
     * Save a consegna.
     * 
     * @param consegna the entity to save
     * @return the persisted entity
     */
    public Consegna save(Consegna consegna) {
        log.debug("Request to save Consegna : {}", consegna);
        
        // quando viene fatto un update sul client (ma non viene cambiato o cancellato il blob),
        // bisogna conservare il vecchio blob
        if(consegna.getId() != null &&
        		consegna.getProtocolloAccettazioneContentType() != null &&
        		consegna.getProtocolloAccettazione() == null) {
        	Consegna consegnaOld = findOne(consegna.getId(), false);
        	consegna.setProtocolloAccettazione(consegnaOld.getProtocolloAccettazione());
        }
        
        Consegna result = consegnaRepository.save(consegna);
        consegnaSearchRepository.save(result);
        
        return result;
    }

    /**
     *  Get all the consegnas.
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true) 
    public Page<Consegna> findAll(Pageable pageable) {
        log.debug("Request to get all Consegnas");
        //Page<Consegna> result = consegnaRepository.findAll(pageable); 
        //return result;
        
        Page<Consegna> result;
        
        // gli amministratori accedono a tutte le consegne
        if(SecurityUtils.isCurrentUserInRole("ROLE_ADMIN")) {
        	result = consegnaRepository.findAll(pageable);
        } else {
        	// get current user
	        User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
	        // get operatore
	        Operatore op = operatoreRepository.findByUser(user);
	        
	        result = consegnaRepository.findByLaboratorio(pageable, op.getLaboratorio()); 
        }
        
        // annullo i blob
        resetBlobs(result);
        
        return result;
    }

    /**
     *  Get one consegna by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true) 
    public Consegna findOne(Long id, boolean resetBlobs) {
        log.debug("Request to get Consegna : {}", id);
        Consegna consegna = consegnaRepository.findOne(id);
        
        if(consegna == null)
        	return null;
        
        // annullo i blob
        if(resetBlobs)
        	resetBlobs(consegna);
        
        // get current user
        User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        // get operatore
        Operatore op = operatoreRepository.findByUser(user);
        
        if(SecurityUtils.isCurrentUserInRole("ROLE_ADMIN") ||
        		(consegna.getLaboratorio().getId() == op.getLaboratorio().getId()))
        	return consegna;
        
        return null;
    }
    
    @Transactional(readOnly = true) 
    public Consegna findByLaboratorioAndDataConsegna(Laboratorio laboratorio, LocalDate date) {
        log.debug("Request to get Consegna by lab and date");
        
        Consegna consegna = consegnaRepository.findByLaboratorioAndDataConsegna(laboratorio, date);
        
        if(consegna == null)
        	return null;
        
        // get current user
        User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        // get operatore
        Operatore op = operatoreRepository.findByUser(user);
        
        if(SecurityUtils.isCurrentUserInRole("ROLE_ADMIN") ||
        		(consegna.getLaboratorio().getId() == op.getLaboratorio().getId()))
        	return consegna;
        
        return null;
    }

    /**
     *  Delete the  consegna by id.
     *  
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Consegna : {}", id);
        consegnaRepository.delete(id);
        consegnaSearchRepository.delete(id);
    }

    /**
     * Search for the consegna corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Consegna> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Consegnas for query {}", query);
        //return consegnaSearchRepository.search(queryStringQuery(query), pageable);
        
        Page<Consegna> result;
        
        // gli amministratori accedono a tutte le cassette e tutti i campioni
        if(SecurityUtils.isCurrentUserInRole("ROLE_ADMIN")) {
        	result = consegnaSearchRepository.search(queryStringQuery(query), pageable);
        } else {
        	User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
	        Operatore op = operatoreRepository.findByUser(user);
	        
	        String q = query + " AND laboratorio.id:" + op.getLaboratorio().getId();
	        
	        result = consegnaSearchRepository.search(queryStringQuery(q), pageable);
        }
        
        // annullo i blob
        resetBlobs(result);
        
        return result;
    }
    
    @Transactional(readOnly = true)
    public byte[] generateModelloProtocolloAccettazione(Long id) {
    	log.debug("--------------generate PDF report----------");
        
    	Consegna consegna = findOne(id, true);
    	
        /* 
         * preparazione della bean collection
         */
        Map<String,Object> parameterMap = new HashMap<String,Object>();
        List<Cassetta> cassettaList = cassettaService.findByConsegna(id);
        //log.debug("############ Cassette trovate per la consegna {}: {}", id, cassettaList.size());
        
        // attenzione al secondo parametro: http://community.jaspersoft.com/questions/527187/nosuchmethodexception-unknown-property
        //JRDataSource jrDataSource = new JRBeanCollectionDataSource(cassettaList, false);
        
        //parameterMap.put("listaCassette", jrDataSource);
        String nomeIstituto = consegna.getLaboratorio().getNome();
        parameterMap.put("LISTA_CASSETTE", cassettaList);
        parameterMap.put("ISTITUTO", nomeIstituto.substring(0, nomeIstituto.indexOf("-") - 1));
        parameterMap.put("ISTITUTO_DESC", consegna.getLaboratorio().getIndirizzo().replace("-", "<br/>"));
        parameterMap.put("ISTITUTO_COMUNE", nomeIstituto.substring(nomeIstituto.indexOf("[") + 1, nomeIstituto.indexOf("]")));
        parameterMap.put("CONSEGNA_DATA", consegna.getDataConsegna().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        parameterMap.put("CONSEGNA_NUM_CASSETTE", consegna.getNumCassette() != null ? consegna.getNumCassette().toString() : null);
        parameterMap.put("CONSEGNA_TRASPORTATORE", consegna.getTrasportatore());
        
        /*
        LocalJasperReportsContext ctx = new LocalJasperReportsContext(DefaultJasperReportsContext.getInstance());
        ctx.setClassLoader(getClass().getClassLoader());
        ctx.setFileResolver(new FileResolver() {
            @Override
            public File resolveFile(String s) {
                return new File(s);
            }
        });
        */
        
        /*
         *  preparazione e riempimento del report
         *  si può caricare il file .jasper già compilato oppure compilare al volo un file .jrxml
         */
        //ClassPathResource jasperFile = new ClassPathResource("test1.jasper");
        ClassPathResource jasperFile = new ClassPathResource("protocollo_accettazione_book.jasper");
        
        //InputStream jasperFileInputStream = ConsegnaService.class.getResourceAsStream("/protocollo_accettazione_book.jrxml");
        
        
        InputStream jasperFileInputStream = null;
		try {
			jasperFileInputStream = new FileInputStream(jasperFile.getFile());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new CustomParameterizedException("modelloAccettazioneError", "File not found", jasperFile.getFilename());
		} catch (IOException e) {
			e.printStackTrace();
			throw new CustomParameterizedException("modelloAccettazioneError", "IOException", jasperFile.getFilename());
		}
		
		
        
        /*
        JasperCompileManager cmpmgr = JasperCompileManager.getInstance(ctx);
        JasperFillManager fillmgr = JasperFillManager.getInstance(ctx);
        JasperExportManager exmgr = JasperExportManager.getInstance(ctx);
        */
        
        JasperReport jasperReport;
        JasperPrint jasperPrint;
        byte[] pdfReport = null;
		try {
			// per caricare un file .jasper compilato:
			jasperReport = (JasperReport) JRLoader.loadObject(jasperFileInputStream);
			
			// per caricare e compilare un file .jrxml:
			//jasperReport = JasperCompileManager.compileReport(jasperFileInputStream);
			
			// riempimento del report
			//jasperPrint = JasperFillManager.fillReport(jasperReport, null, jrDataSource);
			jasperPrint = JasperFillManager.fillReport(jasperReport, parameterMap, new JREmptyDataSource());
			
			// esportazione del report in formato pdf (in memoria)
			pdfReport = JasperExportManager.exportReportToPdf(jasperPrint);
			
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new CustomParameterizedException("modelloAccettazioneError", "JRException", e.toString());
		}
		
		return pdfReport;
    }
    
    private void resetBlobs(Page<Consegna> page) {
		page.forEach(consegna-> resetBlobs(consegna));
	}
	
    public Consegna resetBlobs(Consegna consegna) {
		consegna.setProtocolloAccettazione(null);
		return consegna;
	}
}
