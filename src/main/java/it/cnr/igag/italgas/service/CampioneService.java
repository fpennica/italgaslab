package it.cnr.igag.italgas.service;

import it.cnr.igag.italgas.domain.Campione;
import it.cnr.igag.italgas.domain.Cassetta;
import it.cnr.igag.italgas.domain.Operatore;
import it.cnr.igag.italgas.domain.Pesata;
import it.cnr.igag.italgas.domain.User;
import it.cnr.igag.italgas.domain.enumeration.TipoCampione;
import it.cnr.igag.italgas.repository.CampioneRepository;
import it.cnr.igag.italgas.repository.CassettaRepository;
import it.cnr.igag.italgas.repository.OperatoreRepository;
import it.cnr.igag.italgas.repository.PesataRepository;
import it.cnr.igag.italgas.repository.UserRepository;
import it.cnr.igag.italgas.repository.search.CampioneSearchRepository;
import it.cnr.igag.italgas.security.SecurityUtils;
import it.cnr.igag.italgas.web.rest.dto.CbPercGranDTO;
import it.cnr.igag.italgas.web.rest.dto.ClassificazioneGeotecnicaDTO;
import it.cnr.igag.italgas.web.rest.dto.SetaccioDTO;
import it.cnr.igag.italgas.web.rest.errors.CustomParameterizedException;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

import org.imgscalr.Scalr;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.inject.Inject;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing Campione.
 */
@Service
@Transactional
public class CampioneService {

    private final Logger log = LoggerFactory.getLogger(CampioneService.class);
    
    @Inject
    private UserRepository userRepository;
    
    @Inject
    private OperatoreRepository operatoreRepository;
    
    @Inject
    private CampioneRepository campioneRepository;
    
    @Inject
    private CampioneSearchRepository campioneSearchRepository;
    
    @Inject
    private CassettaRepository cassettaRepository;
    
    @Inject
    private PesataService pesataService;
    
    @Inject
    private PesataRepository pesataRepository;
    
    /**
     * Save a campione.
     * 
     * @param campione the entity to save
     * @return the persisted entity
     */
    public Campione save(Campione campione) {
        log.debug("Request to save Campione : {}", campione);
        
        // quando viene fatto un update sul client (ma non viene cambiato o cancellato il blob),
        // bisogna conservare il vecchio blob
	    if(campione.getId() != null) {    
	    	Campione campioneOld = findOne(campione.getId(), false);
	        if(campione.getFotoSacchettoContentType() != null &&
	        		campione.getFotoSacchetto() == null) {
	        	campione.setFotoSacchetto(campioneOld.getFotoSacchetto());
	        }
	        if(campione.getFotoCampioneContentType() != null &&
	        		campione.getFotoCampione() == null) {
	        	campione.setFotoCampione(campioneOld.getFotoCampione());
	        }
	        if(campione.getFotoEtichettaContentType() != null &&
	        		campione.getFotoEtichetta() == null) {
	        	campione.setFotoEtichetta(campioneOld.getFotoEtichetta());
	        }
	        if(campione.getCurvaContentType() != null &&
	        		campione.getCurva() == null) {
	        	campione.setCurva(campioneOld.getCurva());
	        }
	        if(campione.getCurvaBinderContentType() != null &&
	        		campione.getCurvaBinder() == null) {
	        	campione.setCurvaBinder(campioneOld.getCurvaBinder());
	        }
	    }
	    
	    // resize delle foto
	    int IMG_WIDTH = 640;
	    // se nella cassetta passata al server c'è l'immagine, vuol dire che che va salvata, che sia un update o no
	    if(campione.getFotoSacchetto() != null) {
		    BufferedImage fotoSacchetto = null;
			try {
				fotoSacchetto = ImageIO.read(new ByteArrayInputStream(campione.getFotoSacchetto()));
			} catch (IOException e) {
				throw new CustomParameterizedException("imageError", "IOException", "impossibile leggere l'immagine");
			}
			BufferedImage scaledImage = Scalr.resize(fotoSacchetto, IMG_WIDTH);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			String imageType = campione.getFotoSacchettoContentType().substring(campione.getFotoSacchettoContentType().indexOf("/") + 1);
		    try {
				ImageIO.write(scaledImage, imageType, baos);
			} catch (IOException e) {
				throw new CustomParameterizedException("imageError", "IOException", "impossibile scrivere l'immagine");
			}
		    byte[] bytes = baos.toByteArray();
		    campione.setFotoSacchetto(bytes);
	    }
	    if(campione.getFotoCampione() != null) {
		    BufferedImage fotoCampione = null;
			try {
				fotoCampione = ImageIO.read(new ByteArrayInputStream(campione.getFotoCampione()));
			} catch (IOException e) {
				throw new CustomParameterizedException("imageError", "IOException", "impossibile leggere l'immagine");
			}
			BufferedImage scaledImage = Scalr.resize(fotoCampione, IMG_WIDTH);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			String imageType = campione.getFotoCampioneContentType().substring(campione.getFotoCampioneContentType().indexOf("/") + 1);
		    try {
				ImageIO.write(scaledImage, imageType, baos);
			} catch (IOException e) {
				throw new CustomParameterizedException("imageError", "IOException", "impossibile scrivere l'immagine");
			}
		    byte[] bytes = baos.toByteArray();
		    campione.setFotoCampione(bytes);
	    }
	    if(campione.getFotoEtichetta() != null) {
		    BufferedImage fotoEtichetta = null;
			try {
				fotoEtichetta = ImageIO.read(new ByteArrayInputStream(campione.getFotoEtichetta()));
			} catch (IOException e) {
				throw new CustomParameterizedException("imageError", "IOException", "impossibile leggere l'immagine");
			}
			BufferedImage scaledImage = Scalr.resize(fotoEtichetta, IMG_WIDTH);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			String imageType = campione.getFotoEtichettaContentType().substring(campione.getFotoEtichettaContentType().indexOf("/") + 1);
		    try {
				ImageIO.write(scaledImage, imageType, baos);
			} catch (IOException e) {
				throw new CustomParameterizedException("imageError", "IOException", "impossibile scrivere l'immagine");
			}
		    byte[] bytes = baos.toByteArray();
		    campione.setFotoEtichetta(bytes);
	    }
        
        Campione result = campioneRepository.save(campione);
        
        // creazione codice campione
        String codiceCampione = result.getCassetta().getCodiceCassetta() + "-" + result.getTipoCampione();
        result.setCodiceCampione(codiceCampione);
        result = campioneRepository.save(result);
        
        campioneSearchRepository.save(result);
        
        return result;
    }

    @Transactional(readOnly = true)
    private List<BigDecimal> generateSerieMasse(Campione campione) {
    	
    	List<Pesata> pesataList = pesataRepository.findByCampioneOrderByNumPesataAsc(campione);
    	
    	if(pesataList == null || pesataList.size() == 0) {
    		throw new CustomParameterizedException("pesataListEmptyError", "Non sono stati inseriti i dati di setacciatura! (campione " + campione.getTipoCampione() + ")");
    	}
    	
    	List<BigDecimal> serieMasse = new ArrayList<BigDecimal>();
    	//BigDecimal sumTratt = new BigDecimal(0);
    	BigDecimal sumFondo = new BigDecimal(0);
    	BigDecimal sumTratt125 = new BigDecimal(0);
    	BigDecimal sumTratt63 = new BigDecimal(0);
    	BigDecimal sumTratt31V5 = new BigDecimal(0);
    	BigDecimal sumTratt16 = new BigDecimal(0);
    	BigDecimal sumTratt8 = new BigDecimal(0);
    	BigDecimal sumTratt6V3 = new BigDecimal(0);
    	BigDecimal sumTratt4 = new BigDecimal(0);
    	BigDecimal sumTratt2 = new BigDecimal(0);
    	BigDecimal sumTratt1 = new BigDecimal(0);
    	BigDecimal sumTratt0V5 = new BigDecimal(0);
    	BigDecimal sumTratt0V25 = new BigDecimal(0);
    	BigDecimal sumTratt0V125 = new BigDecimal(0);
    	BigDecimal sumTratt0V075 = new BigDecimal(0);
    	
    	for(Pesata p: pesataList) {
    		sumTratt125 = sumTratt125.add(p.getTratt125Mm());
    		sumTratt63 = sumTratt63.add(p.getTratt63Mm());
    		sumTratt31V5 = sumTratt31V5.add(p.getTratt31V5Mm());
    		sumTratt16 = sumTratt16.add(p.getTratt16Mm());
    		sumTratt8 = sumTratt8.add(p.getTratt8Mm());
    		sumTratt6V3 = sumTratt6V3.add(p.getTratt6V3Mm());
    		sumTratt4 = sumTratt4.add(p.getTratt4Mm());
    		sumTratt2 = sumTratt2.add(p.getTratt2Mm());
    		sumTratt1 = sumTratt1.add(p.getTratt1Mm());
    		sumTratt0V5 = sumTratt0V5.add(p.getTratt0V5Mm());
    		sumTratt0V25 = sumTratt0V25.add(p.getTratt0V25Mm());
    		sumTratt0V125 = sumTratt0V125.add(p.getTratt0V125Mm());
    		sumTratt0V075 = sumTratt0V075.add(p.getTratt0V075Mm());
    		sumFondo = sumFondo.add(p.getFondo());
    	}
    	
    	serieMasse.add(sumTratt125);
    	serieMasse.add(sumTratt63);
    	serieMasse.add(sumTratt31V5);
    	serieMasse.add(sumTratt16);
    	serieMasse.add(sumTratt8);
    	serieMasse.add(sumTratt6V3);
    	serieMasse.add(sumTratt4);
    	serieMasse.add(sumTratt2);
    	serieMasse.add(sumTratt1);
    	serieMasse.add(sumTratt0V5);
    	serieMasse.add(sumTratt0V25);
    	serieMasse.add(sumTratt0V125);
    	serieMasse.add(sumTratt0V075);
    	serieMasse.add(sumFondo);
    	
    	return serieMasse;
    }
    
