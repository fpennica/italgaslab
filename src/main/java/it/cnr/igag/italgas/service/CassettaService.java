package it.cnr.igag.italgas.service;

import it.cnr.igag.italgas.domain.Campione;
import it.cnr.igag.italgas.domain.Cassetta;
import it.cnr.igag.italgas.domain.Consegna;
import it.cnr.igag.italgas.domain.Operatore;
import it.cnr.igag.italgas.domain.User;
import it.cnr.igag.italgas.domain.enumeration.TipoCampione;
import it.cnr.igag.italgas.repository.CampioneRepository;
import it.cnr.igag.italgas.repository.CassettaRepository;
import it.cnr.igag.italgas.repository.ConsegnaRepository;
import it.cnr.igag.italgas.repository.OperatoreRepository;
import it.cnr.igag.italgas.repository.UserRepository;
import it.cnr.igag.italgas.repository.search.CassettaSearchRepository;
import it.cnr.igag.italgas.security.SecurityUtils;
import it.cnr.igag.italgas.web.rest.dto.ClassificazioneGeotecnicaDTO;
import it.cnr.igag.italgas.web.rest.errors.CustomParameterizedException;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import static org.elasticsearch.index.query.QueryBuilders.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service Implementation for managing Cassetta.
 */
@Service
@Transactional
public class CassettaService {

    private final Logger log = LoggerFactory.getLogger(CassettaService.class);

    @Inject
    private UserRepository userRepository;

    @Inject
    private OperatoreRepository operatoreRepository;

    @Inject
    private ConsegnaRepository consegnaRepository;

    @Inject
    private CassettaRepository cassettaRepository;

    @Inject
    private CampioneRepository campioneRepository;

    @Inject
    private CassettaSearchRepository cassettaSearchRepository;

    @Inject
    private CampioneService campioneService;

    /**
     * Save a cassetta.
     *
     * @param cassetta the entity to save
     * @return the persisted entity
     */
    public Cassetta save(Cassetta cassetta) {
        log.debug("Request to save Cassetta : {}", cassetta);

        // quando viene fatto un update sul client (ma non viene cambiato o cancellato il blob),
        // bisogna conservare il vecchio blob
	    if(cassetta.getId() != null) {
        	Cassetta cassettaOld = findOne(cassetta.getId(), false);
	        if(cassetta.getFotoContenitoreContentType() != null &&
	        		cassetta.getFotoContenitore() == null) {
	        	cassetta.setFotoContenitore(cassettaOld.getFotoContenitore());
	        }
	        if(cassetta.getFotoContenutoContentType() != null &&
	        		cassetta.getFotoContenuto() == null) {
	        	cassetta.setFotoContenuto(cassettaOld.getFotoContenuto());
	        }
	        if(cassetta.getCertificatoPdfContentType() != null &&
	        		cassetta.getCertificatoPdf() == null) {
	        	cassetta.setCertificatoPdf(cassettaOld.getCertificatoPdf());
	        }
	    }

	    // resize delle foto
	    int IMG_WIDTH = 640;
	    // se nella cassetta passata al server c'è l'immagine, vuol dire che che va salvata, che sia un update o no
	    if(cassetta.getFotoContenitore() != null) {
		    BufferedImage fotoContenitore = null;
			try {
				fotoContenitore = ImageIO.read(new ByteArrayInputStream(cassetta.getFotoContenitore()));
			} catch (IOException e) {
				throw new CustomParameterizedException("imageError", "IOException", "impossibile leggere l'immagine");
			}
			BufferedImage scaledImage = Scalr.resize(fotoContenitore, IMG_WIDTH);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			String imageType = cassetta.getFotoContenitoreContentType().substring(cassetta.getFotoContenitoreContentType().indexOf("/") + 1);
		    try {
				ImageIO.write(scaledImage, imageType, baos);
			} catch (IOException e) {
				throw new CustomParameterizedException("imageError", "IOException", "impossibile scrivere l'immagine");
			}
		    byte[] bytes = baos.toByteArray();
		    cassetta.setFotoContenitore(bytes);
	    }
	    if(cassetta.getFotoContenuto() != null) {
		    BufferedImage fotoContenuto = null;
			try {
				fotoContenuto = ImageIO.read(new ByteArrayInputStream(cassetta.getFotoContenuto()));
			} catch (IOException e) {
				throw new CustomParameterizedException("imageError", "IOException", "impossibile leggere l'immagine");
			}
			BufferedImage scaledImage = Scalr.resize(fotoContenuto, IMG_WIDTH);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			String imageType = cassetta.getFotoContenutoContentType().substring(cassetta.getFotoContenutoContentType().indexOf("/") + 1);
		    try {
				ImageIO.write(scaledImage, imageType, baos);
			} catch (IOException e) {
				throw new CustomParameterizedException("imageError", "IOException", "impossibile scrivere l'immagine");
			}
		    byte[] bytes = baos.toByteArray();
		    cassetta.setFotoContenuto(bytes);
	    }


	    Cassetta result = cassettaRepository.save(cassetta);

        // creazione codice cassetta
        String codiceCassetta = result.getConsegna().getLaboratorio().getIstituto() + "-" +
        		result.getConsegna().getDataConsegna().toString().replace("-", "") + "-" +
        		"ID" + result.getId().toString() + "-" +
        		"MSL" + (result.getRifGeografo() != null && !result.getRifGeografo().isEmpty() ? result.getRifGeografo() : "-") + "-" +
        		"ODL" + (result.getOdl() != null && !result.getOdl().isEmpty() ? result.getOdl().replace(" ", "") : "-");
        result.setCodiceCassetta(codiceCassetta);
        result = cassettaRepository.save(result);

        cassettaSearchRepository.save(result);

        return result;
    }