    @Transactional(readOnly = true)
    private List<BigDecimal> generateSerieTrattenuti(Campione campione) {
    	
    	List<Pesata> pesataList = pesataRepository.findByCampioneOrderByNumPesataAsc(campione);
    	
    	if(pesataList == null || pesataList.size() == 0) {
    		throw new CustomParameterizedException("pesataListEmptyError", "Non sono stati inseriti i dati di setacciatura! (campione " + campione.getTipoCampione() + ")");
    	}
    	
    	List<BigDecimal> serieMasse = generateSerieMasse(campione);
    	List<BigDecimal> serieTrattenuti = new ArrayList<BigDecimal>();
    	BigDecimal sumTratt = new BigDecimal(0);
    	
//    	BigDecimal sumFondo = new BigDecimal(0);
//    	BigDecimal sumTratt125 = new BigDecimal(0);
//    	BigDecimal sumTratt63 = new BigDecimal(0);
//    	BigDecimal sumTratt31V5 = new BigDecimal(0);
//    	BigDecimal sumTratt16 = new BigDecimal(0);
//    	BigDecimal sumTratt8 = new BigDecimal(0);
//    	BigDecimal sumTratt6V3 = new BigDecimal(0);
//    	BigDecimal sumTratt4 = new BigDecimal(0);
//    	BigDecimal sumTratt2 = new BigDecimal(0);
//    	BigDecimal sumTratt1 = new BigDecimal(0);
//    	BigDecimal sumTratt0V5 = new BigDecimal(0);
//    	BigDecimal sumTratt0V25 = new BigDecimal(0);
//    	BigDecimal sumTratt0V125 = new BigDecimal(0);
//    	BigDecimal sumTratt0V075 = new BigDecimal(0);
    	
    	BigDecimal sumTratt125 = serieMasse.get(0);
    	BigDecimal sumTratt63 = serieMasse.get(1);
    	BigDecimal sumTratt31V5 = serieMasse.get(2);
    	BigDecimal sumTratt16 = serieMasse.get(3);
    	BigDecimal sumTratt8 = serieMasse.get(4);
    	BigDecimal sumTratt6V3 = serieMasse.get(5);
    	BigDecimal sumTratt4 = serieMasse.get(6);
    	BigDecimal sumTratt2 = serieMasse.get(7);
    	BigDecimal sumTratt1 = serieMasse.get(8);
    	BigDecimal sumTratt0V5 = serieMasse.get(9);
    	BigDecimal sumTratt0V25 = serieMasse.get(10);
    	BigDecimal sumTratt0V125 = serieMasse.get(11);
    	BigDecimal sumTratt0V075 = serieMasse.get(12);
    	BigDecimal sumFondo = serieMasse.get(13);
    	
    	for(Pesata p: pesataList) {
    		// sommatoria trattenuti
    		sumTratt = sumTratt.add(p.getTratt125Mm());
    		sumTratt = sumTratt.add(p.getTratt63Mm());
    		sumTratt = sumTratt.add(p.getTratt31V5Mm());
    		sumTratt = sumTratt.add(p.getTratt16Mm());
    		sumTratt = sumTratt.add(p.getTratt8Mm());
    		sumTratt = sumTratt.add(p.getTratt6V3Mm());
    		sumTratt = sumTratt.add(p.getTratt4Mm());
    		sumTratt = sumTratt.add(p.getTratt2Mm());
    		sumTratt = sumTratt.add(p.getTratt1Mm());
    		sumTratt = sumTratt.add(p.getTratt0V5Mm());
    		sumTratt = sumTratt.add(p.getTratt0V25Mm());
    		sumTratt = sumTratt.add(p.getTratt0V125Mm());
    		sumTratt = sumTratt.add(p.getTratt0V075Mm());
    		sumTratt = sumTratt.add(p.getFondo());
    		log.debug("sumTratt parziale: {}", sumTratt.toString());
    		
    		//sumFondo = sumFondo.add(p.getFondo());
    	}
    	log.debug("sumTratt: {}", sumTratt.toString());
    	
    	if(sumTratt.compareTo(BigDecimal.ZERO) == 0) {
    		throw new CustomParameterizedException("sumTrattError", "La sommatoria dei trattenuti è 0, controllare i dati inseriti!");
    	}
    	
//    	serieTrattenuti.add(sumTratt125.multiply(new BigDecimal(100)).divide(sumTratt, 2, BigDecimal.ROUND_HALF_EVEN));
//    	serieTrattenuti.add(sumTratt63.multiply(new BigDecimal(100)).divide(sumTratt, 2, BigDecimal.ROUND_HALF_EVEN));
//    	serieTrattenuti.add(sumTratt31V5.multiply(new BigDecimal(100)).divide(sumTratt, 2, BigDecimal.ROUND_HALF_EVEN));
//    	serieTrattenuti.add(sumTratt16.multiply(new BigDecimal(100)).divide(sumTratt, 2, BigDecimal.ROUND_HALF_EVEN));
//    	serieTrattenuti.add(sumTratt8.multiply(new BigDecimal(100)).divide(sumTratt, 2, BigDecimal.ROUND_HALF_EVEN));
//    	serieTrattenuti.add(sumTratt6V3.multiply(new BigDecimal(100)).divide(sumTratt, 2, BigDecimal.ROUND_HALF_EVEN));
//    	serieTrattenuti.add(sumTratt4.multiply(new BigDecimal(100)).divide(sumTratt, 2, BigDecimal.ROUND_HALF_EVEN));
//    	serieTrattenuti.add(sumTratt2.multiply(new BigDecimal(100)).divide(sumTratt, 2, BigDecimal.ROUND_HALF_EVEN));
//    	serieTrattenuti.add(sumTratt1.multiply(new BigDecimal(100)).divide(sumTratt, 2, BigDecimal.ROUND_HALF_EVEN));
//    	serieTrattenuti.add(sumTratt0V5.multiply(new BigDecimal(100)).divide(sumTratt, 2, BigDecimal.ROUND_HALF_EVEN));
//    	serieTrattenuti.add(sumTratt0V25.multiply(new BigDecimal(100)).divide(sumTratt, 2, BigDecimal.ROUND_HALF_EVEN));
//    	serieTrattenuti.add(sumTratt0V125.multiply(new BigDecimal(100)).divide(sumTratt, 2, BigDecimal.ROUND_HALF_EVEN));
//    	serieTrattenuti.add(sumTratt0V075.multiply(new BigDecimal(100)).divide(sumTratt, 2, BigDecimal.ROUND_HALF_EVEN));
//    	serieTrattenuti.add(sumFondo.multiply(new BigDecimal(100)).divide(sumTratt, 2, BigDecimal.ROUND_HALF_EVEN));
    	
    	serieTrattenuti.add(sumTratt125.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt63.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt31V5.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt16.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt8.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt6V3.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt4.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt2.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt1.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt0V5.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt0V25.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt0V125.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt0V075.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumFondo.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	
    	return serieTrattenuti;
    }
    
    @Transactional(readOnly = true)
    private List<BigDecimal> generateSerieTrattenutiCampioneB(Campione campione) {
    	
    	List<Pesata> pesataList = pesataRepository.findByCampioneOrderByNumPesataAsc(campione);
    	
    	if(pesataList == null || pesataList.size() == 0) {
    		throw new CustomParameterizedException("pesataListEmptyError", "Non sono stati inseriti i dati di setacciatura! (campione " + campione.getTipoCampione() + ")");
    	}
    	
    	List<BigDecimal> serieTrattenuti = new ArrayList<BigDecimal>();
    	BigDecimal sumTratt = new BigDecimal(0);
    	BigDecimal sumFondo = new BigDecimal(0);
    	BigDecimal sumTrattB100 = new BigDecimal(0);
    	BigDecimal sumTrattB6V3 = new BigDecimal(0);
    	BigDecimal sumTrattB2 = new BigDecimal(0);
    	BigDecimal sumTrattB0V5 = new BigDecimal(0);
    	    	
    	for(Pesata p: pesataList) {
    		sumTrattB100 = sumTrattB100.add(p.getTrattB100Mm());
    		sumTrattB6V3 = sumTrattB6V3.add(p.getTrattB6V3Mm());
    		sumTrattB2 = sumTrattB2.add(p.getTrattB2Mm());
    		sumTrattB0V5 = sumTrattB0V5.add(p.getTrattB0V5Mm());
    		    		
    		// sommatoria trattenuti + fondo
    		sumTratt = sumTratt.add(p.getTrattB100Mm());
    		sumTratt = sumTratt.add(p.getTrattB6V3Mm());
    		sumTratt = sumTratt.add(p.getTrattB2Mm());
    		sumTratt = sumTratt.add(p.getTrattB0V5Mm());
    		sumTratt = sumTratt.add(p.getFondo());
    		log.debug("sumTratt parziale: {}", sumTratt.toString());
    		
    		sumFondo = sumFondo.add(p.getFondo());
    	}
    	log.debug("sumTratt: {}", sumTratt.toString());
    	
//    	if(sumTratt.compareTo(BigDecimal.ZERO) == 0) {
//    		throw new CustomParameterizedException("sumTrattError", "La sommatoria dei trattenuti è 0, controllare i dati inseriti!");
//    	}
    	
    	serieTrattenuti.add(sumTrattB100);
    	serieTrattenuti.add(sumTrattB6V3);
    	serieTrattenuti.add(sumTrattB2);
    	serieTrattenuti.add(sumTrattB0V5);
    	serieTrattenuti.add(sumFondo);
    	
    	return serieTrattenuti;
    }
    
    @Transactional(readOnly = true)
    private List<BigDecimal> generateSerieTrattenutiCb(Campione campione, Boolean binder) {
    	// generazione lista trattenuti PERC    	
    	
    	List<Pesata> pesataList = new ArrayList<Pesata>();
    	
    	if(binder)
    		pesataList = pesataRepository.findByCampioneAndBinderTrueOrderByNumPesataAsc(campione);
    	else
    		pesataList = pesataRepository.findByCampioneAndBinderFalseOrderByNumPesataAsc(campione);
    	
    	if(pesataList == null || pesataList.size() == 0) {
    		throw new CustomParameterizedException("pesataListEmptyError", "Non sono stati inseriti i dati di setacciatura! (campione " + campione.getTipoCampione() + ")");
    	}
    	
    	
    	// serie con le sommatorie delle masse trattenute
    	List<BigDecimal> serieMasse = new ArrayList<BigDecimal>();
    	
    	BigDecimal sumTratt63 = new BigDecimal(0);
    	BigDecimal sumTratt40 = new BigDecimal(0);
    	BigDecimal sumTratt31V5 = new BigDecimal(0);
    	BigDecimal sumTratt20 = new BigDecimal(0);
    	BigDecimal sumTratt16 = new BigDecimal(0);
    	BigDecimal sumTratt14 = new BigDecimal(0);
    	BigDecimal sumTratt12V5 = new BigDecimal(0);
    	BigDecimal sumTratt10 = new BigDecimal(0);
    	BigDecimal sumTratt8 = new BigDecimal(0);
    	BigDecimal sumTratt6V3 = new BigDecimal(0);
    	BigDecimal sumTratt4 = new BigDecimal(0);
    	BigDecimal sumTratt2 = new BigDecimal(0);
    	BigDecimal sumTratt1 = new BigDecimal(0);
    	BigDecimal sumTratt0V5 = new BigDecimal(0);
    	BigDecimal sumTratt0V25 = new BigDecimal(0);
    	BigDecimal sumTratt0V063 = new BigDecimal(0);
    	BigDecimal sumFondo = new BigDecimal(0);
    	
    	for(Pesata p: pesataList) {
    		sumTratt63 = sumTratt63.add(p.getTrattCb63Mm());
    		sumTratt40 = sumTratt40.add(p.getTrattCb40Mm());
    		sumTratt31V5 = sumTratt31V5.add(p.getTrattCb31V5Mm());
    		sumTratt20 = sumTratt20.add(p.getTrattCb20Mm());
    		sumTratt16 = sumTratt16.add(p.getTrattCb16Mm());
    		sumTratt14 = sumTratt14.add(p.getTrattCb14Mm());
    		sumTratt12V5 = sumTratt12V5.add(p.getTrattCb12V5Mm());
    		sumTratt10 = sumTratt10.add(p.getTrattCb10Mm());
    		sumTratt8 = sumTratt8.add(p.getTrattCb8Mm());
    		sumTratt6V3 = sumTratt6V3.add(p.getTrattCb6V3Mm());
    		sumTratt4 = sumTratt4.add(p.getTrattCb4Mm());
    		sumTratt2 = sumTratt2.add(p.getTrattCb2Mm());
    		sumTratt1 = sumTratt1.add(p.getTrattCb1Mm());
    		sumTratt0V5 = sumTratt0V5.add(p.getTrattCb0V5Mm());
    		sumTratt0V25 = sumTratt0V25.add(p.getTrattCb0V25Mm());
    		sumTratt0V063 = sumTratt0V063.add(p.getTrattCb0V063Mm());
    		sumFondo = sumFondo.add(p.getFondo());
    	}
    	
    	serieMasse.add(sumTratt63);
    	serieMasse.add(sumTratt40);
    	serieMasse.add(sumTratt31V5);
    	serieMasse.add(sumTratt20);
    	serieMasse.add(sumTratt16);
    	serieMasse.add(sumTratt14);
    	serieMasse.add(sumTratt12V5);
    	serieMasse.add(sumTratt10);
    	serieMasse.add(sumTratt8);
    	serieMasse.add(sumTratt6V3);
    	serieMasse.add(sumTratt4);
    	serieMasse.add(sumTratt2);
    	serieMasse.add(sumTratt1);
    	serieMasse.add(sumTratt0V5);
    	serieMasse.add(sumTratt0V25);
    	serieMasse.add(sumTratt0V063);
    	
    	
    	BigDecimal sumFondoConPass0063 = null;
    	
    	//mah???
    	if(binder) {
    		if(campione.getCbbLegMap1() == null)
    			throw new CustomParameterizedException("Compilare la parte riguardante la percentuale di legante del binder", "");
    		if(campione.getCbbPPostLav() == null)
    			throw new CustomParameterizedException("Compilare la parte riguardante il lavaggio a 0,063mm del binder", "");
    		if(campione.getCbbLegMap2() != null) {
    			sumFondoConPass0063 = sumFondo.add(campione.getCbbLegMap1().add(campione.getCbbLegMap2()).subtract(campione.getCbbPPostLav()));
    		} else {
    			sumFondoConPass0063 = sumFondo.add(campione.getCbbLegMap1()).subtract(campione.getCbbPPostLav());
    		}
    			
    		serieMasse.add(sumFondoConPass0063);
    	} else {
    		if(campione.getCbuLegMap1() == null)
    			throw new CustomParameterizedException("Compilare la parte riguardante la percentuale di legante dello strato di usura", "");
    		if(campione.getCbuPPostLav() == null)
    			throw new CustomParameterizedException("Compilare la parte riguardante il lavaggio a 0,063mm dello strato di usura", "");
    		sumFondoConPass0063 = sumFondo.add(campione.getCbuLegMap1().subtract(campione.getCbuPPostLav()));
    		serieMasse.add(sumFondoConPass0063);
    	}
    	
    	
    	// serie con i trattenuti PERC
    	List<BigDecimal> serieTrattenuti = new ArrayList<BigDecimal>();
    	
    	
    	BigDecimal sumTratt = new BigDecimal(0);
    	
    	for(BigDecimal b: serieMasse) {
    		// sommatoria trattenuti
    		sumTratt = sumTratt.add(b);
    		
    		log.debug("sumTratt parziale: {}", sumTratt.toString());
    		
    	}
    	log.debug("sumTratt: {}", sumTratt.toString());
    	
    	if(sumTratt.compareTo(BigDecimal.ZERO) == 0) {
    		throw new CustomParameterizedException("sumTrattError", "La sommatoria dei trattenuti è 0, controllare i dati inseriti!");
    	}
    	
    	serieTrattenuti.add(sumTratt63.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt40.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt31V5.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt20.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt16.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt14.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt12V5.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt10.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt8.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt6V3.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt4.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt2.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt1.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt0V5.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt0V25.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumTratt0V063.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	serieTrattenuti.add(sumFondoConPass0063.divide(sumTratt, 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100)));
    	
    	return serieTrattenuti;
    }
    
    @Transactional(readOnly = true)
    public List<SetaccioDTO> generateListaSetacci(Campione campione) {
    	
    	List<Pesata> pesataList = pesataRepository.findByCampioneOrderByNumPesataAsc(campione);
    	
    	if(pesataList == null || pesataList.size() == 0) {
    		return null;
    	}
    	
    	List<BigDecimal> serieSetacci = new ArrayList<BigDecimal>();
    	serieSetacci.add(new BigDecimal("125.000"));
    	serieSetacci.add(new BigDecimal("63.000"));
    	serieSetacci.add(new BigDecimal("31.500"));
    	serieSetacci.add(new BigDecimal("16.000"));
    	serieSetacci.add(new BigDecimal("8.000"));
    	serieSetacci.add(new BigDecimal("6.300"));
    	serieSetacci.add(new BigDecimal("4.000"));
    	serieSetacci.add(new BigDecimal("2.000"));
    	serieSetacci.add(new BigDecimal("1.000"));
    	serieSetacci.add(new BigDecimal("0.500"));
    	serieSetacci.add(new BigDecimal("0.250"));
    	serieSetacci.add(new BigDecimal("0.125"));
    	serieSetacci.add(new BigDecimal("0.075"));
    	serieSetacci.add(new BigDecimal("0.000"));
    	
    	List<BigDecimal> serieMasse = generateSerieMasse(campione);
    	List<BigDecimal> serieTrattenuti = generateSerieTrattenuti(campione);
    	List<BigDecimal> serieTrattenutiCumulati = new ArrayList<BigDecimal>();
    	List<BigDecimal> seriePassantiCumulati = new ArrayList<BigDecimal>();
    	
    	BigDecimal cumVal = new BigDecimal(0);
    	
    	for(BigDecimal t: serieTrattenuti) {
    		cumVal = cumVal.add(t);
    		//log.debug("Tratt: {} - TrattCum: {} - PassCum: {}", t, cumVal, new BigDecimal(100).subtract(cumVal));
    		serieTrattenutiCumulati.add(cumVal);
    		seriePassantiCumulati.add(new BigDecimal(100).subtract(cumVal));
    	}
    	
    	List<SetaccioDTO> listaSetacci = new ArrayList<SetaccioDTO>();
    	
    	int count = 0;
    	for(BigDecimal m: serieMasse) {
    		SetaccioDTO dto = new SetaccioDTO(
    				serieSetacci.get(count),
    				serieMasse.get(count),
    				serieTrattenuti.get(count),
    				serieTrattenutiCumulati.get(count),
    				seriePassantiCumulati.get(count)
    		);
    		listaSetacci.add(dto);
    		count++;
    	}
    	
    	return listaSetacci;
    }
    
    @Transactional(readOnly = true)
    public List<SetaccioDTO> generateListaSetacciCb(Campione campione, Boolean binder) {
    	
    	List<Pesata> pesataList = new ArrayList<Pesata>();
    	if(binder)
    		pesataList = pesataRepository.findByCampioneAndBinderTrueOrderByNumPesataAsc(campione);
    	else
    		pesataList = pesataRepository.findByCampioneAndBinderFalseOrderByNumPesataAsc(campione);
    	
    	if(pesataList == null || pesataList.size() == 0) {
    		return null;
    	}
    	
    	List<BigDecimal> serieSetacci = new ArrayList<BigDecimal>();
    	serieSetacci.add(new BigDecimal("63.000"));
    	serieSetacci.add(new BigDecimal("40.000"));
    	serieSetacci.add(new BigDecimal("31.500"));
    	serieSetacci.add(new BigDecimal("20.000"));
    	serieSetacci.add(new BigDecimal("16.000"));
    	serieSetacci.add(new BigDecimal("14.000"));
    	serieSetacci.add(new BigDecimal("12.500"));
    	serieSetacci.add(new BigDecimal("10.000"));
    	serieSetacci.add(new BigDecimal("8.000"));
    	serieSetacci.add(new BigDecimal("6.300"));
    	serieSetacci.add(new BigDecimal("4.000"));
    	serieSetacci.add(new BigDecimal("2.000"));
    	serieSetacci.add(new BigDecimal("1.000"));
    	serieSetacci.add(new BigDecimal("0.500"));
    	serieSetacci.add(new BigDecimal("0.250"));
    	serieSetacci.add(new BigDecimal("0.063"));
    	serieSetacci.add(new BigDecimal("0.000"));
    	
    	// serie con le sommatorie delle masse trattenute
    	List<BigDecimal> serieMasse = new ArrayList<BigDecimal>();
    	
    	BigDecimal sumTratt63 = new BigDecimal(0);
    	BigDecimal sumTratt40 = new BigDecimal(0);
    	BigDecimal sumTratt31V5 = new BigDecimal(0);
    	BigDecimal sumTratt20 = new BigDecimal(0);
    	BigDecimal sumTratt16 = new BigDecimal(0);
    	BigDecimal sumTratt14 = new BigDecimal(0);
    	BigDecimal sumTratt12V5 = new BigDecimal(0);
    	BigDecimal sumTratt10 = new BigDecimal(0);
    	BigDecimal sumTratt8 = new BigDecimal(0);
    	BigDecimal sumTratt6V3 = new BigDecimal(0);
    	BigDecimal sumTratt4 = new BigDecimal(0);
    	BigDecimal sumTratt2 = new BigDecimal(0);
    	BigDecimal sumTratt1 = new BigDecimal(0);
    	BigDecimal sumTratt0V5 = new BigDecimal(0);
    	BigDecimal sumTratt0V25 = new BigDecimal(0);
    	BigDecimal sumTratt0V063 = new BigDecimal(0);
    	BigDecimal sumFondo = new BigDecimal(0);
    	
    	for(Pesata p: pesataList) {
    		sumTratt63 = sumTratt63.add(p.getTrattCb63Mm());
    		sumTratt40 = sumTratt40.add(p.getTrattCb40Mm());
    		sumTratt31V5 = sumTratt31V5.add(p.getTrattCb31V5Mm());
    		sumTratt20 = sumTratt20.add(p.getTrattCb20Mm());
    		sumTratt16 = sumTratt16.add(p.getTrattCb16Mm());
    		sumTratt14 = sumTratt14.add(p.getTrattCb14Mm());
    		sumTratt12V5 = sumTratt12V5.add(p.getTrattCb12V5Mm());
    		sumTratt10 = sumTratt10.add(p.getTrattCb10Mm());
    		sumTratt8 = sumTratt8.add(p.getTrattCb8Mm());
    		sumTratt6V3 = sumTratt6V3.add(p.getTrattCb6V3Mm());
    		sumTratt4 = sumTratt4.add(p.getTrattCb4Mm());
    		sumTratt2 = sumTratt2.add(p.getTrattCb2Mm());
    		sumTratt1 = sumTratt1.add(p.getTrattCb1Mm());
    		sumTratt0V5 = sumTratt0V5.add(p.getTrattCb0V5Mm());
    		sumTratt0V25 = sumTratt0V25.add(p.getTrattCb0V25Mm());
    		sumTratt0V063 = sumTratt0V063.add(p.getTrattCb0V063Mm());
    		sumFondo = sumFondo.add(p.getFondo());
    	}
    	
    	serieMasse.add(sumTratt63);
    	serieMasse.add(sumTratt40);
    	serieMasse.add(sumTratt31V5);
    	serieMasse.add(sumTratt20);
    	serieMasse.add(sumTratt16);
    	serieMasse.add(sumTratt14);
    	serieMasse.add(sumTratt12V5);
    	serieMasse.add(sumTratt10);
    	serieMasse.add(sumTratt8);
    	serieMasse.add(sumTratt6V3);
    	serieMasse.add(sumTratt4);
    	serieMasse.add(sumTratt2);
    	serieMasse.add(sumTratt1);
    	serieMasse.add(sumTratt0V5);
    	serieMasse.add(sumTratt0V25);
    	serieMasse.add(sumTratt0V063);
    	// il problema è che a sumFondo va aggiunto il passante a 0.063, sia qui che per i trattenuti
    	//serieMasse.add(new BigDecimal("20"));
    	
    	//mah???
//    	if(binder)
//    		serieMasse.add(sumFondo.add(campione.getCbbLegMap1().add(campione.getCbbLegMap2()).subtract(campione.getCbbPPostLav())));
//    	else
//    		serieMasse.add(sumFondo.add(campione.getCbuLegMap1().subtract(campione.getCbuPPostLav())));
    	
    	BigDecimal sumFondoConPass0063 = null;
    	if(binder) {
    		if(campione.getCbbLegMap1() == null)
    			throw new CustomParameterizedException("Compilare la parte riguardante la percentuale di legante del binder", "");
    		if(campione.getCbbPPostLav() == null)
    			throw new CustomParameterizedException("Compilare la parte riguardante il lavaggio a 0,063mm del binder", "");
    		if(campione.getCbbLegMap2() != null) {
    			sumFondoConPass0063 = sumFondo.add(campione.getCbbLegMap1().add(campione.getCbbLegMap2()).subtract(campione.getCbbPPostLav()));
    		} else {
    			sumFondoConPass0063 = sumFondo.add(campione.getCbbLegMap1()).subtract(campione.getCbbPPostLav());
    		}
    			
    		serieMasse.add(sumFondoConPass0063);
    	} else {
    		if(campione.getCbuLegMap1() == null)
    			throw new CustomParameterizedException("Compilare la parte riguardante la percentuale di legante dello strato di usura", "");
    		if(campione.getCbuPPostLav() == null)
    			throw new CustomParameterizedException("Compilare la parte riguardante il lavaggio a 0,063mm dello strato di usura", "");
    		sumFondoConPass0063 = sumFondo.add(campione.getCbuLegMap1().subtract(campione.getCbuPPostLav()));
    		serieMasse.add(sumFondoConPass0063);
    	}
    	
    	
    	
    	List<BigDecimal> serieTrattenuti = generateSerieTrattenutiCb(campione, binder);
    	List<BigDecimal> serieTrattenutiCumulati = new ArrayList<BigDecimal>();
    	List<BigDecimal> seriePassantiCumulati = new ArrayList<BigDecimal>();
    	
    	BigDecimal cumVal = new BigDecimal(0);
    	
    	for(BigDecimal t: serieTrattenuti) {
    		cumVal = cumVal.add(t);
    		//log.debug("Tratt: {} - TrattCum: {} - PassCum: {}", t, cumVal, new BigDecimal(100).subtract(cumVal));
    		serieTrattenutiCumulati.add(cumVal);
    		seriePassantiCumulati.add(new BigDecimal(100).subtract(cumVal));
    	}
    	
    	List<SetaccioDTO> listaSetacci = new ArrayList<SetaccioDTO>();
    	
    	int count = 0;
    	for(BigDecimal m: serieMasse) {
    		SetaccioDTO dto = new SetaccioDTO(
    				serieSetacci.get(count),
    				serieMasse.get(count),
    				serieTrattenuti.get(count),
    				serieTrattenutiCumulati.get(count),
    				seriePassantiCumulati.get(count)
    		);
    		listaSetacci.add(dto);
    		count++;
    	}
    	
    	return listaSetacci;
    }
    
    @Transactional(readOnly = true)
    public Boolean checkConformAA1(Campione campione) {
    	
    	List<Pesata> pesataList = pesataService.findByCampione(campione.getId());
    	
    	if(pesataList == null || pesataList.size() == 0) {
    		return null;
    	}
    	
    	List<BigDecimal> serieTrattenuti = generateSerieTrattenuti(campione);
    	List<BigDecimal> seriePassantiCumulati = new ArrayList<BigDecimal>();
    	
    	BigDecimal cumVal = new BigDecimal(0);
    	
    	for(BigDecimal t: serieTrattenuti) {
    		cumVal = cumVal.add(t);
    		//log.debug("Tratt: {} - TrattCum: {} - PassCum: {}", t, cumVal, new BigDecimal(100).subtract(cumVal));
    		seriePassantiCumulati.add(new BigDecimal(100).subtract(cumVal));
    	}
    	
    	Boolean check6mm = seriePassantiCumulati.get(5).compareTo(new BigDecimal("95")) == 1;
    	Boolean check2mm = seriePassantiCumulati.get(7).compareTo(new BigDecimal("70")) == 1;
    	Boolean check0075mm = seriePassantiCumulati.get(12).compareTo(new BigDecimal("5")) == -1;
//    	log.debug("############# check6mm: {} {}", seriePassantiCumulati.get(5), check6mm);
//    	log.debug("############# check2mm {} {}", seriePassantiCumulati.get(7), check2mm);
//    	log.debug("############# check0075mm {} {}", seriePassantiCumulati.get(12), check0075mm);
    	
    	return check6mm && check2mm && check0075mm;
    }
    
    
    @Transactional(readOnly = true)
    public BigDecimal checkPassanteB(Campione campione) {
    	
    	List<Pesata> pesataList = pesataService.findByCampione(campione.getId());
    	
    	if(pesataList == null || pesataList.size() == 0) {
    		return null;
    	}
    	
    	List<BigDecimal> serieTrattenuti = generateSerieTrattenutiCampioneB(campione);
    	
    	BigDecimal sommatoria = new BigDecimal(0);
    	for(BigDecimal b : serieTrattenuti) {
    		//log.debug("################### {}", b);
    		sommatoria = sommatoria.add(b);
    	}
    	
    	if(sommatoria.compareTo(BigDecimal.ZERO) == 0)
    		return new BigDecimal("100");
    		
    	BigDecimal trattenuto100perc = serieTrattenuti.get(0).multiply(new BigDecimal("100")).divide(sommatoria, 2, BigDecimal.ROUND_HALF_EVEN);
    	BigDecimal passante100perc = new BigDecimal("100").subtract(trattenuto100perc);
    	
    	return passante100perc;
    	
    }
    
    @Transactional(readOnly = true)
    public ClassificazioneGeotecnicaDTO generateClassificazioneGeotecnica(Campione campione) {
    	
    	// per B va fatta una classificazione a parte
    	//log.debug(campione.getTipoCampione().toString());
    	if(campione == null || campione.getTipoCampione().equals(TipoCampione.C) || campione.getTipoCampione().equals(TipoCampione.CB_A)
    			 || campione.getTipoCampione().equals(TipoCampione.CB_B)) {
    		throw new CustomParameterizedException("classificazioneGeotecnicaError", "Campione non corretto");
    	}
    	
    	if(campione.getTipoCampione().equals(TipoCampione.A) || campione.getTipoCampione().equals(TipoCampione.A1)) {
	    	List<BigDecimal> serieTrattenuti = generateSerieTrattenuti(campione);
	    	
	    	List<BigDecimal> serieTrattenutiCumulati = new ArrayList<BigDecimal>();
	    	List<BigDecimal> seriePassantiCumulati = new ArrayList<BigDecimal>();
	    	BigDecimal cumVal = new BigDecimal(0);
	    	
	    	for(BigDecimal t: serieTrattenuti) {
	    		cumVal = cumVal.add(t);
	    		log.debug("Tratt: {} - TrattCum: {} - PassCum: {}", t, cumVal, new BigDecimal(100).subtract(cumVal));
	    		serieTrattenutiCumulati.add(cumVal);
	    		seriePassantiCumulati.add(new BigDecimal(100).subtract(cumVal));
	    	}
	    	
	    	BigDecimal ghiaiaPerc = serieTrattenutiCumulati.get(5);
	    	BigDecimal sabbiaGrossaPerc = seriePassantiCumulati.get(5).subtract(seriePassantiCumulati.get(7));
	    	BigDecimal sabbiaPerc = seriePassantiCumulati.get(7).subtract(seriePassantiCumulati.get(12));
	    	BigDecimal limoArgillaPerc = seriePassantiCumulati.get(12);
	    	
	    	ClassificazioneGeotecnicaDTO dto = new ClassificazioneGeotecnicaDTO(ghiaiaPerc, sabbiaGrossaPerc, sabbiaPerc, limoArgillaPerc);
	    	
	    	HashMap<String, BigDecimal> percMap = new HashMap<String, BigDecimal>();
			percMap.put("Ghiaia", ghiaiaPerc);
			//percMap.put("Sabbia grossa", sabbiaGrossaPerc);
			//percMap.put("Sabbia", sabbiaPerc);
			percMap.put("Sabbia", sabbiaGrossaPerc.add(sabbiaPerc)); // sabbia = sabbia grossa + sabbia
			percMap.put("Limo e Argilla", limoArgillaPerc);
			
	        List<Map.Entry<String, BigDecimal>> list =
	                new LinkedList<Map.Entry<String, BigDecimal>>(percMap.entrySet());

	        Collections.sort(list, new Comparator<Map.Entry<String, BigDecimal>>() {
	            public int compare(Map.Entry<String, BigDecimal> o1,
	                               Map.Entry<String, BigDecimal> o2) {
	                return (o2.getValue()).compareTo(o1.getValue());
	            }
	        });

	        LinkedHashMap<String, BigDecimal> sortedPercMap = new LinkedHashMap<String, BigDecimal>();
	        for (Map.Entry<String, BigDecimal> entry : list) {
	        	sortedPercMap.put(entry.getKey(), entry.getValue());
	        }
			
	        // primo classificato
			StringBuffer sb = new StringBuffer(sortedPercMap.keySet().toArray()[0].toString());
			
			// secondo classificato
			BigDecimal secondo = (BigDecimal) sortedPercMap.values().toArray()[1];
			
			// terzo classificato
			BigDecimal terzo = (BigDecimal) sortedPercMap.values().toArray()[2];
			
			// quarto classificato
			//BigDecimal quarto = (BigDecimal) sortedPercMap.values().toArray()[3];
			
			// parte " con " (valore < 50 e >= 25)
			if(secondo.compareTo(new BigDecimal(50)) < 0 && secondo.compareTo(new BigDecimal(25)) >= 0) {
				sb.append(" con " + sortedPercMap.keySet().toArray()[1]);
			}
			if(terzo.compareTo(new BigDecimal(50)) < 0 && terzo.compareTo(new BigDecimal(25)) >= 0) {
				sb.append(" con " + sortedPercMap.keySet().toArray()[2]);
			}
			/*if(quarto.compareTo(new BigDecimal(50)) < 0 && quarto.compareTo(new BigDecimal(25)) >= 0) {
				sb.append(" con " + sortedPercMap.keySet().toArray()[3]);
			}*/
			
			// parte "oso" (valore < 25 e >= 10)
			if(secondo.compareTo(new BigDecimal(25)) < 0 && secondo.compareTo(new BigDecimal(10)) >= 0) {
				if(sortedPercMap.keySet().toArray()[1].equals("Limo e Argilla")) {
					sb.append(" Limosa e Argillosa");
				}
				if(sortedPercMap.keySet().toArray()[1].equals("Sabbia")) {
					sb.append(" Sabbiosa");
				}
				if(sortedPercMap.keySet().toArray()[1].equals("Ghiaia")) {
					sb.append(" Ghiaiosa");
				}
//				if(sortedPercMap.keySet().toArray()[1].equals("Sabbia grossa")) {
//					// fix per "sabbia sabbiosa"
//					if(sortedPercMap.keySet().toArray()[0].equals("Sabbia")) {
//						sb.append(" e Sabbia grossa");
//					} else {
//						sb.append(" Sabbiosa (Sabbia grossa)");
//					}
//					sb.append(" Sabbiosa (Sabbia grossa)");
//				}
				
			}
			if(terzo.compareTo(new BigDecimal(25)) < 0 && terzo.compareTo(new BigDecimal(10)) >= 0) {
				if(sortedPercMap.keySet().toArray()[2].equals("Limo e Argilla")) {
					sb.append(" Limosa e Argillosa");
				}
				if(sortedPercMap.keySet().toArray()[2].equals("Sabbia")) {
					sb.append(" Sabbiosa");
				}
				if(sortedPercMap.keySet().toArray()[2].equals("Ghiaia")) {
					sb.append(" Ghiaiosa");
				}
//				if(sortedPercMap.keySet().toArray()[2].equals("Sabbia grossa")) {
//					sb.append(" Sabbiosa (Sabbia grossa)");
//				}
			}
//			if(quarto.compareTo(new BigDecimal(25)) < 0 && quarto.compareTo(new BigDecimal(10)) >= 0) {
//				if(sortedPercMap.keySet().toArray()[3].equals("Limo e Argilla")) {
//					sb.append(" Limosa e Argillosa");
//				}
//				if(sortedPercMap.keySet().toArray()[3].equals("Sabbia")) {
//					sb.append(" Sabbiosa");
//				}
//				if(sortedPercMap.keySet().toArray()[3].equals("Ghiaia")) {
//					sb.append(" Ghiaiosa");
//				}
//				if(sortedPercMap.keySet().toArray()[3].equals("Sabbia grossa")) {
//					sb.append(" Sabbiosa (Sabbia grossa)");
//				}
//			}
			
			// parte " debolmente" (valore < 10 e >= 5)
			if(secondo.compareTo(new BigDecimal(10)) < 0 && secondo.compareTo(new BigDecimal(5)) >= 0) {
				if(sortedPercMap.keySet().toArray()[1].equals("Limo e Argilla")) {
					sb.append(" debolmente Limosa e Argillosa");
				}
				if(sortedPercMap.keySet().toArray()[1].equals("Sabbia")) {
					sb.append(" debolmente Sabbiosa");
				}
				if(sortedPercMap.keySet().toArray()[1].equals("Ghiaia")) {
					sb.append(" debolmente Ghiaiosa");
				}
//				if(sortedPercMap.keySet().toArray()[1].equals("Sabbia grossa")) {
//					sb.append(" Sabbiosa (Sabbia grossa)");
//				}
				
			}
			if(terzo.compareTo(new BigDecimal(10)) < 0 && terzo.compareTo(new BigDecimal(5)) >= 0) {
				if(sortedPercMap.keySet().toArray()[2].equals("Limo e Argilla")) {
					sb.append(" debolmente Limosa e Argillosa");
				}
				if(sortedPercMap.keySet().toArray()[2].equals("Sabbia")) {
					sb.append(" debolmente Sabbiosa");
				}
				if(sortedPercMap.keySet().toArray()[2].equals("Ghiaia")) {
					sb.append(" debolmente Ghiaiosa");
				}
//				if(sortedPercMap.keySet().toArray()[2].equals("Sabbia grossa")) {
//					sb.append(" Sabbiosa (Sabbia grossa)");
//				}
			}
//			if(quarto.compareTo(new BigDecimal(10)) < 0 && quarto.compareTo(new BigDecimal(5)) >= 0) {
//				if(sortedPercMap.keySet().toArray()[3].equals("Limo e Argilla")) {
//					sb.append(" debolmente Limosa e Argillosa");
//				}
//				if(sortedPercMap.keySet().toArray()[3].equals("Sabbia")) {
//					sb.append(" debolmente Sabbiosa");
//				}
//				if(sortedPercMap.keySet().toArray()[3].equals("Ghiaia")) {
//					sb.append(" debolmente Ghiaiosa");
//				}
//				if(sortedPercMap.keySet().toArray()[3].equals("Sabbia grossa")) {
//					sb.append(" Sabbiosa (Sabbia grossa)");
//				}
//			}
			
			dto.setClassificazioneGeotecnica(sb.toString());
			
			log.debug("fantastica classificazione geotecnica: {}", sb.toString());
			
//			for(BigDecimal perc : sortedPercMap.values()) {
//				log.debug(perc.toString());
//			}
	    	
	    	return dto;
    	}
    	
    	if(campione.getTipoCampione().equals(TipoCampione.B)) {
    		    		
    		List<BigDecimal> serieTrattenuti = generateSerieTrattenutiCampioneB(campione);
    		
    		// setacciatura facoltativa a parte quella a 100
    		// controllo se ci sono trattenuti nelle setacciature < 100

    		BigDecimal sumTrattMin100 = serieTrattenuti.get(1).add(serieTrattenuti.get(2)).add(serieTrattenuti.get(3));
    		
    		// se c'è solo il trattenuto a 100 ritorno null (la class geotec verrà presa da descrizione campione)
    		if(sumTrattMin100.compareTo(BigDecimal.ZERO) == 0) {
    			return null;
    		}
    		
    		//log.debug("add {}", serieTrattenuti.get(1).add(serieTrattenuti.get(2)).toString());
    		//log.debug("dividendo {}",serieTrattenuti.get(1).add(serieTrattenuti.get(2)).add(serieTrattenuti.get(3)).add(serieTrattenuti.get(4)));
    		
//    		serieTrattenuti.add(sumTrattB100);
//        	serieTrattenuti.add(sumTrattB6V3);
//        	serieTrattenuti.add(sumTrattB2);
//        	serieTrattenuti.add(sumTrattB0V5);
//        	serieTrattenuti.add(sumFondo);
    		
    		BigDecimal ghiaiaPerc = serieTrattenuti.get(1).add(serieTrattenuti.get(0)).multiply(new BigDecimal(100))
    				.divide(serieTrattenuti.get(0).add(serieTrattenuti.get(1)).add(serieTrattenuti.get(2)).add(serieTrattenuti.get(3)).add(serieTrattenuti.get(4)), 8, BigDecimal.ROUND_HALF_EVEN);
	    	BigDecimal sabbiaPerc = serieTrattenuti.get(2).add(serieTrattenuti.get(3)).multiply(new BigDecimal(100))
    				.divide(serieTrattenuti.get(0).add(serieTrattenuti.get(1)).add(serieTrattenuti.get(2)).add(serieTrattenuti.get(3)).add(serieTrattenuti.get(4)), 8, BigDecimal.ROUND_HALF_EVEN);
	    	BigDecimal limoArgillaPerc = serieTrattenuti.get(4).multiply(new BigDecimal(100))
    				.divide(serieTrattenuti.get(0).add(serieTrattenuti.get(1)).add(serieTrattenuti.get(2)).add(serieTrattenuti.get(3)).add(serieTrattenuti.get(4)), 8, BigDecimal.ROUND_HALF_EVEN);
	    	
	    	ClassificazioneGeotecnicaDTO dto = new ClassificazioneGeotecnicaDTO(ghiaiaPerc, sabbiaPerc, limoArgillaPerc);
	    	
	    	HashMap<String, BigDecimal> percMap = new HashMap<String, BigDecimal>();
			percMap.put("Ghiaia", ghiaiaPerc);
			percMap.put("Sabbia", sabbiaPerc);
			percMap.put("Limo e Argilla", limoArgillaPerc);
			
	        List<Map.Entry<String, BigDecimal>> list =
	                new LinkedList<Map.Entry<String, BigDecimal>>(percMap.entrySet());

	        Collections.sort(list, new Comparator<Map.Entry<String, BigDecimal>>() {
	            public int compare(Map.Entry<String, BigDecimal> o1,
	                               Map.Entry<String, BigDecimal> o2) {
	                return (o2.getValue()).compareTo(o1.getValue());
	            }
	        });

	        LinkedHashMap<String, BigDecimal> sortedPercMap = new LinkedHashMap<String, BigDecimal>();
	        for (Map.Entry<String, BigDecimal> entry : list) {
	        	sortedPercMap.put(entry.getKey(), entry.getValue());
	        }
			
	        // primo classificato
			StringBuffer sb = new StringBuffer(sortedPercMap.keySet().toArray()[0].toString());
			
			// secondo classificato
			BigDecimal secondo = (BigDecimal) sortedPercMap.values().toArray()[1];
			
			// terzo classificato
			BigDecimal terzo = (BigDecimal) sortedPercMap.values().toArray()[2];
			
			// parte " con " (valore < 50 e >= 25)
			if(secondo.compareTo(new BigDecimal(50)) < 0 && secondo.compareTo(new BigDecimal(25)) >= 0) {
				sb.append(" con " + sortedPercMap.keySet().toArray()[1]);
			}
			if(terzo.compareTo(new BigDecimal(50)) < 0 && terzo.compareTo(new BigDecimal(25)) >= 0) {
				sb.append(" con " + sortedPercMap.keySet().toArray()[2]);
			}
			
			// parte "oso" (valore < 25 e >= 10)
			if(secondo.compareTo(new BigDecimal(25)) < 0 && secondo.compareTo(new BigDecimal(10)) >= 0) {
				if(sortedPercMap.keySet().toArray()[1].equals("Sabbia")) {
					sb.append(" Sabbiosa ");
				}
				if(sortedPercMap.keySet().toArray()[1].equals("Limo e Argilla")) {
					sb.append(" Limosa e Argillosa ");
				}
				if(sortedPercMap.keySet().toArray()[1].equals("Ghiaia")) {
					sb.append(" Ghiaiosa ");
				}
				
			}
			if(terzo.compareTo(new BigDecimal(25)) < 0 && terzo.compareTo(new BigDecimal(10)) >= 0) {
				if(sortedPercMap.keySet().toArray()[2].equals("Sabbia")) {
					sb.append(" Sabbiosa ");
				}
				if(sortedPercMap.keySet().toArray()[2].equals("Limo e Argilla")) {
					sb.append(" Limosa e Argillosa ");
				}
				if(sortedPercMap.keySet().toArray()[2].equals("Ghiaia")) {
					sb.append(" Ghiaiosa ");
				}
			}
			
			// parte " debolmente" (valore < 10 e >= 5)
			if(secondo.compareTo(new BigDecimal(10)) < 0 && secondo.compareTo(new BigDecimal(5)) >= 0) {
				if(sortedPercMap.keySet().toArray()[1].equals("Sabbia")) {
					sb.append(" debolmente Sabbiosa ");
				}
				if(sortedPercMap.keySet().toArray()[1].equals("Limo e Argilla")) {
					sb.append(" debolmente Limosa e Argillosa");
				}
				if(sortedPercMap.keySet().toArray()[1].equals("Ghiaia")) {
					sb.append(" debolmente Ghiaiosa");
				}
				
			}
			if(terzo.compareTo(new BigDecimal(10)) < 0 && terzo.compareTo(new BigDecimal(5)) >= 0) {
				if(sortedPercMap.keySet().toArray()[2].equals("Sabbia")) {
					sb.append(" debolmente Sabbiosa ");
				}
				if(sortedPercMap.keySet().toArray()[2].equals("Limo e Argilla")) {
					sb.append(" debolmente Limosa e Argillosa");
				}
				if(sortedPercMap.keySet().toArray()[2].equals("Ghiaia")) {
					sb.append(" debolmente Ghiaiosa");
				}
			}
			
			dto.setClassificazioneGeotecnica(sb.toString());
			
			log.debug("fantastica classificazione geotecnica: {}", sb.toString());
			
//			for(BigDecimal perc : sortedPercMap.values()) {
//				log.debug(perc.toString());
//			}
	    	
	    	return dto;
    	}
    	
    	return null;
    	
    }
    
    @Transactional(readOnly = true)
    public CbPercGranDTO generateCbUsuraPercGran(Campione campione) {
    	
    	List<BigDecimal> serieTrattenuti = generateSerieTrattenutiCb(campione, false);
    	
    	List<BigDecimal> serieTrattenutiCumulati = new ArrayList<BigDecimal>();
    	List<BigDecimal> seriePassantiCumulati = new ArrayList<BigDecimal>();
    	BigDecimal cumVal = new BigDecimal(0);
    	
    	for(BigDecimal t: serieTrattenuti) {
    		cumVal = cumVal.add(t);
    		//log.debug("Tratt: {} - TrattCum: {} - PassCum: {}", t, cumVal, new BigDecimal(100).subtract(cumVal));
    		serieTrattenutiCumulati.add(cumVal);
    		seriePassantiCumulati.add(new BigDecimal(100).subtract(cumVal));
    	}
    	
    	BigDecimal ghiaiaPerc = serieTrattenutiCumulati.get(11);
    	BigDecimal sabbiaPerc = seriePassantiCumulati.get(11).subtract(seriePassantiCumulati.get(15));
    	BigDecimal limoArgillaPerc = seriePassantiCumulati.get(15);
    	
    	CbPercGranDTO dto = new CbPercGranDTO(ghiaiaPerc, sabbiaPerc, limoArgillaPerc);
    	
    	return dto;
    }
    
    @Transactional(readOnly = true)
    public CbPercGranDTO generateCbBinderPercGran(Campione campione) {
    	
    	List<BigDecimal> serieTrattenuti = generateSerieTrattenutiCb(campione, true);
    	
    	List<BigDecimal> serieTrattenutiCumulati = new ArrayList<BigDecimal>();
    	List<BigDecimal> seriePassantiCumulati = new ArrayList<BigDecimal>();
    	BigDecimal cumVal = new BigDecimal(0);
    	
    	for(BigDecimal t: serieTrattenuti) {
    		cumVal = cumVal.add(t);
    		//log.debug("Tratt: {} - TrattCum: {} - PassCum: {}", t, cumVal, new BigDecimal(100).subtract(cumVal));
    		serieTrattenutiCumulati.add(cumVal);
    		seriePassantiCumulati.add(new BigDecimal(100).subtract(cumVal));
    	}
    	
    	BigDecimal ghiaiaPerc = serieTrattenutiCumulati.get(11);
    	BigDecimal sabbiaPerc = seriePassantiCumulati.get(11).subtract(seriePassantiCumulati.get(15));
    	BigDecimal limoArgillaPerc = seriePassantiCumulati.get(15);
    	
    	CbPercGranDTO dto = new CbPercGranDTO(ghiaiaPerc, sabbiaPerc, limoArgillaPerc);
    	
    	return dto;
    }
    
    @Transactional(readOnly = true)
    public byte[] generateCurvaGranulometrica(Campione campione) {
    	
    	List<BigDecimal> serieTrattenuti = generateSerieTrattenuti(campione);
    	
    	List<BigDecimal> serieTrattenutiCumulati = new ArrayList<BigDecimal>();
    	List<BigDecimal> seriePassantiCumulati = new ArrayList<BigDecimal>();
    	BigDecimal cumVal = new BigDecimal(0);
    	
    	for(BigDecimal t: serieTrattenuti) {
    		cumVal = cumVal.add(t);
    		log.debug("Tratt: {} - TrattCum: {} - PassCum: {}", t, cumVal, new BigDecimal(100).subtract(cumVal));
    		serieTrattenutiCumulati.add(cumVal);
    		seriePassantiCumulati.add(new BigDecimal(100).subtract(cumVal));
    	}
    	
    	final XYSeries s1 = new XYSeries("Curva granulometrica");
        s1.add(125, seriePassantiCumulati.get(0));
        s1.add(63, seriePassantiCumulati.get(1));
        s1.add(31.5, seriePassantiCumulati.get(2));
        s1.add(16, seriePassantiCumulati.get(3));
        s1.add(8, seriePassantiCumulati.get(4));
        s1.add(6.3, seriePassantiCumulati.get(5));
        s1.add(4, seriePassantiCumulati.get(6));
        s1.add(2, seriePassantiCumulati.get(7));
        s1.add(1, seriePassantiCumulati.get(8));
        s1.add(0.5, seriePassantiCumulati.get(9));
        s1.add(0.25, seriePassantiCumulati.get(10));
        s1.add(0.125, seriePassantiCumulati.get(11));
        s1.add(0.075, seriePassantiCumulati.get(12));
        
        final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(s1);
        
//        final JFreeChart chart = ChartFactory.createXYLineChart(
//            "Curva granulometrica",          // chart title
//            "Diametro (mm)",               // domain axis label
//            "Passante (%)",                  // range axis label
//            dataset,                  // data
//            PlotOrientation.VERTICAL,
//            true,                     // include legend
//            true,
//            false
//        );
        
        //final XYPlot plot = chart.getXYPlot();
        
        final LogarithmicAxis domainAxis = new LogarithmicAxis("Diametro (mm)");
        domainAxis.setLabelFont(new Font("sans", Font.BOLD, 12));
        domainAxis.setRange(0.0001, 1000);
        //domainAxis.setAutoRange(true);
        //domainAxis.setAutoRangeIncludesZero(true);
        //domainAxis.setAllowNegativesFlag(true);
        //domainAxis.setTickUnit(new NumberTickUnit(1000));
        final NumberAxis rangeAxis = new NumberAxis("Passante (%)");
        rangeAxis.setLabelFont(new Font("sans", Font.BOLD, 12));
        rangeAxis.setRange(0, 100);
        rangeAxis.setTickUnit(new NumberTickUnit(10));
        
        //domainAxis.setUpperBound(100);
        //domainAxis.setLowerBound(0);
        //domainAxis.setMinorTickMarksVisible(false);
        //domainAxis.setNumberFormatOverride(new DecimalFormat("0000.000"));
        
        final XYItemRenderer renderer = new XYLineAndShapeRenderer(true, true);
        final XYPlot plot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        
        // eventualmente per fare linee e punti di diversi colori:
        /*
            
        // Create a single plot containing both the scatter and line
	    XYPlot plot = new XYPlot();
	
	
	    // Create the scatter data, renderer, and axis
	    XYDataset collection1 = getScatterPlotData();
	    XYItemRenderer renderer1 = new XYLineAndShapeRenderer(false, true);   // Shapes only
	    ValueAxis domain1 = new NumberAxis("Domain1");
	    ValueAxis range1 = new NumberAxis("Range1");
	
	    // Set the scatter data, renderer, and axis into plot
	    plot.setDataset(0, collection1);
	    plot.setRenderer(0, renderer1);
	    plot.setDomainAxis(0, domain1);
	    plot.setRangeAxis(0, range1);
	
	    // Map the scatter to the first Domain and first Range
	    plot.mapDatasetToDomainAxis(0, 0);
	    plot.mapDatasetToRangeAxis(0, 0);
	
	
	    // Create the line data, renderer, and axis
	    XYDataset collection2 = getLinePlotData();
	    XYItemRenderer renderer2 = new XYLineAndShapeRenderer(true, false);   // Lines only
	    ValueAxis domain2 = new NumberAxis("Domain2");
	    ValueAxis range2 = new NumberAxis("Range2");
	
	    // Set the line data, renderer, and axis into plot
	    plot.setDataset(1, collection2);
	    plot.setRenderer(1, renderer2);
	    plot.setDomainAxis(1, domain2);
	    plot.setRangeAxis(1, range2);
	
	    // Map the line to the second Domain and second Range
	    plot.mapDatasetToDomainAxis(1, 1);
	    plot.mapDatasetToRangeAxis(1, 1);
	
	    // Create the chart with the plot and a legend
	    JFreeChart chart = new JFreeChart("Multi Dataset Chart", JFreeChart.DEFAULT_TITLE_FONT, plot, true); 
         
        */
        
        //final Marker argilla = new ValueMarker(0.002);
        final Marker argilla = new ValueMarker(0.001);
        //start.setPaint(Color.green);
        //argilla.setStroke(new BasicStroke(0.0f));
        argilla.setLabelFont(new Font("sans", Font.BOLD, 10));
        //argilla.setLabel("argilla (0.002 mm)  ");
        argilla.setLabel("argilla  ");
        argilla.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        argilla.setLabelOffset(new RectangleInsets(5, 15, 0, 0));
        argilla.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
        argilla.setPaint(new Color(0, 0, 0, 0));
        plot.addDomainMarker(argilla);
        
//        final Marker argillaArrow = new ValueMarker(0.002);
//        argillaArrow.setStroke(new BasicStroke(0.0f));
//        argillaArrow.setLabelFont(new Font("sans", Font.BOLD, 14));
//        argillaArrow.setLabel("\u2193");
//        argillaArrow.setLabelAnchor(RectangleAnchor.TOP);
//        argillaArrow.setLabelTextAnchor(TextAnchor.CENTER);
//        argillaArrow.setLabelOffset(new RectangleInsets(10, 0, 0, 0));
//        plot.addDomainMarker(argillaArrow);
        
        //final Marker limo = new ValueMarker(0.074);
        final Marker limo = new ValueMarker(0.03);
        //start.setPaint(Color.green);
        limo.setLabelFont(new Font("sans", Font.BOLD, 10));
        //limo.setLabel("limo (0.074 mm)  ");
        limo.setLabel("limo  ");
        limo.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        limo.setLabelOffset(new RectangleInsets(5, 15, 0, 0));
        limo.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
        limo.setPaint(new Color(0, 0, 0, 0));
        plot.addDomainMarker(limo);
        
//        final Marker limoArrow = new ValueMarker(0.074);
//        limoArrow.setStroke(new BasicStroke(0.0f));
//        limoArrow.setLabelFont(new Font("sans", Font.BOLD, 14));
//        limoArrow.setLabel("\u2193");
//        limoArrow.setLabelAnchor(RectangleAnchor.TOP);
//        limoArrow.setLabelTextAnchor(TextAnchor.CENTER);
//        limoArrow.setLabelOffset(new RectangleInsets(10, 0, 0, 0));
//        plot.addDomainMarker(limoArrow);
        
        //final Marker sabbia = new ValueMarker(2);
        final Marker sabbia = new ValueMarker(1);
        //start.setPaint(Color.green);
        sabbia.setLabelFont(new Font("sans", Font.BOLD, 10));
        //sabbia.setLabel("sabbia (2 mm)  ");
        sabbia.setLabel("sabbia  ");
        sabbia.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        sabbia.setLabelOffset(new RectangleInsets(5, 15, 0, 0));
        sabbia.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
        sabbia.setPaint(new Color(0, 0, 0, 0));
        plot.addDomainMarker(sabbia);
        
//        final Marker sabbiaArrow = new ValueMarker(2);
//        sabbiaArrow.setStroke(new BasicStroke(0.0f));
//        sabbiaArrow.setLabelFont(new Font("sans", Font.BOLD, 14));
//        sabbiaArrow.setLabel("\u2193");
//        sabbiaArrow.setLabelAnchor(RectangleAnchor.TOP);
//        sabbiaArrow.setLabelTextAnchor(TextAnchor.CENTER);
//        sabbiaArrow.setLabelOffset(new RectangleInsets(10, 0, 0, 0));
//        plot.addDomainMarker(sabbiaArrow);
        
        final Marker ghiaia = new ValueMarker(1000);
        //start.setPaint(Color.green);
        ghiaia.setLabelFont(new Font("sans", Font.BOLD, 10));
        ghiaia.setLabel("ghiaia    ");
        ghiaia.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        ghiaia.setLabelOffset(new RectangleInsets(5, 15, 0, 0));
        ghiaia.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
        plot.addDomainMarker(ghiaia);
        
//        final Marker ghiaiaArrow = new ValueMarker(1000);
//        ghiaiaArrow.setStroke(new BasicStroke(0.0f));
//        ghiaiaArrow.setLabelFont(new Font("sans", Font.BOLD, 14));
//        ghiaiaArrow.setLabel("\u2193");
//        ghiaiaArrow.setLabelAnchor(RectangleAnchor.TOP);
//        ghiaiaArrow.setLabelTextAnchor(TextAnchor.CENTER);
//        ghiaiaArrow.setLabelOffset(new RectangleInsets(10, 0, 0, 0));
//        plot.addDomainMarker(ghiaiaArrow);
        
//        plot.setDomainAxis(domainAxis);
//        plot.setRangeAxis(rangeAxis);
//        chart.setBackgroundPaint(Color.white);
//        plot.setOutlinePaint(Color.black);
        
        final JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        chart.setPadding(new RectangleInsets(10, 10, 10, 10));
        
//        LogarithmicAxis axis = (LogarithmicAxis) chart.getXYPlot().getDomainAxis();
//        axis.setNumberFormatOverride(new DecimalFormat("0000.000"));
        
        int width = 800;
        int height = 450;
//        File XYChart = new File( "XYLineChart.png" ); 
//        try {
//			ChartUtilities.saveChartAsPNG( XYChart, chart, width, height);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
        
        BufferedImage objBufferedImage = chart.createBufferedImage(width,height);
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        try {
            ImageIO.write(objBufferedImage, "png", bas);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] byteArray = bas.toByteArray();
        
        //campione.setCurva(byteArray);
        //campione.setCurvaContentType("image/png");
        //Campione result = campioneRepository.save(campione);
        //Campione result = save(campione);
    	
    	//return result;
        return byteArray;
    	
    }
    
    @Transactional(readOnly = true)
    public byte[] generateCurvaGranulometricaCb(Campione campione, Boolean binder) {
    	// serie trattenuti PERC
    	List<BigDecimal> serieTrattenuti = generateSerieTrattenutiCb(campione, binder);
    	
    	List<BigDecimal> serieTrattenutiCumulati = new ArrayList<BigDecimal>();
    	List<BigDecimal> seriePassantiCumulati = new ArrayList<BigDecimal>();
    	BigDecimal cumVal = new BigDecimal(0);
    	
    	for(BigDecimal t: serieTrattenuti) {
    		cumVal = cumVal.add(t);
    		log.debug("Tratt: {} - TrattCum: {} - PassCum: {}", t, cumVal, new BigDecimal(100).subtract(cumVal));
    		serieTrattenutiCumulati.add(cumVal);
    		seriePassantiCumulati.add(new BigDecimal(100).subtract(cumVal));
    	}
    	
    	final XYSeries s1 = new XYSeries("Curva granulometrica");
        s1.add(63, seriePassantiCumulati.get(0));
        s1.add(40, seriePassantiCumulati.get(1));
        s1.add(31.5, seriePassantiCumulati.get(2));
        s1.add(20, seriePassantiCumulati.get(3));
        s1.add(16, seriePassantiCumulati.get(4));
        s1.add(14, seriePassantiCumulati.get(5));
        s1.add(12.5, seriePassantiCumulati.get(6));
        s1.add(10, seriePassantiCumulati.get(7));
        s1.add(8, seriePassantiCumulati.get(8));
        s1.add(6.3, seriePassantiCumulati.get(9));
        s1.add(4, seriePassantiCumulati.get(10));
        s1.add(2, seriePassantiCumulati.get(11));
        s1.add(1, seriePassantiCumulati.get(12));
        s1.add(0.5, seriePassantiCumulati.get(13));
        s1.add(0.25, seriePassantiCumulati.get(14));
        s1.add(0.063, seriePassantiCumulati.get(15));
        
        final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(s1);
        
        final LogarithmicAxis domainAxis = new LogarithmicAxis("Diametro (mm)");
        domainAxis.setLabelFont(new Font("sans", Font.BOLD, 12));
        domainAxis.setRange(0.0001, 100);
        
        final NumberAxis rangeAxis = new NumberAxis("Passante (%)");
        rangeAxis.setLabelFont(new Font("sans", Font.BOLD, 12));
        rangeAxis.setRange(0, 100);
        rangeAxis.setTickUnit(new NumberTickUnit(10));
        
        final XYItemRenderer renderer = new XYLineAndShapeRenderer(true, true);
        final XYPlot plot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        
        final Marker argilla = new ValueMarker(0.001);
        //start.setPaint(Color.green);
        //argilla.setStroke(new BasicStroke(0.0f));
        argilla.setLabelFont(new Font("sans", Font.BOLD, 10));
        //argilla.setLabel("argilla (0.002 mm)  ");
        argilla.setLabel("argilla  ");
        argilla.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        argilla.setLabelOffset(new RectangleInsets(5, 15, 0, 0));
        argilla.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
        argilla.setPaint(new Color(0, 0, 0, 0));
        plot.addDomainMarker(argilla);

        final Marker limo = new ValueMarker(0.03);
        //start.setPaint(Color.green);
        limo.setLabelFont(new Font("sans", Font.BOLD, 10));
        //limo.setLabel("limo (0.074 mm)  ");
        limo.setLabel("limo  ");
        limo.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        limo.setLabelOffset(new RectangleInsets(5, 15, 0, 0));
        limo.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
        limo.setPaint(new Color(0, 0, 0, 0));
        plot.addDomainMarker(limo);

        final Marker sabbia = new ValueMarker(1);
        //start.setPaint(Color.green);
        sabbia.setLabelFont(new Font("sans", Font.BOLD, 10));
        //sabbia.setLabel("sabbia (2 mm)  ");
        sabbia.setLabel("sabbia  ");
        sabbia.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        sabbia.setLabelOffset(new RectangleInsets(5, 15, 0, 0));
        sabbia.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
        sabbia.setPaint(new Color(0, 0, 0, 0));
        plot.addDomainMarker(sabbia);
        
        final Marker ghiaia = new ValueMarker(100);
        //start.setPaint(Color.green);
        ghiaia.setLabelFont(new Font("sans", Font.BOLD, 10));
        ghiaia.setLabel("ghiaia    ");
        ghiaia.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        ghiaia.setLabelOffset(new RectangleInsets(5, 15, 0, 0));
        ghiaia.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
        plot.addDomainMarker(ghiaia);
        
        final JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        chart.setPadding(new RectangleInsets(10, 10, 10, 10));
        
        int width = 800;
        int height = 450;
        
        BufferedImage objBufferedImage = chart.createBufferedImage(width,height);
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        try {
            ImageIO.write(objBufferedImage, "png", bas);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] byteArray = bas.toByteArray();
        
        return byteArray;
    	
    }
    
    
    @Transactional(readOnly = true) 
    public byte[] generatePieChart(ClassificazioneGeotecnicaDTO dto) {
    	
    	// visto che Excel bara con gli arrotondamenti della piechart, lo faccio pure io
//    	BigDecimal limoArgillaAdjusted = new BigDecimal("100")
//    			.subtract(dto.getGhiaiaPerc().setScale(0, BigDecimal.ROUND_HALF_EVEN)
//    					.add(dto.getSabbiaGrossaPerc().setScale(0, BigDecimal.ROUND_HALF_EVEN))
//    					.add(dto.getSabbiaPerc().setScale(0, BigDecimal.ROUND_HALF_EVEN)));
    	
    	BigDecimal ghiaiaSabbiaSumRouded = dto.getGhiaiaPerc().setScale(0, BigDecimal.ROUND_HALF_EVEN)
				.add(dto.getSabbiaGrossaPerc().setScale(0, BigDecimal.ROUND_HALF_EVEN))
				.add(dto.getSabbiaPerc().setScale(0, BigDecimal.ROUND_HALF_EVEN));
    	
    	BigDecimal limoArgillaRounded = dto.getLimoArgillaPerc().setScale(0, BigDecimal.ROUND_HALF_EVEN);
    	
    	BigDecimal cheat = new BigDecimal("0");
    	if(ghiaiaSabbiaSumRouded.add(limoArgillaRounded).compareTo(new BigDecimal("100")) > 0 && 
    			limoArgillaRounded.compareTo(new BigDecimal("0")) > 0) {
    		cheat = ghiaiaSabbiaSumRouded.add(limoArgillaRounded).subtract(new BigDecimal("100"));
    	}
    	
    	DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Ghiaia", dto.getGhiaiaPerc().setScale(0, BigDecimal.ROUND_HALF_EVEN));
        dataset.setValue("Sabbia grossa", dto.getSabbiaGrossaPerc().setScale(0, BigDecimal.ROUND_HALF_EVEN));
        dataset.setValue("Sabbia", dto.getSabbiaPerc().setScale(0, BigDecimal.ROUND_HALF_EVEN).subtract(cheat)); //cheat!
        dataset.setValue("Limo e Argilla", dto.getLimoArgillaPerc().setScale(0, BigDecimal.ROUND_HALF_EVEN));
        //dataset.setValue("Limo e Argilla", limoArgillaAdjusted);
        
        JFreeChart chart = ChartFactory.createPieChart(
            null,  // chart title
            dataset,             // data
            true,               // include legend
            false,
            false
        );
        //chart.setBackgroundPaint(Color.white);
        
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{2}"));
        //((AbstractPieItemLabelGenerator) plot.getLabelGenerator()).getPercentFormat().setMaximumFractionDigits(2);
        //((AbstractPieItemLabelGenerator) plot.getLabelGenerator()).getPercentFormat().setRoundingMode(RoundingMode.HALF_EVEN);
        plot.setBackgroundPaint(Color.white);
        plot.setLabelFont(new Font("ItalgaslabDejaVuSans", Font.PLAIN, 10));
        plot.setSectionPaint("Ghiaia", new Color(33, 87, 180));
        plot.setSectionPaint("Sabbia grossa", new Color(176, 52, 52));
        plot.setSectionPaint("Sabbia", new Color(117, 180, 79));
        plot.setSectionPaint("Limo e Argilla", new Color(242, 168, 41));
        plot.setNoDataMessage("No data available");
        plot.setCircular(true);
        plot.setLabelGap(0.02);
        
        BufferedImage objBufferedImage = chart.createBufferedImage(400,300);
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        try {
            ImageIO.write(objBufferedImage, "png", bas);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] byteArray = bas.toByteArray();
        return byteArray;
    	
    }
    
    @Transactional(readOnly = true) 
    public byte[] generatePieChartCb(CbPercGranDTO dto) {
    	
    	// visto che Excel bara con gli arrotondamenti della piechart, lo faccio pure io
    	BigDecimal ghiaiaSabbiaSumRouded = dto.getGhiaiaPerc().setScale(0, BigDecimal.ROUND_HALF_EVEN)
				.add(dto.getSabbiaPerc().setScale(0, BigDecimal.ROUND_HALF_EVEN));
    	
    	BigDecimal limoArgillaRounded = dto.getLimoArgillaPerc().setScale(0, BigDecimal.ROUND_HALF_EVEN);
    	
    	BigDecimal cheat = new BigDecimal("0");
    	if(ghiaiaSabbiaSumRouded.add(limoArgillaRounded).compareTo(new BigDecimal("100")) > 0 && 
    			limoArgillaRounded.compareTo(new BigDecimal("0")) > 0) {
    		cheat = ghiaiaSabbiaSumRouded.add(limoArgillaRounded).subtract(new BigDecimal("100"));
    	}
    	
    	DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Ghiaia", dto.getGhiaiaPerc().setScale(0, BigDecimal.ROUND_HALF_EVEN));
        dataset.setValue("Sabbia", dto.getSabbiaPerc().setScale(0, BigDecimal.ROUND_HALF_EVEN).subtract(cheat)); //cheat!
        dataset.setValue("Limo e Argilla", dto.getLimoArgillaPerc().setScale(0, BigDecimal.ROUND_HALF_EVEN));
        //dataset.setValue("Limo e Argilla", limoArgillaAdjusted);
        
        JFreeChart chart = ChartFactory.createPieChart(
            null,  // chart title
            dataset,             // data
            true,               // include legend
            false,
            false
        );
        //chart.setBackgroundPaint(Color.white);
        
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{2}"));
        //((AbstractPieItemLabelGenerator) plot.getLabelGenerator()).getPercentFormat().setMaximumFractionDigits(2);
        //((AbstractPieItemLabelGenerator) plot.getLabelGenerator()).getPercentFormat().setRoundingMode(RoundingMode.HALF_EVEN);
        plot.setBackgroundPaint(Color.white);
        plot.setLabelFont(new Font("ItalgaslabDejaVuSans", Font.PLAIN, 10));
        plot.setSectionPaint("Ghiaia", new Color(33, 87, 180));
        //plot.setSectionPaint("Sabbia grossa", new Color(176, 52, 52));
        plot.setSectionPaint("Sabbia", new Color(176, 52, 52));
        plot.setSectionPaint("Limo e Argilla", new Color(117, 180, 79));
        plot.setNoDataMessage("No data available");
        plot.setCircular(true);
        plot.setLabelGap(0.02);
        
        BufferedImage objBufferedImage = chart.createBufferedImage(400,300);
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        try {
            ImageIO.write(objBufferedImage, "png", bas);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] byteArray = bas.toByteArray();
        return byteArray;
    	
    }
    
    /**
     *  Get all the campiones.
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true) 
    public Page<Campione> findAll(Pageable pageable) {
        log.debug("Request to get all Campiones");
        //Page<Campione> result = campioneRepository.findAll(pageable); 
        //return result;
        
        // gli amministratori accedono a tutte le cassette e tutti i campioni
        if(SecurityUtils.isCurrentUserInRole("ROLE_ADMIN")) {
        	Page<Campione> result = campioneRepository.findAll(pageable);
        	
        	// annullo i blob
        	resetBlobs(result);
        	
        	return result; 
        }
        
        // get current user
        User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        // get operatore
        Operatore op = operatoreRepository.findByUser(user);
        
        Page<Campione> result = campioneRepository.findByLaboratorio(pageable, op.getLaboratorio());
        
        // annullo i blob
        resetBlobs(result);
        
        return result;
    }
    
    @Transactional(readOnly = true)
    public Page<Campione> findByCassetta(Pageable pageable, Long idCassetta) {
    	log.debug("Request to get all Campiones by Cassetta");
    	
    	Cassetta cassetta = cassettaRepository.findOne(idCassetta);
    	
    	// gli amministratori accedono a tutte le cassette e tutti i campioni
        if(SecurityUtils.isCurrentUserInRole("ROLE_ADMIN")) {
        	Page<Campione> result = campioneRepository.findByCassetta(pageable, cassetta);
        	
        	// annullo i blob
        	resetBlobs(result);
        	
        	return result;
        }
        
        // get current user
        User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        // get operatore
        Operatore op = operatoreRepository.findByUser(user);
        
        if(cassetta.getConsegna().getLaboratorio().getId() != op.getLaboratorio().getId())
        	return null;
        
        Page<Campione> result = campioneRepository.findByCassetta(pageable, cassetta);
    	
    	// annullo i blob
    	resetBlobs(result);
    	
    	return result;
	}

    /**
     *  Get one campione by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true) 
    public Campione findOne(Long id, boolean resetBlobs) {
        log.debug("Request to get Campione : {}", id);
        Campione campione = campioneRepository.findOne(id);
        
        if(campione == null)
        	return null;
        
        // annullo i blob
        if(resetBlobs)
        	resetBlobs(campione);
        
        // get current user
        User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        // get operatore
        Operatore op = operatoreRepository.findByUser(user);
        
        // gli amministratori accedono a tutte le cassette e tutti i campioni
        if(SecurityUtils.isCurrentUserInRole("ROLE_ADMIN") ||
        		(campione.getCassetta().getConsegna().getLaboratorio().getId() == op.getLaboratorio().getId())) {
        	return campione; 
        }
        
        return null;
    }

    /**
     *  Delete the  campione by id.
     *  
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Campione : {}", id);
        campioneRepository.delete(id);
        campioneSearchRepository.delete(id);
    }

    /**
     * Search for the campione corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Campione> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Campiones for query {}", query);
        //return campioneSearchRepository.search(queryStringQuery(query), pageable);
        
        // gli amministratori accedono a tutte le cassette e tutti i campioni
        if(SecurityUtils.isCurrentUserInRole("ROLE_ADMIN")) {
        	Page<Campione> result = campioneSearchRepository.search(queryStringQuery(query), pageable);
        	
        	// annullo i blob
        	resetBlobs(result);
        	
        	return result;
        }
        
        User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        Operatore op = operatoreRepository.findByUser(user);
        
        String q = query + " AND cassetta.consegna.laboratorio.id:" + op.getLaboratorio().getId();
        
        Page<Campione> result = campioneSearchRepository.search(queryStringQuery(q), pageable);
        
        // annullo i blob
        resetBlobs(result);
        
        return result;
    }
    
    
    @Transactional(readOnly = true)
    public byte[] generateRapportoCb(Long idCampione) {
    	
    	Campione c = campioneRepository.findOne(idCampione);
    	
    	if(c == null)
    		return null;
    	
    	Map<String,Object> parameterMap = new HashMap<String,Object>();
    	
    	String nomeIstituto = c.getCassetta().getConsegna().getLaboratorio().getNome();
        parameterMap.put("ISTITUTO", nomeIstituto.substring(0, nomeIstituto.indexOf("-") - 1));
        parameterMap.put("ISTITUTO_DESC", c.getCassetta().getConsegna().getLaboratorio().getIndirizzo().replace("-", "<br/>"));
        
        parameterMap.put("CAMPIONE_CB", c);
        
        String note = null; 
		if(c.getNote() != null && !c.getNote().isEmpty() && c.getNote().indexOf("###") > -1) {
			note = c.getNote().substring(0, c.getNote().indexOf("###"));
			String operatoreCustom = c.getNote().substring(c.getNote().indexOf("###")).replace("###", "");
			
			if(operatoreCustom != null && !operatoreCustom.isEmpty() && c.getOperatoreAnalisi() == null) {
				parameterMap.put("CAMPIONE_CB_OPERATORE", operatoreCustom);
			}
			
		} else {
			note = c.getNote();
			if(c.getOperatoreAnalisi() != null)
				parameterMap.put("CAMPIONE_CB_OPERATORE", c.getOperatoreAnalisi().getUser().getFirstName() + " " + c.getOperatoreAnalisi().getUser().getLastName());
		}
		parameterMap.put("CAMPIONE_CB_NOTE", note);

		if(c.getFotoSacchetto() != null) {
        	ByteArrayInputStream baisCampioneAFotoSacchetto = new ByteArrayInputStream(c.getFotoSacchetto());
            parameterMap.put("CAMPIONE_CB_FOTO_SACCHETTO", baisCampioneAFotoSacchetto);
        }
		if(c.getFotoCampione() != null) {
        	ByteArrayInputStream baisCampioneAFotoCampione = new ByteArrayInputStream(c.getFotoCampione());
            parameterMap.put("CAMPIONE_CB_FOTO_CAMPIONE", baisCampioneAFotoCampione);
        }
		if(c.getFotoEtichetta() != null) {
        	ByteArrayInputStream baisCampioneAFotoCampione = new ByteArrayInputStream(c.getFotoEtichetta());
            parameterMap.put("CAMPIONE_CB_FOTO_ETICHETTA", baisCampioneAFotoCampione);
        }
		if(c.getCurva() != null) {
        	ByteArrayInputStream baisCampioneACurva = new ByteArrayInputStream(c.getCurva());
            parameterMap.put("CAMPIONE_CB_USURA_CURVA", baisCampioneACurva);
        }
		if(c.getCurvaBinder() != null) {
        	ByteArrayInputStream baisCampioneACurva = new ByteArrayInputStream(c.getCurvaBinder());
            parameterMap.put("CAMPIONE_CB_BINDER_CURVA", baisCampioneACurva);
        }
		
		// CAMPIONE_CB_USURA_SPESSORE_MEDIO
		// ((vm.campioneSelected.cbuSpessEst1p1 + vm.campioneSelected.cbuSpessMedp1 + vm.campioneSelected.cbuSpessEst2p1) / 3  +
        // (vm.campioneSelected.cbuSpessEst1p2 + vm.campioneSelected.cbuSpessMedp2 + vm.campioneSelected.cbuSpessEst2p2) / 3) / 2 
		BigDecimal usuraSpessMedP1 = null;
		try {
			usuraSpessMedP1 = c.getCbuSpessEst1p1().add(c.getCbuSpessMedp1()).add(c.getCbuSpessEst2p1())
					.divide(new BigDecimal("3"), 10, BigDecimal.ROUND_HALF_EVEN);
		} catch(Exception e) {
			//manca p1?
			throw new CustomParameterizedException("errore nei dati", "Controllare i dati cb usura spessore provino 1");
		}
		
		BigDecimal usuraSpessMedP2 = null;
		try {
			usuraSpessMedP2 = c.getCbuSpessEst1p2().add(c.getCbuSpessMedp2()).add(c.getCbuSpessEst2p2())
					.divide(new BigDecimal("3"), 10, BigDecimal.ROUND_HALF_EVEN);
		} catch(Exception e) {
			//manca p2
		}
		
		BigDecimal usuraSpessMedio = null;
		if(usuraSpessMedP2 != null) {
			usuraSpessMedio = usuraSpessMedP1.add(usuraSpessMedP2).divide(new BigDecimal("2"), 2, BigDecimal.ROUND_HALF_EVEN);
		} else {
			usuraSpessMedio = usuraSpessMedP1;
		}
		
		parameterMap.put("CAMPIONE_CB_USURA_SPESSORE_MEDIO", usuraSpessMedio.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		
		BigDecimal binderSpessMedP1 = null;
		try {
			binderSpessMedP1 = c.getCbbSpessEst1p1().add(c.getCbbSpessMedp1()).add(c.getCbbSpessEst2p1())
					.divide(new BigDecimal("3"), 10, BigDecimal.ROUND_HALF_EVEN);
		} catch(Exception e) {
			//manca p1?
			throw new CustomParameterizedException("errore nei dati", "Controllare i dati cb binder spessore provino 1");
		}	
		
		BigDecimal binderSpessMedP2 = null;
		try {
			binderSpessMedP2 = c.getCbbSpessEst1p2().add(c.getCbbSpessMedp2()).add(c.getCbbSpessEst2p2())
					.divide(new BigDecimal("3"), 10, BigDecimal.ROUND_HALF_EVEN);
		} catch(Exception e) {
			//manca p2
		}
		
		BigDecimal binderSpessMedio = null;
		if(usuraSpessMedP2 != null) {
			binderSpessMedio = binderSpessMedP1.add(binderSpessMedP2).divide(new BigDecimal("2"), 2, BigDecimal.ROUND_HALF_EVEN);
		} else {
			binderSpessMedio = binderSpessMedP1;
		}
		
		parameterMap.put("CAMPIONE_CB_BINDER_SPESSORE_MEDIO", binderSpessMedio.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		
		// CAMPIONE_CB_USURA_DENSITA_BSSD
		//usurabssd1 = ( vm.campioneSelected.cbuSsdM1p1 / (vm.campioneSelected.cbuSsdM3p1 - vm.campioneSelected.cbuSsdM2p1) ) *
		//		((-0.00532 * vm.campioneSelected.cbuSsdTp1 * vm.campioneSelected.cbuSsdTp1 + 
		//		0.00759 * vm.campioneSelected.cbuSsdTp1 + 1000.25205) / 1000);
        //usurabssd2 = ( vm.campioneSelected.cbuSsdM1p2 / (vm.campioneSelected.cbuSsdM3p2 - vm.campioneSelected.cbuSsdM2p2) ) * ((-0.00532 * vm.campioneSelected.cbuSsdTp2 * vm.campioneSelected.cbuSsdTp2 + 0.00759 * vm.campioneSelected.cbuSsdTp2 + 1000.25205) / 1000);
        //usurabssd2 ? vm.usuramedbssd = (usurabssd1+usurabssd2)/2 : vm.usuramedbssd = usurabssd1; vm.usuramedbssd
		
		BigDecimal usurabssd1 = null;
		try{
			usurabssd1 = c.getCbuSsdM1p1().divide(c.getCbuSsdM3p1().subtract(c.getCbuSsdM2p1()), 10, BigDecimal.ROUND_HALF_EVEN)
				.multiply(new BigDecimal("-0.00532").multiply(c.getCbuSsdTp1()).multiply(c.getCbuSsdTp1())
				.add(new BigDecimal("0.00759").multiply(c.getCbuSsdTp1())).add(new BigDecimal("1000.25205"))
				.divide(new BigDecimal("1000"), 10, BigDecimal.ROUND_HALF_EVEN));
		} catch(Exception e) {
			//manca p1?
			throw new CustomParameterizedException("errore nei dati", "Controllare i dati cb usura SSD provino 1");
		}
		
		BigDecimal usurabssd2 = null;
		try{
			usurabssd2 = c.getCbuSsdM1p2().divide(c.getCbuSsdM3p2().subtract(c.getCbuSsdM2p2()), 10, BigDecimal.ROUND_HALF_EVEN)
				.multiply(new BigDecimal("-0.00532").multiply(c.getCbuSsdTp2()).multiply(c.getCbuSsdTp2())
				.add(new BigDecimal("0.00759").multiply(c.getCbuSsdTp2())).add(new BigDecimal("1000.25205"))
				.divide(new BigDecimal("1000"), 10, BigDecimal.ROUND_HALF_EVEN));
		} catch(Exception e) {
			//manca p2
		}
		
		BigDecimal usuramedbssd = null;
		if(usurabssd2 != null) {
			usuramedbssd = usurabssd1.add(usurabssd2).divide(new BigDecimal("2"), 10, BigDecimal.ROUND_HALF_EVEN);
		} else {
			usuramedbssd = usurabssd1;
		}
		parameterMap.put("CAMPIONE_CB_USURA_DENSITA_BSSD", usuramedbssd.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		
		// CAMPIONE_CB_BINDER_DENSITA_BSSD
		BigDecimal binderbssd1 = null;
		try{
			binderbssd1 = c.getCbbSsdM1p1().divide(c.getCbbSsdM3p1().subtract(c.getCbbSsdM2p1()), 10, BigDecimal.ROUND_HALF_EVEN)
				.multiply(new BigDecimal("-0.00532").multiply(c.getCbbSsdTp1()).multiply(c.getCbbSsdTp1())
				.add(new BigDecimal("0.00759").multiply(c.getCbbSsdTp1())).add(new BigDecimal("1000.25205"))
				.divide(new BigDecimal("1000"), 10, BigDecimal.ROUND_HALF_EVEN));
		} catch(Exception e) {
			//manca p1?
			throw new CustomParameterizedException("errore nei dati", "Controllare i dati cb binder SSD provino 1");
		}
		log.debug("######### binderbssd1 {}", binderbssd1);
		
		BigDecimal binderbssd2 = null;
		try{
			binderbssd2 = c.getCbbSsdM1p2().divide(c.getCbbSsdM3p2().subtract(c.getCbbSsdM2p2()), 10, BigDecimal.ROUND_HALF_EVEN)
				.multiply(new BigDecimal("-0.00532").multiply(c.getCbbSsdTp2()).multiply(c.getCbbSsdTp2())
				.add(new BigDecimal("0.00759").multiply(c.getCbbSsdTp2())).add(new BigDecimal("1000.25205"))
				.divide(new BigDecimal("1000"), 10, BigDecimal.ROUND_HALF_EVEN));
		} catch(Exception e) {
			//manca p2
		}
		log.debug("######### binderbssd2 {}", binderbssd2);
		
		BigDecimal bindermedbssd = null;
		if(binderbssd2 != null) {
			bindermedbssd = binderbssd1.add(binderbssd2).divide(new BigDecimal("2"), 10, BigDecimal.ROUND_HALF_EVEN);
		} else {
			bindermedbssd = binderbssd1;
		}
		log.debug("######### bindermedbssd {}", bindermedbssd);
		parameterMap.put("CAMPIONE_CB_BINDER_DENSITA_BSSD", bindermedbssd.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		
		
		// CAMPIONE_CB_USURA_MASSA_LEGANTE
		BigDecimal usuramassaleg = null;
		try{
			usuramassaleg = c.getCbuLegMmp1().subtract(c.getCbuLegMap1()).setScale(2, BigDecimal.ROUND_HALF_EVEN);
		} catch(Exception e) {
			throw new CustomParameterizedException("errore nei dati", "Controllare i dati cb usura legante");
		}
		parameterMap.put("CAMPIONE_CB_USURA_MASSA_LEGANTE", usuramassaleg);
		
		
		BigDecimal bindermassalegp1 = null;
		try{
			bindermassalegp1 = c.getCbbLegMmp1().subtract(c.getCbbLegMap1());
		} catch(Exception e) {
			throw new CustomParameterizedException("errore nei dati", "Controllare i dati cb binder legante");
		}
		
		BigDecimal bindermassalegp2 = null;
		try{
			bindermassalegp2 = c.getCbbLegMmp2().subtract(c.getCbbLegMap2());
		} catch(Exception e) {
			// manca p2
		}
		
		BigDecimal bindermedmassaleg = null;
		if(bindermassalegp2 != null){
			bindermedmassaleg = bindermassalegp1.add(bindermassalegp2).divide(new BigDecimal("2"), 2, BigDecimal.ROUND_HALF_EVEN);
		} else {
			bindermedmassaleg = bindermassalegp1;
		}
		parameterMap.put("CAMPIONE_CB_BINDER_MASSA_LEGANTE", bindermedmassaleg);
		
		
		// CAMPIONE_CB_USURA_PERC_BIT_AGGR
		BigDecimal usurapercbitaggr = null;
		try{
			usurapercbitaggr = c.getCbuLegMmp1().subtract(c.getCbuLegMap1()).divide(c.getCbuLegMap1(), 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_EVEN);
		} catch(Exception e) {
			throw new CustomParameterizedException("errore nei dati", "Controllare i dati cb usura legante");
		}
		parameterMap.put("CAMPIONE_CB_USURA_PERC_BIT_AGGR", usurapercbitaggr);
		
		BigDecimal binderpercbitaggr1 = null;
		try{
			binderpercbitaggr1 = c.getCbbLegMmp1().subtract(c.getCbbLegMap1()).divide(c.getCbbLegMap1(), 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal("100"));
		} catch(Exception e) {
			throw new CustomParameterizedException("errore nei dati", "Controllare i dati cb binder legante");
		}
		
		BigDecimal binderpercbitaggr2 = null;
		try{
			binderpercbitaggr2 = c.getCbbLegMmp2().subtract(c.getCbbLegMap2()).divide(c.getCbbLegMap2(), 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal("100"));
		} catch(Exception e) {
			// manca p2
		}
		
		BigDecimal bindermedpercbitaggr = null;
		if(binderpercbitaggr2 != null) {
			bindermedpercbitaggr = binderpercbitaggr1.add(binderpercbitaggr2).divide(new BigDecimal("2"), 2, BigDecimal.ROUND_HALF_EVEN);
		} else {
			bindermedpercbitaggr = binderpercbitaggr1.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		}
		parameterMap.put("CAMPIONE_CB_BINDER_PERC_BIT_AGGR", bindermedpercbitaggr);
		
		
		// CAMPIONE_CB_USURA_PERC_BIT_MISC
		BigDecimal usurapercbitmisc = null;
		try{
			usurapercbitmisc = c.getCbuLegMmp1().subtract(c.getCbuLegMap1()).divide(c.getCbuLegMmp1(), 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal("100"));
		} catch(Exception e) {
			throw new CustomParameterizedException("errore nei dati", "Controllare i dati cb usura legante");
		}
		parameterMap.put("CAMPIONE_CB_USURA_PERC_BIT_MISC", usurapercbitmisc.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		
		BigDecimal binderpercbitmisc1 = null;
		try{
			binderpercbitmisc1 = c.getCbbLegMmp1().subtract(c.getCbbLegMap1()).divide(c.getCbbLegMmp1(), 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal("100"));
		} catch(Exception e) {
			throw new CustomParameterizedException("errore nei dati", "Controllare i dati cb binder legante");
		}
		
		BigDecimal binderpercbitmisc2 = null;
		try{
			binderpercbitmisc2 = c.getCbbLegMmp2().subtract(c.getCbbLegMap2()).divide(c.getCbbLegMmp2(), 10, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal("100"));
		} catch(Exception e) {
			// manca p2
		}
		
		BigDecimal bindermedpercbitmisc = null;
		if(binderpercbitmisc2 != null) {
			bindermedpercbitmisc = binderpercbitmisc1.add(binderpercbitmisc2).divide(new BigDecimal("2"), 10, BigDecimal.ROUND_HALF_EVEN);
		} else {
			bindermedpercbitmisc = binderpercbitmisc1;
		}
		parameterMap.put("CAMPIONE_CB_BINDER_PERC_BIT_MISC", bindermedpercbitmisc.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		
		
		
		// CAMPIONE_CB_USURA_DENSITA_AGGR
		/*
		 *  rw1 = -0.00532 * vm.campioneSelected.cbuMvaT1pic1 * vm.campioneSelected.cbuMvaT1pic1 + 0.00759 * vm.campioneSelected.cbuMvaT1pic1 + 1000.25205;
            vpic1 = ((vm.campioneSelected.cbuMvaMpwpic1 - vm.campioneSelected.cbuMvaM0pic1) / 1000) / rw1;
            rw2 = -0.00532 * vm.campioneSelected.cbuMvaT1pic2 * vm.campioneSelected.cbuMvaT1pic2 + 0.00759 * vm.campioneSelected.cbuMvaT1pic2 + 1000.25205;
            vpic2 = ((vm.campioneSelected.cbuMvaMpwpic2 - vm.campioneSelected.cbuMvaM0pic2) / 1000) / rw2;
            rw3 = (-0.00532 * vm.campioneSelected.cbuMvaT2pic1 * vm.campioneSelected.cbuMvaT2pic1 + 0.00759 * vm.campioneSelected.cbuMvaT2pic1 + 1000.25205)/1000;
            raggr1 = (vm.campioneSelected.cbuMvaMpapic1 - vm.campioneSelected.cbuMvaM0pic1) / ((vpic1 * 1000000) - (vm.campioneSelected.cbuMvaM2pic1 - (vm.campioneSelected.cbuMvaMpapic1 - vm.campioneSelected.cbuMvaM0pic1) - vm.campioneSelected.cbuMvaM0pic1) / rw3);
            rw4 = (-0.00532 * vm.campioneSelected.cbuMvaT2pic2 * vm.campioneSelected.cbuMvaT2pic2 + 0.00759 * vm.campioneSelected.cbuMvaT2pic2 + 1000.25205)/1000;
            raggr2 = (vm.campioneSelected.cbuMvaMpapic2 - vm.campioneSelected.cbuMvaM0pic2) / ((vpic2 * 1000000) - (vm.campioneSelected.cbuMvaM2pic2 - (vm.campioneSelected.cbuMvaMpapic2 - vm.campioneSelected.cbuMvaM0pic2) - vm.campioneSelected.cbuMvaM0pic2) / rw4);
            raggr2 ? vm.medraggr = (raggr1+raggr2)/2 : vm.medraggr = raggr1;
		 */
		BigDecimal rw1 = null;
		try{
			rw1 = new BigDecimal("-0.00532").multiply(c.getCbuMvaT1pic1().multiply(c.getCbuMvaT1pic1())).add(new BigDecimal("0.00759").multiply(c.getCbuMvaT1pic1())).add(new BigDecimal("1000.25205"));
		} catch(Exception e) {
			throw new CustomParameterizedException("errore nei dati", "Controllare i dati cb usura massa volumica aggregato");
		}
		log.debug("################## rw1: {}", rw1);
		
		BigDecimal vpic1 = null;
		try{
			vpic1 = c.getCbuMvaMpwpic1().subtract(c.getCbuMvaM0pic1()).divide(new BigDecimal("1000"), 10, BigDecimal.ROUND_HALF_EVEN).divide(rw1, 10, BigDecimal.ROUND_HALF_EVEN);
		} catch(Exception e) {
			throw new CustomParameterizedException("errore nei dati", "Controllare i dati cb usura massa volumica aggregato");
		}
		log.debug("################## vpic1: {}", vpic1);
		
		BigDecimal rw2 = null;
		try{
			rw2 = new BigDecimal("-0.00532").multiply(c.getCbuMvaT1pic2().multiply(c.getCbuMvaT1pic2())).add(new BigDecimal("0.00759").multiply(c.getCbuMvaT1pic2())).add(new BigDecimal("1000.25205"));
		} catch(Exception e) {
			// manca pic2
		}
		log.debug("################## rw2: {}", rw2);
		
		BigDecimal vpic2 = null;
		try{
			vpic2 = c.getCbuMvaMpwpic2().subtract(c.getCbuMvaM0pic2()).divide(new BigDecimal("1000"), 10, BigDecimal.ROUND_HALF_EVEN).divide(rw2, 10, BigDecimal.ROUND_HALF_EVEN);
		} catch(Exception e) {
			// manca pic2
		}
		log.debug("################## vpic2: {}", vpic2);
		
		BigDecimal rw3 = null;
		try{
			rw3 = new BigDecimal("-0.00532").multiply(c.getCbuMvaT2pic1().multiply(c.getCbuMvaT2pic1())).add(new BigDecimal("0.00759").multiply(c.getCbuMvaT2pic1())).add(new BigDecimal("1000.25205")).divide(new BigDecimal("1000"), 10, BigDecimal.ROUND_HALF_EVEN);
		} catch(Exception e) {
			throw new CustomParameterizedException("errore nei dati", "Controllare i dati cb usura massa volumica aggregato");
		}
		log.debug("################## rw3: {}", rw3);
		
		BigDecimal raggr1 = null;
		BigDecimal num = null;
		BigDecimal num1 = null;
		BigDecimal num2 = null;
		BigDecimal num3 = null;
		BigDecimal denom = null;
		try{
			num = c.getCbuMvaMpapic1().subtract(c.getCbuMvaM0pic1());
			num1 = vpic1.multiply(new BigDecimal("1000000"));
			num2 = c.getCbuMvaM2pic1().subtract(c.getCbuMvaMpapic1().subtract(c.getCbuMvaM0pic1()))
					.subtract(c.getCbuMvaM0pic1());
			num3 = rw3;
			denom = num1.subtract(num2.divide(num3, 10, BigDecimal.ROUND_HALF_EVEN));
//			raggr1 = c.getCbuMvaMpapic1().subtract(c.getCbuMvaM0pic1()).divide(vpic1.multiply(new BigDecimal("1000000"))
//					.subtract(c.getCbuMvaM2pic1().subtract(c.getCbuMvaMpapic1()).subtract(c.getCbuMvaM0pic1())
//							.subtract(c.getCbuMvaM0pic1())).divide(rw3, 10, BigDecimal.ROUND_HALF_EVEN), 10, BigDecimal.ROUND_HALF_EVEN);
			raggr1 = num.divide(denom, 10, BigDecimal.ROUND_HALF_EVEN);
		} catch(Exception e) {
			throw new CustomParameterizedException("errore nei dati", "Controllare i dati cb usura massa volumica aggregato");
		}
		log.debug("################## raggr1: {}", raggr1);
		
		BigDecimal rw4 = null;
		try{
			rw4 = new BigDecimal("-0.00532").multiply(c.getCbuMvaT2pic2().multiply(c.getCbuMvaT2pic2())).add(new BigDecimal("0.00759").multiply(c.getCbuMvaT2pic2())).add(new BigDecimal("1000.25205")).divide(new BigDecimal("1000"), 10, BigDecimal.ROUND_HALF_EVEN);
		} catch(Exception e) {
			// manca pic2
		}
		log.debug("################## rw4: {}", rw4);
		
		BigDecimal raggr2 = null;
		try{
			num = c.getCbuMvaMpapic2().subtract(c.getCbuMvaM0pic2());
			num1 = vpic2.multiply(new BigDecimal("1000000"));
			num2 = c.getCbuMvaM2pic2().subtract(c.getCbuMvaMpapic2().subtract(c.getCbuMvaM0pic2()))
					.subtract(c.getCbuMvaM0pic2());
			num3 = rw4;
			denom = num1.subtract(num2.divide(num3, 10, BigDecimal.ROUND_HALF_EVEN));
//			raggr2 = c.getCbuMvaMpapic2().subtract(c.getCbuMvaM0pic2()).divide(vpic2.multiply(new BigDecimal("1000000"))
//					.subtract(c.getCbuMvaM2pic2().subtract(c.getCbuMvaMpapic2().subtract(c.getCbuMvaM0pic2())
//							.subtract(c.getCbuMvaM0pic2()))).divide(rw4, 10, BigDecimal.ROUND_HALF_EVEN), 10, BigDecimal.ROUND_HALF_EVEN);
			raggr2 = num.divide(denom, 10, BigDecimal.ROUND_HALF_EVEN);
		} catch(Exception e) {
			// manca pic2
		}
		log.debug("################## raggr2: {}", raggr2);
		
		BigDecimal usuramedraggr = null;
		if(raggr2 != null) {
			usuramedraggr = raggr1.add(raggr2).divide(new BigDecimal("2"), 10, BigDecimal.ROUND_HALF_EVEN);
		} else {
			usuramedraggr = raggr1;
		}
		log.debug("################## usuramedraggr: {}", usuramedraggr);
		parameterMap.put("CAMPIONE_CB_USURA_DENSITA_AGGR", usuramedraggr.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		
		
		
		
		// CAMPIONE_CB_BINDER_DENSITA_AGGR
		
		BigDecimal rw1p1 = null;
		BigDecimal vpic1p1 = null;
		BigDecimal rw2p1 = null;
		BigDecimal vpic2p1 = null;
		BigDecimal rw3p1 = null;
		BigDecimal raggr1p1 = null;
		BigDecimal rw4p1 = null;
		BigDecimal raggr2p1 = null;
		try{
			rw1p1 = new BigDecimal("-0.00532").multiply(c.getCbbMvaT1pic1p1().multiply(c.getCbbMvaT1pic1p1())).add(new BigDecimal("0.00759").multiply(c.getCbbMvaT1pic1p1())).add(new BigDecimal("1000.25205"));
			log.debug("################## rw1p1: {}", rw1p1);
			vpic1p1 = c.getCbbMvaMpwpic1p1().subtract(c.getCbbMvaM0pic1p1()).divide(new BigDecimal("1000"), 10, BigDecimal.ROUND_HALF_EVEN).divide(rw1p1, 10, BigDecimal.ROUND_HALF_EVEN);
			log.debug("################## vpic1p1: {}", vpic1p1);
			
			try{
				rw2p1 = new BigDecimal("-0.00532").multiply(c.getCbbMvaT1pic2p1().multiply(c.getCbbMvaT1pic2p1())).add(new BigDecimal("0.00759").multiply(c.getCbbMvaT1pic2p1())).add(new BigDecimal("1000.25205"));
				log.debug("################## rw2p1: {}", rw2p1);
				vpic2p1 = c.getCbbMvaMpwpic2p1().subtract(c.getCbbMvaM0pic2p1()).divide(new BigDecimal("1000"), 10, BigDecimal.ROUND_HALF_EVEN).divide(rw2p1, 10, BigDecimal.ROUND_HALF_EVEN);
				log.debug("################## vpic2p1: {}", vpic2p1);
			} catch(Exception e) {
				// manca il secondo pic del p1
			}
			
			rw3p1 = new BigDecimal("-0.00532").multiply(c.getCbbMvaT2pic1p1().multiply(c.getCbbMvaT2pic1p1())).add(new BigDecimal("0.00759").multiply(c.getCbbMvaT2pic1p1())).add(new BigDecimal("1000.25205")).divide(new BigDecimal("1000"), 10, BigDecimal.ROUND_HALF_EVEN);
			log.debug("################## rw3p1: {}", rw3p1);
		
//			num = c.getCbbMvaMpapic1p1().subtract(c.getCbbMvaM0pic1p1());
//			denom = vpic1p1.multiply(new BigDecimal("1000000"))
//					.subtract(c.getCbbMvaM2pic1p1().subtract(c.getCbbMvaMpapic1p1().subtract(c.getCbbMvaM0pic1p1()))
//							.subtract(c.getCbbMvaM0pic1p1())).divide(rw3p1, 10, BigDecimal.ROUND_HALF_EVEN);

			num = c.getCbbMvaMpapic1p1().subtract(c.getCbbMvaM0pic1p1());
			num1 = vpic1p1.multiply(new BigDecimal("1000000"));
			num2 = c.getCbbMvaM2pic1p1().subtract(c.getCbbMvaMpapic1p1().subtract(c.getCbbMvaM0pic1p1()))
					.subtract(c.getCbbMvaM0pic1p1());
			num3 = rw3p1;
			denom = num1.subtract(num2.divide(num3, 10, BigDecimal.ROUND_HALF_EVEN));
			
			raggr1p1 = num.divide(denom, 10, BigDecimal.ROUND_HALF_EVEN);
			log.debug("################## raggr1p1: {}", raggr1p1);
		
			try{
				rw4p1 = new BigDecimal("-0.00532").multiply(c.getCbbMvaT2pic2p1().multiply(c.getCbbMvaT2pic2p1())).add(new BigDecimal("0.00759").multiply(c.getCbbMvaT2pic2p1())).add(new BigDecimal("1000.25205")).divide(new BigDecimal("1000"), 10, BigDecimal.ROUND_HALF_EVEN);
				log.debug("################## rw4p1: {}", rw4p1);
			
				num = c.getCbbMvaMpapic2p1().subtract(c.getCbbMvaM0pic2p1());
				num1 = vpic2p1.multiply(new BigDecimal("1000000"));
				num2 = c.getCbbMvaM2pic2p1().subtract(c.getCbbMvaMpapic2p1().subtract(c.getCbbMvaM0pic2p1()))
						.subtract(c.getCbbMvaM0pic2p1());
				num3 = rw4p1;
				denom = num1.subtract(num2.divide(num3, 10, BigDecimal.ROUND_HALF_EVEN));
				
				raggr2p1 = num.divide(denom, 10, BigDecimal.ROUND_HALF_EVEN);
				log.debug("################## raggr2p1: {}", raggr2p1);
			} catch(Exception e) {
				// manca il secondo pic del p1
			}
			
		} catch(Exception e) {
			throw new CustomParameterizedException("errore nei dati", "Controllare i dati cb binder massa volumica aggregato");
		}
		
		
		
		BigDecimal rw1p2 = null;
		BigDecimal vpic1p2 = null;
		BigDecimal rw2p2 = null;
		BigDecimal vpic2p2 = null;
		BigDecimal rw3p2 = null;
		BigDecimal raggr1p2 = null;
		BigDecimal rw4p2 = null;
		BigDecimal raggr2p2 = null;
		try{
			rw1p2 = new BigDecimal("-0.00532").multiply(c.getCbbMvaT1pic1p2().multiply(c.getCbbMvaT1pic1p2())).add(new BigDecimal("0.00759").multiply(c.getCbbMvaT1pic1p2())).add(new BigDecimal("1000.25205"));
			log.debug("################## rw1p2: {}", rw1p2);
			vpic1p2 = c.getCbbMvaMpwpic1p2().subtract(c.getCbbMvaM0pic1p2()).divide(new BigDecimal("1000"), 10, BigDecimal.ROUND_HALF_EVEN).divide(rw1p2, 10, BigDecimal.ROUND_HALF_EVEN);
			log.debug("################## vpic1p2: {}", vpic1p2);
			
			try{
				rw2p2 = new BigDecimal("-0.00532").multiply(c.getCbbMvaT1pic2p2().multiply(c.getCbbMvaT1pic2p2())).add(new BigDecimal("0.00759").multiply(c.getCbbMvaT1pic2p2())).add(new BigDecimal("1000.25205"));
				log.debug("################## rw2p2: {}", rw2p2);
				vpic2p2 = c.getCbbMvaMpwpic2p2().subtract(c.getCbbMvaM0pic2p2()).divide(new BigDecimal("1000"), 10, BigDecimal.ROUND_HALF_EVEN).divide(rw2p2, 10, BigDecimal.ROUND_HALF_EVEN);
				log.debug("################## vpic2p2: {}", vpic2p2);
			} catch(Exception e) {
				// manca il secondo pic del p2
			}
			
			rw3p2 = new BigDecimal("-0.00532").multiply(c.getCbbMvaT2pic1p2().multiply(c.getCbbMvaT2pic1p2())).add(new BigDecimal("0.00759").multiply(c.getCbbMvaT2pic1p2())).add(new BigDecimal("1000.25205")).divide(new BigDecimal("1000"), 10, BigDecimal.ROUND_HALF_EVEN);
			log.debug("################## rw3p2: {}", rw3p2);
		
//			num = c.getCbbMvaMpapic1p2().subtract(c.getCbbMvaM0pic1p2());
//			denom = vpic1p2.multiply(new BigDecimal("1000000"))
//					.subtract(c.getCbbMvaM2pic1p2().subtract(c.getCbbMvaMpapic1p2().subtract(c.getCbbMvaM0pic1p2()))
//							.subtract(c.getCbbMvaM0pic1p2())).divide(rw3p2, 10, BigDecimal.ROUND_HALF_EVEN);

			num = c.getCbbMvaMpapic1p2().subtract(c.getCbbMvaM0pic1p2());
			num1 = vpic1p2.multiply(new BigDecimal("1000000"));
			num2 = c.getCbbMvaM2pic1p2().subtract(c.getCbbMvaMpapic1p2().subtract(c.getCbbMvaM0pic1p2()))
					.subtract(c.getCbbMvaM0pic1p2());
			num3 = rw3p2;
			denom = num1.subtract(num2.divide(num3, 10, BigDecimal.ROUND_HALF_EVEN));
			
			raggr1p2 = num.divide(denom, 10, BigDecimal.ROUND_HALF_EVEN);
			log.debug("################## raggr1p2: {}", raggr1p2);
		
			try{
				rw4p2 = new BigDecimal("-0.00532").multiply(c.getCbbMvaT2pic2p2().multiply(c.getCbbMvaT2pic2p2())).add(new BigDecimal("0.00759").multiply(c.getCbbMvaT2pic2p2())).add(new BigDecimal("1000.25205")).divide(new BigDecimal("1000"), 10, BigDecimal.ROUND_HALF_EVEN);
				log.debug("################## rw4p2: {}", rw4p2);
			
				num = c.getCbbMvaMpapic2p2().subtract(c.getCbbMvaM0pic2p2());
				num1 = vpic2p2.multiply(new BigDecimal("1000000"));
				num2 = c.getCbbMvaM2pic2p2().subtract(c.getCbbMvaMpapic2p2().subtract(c.getCbbMvaM0pic2p2()))
						.subtract(c.getCbbMvaM0pic2p2());
				num3 = rw4p2;
				denom = num1.subtract(num2.divide(num3, 10, BigDecimal.ROUND_HALF_EVEN));
	
				raggr2p2 = num.divide(denom, 10, BigDecimal.ROUND_HALF_EVEN);
				log.debug("################## raggr2p2: {}", raggr2p2);
			} catch(Exception e) {
				// manca il secondo pic del p2
			}
			
		} catch(Exception e) {
			// manca p2
		}
		
		
		
		BigDecimal bindermedraggr = null;
		
		BigDecimal medraggrp1 = null;
		if(raggr2p1 != null) {
			//c'è il pic2
			medraggrp1 = raggr1p1.add(raggr2p1).divide(new BigDecimal("2"), 10, BigDecimal.ROUND_HALF_EVEN);
		} else {
			medraggrp1 = raggr1p1;
		}
		
		BigDecimal medraggrp2 = null;
		if(raggr2p2 != null) {
			//c'è il pic2 (e implicitamente il p2)
			medraggrp2 = raggr1p2.add(raggr2p2).divide(new BigDecimal("2"), 10, BigDecimal.ROUND_HALF_EVEN);
		} else {
			medraggrp2 = raggr1p2;
		}
		
		if(medraggrp2 != null) {
			bindermedraggr = medraggrp1.add(medraggrp2).divide(new BigDecimal("2"), 10, BigDecimal.ROUND_HALF_EVEN);
		} else {
			bindermedraggr = medraggrp1;
		}
		
		log.debug("################## bindermedraggr: {}", bindermedraggr);
		parameterMap.put("CAMPIONE_CB_BINDER_DENSITA_AGGR", bindermedraggr.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		
		
		
		
		BigDecimal usurapercvuoti = null;
		BigDecimal parte1 = null;
		BigDecimal parte2 = null;
		BigDecimal parte3 = null;
		BigDecimal parte4 = null;
		try{
			parte1 = new BigDecimal("100").subtract(usurapercbitmisc);
			parte2 = usurapercbitmisc.divide(new BigDecimal("1.02"), 10, BigDecimal.ROUND_HALF_EVEN);
			log.debug("################## usurapercbitmisc: {}", usurapercbitmisc);
			parte3 = parte1.divide(usuramedraggr, 10, BigDecimal.ROUND_HALF_EVEN);
			log.debug("################## usuramedraggr: {}", usuramedraggr);
			//parte3 = usurapercbitmisc.divide(new BigDecimal("1.02"), 10, BigDecimal.ROUND_HALF_EVEN).add(new BigDecimal("100").subtract(usurapercbitmisc).divide(usuramedraggr, 10, BigDecimal.ROUND_HALF_EVEN));
			parte4 = parte2.add(parte3);
			usurapercvuoti = new BigDecimal("100").subtract(usuramedbssd.multiply(parte4));
		} catch(Exception e) {
			throw new CustomParameterizedException("errore nei dati", "Controllare i dati cb usura ssd e legante");
		}
		log.debug("################## usurapercvuoti: {}", usurapercvuoti);
		parameterMap.put("CAMPIONE_CB_USURA_PERC_VUOTI", usurapercvuoti.setScale(2, BigDecimal.ROUND_HALF_EVEN ));
		
		
		
		BigDecimal binderpercvuoti = null;
		try{
			parte1 = new BigDecimal("100").subtract(bindermedpercbitmisc);
			parte2 = bindermedpercbitmisc.divide(new BigDecimal("1.02"), 10, BigDecimal.ROUND_HALF_EVEN);
			log.debug("################## bindermedpercbitmisc: {}", bindermedpercbitmisc);
			parte3 = parte1.divide(bindermedraggr, 10, BigDecimal.ROUND_HALF_EVEN);
			log.debug("################## bindermedraggr: {}", bindermedraggr);
			//parte3 = usurapercbitmisc.divide(new BigDecimal("1.02"), 10, BigDecimal.ROUND_HALF_EVEN).add(new BigDecimal("100").subtract(usurapercbitmisc).divide(usuramedraggr, 10, BigDecimal.ROUND_HALF_EVEN));
			parte4 = parte2.add(parte3);
			binderpercvuoti = new BigDecimal("100").subtract(bindermedbssd.multiply(parte4));
		} catch(Exception e) {
			throw new CustomParameterizedException("errore nei dati", "Controllare i dati cb binder ssd e legante");
		}
		log.debug("################## binderpercvuoti: {}", binderpercvuoti);
		parameterMap.put("CAMPIONE_CB_BINDER_PERC_VUOTI", binderpercvuoti.setScale(2, BigDecimal.ROUND_HALF_EVEN ));
		
		
		
		
		//granulometria usura
		parameterMap.put("CAMPIONE_CB_USURA_GRANULOMETRIA", generateListaSetacciCb(c, false));
		
		//granulometria binder
		parameterMap.put("CAMPIONE_CB_BINDER_GRANULOMETRIA", generateListaSetacciCb(c, true));
		
		
		
		//passante a 0063 NON SERVE
//		if(binder) {
//    		sumFondoConPass0063 = sumFondo.add(campione.getCbbLegMap1().add(campione.getCbbLegMap2()).subtract(campione.getCbbPPostLav()));
//    		serieMasse.add(sumFondoConPass0063);
//    	} else {
//    		sumFondoConPass0063 = sumFondo.add(campione.getCbuLegMap1().subtract(campione.getCbuPPostLav()));
//    		serieMasse.add(sumFondoConPass0063);
//    	}
//		BigDecimal usurapass0063 = c.getCbuLegMap1().subtract(c.getCbuPPostLav()).setScale(2, BigDecimal.ROUND_HALF_EVEN);
//		parameterMap.put("CAMPIONE_CB_USURA_PASSANTE0063", usurapass0063);
		
		
		
		
		CbPercGranDTO percGeo = generateCbUsuraPercGran(c);
		byte[] pieChart = generatePieChartCb(percGeo);
		ByteArrayInputStream baisPieChart = new ByteArrayInputStream(pieChart);
		parameterMap.put("CAMPIONE_CB_USURA_PIECHART", baisPieChart);
		
		CbPercGranDTO percGeoBinder = generateCbBinderPercGran(c);
		byte[] pieChartBinder = generatePieChartCb(percGeoBinder);
		ByteArrayInputStream baisPieChartBinder = new ByteArrayInputStream(pieChartBinder);
		parameterMap.put("CAMPIONE_CB_BINDER_PIECHART", baisPieChartBinder);
		
		
		
		
		
		ClassPathResource jasperFile = new ClassPathResource("certificato_cb_book.jasper");
        
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
			
			List<JRPrintPage> pages = jasperPrint.getPages();
			int count = 0;
			for (Iterator<JRPrintPage> i=pages.iterator(); i.hasNext();) {
				count++;
		        JRPrintPage page = i.next();
		        if (count == 3 || count == 6)
		        	i.remove();
		    }
			
			// esportazione del report in formato pdf (in memoria)
			pdfReport = JasperExportManager.exportReportToPdf(jasperPrint);
			
		} catch (JRException e) {
			e.printStackTrace();
			throw new CustomParameterizedException("modelloCertificatoError", "JRException", e.toString());
		}
        
    	return pdfReport;
    }
    
    
    
    
    private void resetBlobs(Page<Campione> page) {
		page.forEach(campione-> resetBlobs(campione));
	}
	
	public Campione resetBlobs(Campione campione) {
		campione.setFotoSacchetto(null);
		campione.setFotoCampione(null);
		campione.setFotoEtichetta(null);
		campione.setCurva(null);
		campione.setCurvaBinder(null);
		campione.getCassetta().setFotoContenitore(null);
		campione.getCassetta().setFotoContenuto(null);
		campione.getCassetta().setCertificatoPdf(null);
		campione.getCassetta().getConsegna().setProtocolloAccettazione(null);
		return campione;
	}
}