    /**
     *  Get all the cassettas.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Cassetta> findAll(Pageable pageable) {
        log.debug("Request to get all Cassettas");

        // gli amministratori accedono a tutte le cassette e tutti i campioni
        if(SecurityUtils.isCurrentUserInRole("ROLE_ADMIN")) {
        	Page<Cassetta> result = cassettaRepository.findAll(pageable);

        	// annullo i blob
        	resetBlobs(result);

        	return result;
        }

        // get current user
        User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        // get operatore
        Operatore op = operatoreRepository.findByUser(user);

        Page<Cassetta> result = cassettaRepository.findByLaboratorio(pageable, op.getLaboratorio());

     	// annullo i blob
        resetBlobs(result);

        return result;
    }

    @Transactional(readOnly = true)
    public Page<Cassetta> findByConsegna(Pageable pageable, Long idConsegna) {
		// TODO Auto-generated method stub
    	log.debug("Request to get all Cassettas by Consegna with id: {}", idConsegna);
        //Page<Cassetta> result = cassettaRepository.findAll(pageable);
        //return result;

    	Consegna consegna = consegnaRepository.findOne(idConsegna);

    	// get current user
        User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        // get operatore
        Operatore op = operatoreRepository.findByUser(user);

        // gli amministratori accedono a tutte le cassette e tutti i campioni
        if(SecurityUtils.isCurrentUserInRole("ROLE_ADMIN") ||
        		(consegna.getLaboratorio().getId() == op.getLaboratorio().getId())) {

        	Page<Cassetta> result = cassettaRepository.findByConsegna(pageable, consegna);

        	// annullo i blob
        	resetBlobs(result);

        	return result;
        }

        return null;
	}

    @Transactional(readOnly = true)
    public List<Cassetta> findByConsegna(Long idConsegna) {
		// TODO Auto-generated method stub
    	log.debug("Request to get all Cassettas (not paged) by Consegna with id: {}", idConsegna);
        //Page<Cassetta> result = cassettaRepository.findAll(pageable);
        //return result;

    	Consegna consegna = consegnaRepository.findOne(idConsegna);

    	// get current user
        User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        // get operatore
        Operatore op = operatoreRepository.findByUser(user);

        // gli amministratori accedono a tutte le cassette e tutti i campioni
        if(SecurityUtils.isCurrentUserInRole("ROLE_ADMIN") ||
        		(consegna.getLaboratorio().getId() == op.getLaboratorio().getId())) {

        	List<Cassetta> result = cassettaRepository.findByConsegnaOrderByRifGeografo(consegna);

        	// annullo i blob
        	//resetBlobs(result);

        	return result;
        }

        return null;
	}

    @Transactional(readOnly = true)
    public List<Cassetta> findByLaboratorio() {
    	// get current user
        //User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        // get operatore
        Operatore op = operatoreRepository.findByLogin();

        List<Cassetta> result = cassettaRepository.findByLaboratorio(op.getLaboratorio());

     	// annullo i blob
        //resetBlobs(result);

		return result;

    }

    /**
     *  Get one cassetta by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Cassetta findOne(Long id, boolean resetBlobs) {
        log.debug("Request to get Cassetta : {}", id);
        Cassetta cassetta = cassettaRepository.findOne(id);

        if(cassetta == null)
        	return null;

        // annullo i blob
        if(resetBlobs)
        	resetBlobs(cassetta);

        // get current user
        User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        // get operatore
        Operatore op = operatoreRepository.findByUser(user);

        // gli amministratori accedono a tutte le cassette e tutti i campioni
        if(SecurityUtils.isCurrentUserInRole("ROLE_ADMIN") ||
        		(cassetta.getConsegna().getLaboratorio().getId() == op.getLaboratorio().getId())) {
        	return cassetta;
        }

        return null;
    }

    /**
     *  Delete the  cassetta by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Cassetta : {}", id);
        cassettaRepository.delete(id);
        cassettaSearchRepository.delete(id);
    }

    /**
     * Search for the cassetta corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Cassetta> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Cassettas for query {}", query);
        //return cassettaSearchRepository.search(queryStringQuery(query), pageable);

        // gli amministratori accedono a tutte le cassette e tutti i campioni
        if(SecurityUtils.isCurrentUserInRole("ROLE_ADMIN")) {
        	Page<Cassetta> result = cassettaSearchRepository.search(queryStringQuery(query), pageable);

            // annullo i blob
        	resetBlobs(result);

            return result;
        	//return cassettaSearchRepository.search(queryStringQuery(query), pageable);
        }

        User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        Operatore op = operatoreRepository.findByUser(user);

        String q = query + " AND consegna.laboratorio.id:" + op.getLaboratorio().getId();

        Page<Cassetta> result = cassettaSearchRepository.search(queryStringQuery(q), pageable);

        // annullo i blob
        resetBlobs(result);

        return result;
    }

    @Transactional(readOnly = true)
	public Page<Cassetta> searchByConsegna(Long idConsegna, String query, Pageable pageable) {
		log.debug("Request to search for a page of Cassettas by Consegna for query {}", query);

		Consegna consegna = consegnaRepository.findOne(idConsegna);

		// gli amministratori accedono a tutte le cassette e tutti i campioni
        if(SecurityUtils.isCurrentUserInRole("ROLE_ADMIN")) {
        	String q = query + " AND consegna.id:" + consegna.getId();
        	Page<Cassetta> result = cassettaSearchRepository.search(queryStringQuery(q), pageable);

            // annullo i blob
        	resetBlobs(result);

            return result;
        	//return cassettaSearchRepository.search(queryStringQuery(q), pageable);
        }

        User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        Operatore op = operatoreRepository.findByUser(user);

        String q = query + " AND consegna.id:" + consegna.getId() + " AND consegna.laboratorio.id:" + op.getLaboratorio().getId();

        Page<Cassetta> result = cassettaSearchRepository.search(queryStringQuery(q), pageable);

        // annullo i blob
        resetBlobs(result);

        return result;
	}

    @Transactional(readOnly = true)
    public byte[] generateModelloCertificato(Long idCassetta) {

    	// prendo la cassetta con tutti i blob
    	Cassetta cassetta = findOne(idCassetta, false);

    	if(cassetta == null)
    		return null;

    	List<Campione> campioneList = campioneRepository.findByCassetta(cassetta);

    	if(campioneList == null || campioneList.size() == 0)
    		return null;

    	Map<String,Object> parameterMap = new HashMap<String,Object>();

    	String nomeIstituto = cassetta.getConsegna().getLaboratorio().getNome();
        parameterMap.put("ISTITUTO", nomeIstituto.substring(0, nomeIstituto.indexOf("-") - 1));
        //log.debug(nomeIstituto);
        parameterMap.put("ISTITUTO_DESC", cassetta.getConsegna().getLaboratorio().getIndirizzo().replace("-", "<br/>"));
        //log.debug(cassetta.getConsegna().getLaboratorio().getIndirizzo());
        parameterMap.put("ISTITUTO_COMUNE", nomeIstituto.substring(nomeIstituto.indexOf("[") + 1, nomeIstituto.indexOf("]")));

        parameterMap.put("CONSEGNA", cassetta.getConsegna());
        parameterMap.put("CASSETTA", cassetta);

        if(cassetta.getFotoContenitore() != null) {
        	// http://stackoverflow.com/questions/17804348/blob-image-not-displayed-in-jasper-report-pdf
        	ByteArrayInputStream baisCassettaFoto = new ByteArrayInputStream(cassetta.getFotoContenitore());
            parameterMap.put("CASSETTA_FOTO", baisCassettaFoto);
        }

        boolean isCampioneA = false;
        boolean isCampioneA1 = false;
        boolean isCampioneB = false;
        boolean isCampioneC = false;
        for(Campione c : campioneList){
        	if(c.getTipoCampione().equals(TipoCampione.A))
        		isCampioneA = true;
        	if(c.getTipoCampione().equals(TipoCampione.A1))
        		isCampioneA1 = true;
        	if(c.getTipoCampione().equals(TipoCampione.B))
        		isCampioneB = true;
        	if(c.getTipoCampione().equals(TipoCampione.C))
        		isCampioneC = true;
        }

        if(!isCampioneA)
        	parameterMap.put("CAMPIONE_A_NOTE", "Campione A non presente");
        if(!isCampioneA1)
        	parameterMap.put("CAMPIONE_A1_NOTE", "Campione A1 non presente");
        if(!isCampioneB)
        	parameterMap.put("CAMPIONE_B_NOTE", "Campione B non presente");
        if(!isCampioneC)
        	parameterMap.put("CAMPIONE_C_NOTE", "Campione C non presente");

        for(Campione c : campioneList) {
    		// per ogni campione creo il relativo parametro e i DTO per classificazione geotecnica e distrib granulometrica
    		if(c.getTipoCampione().equals(TipoCampione.A)) {
    			parameterMap.put("CAMPIONE_A", c);

    			if(c.getFotoSacchetto() != null) {
    	        	ByteArrayInputStream baisCampioneAFotoSacchetto = new ByteArrayInputStream(c.getFotoSacchetto());
    	            parameterMap.put("CAMPIONE_A_FOTO_SACCHETTO", baisCampioneAFotoSacchetto);
    	        }
    			if(c.getFotoCampione() != null) {
    	        	ByteArrayInputStream baisCampioneAFotoCampione = new ByteArrayInputStream(c.getFotoCampione());
    	            parameterMap.put("CAMPIONE_A_FOTO_CAMPIONE", baisCampioneAFotoCampione);
    	        }
    			if(c.getCurva() != null) {
    	        	ByteArrayInputStream baisCampioneACurva = new ByteArrayInputStream(c.getCurva());
    	            parameterMap.put("CAMPIONE_A_CURVA", baisCampioneACurva);
    	        }

    			ClassificazioneGeotecnicaDTO classGeo = campioneService.generateClassificazioneGeotecnica(c);
    			byte[] pieChart = campioneService.generatePieChart(classGeo);
    			ByteArrayInputStream baisPieChart = new ByteArrayInputStream(pieChart);
    			parameterMap.put("CAMPIONE_A_PIECHART", baisPieChart);

    			parameterMap.put("CAMPIONE_A_CLASS_GEOTEC", classGeo.getClassificazioneGeotecnica());

    			// per inserire comunque operatore sconosciuto/scritto male/operatori multipli
    			String note = null;
    			if(c.getNote() != null && !c.getNote().isEmpty() && c.getNote().indexOf("###") > -1) {
    				note = c.getNote().substring(0, c.getNote().indexOf("###"));
	    			String operatoreCustom = c.getNote().substring(c.getNote().indexOf("###")).replace("###", "");

	    			if(operatoreCustom != null && !operatoreCustom.isEmpty() && c.getOperatoreAnalisi() == null) {

	    				parameterMap.put("CAMPIONE_A_OPERATORE", operatoreCustom);
	    			}

    			} else {
    				note = c.getNote();
    				if(c.getOperatoreAnalisi() != null)
    					parameterMap.put("CAMPIONE_A_OPERATORE", c.getOperatoreAnalisi().getUser().getFirstName() + " " + c.getOperatoreAnalisi().getUser().getLastName());
    			}

    			parameterMap.put("CAMPIONE_A_NOTE", note);

    			parameterMap.put("CAMPIONE_A_GRANULOMETRIA", campioneService.generateListaSetacci(c));

    			parameterMap.put("CAMPIONE_A_CONFORME", campioneService.checkConformAA1(c));
    		}

    		if(c.getTipoCampione().equals(TipoCampione.A1)) {
    			parameterMap.put("CAMPIONE_A1", c);

    			if(c.getFotoSacchetto() != null) {
    	        	ByteArrayInputStream baisCampioneAFotoSacchetto = new ByteArrayInputStream(c.getFotoSacchetto());
    	            parameterMap.put("CAMPIONE_A1_FOTO_SACCHETTO", baisCampioneAFotoSacchetto);
    	        }
    			if(c.getFotoCampione() != null) {
    	        	ByteArrayInputStream baisCampioneAFotoCampione = new ByteArrayInputStream(c.getFotoCampione());
    	            parameterMap.put("CAMPIONE_A1_FOTO_CAMPIONE", baisCampioneAFotoCampione);
    	        }
    			if(c.getCurva() != null) {
    	        	ByteArrayInputStream baisCampioneACurva = new ByteArrayInputStream(c.getCurva());
    	            parameterMap.put("CAMPIONE_A1_CURVA", baisCampioneACurva);
    	        }

    			ClassificazioneGeotecnicaDTO classGeo = campioneService.generateClassificazioneGeotecnica(c);
    			byte[] pieChart = campioneService.generatePieChart(classGeo);
    			ByteArrayInputStream baisPieChart = new ByteArrayInputStream(pieChart);
    			parameterMap.put("CAMPIONE_A1_PIECHART", baisPieChart);

    			parameterMap.put("CAMPIONE_A1_CLASS_GEOTEC", classGeo.getClassificazioneGeotecnica());

    			// per inserire comunque operatore sconosciuto/scritto male/operatori multipli
    			String note = null;
    			if(c.getNote() != null && !c.getNote().isEmpty() && c.getNote().indexOf("###") > -1) {
    				note = c.getNote().substring(0, c.getNote().indexOf("###"));
	    			String operatoreCustom = c.getNote().substring(c.getNote().indexOf("###")).replace("###", "");

	    			if(operatoreCustom != null && !operatoreCustom.isEmpty() && c.getOperatoreAnalisi() == null) {
//	    				Operatore o = operatoreRepository.findOneByUserId(6); //operatore d'appoggio: "operatore"
//	    				o.getUser().setFirstName(operatoreCustom); //sul rapporto viene messo firstName + " " + lastName
//	    				o.getUser().setLastName("");
//
//	    				c.setOperatoreAnalisi(o); // il campione ha ora l'operatoreCustom (non salvato nel db)

	    				parameterMap.put("CAMPIONE_A1_OPERATORE", operatoreCustom);
	    			}

    			} else {
    				note = c.getNote();
    				if(c.getOperatoreAnalisi() != null)
    					parameterMap.put("CAMPIONE_A1_OPERATORE", c.getOperatoreAnalisi().getUser().getFirstName() + " " + c.getOperatoreAnalisi().getUser().getLastName());
    			}

    			parameterMap.put("CAMPIONE_A1_NOTE", note);

    			parameterMap.put("CAMPIONE_A1_GRANULOMETRIA", campioneService.generateListaSetacci(c));

    			parameterMap.put("CAMPIONE_A1_CONFORME", campioneService.checkConformAA1(c));
    		}

    		if(c.getTipoCampione().equals(TipoCampione.B)) {
    			parameterMap.put("CAMPIONE_B", c);

    			if(c.getFotoSacchetto() != null) {
    	        	ByteArrayInputStream baisCampioneAFotoSacchetto = new ByteArrayInputStream(c.getFotoSacchetto());
    	            parameterMap.put("CAMPIONE_B_FOTO_SACCHETTO", baisCampioneAFotoSacchetto);
    	        }
    			if(c.getFotoCampione() != null) {
    	        	ByteArrayInputStream baisCampioneAFotoCampione = new ByteArrayInputStream(c.getFotoCampione());
    	            parameterMap.put("CAMPIONE_B_FOTO_CAMPIONE", baisCampioneAFotoCampione);
    	        }

    			ClassificazioneGeotecnicaDTO classGeo = campioneService.generateClassificazioneGeotecnica(c);

    			if(classGeo != null) {
    				parameterMap.put("CAMPIONE_B_CLASS_GEOTEC", classGeo.getClassificazioneGeotecnica());
    			} else {
    				parameterMap.put("CAMPIONE_B_CLASS_GEOTEC", c.getDescrizioneCampione());
    			}

    			parameterMap.put("CAMPIONE_B_PASSANTE_100", campioneService.checkPassanteB(c));

    			// per inserire comunque operatore sconosciuto/scritto male/operatori multipli
    			String note = null;
    			if(c.getNote() != null && !c.getNote().isEmpty() && c.getNote().indexOf("###") > -1) {
    				note = c.getNote().substring(0, c.getNote().indexOf("###"));
	    			String operatoreCustom = c.getNote().substring(c.getNote().indexOf("###")).replace("###", "");

	    			if(operatoreCustom != null && !operatoreCustom.isEmpty() && c.getOperatoreAnalisi() == null) {
//	    				Operatore o1 = operatoreRepository.findOneByUserId(6); //operatore d'appoggio: "operatore"
//	    				o1.getUser().setFirstName(operatoreCustom); //sul rapporto viene messo firstName + " " + lastName
//	    				o1.getUser().setLastName("");
//
//	    				c.setOperatoreAnalisi(o1); // il campione ha ora l'operatoreCustom (non salvato nel db)

	    				parameterMap.put("CAMPIONE_B_OPERATORE", operatoreCustom);
	    			}

    			} else {
    				note = c.getNote();
    				if(c.getOperatoreAnalisi() != null)
    					parameterMap.put("CAMPIONE_B_OPERATORE", c.getOperatoreAnalisi().getUser().getFirstName() + " " + c.getOperatoreAnalisi().getUser().getLastName());
    			}
    			parameterMap.put("CAMPIONE_B_NOTE", note);

    		}

    		if(c.getTipoCampione().equals(TipoCampione.C)) {
    			parameterMap.put("CAMPIONE_C", c);

    			if(c.getFotoSacchetto() != null) {
    	        	ByteArrayInputStream baisCampioneAFotoSacchetto = new ByteArrayInputStream(c.getFotoSacchetto());
    	            parameterMap.put("CAMPIONE_C_FOTO_SACCHETTO", baisCampioneAFotoSacchetto);
    	        }
    			if(c.getFotoCampione() != null) {
    	        	ByteArrayInputStream baisCampioneAFotoCampione = new ByteArrayInputStream(c.getFotoCampione());
    	            parameterMap.put("CAMPIONE_C_FOTO_CAMPIONE", baisCampioneAFotoCampione);
    	        }

    			// per inserire comunque operatore sconosciuto/scritto male/operatori multipli
    			String note = null;
    			if(c.getNote() != null && !c.getNote().isEmpty() && c.getNote().indexOf("###") > -1) {
    				note = c.getNote().substring(0, c.getNote().indexOf("###"));
	    			String operatoreCustom = c.getNote().substring(c.getNote().indexOf("###")).replace("###", "");

	    			if(operatoreCustom != null && !operatoreCustom.isEmpty() && c.getOperatoreAnalisi() == null) {
//	    				Operatore o2 = operatoreRepository.findOneByUserId(6); //operatore d'appoggio: "operatore"
//	    				o2.getUser().setFirstName(operatoreCustom); //sul rapporto viene messo firstName + " " + lastName
//	    				o2.getUser().setLastName("");
//
//	    				c.setOperatoreAnalisi(o2); // il campione ha ora l'operatoreCustom (non salvato nel db)

	    				parameterMap.put("CAMPIONE_C_OPERATORE", operatoreCustom);
	    			}

    			} else {
    				note = c.getNote();
    				if(c.getOperatoreAnalisi() != null)
    					parameterMap.put("CAMPIONE_C_OPERATORE", c.getOperatoreAnalisi().getUser().getFirstName() + " " + c.getOperatoreAnalisi().getUser().getLastName());
    			}

    			parameterMap.put("CAMPIONE_C_NOTE", note);

    		}
    	}

        //parameterMap.put("SERIE_MASSE_A", serieMasseA);

        ClassPathResource jasperFile = new ClassPathResource("certificato_book.jasper");
        //InputStream jasperFileInputStream = CassettaService.class.getResourceAsStream("/certificato_book.jrxml");

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
			e.printStackTrace();
			throw new CustomParameterizedException("modelloCertificatoError", "JRException", e.toString());
		}

    	return pdfReport;

    }

    public String exportModelloCertificato(long idCassetta) {

    	return null;
    }

	private void resetBlobs(Page<Cassetta> page) {
		page.forEach(cassetta-> resetBlobs(cassetta));
	}

	public Cassetta resetBlobs(Cassetta cassetta) {
		cassetta.setFotoContenitore(null);
        cassetta.setFotoContenuto(null);
        cassetta.setCertificatoPdf(null);
        cassetta.getConsegna().setProtocolloAccettazione(null);
        return cassetta;
	}

}
