package it.cnr.igag.italgas.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.inject.Inject;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSimpleShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import it.cnr.igag.italgas.domain.Campione;
import it.cnr.igag.italgas.domain.Cassetta;
import it.cnr.igag.italgas.domain.CodiceIstat;
import it.cnr.igag.italgas.domain.Consegna;
import it.cnr.igag.italgas.domain.Laboratorio;
import it.cnr.igag.italgas.domain.Operatore;
import it.cnr.igag.italgas.domain.Pesata;
import it.cnr.igag.italgas.domain.User;
import it.cnr.igag.italgas.domain.enumeration.StatoAttualeCassetta;
import it.cnr.igag.italgas.domain.enumeration.StatoContenitore;
import it.cnr.igag.italgas.domain.enumeration.TipoCampione;
import it.cnr.igag.italgas.repository.CampioneRepository;
import it.cnr.igag.italgas.repository.CassettaRepository;
import it.cnr.igag.italgas.repository.CodiceIstatRepository;
import it.cnr.igag.italgas.repository.ConsegnaRepository;
import it.cnr.igag.italgas.repository.LaboratorioRepository;
import it.cnr.igag.italgas.repository.OperatoreRepository;
import it.cnr.igag.italgas.repository.PesataRepository;
import it.cnr.igag.italgas.repository.UserRepository;
import it.cnr.igag.italgas.web.rest.dto.ClassificazioneGeotecnicaDTO;
import it.cnr.igag.italgas.web.rest.errors.CustomParameterizedException;

@Service
//@Transactional
public class ExcelImportService {

	private final Logger log = LoggerFactory.getLogger(ExcelImportService.class);
	
	@Inject
    private LaboratorioRepository laboratorioRepository;
	
	@Inject
    private ConsegnaRepository consegnaRepository;
	
	@Inject
    private CassettaRepository cassettaRepository;
	
	@Inject
    private CodiceIstatRepository codiceIstatRepository;
	
	@Inject
    private CampioneRepository campioneRepository;
	
	@Inject
    private CampioneService campioneService;
	
	@Inject
    private UserRepository userRepository;
	
	@Inject
    private OperatoreRepository operatoreRepository;
	
	@Inject
    private PesataRepository pesataRepository;
	
	private String importRoot = "/media/francesco/Elements/TEST/";
	
	private String currentPath = null;
	private HashMap<String,List<String>> errorMap = new HashMap<String,List<String>>();
	
	// foto sacchetto e campione
	private byte[] currentFotoSacchetto = null;
	private String currentFotoSacchettoContentType = null;
	private byte[] currentFotoCampione = null;
	private String currentFotoCampioneContentType = null;
	private byte[] currentCurvaGranulometrica = null;
	
	@Async
	public void importData(String importPath, String root) {
		
		String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.ITALIAN));
		String nowLoggingStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.ITALIAN));
		
		MDC.put("jobId", nowLoggingStamp); // UUID.randomUUID().toString());
		
		importRoot = root;
		
		currentPath = null;
		errorMap = new HashMap<String,List<String>>();
		currentFotoSacchetto = null;
		currentFotoSacchettoContentType = null;
		currentFotoCampione = null;
		currentFotoCampioneContentType = null;
		currentCurvaGranulometrica = null;
		
		log.info("# ITG-LAB - Rapporto importazione dati da file Excel in data {}", now);
		//log.info("**DATA/ORA**: {}", now);
		log.info("**CARTELLA DI IMPORTAZIONE**: `{}`", importPath);
		log.info("## File XLSM presenti nella cartella: `{}`", importPath);
		log.info("```");
		String tree = printDirectoryTree(new File(importPath));
		log.info(tree);
		log.info("```");
		
		// controllo cartella e file contenuti, se non esiste cartella o non ci sono file invio errore
		List<Path> filePathList = new ArrayList<>();
		try {
			Files.walkFileTree(Paths.get(importPath), new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
					if (e == null) {
						DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{xlsm,XLSM}");

						//log.trace("* Cartella: `{}`", dir);
						for (Path entry : stream) {
							if(!entry.toString().contains("CheckboxTranslator") && !entry.toString().startsWith("~"))
								filePathList.add(entry);
							//log.debug("\t* {}", entry);
						}

						return FileVisitResult.CONTINUE;
					} else {
						// directory iteration failed
						throw e;
					}
				}
			});
		} catch (IOException e1) {
			throw new CustomParameterizedException("Import error", "Errore nella scansione della cartella");
		}
		
		if(filePathList.size() == 0) {
			throw new CustomParameterizedException("Import error", "Nessun file xlsm contenuto nella cartella " + importPath);
		}
		
		List<String> failedImports = new ArrayList<>();
		List<String> succededImports = new ArrayList<>();
		
		
		/*
		 * Iterazione su ogni file xlsm
		 */
		for (Path path : filePathList) {
			currentPath = path.toString().replace(importRoot, "");;
			
			log.info("## Importazione file `{}`", path.getFileName());
			log.info("Percorso: `{}`", currentPath);
			
			XSSFWorkbook workbook = null;
			FileInputStream file = null;
			FileOutputStream fileOut = null;
			
			try {
				// determinazione laboratorio
				Laboratorio laboratorio = detectLaboratorio(path);
				if(laboratorio == null) {
					throw new CustomParameterizedException("Import error", "Impossibile stabilire il laboratorio di appartenenza");
				}
				
				file = new FileInputStream(new File(path.toUri()));
				workbook = new XSSFWorkbook(file);
				
				//controllo se il file è già stato importato
				Cassetta existingCassetta = checkImported(workbook);
				if(existingCassetta != null) {
					log.info("Il file {} è già stato importato: cassetta `{}`, id `{}`, note `{}`", currentPath,
							existingCassetta.getCodiceCassetta(), existingCassetta.getId(), existingCassetta.getNote());
					continue;
				}
				
				// determinazione consegna
				Consegna consegna = checkConsegna(laboratorio, workbook);
				if(consegna == null) {
					log.error("Impossibile determinare la consegna relativa al file {}", currentPath);
					failedImports.add(currentPath);
					continue;
				}
				
				// cassetta
				Cassetta cassetta = null;
				try {
					cassetta = insertCassetta(consegna, workbook);
				} catch(Exception e) {
					log.error("Impossibile estrarre i dati della cassetta relativa al file {}", currentPath);
					
					ArrayList<String> errorList = new ArrayList<String>();
					errorList.add("Impossibile estrarre i dati della cassetta");
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					errorList.add(sw.toString());
					
					failedImports.add(currentPath);
					
					if(errorMap.get(currentPath) == null) {
						errorMap.put(currentPath, errorList);
					} else {
						for(String err : errorList) {
							errorMap.get(currentPath).add(err);
						}
					}
					
					continue;
				}
				
				if(cassetta == null) {
					log.error("Errore nell'inserimento della cassetta relativa al file {}", currentPath);
					failedImports.add(currentPath);
					continue;
				}
				
				// campioni
				try{
					Campione campioneA = insertCampioneA(cassetta, workbook);
					if(campioneA != null) {
						saveFotoSacchetto(campioneA);
						saveFotoCampione(campioneA);
						saveCurvaGranulometrica(campioneA);
					}
				} catch(Exception e) {
					log.error("Impossibile estrarre i dati del campione A relativo al file {}", currentPath);
					
					ArrayList<String> errorList = new ArrayList<String>();
					errorList.add("Impossibile estrarre i dati del campione A");
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					errorList.add(sw.toString());
					
					if(errorMap.get(currentPath) == null) {
						errorMap.put(currentPath, errorList);
					} else {
						for(String err : errorList) {
							errorMap.get(currentPath).add(err);
						}
					}
				}
					
					
				try{
					Campione campioneA1 = insertCampioneA1(cassetta, workbook);
					if(campioneA1 != null) {
						saveFotoSacchetto(campioneA1);
						saveFotoCampione(campioneA1);
						saveCurvaGranulometrica(campioneA1);
					}
				} catch(Exception e) {
					log.error("Impossibile estrarre i dati del campione A1 relativo al file {}", currentPath);
					
					ArrayList<String> errorList = new ArrayList<String>();
					errorList.add("Impossibile estrarre i dati del campione A1");
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					errorList.add(sw.toString());
					
					if(errorMap.get(currentPath) == null) {
						errorMap.put(currentPath, errorList);
					} else {
						for(String err : errorList) {
							errorMap.get(currentPath).add(err);
						}
					}
				}
					
				try{	
					Campione campioneB = insertCampioneB(cassetta, workbook);
					if(campioneB != null) {
						saveFotoSacchetto(campioneB);
						saveFotoCampione(campioneB);
					}
				} catch(Exception e) {
					log.error("Impossibile estrarre i dati del campione B relativo al file {}", currentPath);
					
					ArrayList<String> errorList = new ArrayList<String>();
					errorList.add("Impossibile estrarre i dati del campione B");
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					errorList.add(sw.toString());
					
					if(errorMap.get(currentPath) == null) {
						errorMap.put(currentPath, errorList);
					} else {
						for(String err : errorList) {
							errorMap.get(currentPath).add(err);
						}
					}
				}
					
				try{	
					Campione campioneC = insertCampioneC(cassetta, workbook);
					if(campioneC != null) {
						saveFotoSacchetto(campioneC);
						saveFotoCampione(campioneC);
					}
				} catch(Exception e) {
					log.error("Impossibile estrarre i dati del campione C relativo al file {}", currentPath);
					
					ArrayList<String> errorList = new ArrayList<String>();
					errorList.add("Impossibile estrarre i dati del campione C");
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					errorList.add(sw.toString());
					
					if(errorMap.get(currentPath) == null) {
						errorMap.put(currentPath, errorList);
					} else {
						for(String err : errorList) {
							errorMap.get(currentPath).add(err);
						}
					}
				}
					
				file.close();
				
				fileOut = new FileOutputStream(new File(path.toUri()));
		
				// salvo l'idCassetta nel file
				XSSFSheet rapportoSheet = workbook.getSheet("Rapporto di prova");
				
				Row row = rapportoSheet.getRow(72);
				XSSFCell cell = (XSSFCell) row.createCell(10);
				cell.setCellValue("idCassetta");
				
				cell = (XSSFCell) row.createCell(11);
				cell.setCellValue(cassetta.getId());
				
				workbook.write(fileOut);
				fileOut.flush();
				fileOut.close();
				
				succededImports.add(currentPath);
				
			} catch (Exception e) {
				log.error("Errore nell'apertura del file {}", currentPath);
				
				ArrayList<String> errorList = new ArrayList<String>();
				errorList.add("Errore nell'apertura del file");
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				errorList.add(sw.toString());
				
				failedImports.add(currentPath);
				
				if(errorMap.get(currentPath) == null) {
					errorMap.put(currentPath, errorList);
				} else {
					for(String err : errorList) {
						errorMap.get(currentPath).add(err);
					}
				}
			}

			
		}
		
		if(errorMap.size() > 0) {
			log.info("## Possibili problemi riscontrati");
			
			for(String path : errorMap.keySet()) {
				log.info("### File: {}", path);
				
				for(String error : errorMap.get(path)) {
					log.info("* {}", error);
				}
			}
		}
		
		log.info("## Riepilogo importazione");
		String nowEnd = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.ITALIAN));
		log.info("**Importazione conclusa il {}**", nowEnd);
		log.info("**Importazione effettuata per {} file.**", succededImports.size());
		log.info("***Importazione fallita per {} file.***", failedImports.size());
		
		if(failedImports.size() > 0) {
			log.info("### Importazioni fallite");
			for(String path : failedImports) {
				log.info("* `{}`", path);
				for(String err : errorMap.get(path)) {
					log.info("\t* {}", err);
				}
			}
		}
		
		MDC.remove("jobId");
		
		return;
	}
	
	private void saveFotoSacchetto(Campione campione) {
		byte[] foto = resizeFoto(currentFotoSacchetto, currentFotoSacchettoContentType);
		campione.setFotoSacchetto(foto);
		campione.setFotoSacchettoContentType(currentFotoSacchettoContentType);
		campioneRepository.save(campione);
	}
	private void saveFotoCampione(Campione campione) {
		byte[] foto = resizeFoto(currentFotoCampione, currentFotoCampioneContentType);
		campione.setFotoCampione(foto);
		campione.setFotoCampioneContentType(currentFotoCampioneContentType);
		campioneRepository.save(campione);
	}
	private void saveCurvaGranulometrica(Campione campione) {
		campione.setCurva(currentCurvaGranulometrica);
		campione.setCurvaContentType("image/png");
		campioneRepository.save(campione);
	}
	
	
	@SuppressWarnings("deprecation")
	public Cassetta checkImported(XSSFWorkbook workbook) {
		XSSFSheet rapportoSheet = workbook.getSheet("Rapporto di prova");
		
		Long idCassetta = null;
		Cassetta cassetta;
		CellReference ref = new CellReference("L73");
		Row row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && c.getCellTypeEnum() == CellType.NUMERIC) {
				log.debug("File già importato, id cassetta memorizzato nel file: {}", c.getNumericCellValue());
				idCassetta = (long) c.getNumericCellValue();
			}
		}
		
		if(idCassetta != null) {
			cassetta = cassettaRepository.findOne(idCassetta);
			return cassetta;
		}
		
		log.debug("Non è presente nel database una cassetta con id: {}, per cui verrà effettuata l'importazione ", idCassetta);
		
		return null;
	}
	
	
	public Laboratorio detectLaboratorio(Path path) {
		log.debug("Determinazione laboratorio di appartenenza...");
		Laboratorio laboratorio = null;
				
		for (int i = 0; i < path.getNameCount(); ++i) {
			String subDir = path.getName(i).toString().toUpperCase();
			
			if(subDir.contains("IAMC")) {
				if(subDir.contains("TP")) {
					laboratorio = laboratorioRepository.findOne(11L); //IAMC-TP
				} else { 
					laboratorio = laboratorioRepository.findOne(12L); //IAMC-ME
				}
			} else if(subDir.contains("IDPA")) {
				laboratorio = laboratorioRepository.findOne(3L);
			} else if(subDir.contains("IGAG")) {
				if(subDir.contains("RM")) {
					laboratorio = laboratorioRepository.findOne(1L);
				} else { 
					laboratorio = laboratorioRepository.findOne(2L);
				}
			} else if(subDir.contains("IGG")) {
				if(subDir.contains("PD")) {
					laboratorio = laboratorioRepository.findOne(5L);
				} else { 
					laboratorio = laboratorioRepository.findOne(4L);
				}
			} else if(subDir.contains("IMAA")) {
				laboratorio = laboratorioRepository.findOne(8L);
			} else if(subDir.contains("IRPI")) {
				if(subDir.contains("BA")) {
					laboratorio = laboratorioRepository.findOne(10L);
				} else if(subDir.contains("CS")) {
					laboratorio = laboratorioRepository.findOne(9L);
				} else {
					laboratorio = laboratorioRepository.findOne(7L);
				}
			} else if(subDir.contains("ISMAR")) {
				laboratorio = laboratorioRepository.findOne(6L);
			}
		}
		
		log.info("Laboratorio di appartenenza: **{}**", laboratorio.getIstituto());
		
		return laboratorio;
		
	}
	
	@SuppressWarnings("deprecation")
	public Consegna checkConsegna(Laboratorio laboratorio, XSSFWorkbook workbook) {
		List<String> errorList = new ArrayList<String>();
		
		log.info("### Determinazione consegna");
		// Get first sheet from the workbook
		XSSFSheet accettazioneSheet = workbook.getSheet("Dati input accettazione");
		XSSFSheet rapportoSheet = workbook.getSheet("Rapporto di prova");

		// data ricezione
		CellReference ref = new CellReference("E15");
		LocalDate date = null;
		Row row = accettazioneSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			
			if(c != null && (c.getCellTypeEnum() == CellType.NUMERIC)) {
				if (DateUtil.isCellDateFormatted(c)) {
	                log.debug("Data di consegna estratta dal file: {}", c.getDateCellValue().toString());
	                date = c.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	                log.debug("Data di consegna convertita in LocalDate: {}", date.toString());
	            }
			} else if(c != null && (c.getCellTypeEnum() == CellType.STRING)) {
				log.debug("La data è in formato testuale e va convertita: {}", c.getStringCellValue());
				DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALIAN);
				DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd/MM/yy", Locale.ITALIAN);
				
				try {
					date = LocalDate.parse(c.getStringCellValue(), formatter1);
				} catch (DateTimeParseException e) {
					try {
						date = LocalDate.parse(c.getStringCellValue(), formatter2);
					} catch (Exception z) {
						date = null;
					}
				}
				
			} else {
				log.debug("Impossibile determinare la data di consegna dalla cella E15 del foglio di accettazione");
			}
		}
		
		if(date == null) {
			ref = new CellReference("F16");
			row = rapportoSheet.getRow(ref.getRow());
			if (row != null) {
				Cell c = row.getCell(ref.getCol());
				
				if(c != null && (c.getCellTypeEnum() == CellType.NUMERIC)) {
					if (DateUtil.isCellDateFormatted(c)) {
		                log.debug("Data di consegna estratta dal file: {}", c.getDateCellValue().toString());
		                date = c.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		                log.debug("Data di consegna convertita in LocalDate: {}", date.toString());
		            }
				} else if(c != null && (c.getCellTypeEnum() == CellType.STRING)) {
					log.debug("La data è in formato testuale e va convertita: {}", c.getStringCellValue());
					DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALIAN);
					DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd/MM/yy", Locale.ITALIAN);
					
					try {
						date = LocalDate.parse(c.getStringCellValue(), formatter1);
					} catch (DateTimeParseException e) {
						try {
							date = LocalDate.parse(c.getStringCellValue(), formatter2);
						} catch (Exception z) {
							date = null;
						}
					}
				
				} else {
					log.debug("Impossibile determinare la data di consegna dalla cella F16 del foglio Rapporto di prova");
				}
			}
		}
		
		if(date == null) {
			log.error("Impossibile determinare la data di consegna");
			errorList.add("Impossibile determinare la data di consegna");
			errorMap.put(currentPath, errorList);
			return null;
		}
		
		Consegna consegna = consegnaRepository.findByLaboratorioAndDataConsegna(laboratorio, date);
		if(consegna != null) {
			log.info("Consegna già esistente nel database, data: **{}**, laboratorio: **{}**, id: **{}**", date, laboratorio.getIstituto(), consegna.getId());
			return consegna;
		} else {
			log.info("Non è presente nel database una consegna in data: {} per il laboratorio: {}", date, laboratorio.getIstituto());

		}
		
		// creo e inserisco la nuova consegna
		consegna = new Consegna();
		consegna.setDataConsegna(date);
		consegna.setLaboratorio(laboratorio);
		
		// protocollo accettazione
		DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(0);
		String protocollo = null;
		ref = new CellReference("E5");
		row = accettazioneSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c.getCellTypeEnum() == CellType.NUMERIC) {
				log.trace("Il campo è numerico (double), presumo sia senza virgola: {}", df.format(c.getNumericCellValue()));
				protocollo = df.format(c.getNumericCellValue());
			} else if(c.getCellTypeEnum() == CellType.STRING) {
				protocollo = c.getStringCellValue();
			} else {
				log.info("* Protocollo accettazione non presente o impossibile da determinare!");
			}
		}
		if(protocollo != null) {
			log.info("* Protocollo accettazione: {}", protocollo);
		}
		consegna.setNumProtocolloAccettazione(protocollo);
		
		consegna = consegnaRepository.save(consegna);
		
		log.info("Inserita nel database una nuova consegna relativa alla data: **{}** per il laboratorio: **{}**, con id: **{}**", date, laboratorio.getIstituto(), consegna.getId());
		
		return consegna;
	}
	
	@SuppressWarnings("deprecation")
	public Cassetta insertCassetta(Consegna consegna, XSSFWorkbook workbook) {
		List<String> errorList = new ArrayList<String>();
		
		log.info("### Creazione e inserimento cassetta");
		// Foglio accettazione
		XSSFSheet accettazioneSheet = workbook.getSheet("Dati input accettazione");
		XSSFSheet rapportoSheet = workbook.getSheet("Rapporto di prova");
		
		DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(0);
        
        DecimalFormat df3 = new DecimalFormat("0.#########");
        //df2.setMaximumFractionDigits(2);
		
        // formato per le coordinate in gradi decimali
        DecimalFormat dfGps = new DecimalFormat("#");
        dfGps.setMaximumFractionDigits(4);
        
		// odl
		String odl = null;
		CellReference ref = new CellReference("E11");
		Row row = accettazioneSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			
			if(c.getCellTypeEnum() == CellType.NUMERIC) {
				log.trace("Il campo è numerico (double), presumo sia senza virgola: {}", df.format(c.getNumericCellValue()));
				odl = df.format(c.getNumericCellValue());
			} else if(c.getCellTypeEnum() == CellType.STRING) {
				odl = c.getStringCellValue();
			} else {
				log.warn("Impossibile determinare ODL!");
			}
		}
		log.info("* ODL (ODS): **{}**", odl);
		
		// mslink (rifGeografo)
		String mslink = null;
		ref = new CellReference("I15");
		row = accettazioneSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			
			if(c.getCellTypeEnum() == CellType.NUMERIC) {
				log.trace("Il campo è numerico (double), presumo sia senza virgola: {}", df.format(c.getNumericCellValue()));
				mslink = df.format(c.getNumericCellValue());
			} else if(c.getCellTypeEnum() == CellType.STRING) {
				mslink = c.getStringCellValue();
			} else {
				log.warn("Impossibile determinare MSLINK/RIFGEOGRAFO!");
			}
		}
		log.info("* MSLINK (RIF GEOGRAFO): **{}**", mslink);
		
		// prioritario
		Boolean prioritario = null;
		ref = new CellReference("E3");
		row = accettazioneSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				switch(c.getStringCellValue()) {
					case "SI":
						prioritario = true;
						log.debug("La cassetta è indicata come prioritaria");
						break;
					case "NO":
						prioritario = false;
						log.debug("La cassetta è indicata come non prioritaria");
						break;
					default:
						log.debug("Prioritario non è nè SI nè NO");
				}
			}
		}
		log.info("* Prioritario: **{}**", prioritario);
		
		// tecnico responsabile
		String tecnicoResponsabile = "";
		ref = new CellReference("E7");
		row = accettazioneSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK) && (c.getCellTypeEnum() == CellType.STRING)) {
				tecnicoResponsabile = c.getStringCellValue();
			}
		}
		log.info("* Tecnico responsabile: **{}**", tecnicoResponsabile);
		
		// impresa appaltatrice
		String impresaAppaltatrice = "";
		ref = new CellReference("E9");
		row = accettazioneSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK) && (c.getCellTypeEnum() == CellType.STRING)) {
				impresaAppaltatrice = c.getStringCellValue();
			}
		}
		log.info("* Impresa appaltatrice: **{}**", impresaAppaltatrice);
		
		// incaricatoAppaltatore
		String incaricatoAppaltatore = "";
		ref = new CellReference("E13");
		row = accettazioneSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK) && (c.getCellTypeEnum() == CellType.STRING)) {
				incaricatoAppaltatore = c.getStringCellValue();
			}
		}
		log.info("* Incaricato appaltatore: **{}**", incaricatoAppaltatore);
		
		// centroOperativo
		String centroOperativo = "";
		ref = new CellReference("I5");
		row = accettazioneSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK) && (c.getCellTypeEnum() == CellType.STRING)) {
				centroOperativo = c.getStringCellValue();
			}
		}
		log.info("* Centro operativo: **{}**", centroOperativo);
		
		// denomCantiere
		String denomCantiere = "";
		ref = new CellReference("I7");
		row = accettazioneSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK) && (c.getCellTypeEnum() == CellType.STRING)) {
				denomCantiere = c.getStringCellValue();
			}
		}
		log.info("* Denominazione cantiere: **{}**", denomCantiere);
		
		/* 
		 * ************
		 * Codice ISTAT
		 * ************
		 */
		// regione, provincia, comune (ricerca con elasticsearch) comune: I9, prov: I13, regione: I11
		String regione = "";
		String provincia = "";
		String comune = "";
		CodiceIstat codiceIstat = null;
		
		ref = new CellReference("I11");
		row = accettazioneSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK) && (c.getCellTypeEnum() == CellType.STRING)) {
				regione = c.getStringCellValue();
			} else {
				ref = new CellReference("D39");
				row = rapportoSheet.getRow(ref.getRow());
				c = row.getCell(ref.getCol());
				if(c != null && (c.getCellTypeEnum() != CellType.BLANK) && (c.getCellTypeEnum() == CellType.STRING)) {
					regione = c.getStringCellValue();
				} else {
					log.error("Impossibile determinare la regione di provenienza");
					errorList.add("Impossibile determinare la regione di provenienza");
					errorMap.put(currentPath, errorList);
				}
			}
		}
		log.info("* Regione indicata nel file Excel: **{}**", regione);
		
		ref = new CellReference("I13");
		row = accettazioneSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK) && (c.getCellTypeEnum() == CellType.STRING)) {
				provincia = c.getStringCellValue();
			} else {
				ref = new CellReference("D41");
				row = rapportoSheet.getRow(ref.getRow());
				c = row.getCell(ref.getCol());
				if(c != null && (c.getCellTypeEnum() != CellType.BLANK) && (c.getCellTypeEnum() == CellType.STRING)) {
					provincia = c.getStringCellValue();
				} else {
					log.error("Impossibile determinare la provincia di provenienza");
					errorList.add("Impossibile determinare la provincia di provenienza");
					errorMap.put(currentPath, errorList);
				}
			}
		}
		log.info("* Provincia indicata nel file Excel: **{}**", provincia);
		
		ref = new CellReference("I9");
		row = accettazioneSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK) && (c.getCellTypeEnum() == CellType.STRING)) {
				comune = c.getStringCellValue();
			} else {
				ref = new CellReference("D42");
				row = rapportoSheet.getRow(ref.getRow());
				c = row.getCell(ref.getCol());
				if(c != null && (c.getCellTypeEnum() != CellType.BLANK) && (c.getCellTypeEnum() == CellType.STRING)) {
					comune = c.getStringCellValue();
				} else {
					log.error("Impossibile determinare il comune di provenienza");
					errorList.add("Impossibile determinare il comune di provenienza");
					errorMap.put(currentPath, errorList);
				}
			}
		}
		log.info("* Comune indicato nel file Excel: **{}**", comune);
		
		// preparo le stringhe
		if(!regione.isEmpty())
			regione = regione.split(" ")[0];
		if(!provincia.isEmpty())
			provincia = provincia.split(" ")[0];
		
		//bat-provincia
		if(provincia.equalsIgnoreCase("bat")) {
			provincia = "Barletta-Andria-Trani";
		}
		
		if(provincia.equalsIgnoreCase("mc")) {
			provincia = "Macerata";
		}
		
		// eccezione "albissola marina / albisola superiore"
		if(comune.equalsIgnoreCase("Albisola Marina")) {
			comune = "Albissola Marina"; 
		}
		
		// eccezione "cerrina"
		if(comune.startsWith("Cerrina")) {
			provincia = "Alessandria"; 
		}
		
		if(comune.equalsIgnoreCase("Jesi")) {
			provincia = "Ancona"; 
		}
		
		if(comune.toLowerCase().startsWith("serrafidalco")) {
			comune = "Serradifalco";
		}
		
		if(comune.toLowerCase().startsWith("villadoro")) {
			comune = "Nicosia"; 
		}
		
		if(comune.equalsIgnoreCase("borsconero")) {
			comune = "Bosconero"; 
		}
		
		List<String> comuneParts = new ArrayList<String>();
		List<String> comunePartsTemp = new ArrayList<String>();
		if(comune.contains("_")) {
			comunePartsTemp = Arrays.asList(comune.split("_"));
		} else {
			comunePartsTemp = Arrays.asList(comune.split(" "));
		}
		//comuneParts.addAll(comunePartsTemp);
		
		//elimino numeri e robe strane
		for(String p : comunePartsTemp) {
			String newP = p.replaceAll("\\d+", "");
			//newP = newP.replaceAll("\\ ", "");
			newP = newP.replaceAll("\\.", "");
			newP = newP.trim();
			
			if(!newP.isEmpty() && !newP.equalsIgnoreCase("id") && (newP.length() > 1 || newP.equalsIgnoreCase("s")))
				comuneParts.add(newP);
		}
		
//		for(String p : comuneParts) {
//			if(p.matches(".*\\d+.*") || //la stringa contiene un numero
//					p.equalsIgnoreCase("id") ||
//					p.toLowerCase().startsWith("id_")) {
//				comuneParts.remove(p);
//			}
//		}
		
		String comunePart = "";
		if((comuneParts.get(0).equalsIgnoreCase("san") ||
				comuneParts.get(0).equalsIgnoreCase("s.") ||
				comuneParts.get(0).equalsIgnoreCase("s")) && comuneParts.size() > 1) {
			comunePart = comuneParts.get(1);
		} else {
			comunePart = comuneParts.get(0);
		}
		
		// tento di individuare il codice istat con il solo nome del comune
		List<CodiceIstat> codiceIstatList = codiceIstatRepository.findByComuneIgnoreCaseContaining(comune);
		
		if(codiceIstatList.size() == 1) {
			codiceIstat = codiceIstatList.get(0);
		} else {
			log.debug("La ricerca per Comune: {}, ha prodotto {} risultati:", comune, codiceIstatList.size());
			for(CodiceIstat c : codiceIstatList) {
				log.debug("\t{}, {}, {}", c.getRegione(), c.getProvincia(), c.getComune());
			}
			
			// **1** tento di individuare il codice istat con il solo nome del comune (parte)
			codiceIstatList = codiceIstatRepository.findByComuneIgnoreCaseContaining(comunePart);
			if(codiceIstatList.size() == 1) {
				codiceIstat = codiceIstatList.get(0);
			} else {
				log.debug("La ricerca per Comune: {}, ha prodotto {} risultati:", comunePart, codiceIstatList.size());
				for(CodiceIstat c : codiceIstatList) {
					log.debug("\t{}, {}, {}", c.getRegione(), c.getProvincia(), c.getComune());
				}
				
				// **2** tento di individuare il codice istat con regione, provincia, comune
				codiceIstatList = codiceIstatRepository.findByRegioneIgnoreCaseContainingAndProvinciaIgnoreCaseContainingAndComuneIgnoreCaseContaining(regione, provincia, comune);
				
				if(codiceIstatList.size() == 1) {
					codiceIstat = codiceIstatList.get(0);
				} else {
					log.debug("La ricerca per Regione: {}, Provincia: {}, Comune: {}, ha prodotto {} risultati:", regione, provincia, comune, codiceIstatList.size());
					for(CodiceIstat c : codiceIstatList) {
						log.debug("\t{}, {}, {}", c.getRegione(), c.getProvincia(), c.getComune());
					}
					
					// **3** tento di individuare il codice istat con regione, provincia, comune (parte)
					codiceIstatList = codiceIstatRepository.findByRegioneIgnoreCaseContainingAndProvinciaIgnoreCaseContainingAndComuneIgnoreCaseContaining(regione, provincia, comunePart);
					if(codiceIstatList.size() == 1) {
						codiceIstat = codiceIstatList.get(0);
					} else  {
						log.debug("La ricerca per Regione: {}, Provincia: {}, Comune: {}, ha prodotto {} risultati:", regione, provincia, comunePart, codiceIstatList.size());
						for(CodiceIstat c : codiceIstatList) {
							log.debug("\t{}, {}, {}", c.getRegione(), c.getProvincia(), c.getComune());
						}
						
						// **4** cerco con regione, provincia, comune (senza containing)
						codiceIstatList = codiceIstatRepository.findByRegioneIgnoreCaseContainingAndProvinciaIgnoreCaseContainingAndComuneIgnoreCase(regione, provincia, comune);
						if(codiceIstatList.size() == 1) {
							codiceIstat = codiceIstatList.get(0);
						} else {
							log.debug("La ricerca per Regione: {}, Provincia: {}, Comune: {} (senza containing), ha prodotto {} risultati:", regione, provincia, comune, codiceIstatList.size());
							for(CodiceIstat c : codiceIstatList) {
								log.debug("\t{}, {}, {}", c.getRegione(), c.getProvincia(), c.getComune());
							}
							
							// **5** cerco con regione, provincia, comune (senza containing) (parte)
							codiceIstatList = codiceIstatRepository.findByRegioneIgnoreCaseContainingAndProvinciaIgnoreCaseContainingAndComuneIgnoreCase(regione, provincia, comunePart);
							if(codiceIstatList.size() == 1) {
								codiceIstat = codiceIstatList.get(0);
							}
						}
					}
					
				} 
			}
		}
		
		if(codiceIstat == null) {
			log.error("Non è stato possibile individuare il codice Istat per Regione: **{}**, Provincia: **{}**, Comune: **{}**", regione, provincia, comune);
			errorList.add("Non è stato possibile individuare il codice Istat per Regione: " + regione + ", Provincia: " + provincia + ", Comune: " + comune);
			errorMap.put(currentPath, errorList);
			return null;
		}
		
		log.info("* Codice Istat rilevato: **{}, {}, {}, {}**", codiceIstat.getCodiceIstat(), codiceIstat.getComune(), codiceIstat.getProvincia(), codiceIstat.getRegione());
		
		/*
		 * **********
		 * Coordinate
		 * **********
		 */
		String coordGpsNord = null;
		ref = new CellReference("M5");
		row = accettazioneSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				if(c.getCellTypeEnum() == CellType.NUMERIC) {
					log.trace("Il campo è numerico (double), presumo sia senza virgola: {}", dfGps.format(c.getNumericCellValue()));
					coordGpsNord = dfGps.format(c.getNumericCellValue());
				} else if(c.getCellTypeEnum() == CellType.STRING) {
					coordGpsNord = c.getStringCellValue().replace("''", "\"");
				} else {
					log.warn("Impossibile importare COORDINATE GPS NORD!");
					errorList.add("Impossibile importare COORDINATE GPS NORD!");
				}
			}
		}
		log.info("* Coordinate GPS Nord: **{}**", coordGpsNord);
		
		String coordGpsEst = null;
		ref = new CellReference("M7");
		row = accettazioneSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				if(c.getCellTypeEnum() == CellType.NUMERIC) {
					log.trace("Il campo è numerico (double), presumo sia senza virgola: {}", dfGps.format(c.getNumericCellValue()));
					coordGpsEst = dfGps.format(c.getNumericCellValue());
				} else if(c.getCellTypeEnum() == CellType.STRING) {
					coordGpsEst = c.getStringCellValue().replace("''", "\"");
				} else {
					log.warn("Impossibile importare COORDINATE GPS EST!");
					errorList.add("Impossibile importare COORDINATE GPS EST!");
				}
			}
		}
		log.info("* Coordinate GPS Est: **{}**", coordGpsEst);
		
		
		/* 
		 * **********
		 * Data scavo
		 * **********
		 */
		ref = new CellReference("M9");
		LocalDate dataScavodate = null;
		row = accettazioneSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			
			if(c != null && c.getCellTypeEnum() == CellType.NUMERIC) {
				if (DateUtil.isCellDateFormatted(c)) {
	                log.debug("Data di esecuzione dello scavo estratta dal file: {}", c.getDateCellValue().toString());
	                dataScavodate = c.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	                log.debug("Data di esecuzione dello scavo convertita in LocalDate: {}", dataScavodate.toString());
	            }
			} else if(c != null && c.getCellTypeEnum() == CellType.STRING) {
				log.debug("La data esecuzione dello scavo è in formato testuale e va convertita: {}", c.getStringCellValue());
				DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALIAN);
				DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd/MM/yy", Locale.ITALIAN);
				
				try {
					dataScavodate = LocalDate.parse(c.getStringCellValue(), formatter1);
 				} catch (DateTimeParseException e) {
 					log.debug("La data non è in formato dd/MM/yyyy");
 				}
 				
 				try {
 					dataScavodate = LocalDate.parse(c.getStringCellValue(), formatter2);
 				} catch (DateTimeParseException e) {
 					log.debug("La data non è in formato dd/MM/yy");
 				}
 				
 				if(dataScavodate != null)
 					log.debug("Data di esecuzione dello scavo testuale convertita in LocalDate: {}", dataScavodate.toString());
				
				
			} else {
				log.warn("Impossibile determinare la data di esecuzione dello scavo!");
				errorList.add("Impossibile determinare la data di esecuzione dello scavo");
			}
		}
		if(dataScavodate != null)
			log.info("* Data scavo: **{}**", dataScavodate);
		
		//num campioni
		String numCampioni = null;
		ref = new CellReference("I17");
		row = accettazioneSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				if(c.getCellTypeEnum() == CellType.NUMERIC) {
					numCampioni = df.format(c.getNumericCellValue());
				} else if(c.getCellTypeEnum() == CellType.STRING) {
					numCampioni = c.getStringCellValue();
				} else {
					log.warn("Impossibile importare numero campioni");
					//errorList.add("Impossibile importare COORDINATE GPS EST!");
				}
			}
		}
		log.info("* Numero campioni: **{}**", numCampioni);
		
		// stato cassetta (da checkbox)
		boolean trueFalse = false;
		StatoContenitore statoContenitore = null;
		for(int j=1; j<5; j++) {
			ref = new CellReference("L" + j);
			row = rapportoSheet.getRow(ref.getRow());
			if (row != null) {
				Cell c = row.getCell(ref.getCol());
				if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
					trueFalse = c.getBooleanCellValue();
				}
			}
			if (trueFalse) {
				if(j==1)
					statoContenitore = StatoContenitore.INTEGRO_CONTENUTO_INTEGRO;
				if(j==2)
					statoContenitore = StatoContenitore.INTEGRO_CONTENUTO_DANNEGGIATO;
				if(j==3)
					statoContenitore = StatoContenitore.DANNEGGIATO_CONTENUTO_INTEGRO;
				if(j==4)
					statoContenitore = StatoContenitore.DANNEGGIATO_CONTENUTO_DANNEGGIATO;
				
				break;
			}
		}
		log.info("* Stato contenitore all'accettazione: **{}**", statoContenitore);
		
		ref = new CellReference("L5");
		Boolean contenutoInquinato = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				contenutoInquinato = trueFalse;
			}
		}
		log.info("* Contenuto inquinato: **{}**", contenutoInquinato);
		
		ref = new CellReference("L6");
		Boolean conglomeratoBituminoso = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				conglomeratoBituminoso = trueFalse;
			}
		}
		log.info("* Presenza conglomerato bituminoso: **{}**", conglomeratoBituminoso);
		
		// presenza campioni
		ref = new CellReference("L7");
		Boolean presenzaCampione10 = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				presenzaCampione10 = trueFalse;
			}
		}
		ref = new CellReference("L8");
		Boolean presenzaCampione11 = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				presenzaCampione11 = trueFalse;
			}
		}
		ref = new CellReference("L9");
		Boolean presenzaCampione12 = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				presenzaCampione12 = trueFalse;
			}
		}
		ref = new CellReference("L10");
		Boolean presenzaCampione13 = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				presenzaCampione13 = trueFalse;
			}
		}
		ref = new CellReference("L11");
		Boolean presenzaCampione14 = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				presenzaCampione14 = trueFalse;
			}
		}
		ref = new CellReference("L12");
		Boolean presenzaCampione15 = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				presenzaCampione15 = trueFalse;
			}
		}
		ref = new CellReference("L13");
		Boolean presenzaCampione16 = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				presenzaCampione16 = trueFalse;
			}
		}
		ref = new CellReference("L14");
		Boolean presenzaCampione17 = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				presenzaCampione17 = trueFalse;
			}
		}
		log.info("* Sigle campioni presenti: \n\t* 10: {}\n\t* 11: {}\n\t* 12: {}\n\t* 13: {}\n\t* 14: {}\n\t* 15: {}\n\t* 16: {}\n\t* 17: {}\n", 
				presenzaCampione10, presenzaCampione11, presenzaCampione12, presenzaCampione13, presenzaCampione14, presenzaCampione15, presenzaCampione16, presenzaCampione17);
		
		// foto cassetta
		byte[] fotoContenitore = null;
		String fotoContenitoreContentType = null;
		
		// http://stackoverflow.com/a/24865123
		XSSFDrawing drawing = rapportoSheet.createDrawingPatriarch();
        for (XSSFShape shape : drawing.getShapes()) {
            if (shape instanceof XSSFPicture) {
                XSSFPicture picture = (XSSFPicture) shape;
                XSSFPictureData xssfPictureData = picture.getPictureData();
                
                if(xssfPictureData == null)
                	continue;
                
                ClientAnchor anchor = picture.getPreferredSize();
                int row1 = anchor.getRow1();
//                int row2 = anchor.getRow2();
//                int col1 = anchor.getCol1();
//                int col2 = anchor.getCol2();
                
                if (row1 > 20 && row1 < 35) { //foto cassetta
                	fotoContenitoreContentType = "image/" + xssfPictureData.suggestFileExtension();
					log.debug("Foto contenitore tipo: {}", fotoContenitoreContentType);
			        fotoContenitore = xssfPictureData.getData();
			        log.debug("Foto contenitore length: {}", fotoContenitore.length);
			        
                }
                
            }
        }
		
 		String sismicitaLocale = null;
 		ref = new CellReference("C562");
 		row = rapportoSheet.getRow(ref.getRow());
 		if (row != null) {
 			Cell c = row.getCell(ref.getCol());
 			
 			if(c.getCellTypeEnum() == CellType.NUMERIC) {
 				sismicitaLocale = df3.format(c.getNumericCellValue());
 				//sismicitaLocale = new BigDecimal(c.getNumericCellValue()).toPlainString();
 			} else if(c.getCellTypeEnum() == CellType.STRING) {
 				sismicitaLocale = c.getStringCellValue();
 			} else {
 				log.trace("Impossibile determinare sismicità locale!");
 			}
 		}
 		log.info("* Sismicità locale: **{}**", sismicitaLocale);
 		
 		String valAccelerazione = null;
 		ref = new CellReference("F564");
 		row = rapportoSheet.getRow(ref.getRow());
 		if (row != null) {
 			Cell c = row.getCell(ref.getCol());
 			
 			if(c.getCellTypeEnum() == CellType.NUMERIC) {
 				valAccelerazione = df3.format(c.getNumericCellValue());
 				//valAccelerazione = new BigDecimal(c.getNumericCellValue()).toPlainString();
 			} else if(c.getCellTypeEnum() == CellType.STRING) {
 				valAccelerazione = c.getStringCellValue().replace("≤", "<=");
 			} else {
 				log.trace("Impossibile determinare valAccelerazione!");
 			}
 		}
 		log.info("* Valore accelerazione: **{}**", valAccelerazione);
		
		Cassetta cassetta = new Cassetta();
		cassetta.setConsegna(consegna);
		cassetta.setOdl(odl);
		cassetta.setRifGeografo(mslink);
		cassetta.setPrioritario(prioritario);
		cassetta.setTecnicoItgResp(tecnicoResponsabile);
		cassetta.setImpresaAppaltatrice(impresaAppaltatrice);
		cassetta.setIncaricatoAppaltatore(incaricatoAppaltatore);
		cassetta.setCentroOperativo(centroOperativo);
		cassetta.setDenomCantiere(denomCantiere);
		cassetta.setCodiceIstat(codiceIstat);
		cassetta.setCoordGpsN(coordGpsNord);
		cassetta.setCoordGpsE(coordGpsEst);
		cassetta.setNumCampioni(numCampioni);
		cassetta.setDataScavo(dataScavodate);
		cassetta.setStatoContenitore(statoContenitore);
		cassetta.setContenutoInquinato(contenutoInquinato);
		cassetta.setConglomeratoBituminoso(conglomeratoBituminoso);
		cassetta.setPresenzaCampione10(presenzaCampione10);
		cassetta.setPresenzaCampione11(presenzaCampione11);
		cassetta.setPresenzaCampione12(presenzaCampione12);
		cassetta.setPresenzaCampione13(presenzaCampione13);
		cassetta.setPresenzaCampione14(presenzaCampione14);
		cassetta.setPresenzaCampione15(presenzaCampione15);
		cassetta.setPresenzaCampione16(presenzaCampione16);
		cassetta.setPresenzaCampione17(presenzaCampione17);
		cassetta.setMsSismicitaLocale(sismicitaLocale);
		cassetta.setMsValAccelerazione(valAccelerazione);
		cassetta.setStatoAttualeCassetta(StatoAttualeCassetta.LAVORAZIONE_TERMINATA);
		
		cassetta.setNote("Importato da file Excel " + currentPath + " il " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.ITALIAN)));
		
		Cassetta result = cassettaRepository.save(cassetta);
		
		// creazione codice cassetta
        String codiceCassetta = result.getConsegna().getLaboratorio().getIstituto() + "-" + 
        		result.getConsegna().getDataConsegna().toString().replace("-", "") + "-" + 
        		"ID" + result.getId().toString() + "-" +
        		"MSL" + (result.getRifGeografo() != null && !result.getRifGeografo().isEmpty() ? result.getRifGeografo() : "-") + "-" + 
        		"ODL" + (result.getOdl() != null && !result.getOdl().isEmpty() ? result.getOdl().replace(" ", "") : "-");
        result.setCodiceCassetta(codiceCassetta);
        result = cassettaRepository.save(result);
        
        //cassettaSearchRepository.save(result);
		
		// blob
		fotoContenitore = resizeFoto(fotoContenitore, fotoContenitoreContentType);
		result.setFotoContenitore(fotoContenitore);
		result.setFotoContenitoreContentType(fotoContenitoreContentType);
		result = cassettaRepository.save(result);
		
		if(errorList.size() > 0) {
			errorMap.put(currentPath, errorList);
		}
		
		return result;
	}
	
	private byte[] resizeFoto(byte[] foto, String contentType) {
		if(foto == null)
			return null;
		
		int IMG_WIDTH = 640;
		
		BufferedImage fotoBI = null;
		try {
			fotoBI = ImageIO.read(new ByteArrayInputStream(foto));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(fotoBI == null || (fotoBI.getWidth() <= IMG_WIDTH)) {
			return foto;
		}
		
		BufferedImage scaledImage = Scalr.resize(fotoBI, IMG_WIDTH);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String imageType = contentType.substring(contentType.indexOf("/") + 1);
	    try {
			ImageIO.write(scaledImage, imageType, baos);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return baos.toByteArray();
	}
	
	@SuppressWarnings("deprecation")
	public Campione insertCampioneA(Cassetta cassetta, XSSFWorkbook workbook) {
		List<String> errorList = new ArrayList<String>();
		
		log.info("### Creazione e inserimento campione A relativo alla cassetta {}", cassetta.getCodiceCassetta());
		
		//XSSFSheet accettazioneSheet = workbook.getSheet("Dati input accettazione");
		XSSFSheet pesaturaSheet = workbook.getSheet("Pesatura");
		XSSFSheet lavorazioneSheet = workbook.getSheet("Lavorazione");
		XSSFSheet rapportoSheet = workbook.getSheet("Rapporto di prova");
		
		DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(0);
        
        DecimalFormat df2 = new DecimalFormat("#");
        df2.setMaximumFractionDigits(2);
		
        /*
         * controllo presenza dati di lavorazione
         */
        CellReference ref = new CellReference("C106");
		Row row = rapportoSheet.getRow(ref.getRow());
		String sumMasse = null;
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if (c != null && (c.getCellTypeEnum() == CellType.NUMERIC)) {
				log.trace("Il campo è numerico (double): {}",
						df2.format(c.getNumericCellValue()));
				sumMasse = df2.format(c.getNumericCellValue());
			} else if (c != null && (c.getCellTypeEnum() == CellType.STRING)) {
				sumMasse = c.getStringCellValue();
			} else if (c != null && (c.getCellTypeEnum() == CellType.FORMULA)) { 
				switch(c.getCachedFormulaResultType()) {
	            case Cell.CELL_TYPE_NUMERIC:
	            	sumMasse = df2.format(c.getNumericCellValue());
	                break;
	            case Cell.CELL_TYPE_STRING:
	            	sumMasse = c.getStringCellValue();
	                break;
		        }
			} else {
				log.warn("Impossibile determinare sommatoria masse setacciatura!");
			}
		}
		log.debug("* Sommatoria masse setacciatura: **{}**", sumMasse);
        
        
        if(sumMasse == null || Double.valueOf(sumMasse.replace(",", ".")) < 1) {
			log.warn("Mancano i dati di setacciatura del campione, lo considero mancante e non lo inserisco nel db.");
			errorList.add("Mancano i dati di setacciatura del campione A, lo considero mancante e non lo inserisco nel db.");

			if(errorMap.get(currentPath) == null) {
				errorMap.put(currentPath, errorList);
			} else {
				for(String err : errorList) {
					errorMap.get(currentPath).add(err);
				}
			}
			
			return null;
		}
        
        
		//sigla campione
		ref = new CellReference("F1");
		row = lavorazioneSheet.getRow(ref.getRow());
		String siglaCampione = "";
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c.getCellTypeEnum() == CellType.NUMERIC) {
				log.trace("Il campo è numerico (double), presumo sia senza virgola: {}", df.format(c.getNumericCellValue()));
				siglaCampione = df.format(c.getNumericCellValue());
			} else if(c.getCellTypeEnum() == CellType.STRING) {
				siglaCampione = c.getStringCellValue();
			} else if (c.getCellTypeEnum() == CellType.FORMULA) { 
				switch(c.getCachedFormulaResultType()) {
		            case Cell.CELL_TYPE_NUMERIC:
		            	siglaCampione = df.format(c.getNumericCellValue());
		                break;
		            case Cell.CELL_TYPE_STRING:
		            	siglaCampione = c.getStringCellValue();
		                break;
		        }
			} else {
				log.warn("Impossibile determinare sigla campione!");
				errorList.add("Impossibile determinare la sigla del campione A");
			}
		}
		log.info("* Sigla campione: **{}**", siglaCampione);
		
		// data lavorazione
 		ref = new CellReference("P1");
 		LocalDate dataAnalisi = null;
 		row = lavorazioneSheet.getRow(ref.getRow());
 		if (row != null) {
 			Cell c = row.getCell(ref.getCol());
 			
 			if(c.getCellTypeEnum() == CellType.NUMERIC) {
 				if (DateUtil.isCellDateFormatted(c)) {
 					log.debug("Data di analisi estratta dal file: {}", c.getDateCellValue().toString());
 	                dataAnalisi = c.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
 	                log.debug("Data di analisi convertita in LocalDate: {}", dataAnalisi.toString());
 	            }
 			} else if(c.getCellTypeEnum() == CellType.STRING) {
 				log.debug("La data è in formato testuale e va convertita: {}", c.getStringCellValue());
 				DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALIAN);
 				DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd/MM/yy", Locale.ITALIAN);
 				
 				try {
 					dataAnalisi = LocalDate.parse(c.getStringCellValue(), formatter1);
 				} catch (DateTimeParseException e) {
 					log.debug("La data non è in formato dd/MM/yyyy");
 				}
 				
 				try {
 					dataAnalisi = LocalDate.parse(c.getStringCellValue(), formatter2);
 				} catch (DateTimeParseException e) {
 					log.debug("La data non è in formato dd/MM/yy");
 				}
 				
 				if(dataAnalisi != null)
 					log.debug("Data di consegna testuale convertita in LocalDate: {}", dataAnalisi.toString());
 				
 			} else {
 				log.warn("Impossibile determinare la data di lavorazione!");
 				errorList.add("Impossibile determinare la data di analisi del campione A");
 			}
 		}
 		if(dataAnalisi != null)
 			log.info("* Data analisi: **{}**", dataAnalisi);
 		
 		// operatore analisi
 		Operatore operatore = null;
 		String operatoreText = null;
		ref = new CellReference("B47");
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				operatoreText = c.getStringCellValue();
				log.debug("Operatore estratto da file Excel: {}", operatoreText);
					
				String[] operatoreTextParts = operatoreText.split(" ");
				
				String cognome = null;
				// caso
				if(operatoreTextParts.length > 2) {
					cognome = operatoreTextParts[2];
				} else if(operatoreTextParts.length == 2) {
					cognome = operatoreTextParts[1];
				} else if(operatoreTextParts.length == 1) {
					//potebbe esserci solo il cognome?
					cognome = operatoreTextParts[0];
				}
				
				if(cognome != null && !cognome.isEmpty()) {
					Optional<User> user = userRepository.findOneByLastNameIgnoreCase(cognome);
					if(user.isPresent()) {
						operatore = operatoreRepository.findOneByUserId(user.get().getId());
					} else {
						// nome e cognome invertiti
						cognome = operatoreTextParts[0];
						user = userRepository.findOneByLastNameIgnoreCase(cognome);
						if(user.isPresent()) {
							operatore = operatoreRepository.findOneByUserId(user.get().getId());
						} else {
							//più nomi
							cognome = operatoreTextParts[operatoreTextParts.length-1];
							user = userRepository.findOneByLastNameIgnoreCase(cognome);
							if(user.isPresent()) {
								operatore = operatoreRepository.findOneByUserId(user.get().getId());
							}
						}
					}
					if(user.isPresent())
						log.info("* Operatore analisi: **{}**", user.get().getLogin());
				}
					
			} else {
				log.warn("Operatore non indicato nel file Excel");
				errorList.add("Operatore analisi del campione A non inserito");
			}
		}
		if(operatore == null) {
			log.warn("Impossibile determinare operatore: {}. L'operatore indicato verrà inserito nelle note.", operatoreText);
		}
		
		// checkbox vari
		boolean trueFalse = false;
		
		ref = new CellReference("L16");
		Boolean ripartizioneQuartatura = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				ripartizioneQuartatura = trueFalse;
			}
		}
		log.info("* Ripartizione o quartatura: **{}**", ripartizioneQuartatura);
		ref = new CellReference("L17");
		Boolean essiccamento = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				essiccamento = trueFalse;
			}
		}
		log.info("* Essiccamento: **{}**", essiccamento);
		ref = new CellReference("L18");
		Boolean setacciaturaSecco = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				setacciaturaSecco = trueFalse;
			}
		}
		log.info("* Setacciatura a secco: **{}**", setacciaturaSecco);
		ref = new CellReference("L19");
		Boolean lavaggioSetacciatura = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				lavaggioSetacciatura = trueFalse;
			}
		}
		log.info("* Lavaggio e setacciatura: **{}**", lavaggioSetacciatura);
		
		//caratteristiche
		ref = new CellReference("L21");
		Boolean sabbia = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				sabbia = trueFalse;
			}
		}
		log.info("* Sabbia: **{}**", sabbia);
		ref = new CellReference("L22");
		Boolean materialeFine = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				materialeFine = trueFalse;
			}
		}
		log.info("* Materiale fine: **{}**", materialeFine);
		ref = new CellReference("L23");
		Boolean materialeOrganico = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				materialeOrganico = trueFalse;
			}
		}
		log.info("* Materiale organico: **{}**", materialeOrganico);
		ref = new CellReference("L24");
		Boolean elementiMagg125Mm = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				elementiMagg125Mm = trueFalse;
			}
		}
		log.info("* Elementi magg. 125: **{}**", elementiMagg125Mm);
		ref = new CellReference("L25");
		Boolean detritiConglomerato = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				detritiConglomerato = trueFalse;
			}
		}
		log.info("* Detriti conglomerato: **{}**", detritiConglomerato);
		ref = new CellReference("L26");
		Boolean argilla = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				argilla = trueFalse;
			}
		}
		log.info("* Argilla: **{}**", argilla);
		ref = new CellReference("L27");
		Boolean elementiArrotondati = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				elementiArrotondati = trueFalse;
			}
		}
		log.info("* Elementi arrotondati: **{}**", elementiArrotondati);
		ref = new CellReference("L28");
		Boolean elementiSpigolosi = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				elementiSpigolosi = trueFalse;
			}
		}
		log.info("* Elementi spigolosi: **{}**", elementiSpigolosi);
		ref = new CellReference("L29");
		Boolean sfabbricidi = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				sfabbricidi = trueFalse;
			}
		}
		log.info("* Sfabbricidi: **{}**", sfabbricidi);
		
		// foto sacchetto e campione
//		byte[] fotoSacchetto = null;
//		String fotoSacchettoContentType = null;
//		byte[] fotoCampione = null;
//		String fotoCampioneContentType = null;
		
		String noteA = "";
		ref = new CellReference("A146");
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK) && (c.getCellTypeEnum() == CellType.STRING)) {
				noteA = c.getStringCellValue();
			}
		}
		
		XSSFDrawing drawing = rapportoSheet.createDrawingPatriarch();
        for (XSSFShape shape : drawing.getShapes()) {
            if (shape instanceof XSSFPicture) {
                XSSFPicture picture = (XSSFPicture) shape;
                XSSFPictureData xssfPictureData = picture.getPictureData();
                
                if(xssfPictureData == null)
                	continue;
                
                ClientAnchor anchor = picture.getPreferredSize();
                int row1 = anchor.getRow1();
//                int row2 = anchor.getRow2();
                int col1 = anchor.getCol1();
//                int col2 = anchor.getCol2();
                
                if (row1 > 60 && row1 < 100 && col1 <= 2) { //foto sacchetto A
                	currentFotoSacchettoContentType = "image/" + xssfPictureData.suggestFileExtension();
					log.debug("Foto sacchetto tipo: {}", currentFotoSacchettoContentType);
					currentFotoSacchetto = xssfPictureData.getData();
			        log.debug("Foto sacchetto length: {}", currentFotoSacchetto.length);
                }
                
                if (row1 > 60 && row1 < 100 && col1 > 2) { //foto campione A
                	currentFotoCampioneContentType = "image/" + xssfPictureData.suggestFileExtension();
					log.debug("Foto campione tipo: {}", currentFotoCampioneContentType);
					currentFotoCampione = xssfPictureData.getData();
			        log.debug("Foto campione length: {}", currentFotoCampione.length);
                }
            }
            if (shape instanceof XSSFSimpleShape){
            	XSSFSimpleShape textBox = (XSSFSimpleShape) shape;
            	XSSFClientAnchor anchor = (XSSFClientAnchor) textBox.getAnchor();
            	int row1 = anchor.getRow1();
//                int row2 = anchor.getRow2();
//                int col1 = anchor.getCol1();
//                int col2 = anchor.getCol2();
            	
                if (row1 > 100 && row1 < 200) //note campione A, se inserite in un textbox
                	if(!textBox.getText().startsWith("CURVA")) {
                		noteA = textBox.getText();
                		if(noteA != null && !noteA.isEmpty())
                    		log.info("* Note campione A: {}", noteA);
                	}
            }
        }
        if(noteA == null || noteA.isEmpty()) {
    		ref = new CellReference("A146");
    		row = rapportoSheet.getRow(ref.getRow());
    		if (row != null) {
    			Cell c = row.getCell(ref.getCol());
    			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
    				noteA = c.getStringCellValue();
    				log.info("* Note campione A: {}", noteA);
    			}
    		}
        }
		
		// pesatura
        log.info("#### Dati pesatura");
		ref = new CellReference("B3");
		row = pesaturaSheet.getRow(ref.getRow());
		String essiccamentoNumTeglia = null;
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if (c != null && (c.getCellTypeEnum() == CellType.NUMERIC)) {
				log.trace("Il campo è numerico (double), presumo sia senza virgola: {}",
						df.format(c.getNumericCellValue()));
				essiccamentoNumTeglia = df.format(c.getNumericCellValue());
			} else if (c != null && (c.getCellTypeEnum() == CellType.STRING)) {
				essiccamentoNumTeglia = c.getStringCellValue();
			} else if (c != null && (c.getCellTypeEnum() == CellType.FORMULA)) { 
				switch(c.getCachedFormulaResultType()) {
	            case Cell.CELL_TYPE_NUMERIC:
	            	essiccamentoNumTeglia = df2.format(c.getNumericCellValue());
	                break;
	            case Cell.CELL_TYPE_STRING:
	            	essiccamentoNumTeglia = c.getStringCellValue();
	                break;
		        }
			} else {
				log.trace("Impossibile determinare numero teglia!");
			}
		}
		log.info("* Numero teglia: **{}**", essiccamentoNumTeglia);
		
		ref = new CellReference("F3");
		row = pesaturaSheet.getRow(ref.getRow());
		String essiccamentoPesoTeglia = "";
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if (c != null && (c.getCellTypeEnum() == CellType.NUMERIC)) {
				log.trace("Il campo è numerico (double): {}",
						df2.format(c.getNumericCellValue()));
				essiccamentoPesoTeglia = df2.format(c.getNumericCellValue());
			} else if (c != null && (c.getCellTypeEnum() == CellType.STRING)) {
				essiccamentoPesoTeglia = c.getStringCellValue();
			} else if (c != null && (c.getCellTypeEnum() == CellType.FORMULA)) { 
				switch(c.getCachedFormulaResultType()) {
	            case Cell.CELL_TYPE_NUMERIC:
	            	essiccamentoPesoTeglia = df2.format(c.getNumericCellValue());
	                break;
	            case Cell.CELL_TYPE_STRING:
	            	essiccamentoPesoTeglia = c.getStringCellValue();
	                break;
		        }
			} else {
				log.warn("Impossibile determinare peso teglia!");
			}
		}
		log.info("* Peso teglia: **{}**", essiccamentoPesoTeglia);
		
		ref = new CellReference("G3");
		row = pesaturaSheet.getRow(ref.getRow());
		String essiccamentoPesoCampioneLordoIniziale = null;
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if (c != null && (c.getCellTypeEnum() == CellType.NUMERIC)) {
				log.trace("Il campo è numerico (double): {}",
						df2.format(c.getNumericCellValue()));
				essiccamentoPesoCampioneLordoIniziale = df2.format(c.getNumericCellValue());
			} else if (c != null && (c.getCellTypeEnum() == CellType.STRING)) {
				essiccamentoPesoCampioneLordoIniziale = c.getStringCellValue();
			} else if (c != null && (c.getCellTypeEnum() == CellType.FORMULA)) { 
				switch(c.getCachedFormulaResultType()) {
	            case Cell.CELL_TYPE_NUMERIC:
	            	essiccamentoPesoCampioneLordoIniziale = df2.format(c.getNumericCellValue());
	                break;
	            case Cell.CELL_TYPE_STRING:
	            	essiccamentoPesoCampioneLordoIniziale = c.getStringCellValue();
	                break;
		        }
			} else {
				log.warn("Impossibile determinare peso campione lordo iniziale!");
				errorList.add("Impossibile determinare peso campione A1 lordo iniziale!");
			}
		}
		log.info("* Peso campione lordo iniziale: **{}**", essiccamentoPesoCampioneLordoIniziale);
		
		ref = new CellReference("I3");
		row = pesaturaSheet.getRow(ref.getRow());
		String essiccamentoPesoCampioneLordo24H = null;
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if (c != null && (c.getCellTypeEnum() == CellType.NUMERIC)) {
				log.trace("Il campo è numerico (double): {}",
						df2.format(c.getNumericCellValue()));
				essiccamentoPesoCampioneLordo24H = df2.format(c.getNumericCellValue());
			} else if (c != null && (c.getCellTypeEnum() == CellType.STRING)) {
				essiccamentoPesoCampioneLordo24H = c.getStringCellValue();
			} else if (c != null && (c.getCellTypeEnum() == CellType.FORMULA)) { 
				switch(c.getCachedFormulaResultType()) {
	            case Cell.CELL_TYPE_NUMERIC:
	            	essiccamentoPesoCampioneLordo24H = df2.format(c.getNumericCellValue());
	                break;
	            case Cell.CELL_TYPE_STRING:
	            	essiccamentoPesoCampioneLordo24H = c.getStringCellValue();
	                break;
		        }
			} else {
				log.warn("Impossibile determinare peso campione lordo 24h!");
				errorList.add("Impossibile determinare peso campione A1 lordo 24h!");
			}
		}
		log.info("* Peso campione lordo 24h: **{}**", essiccamentoPesoCampioneLordo24H);
		
		ref = new CellReference("L3");
		row = pesaturaSheet.getRow(ref.getRow());
		String essiccamentoPesoCampioneLordo48H = null;
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if (c != null && (c.getCellTypeEnum() == CellType.NUMERIC)) {
				log.trace("Il campo è numerico (double): {}",
						df2.format(c.getNumericCellValue()));
				essiccamentoPesoCampioneLordo48H = df2.format(c.getNumericCellValue());
			} else if (c != null && (c.getCellTypeEnum() == CellType.STRING)) {
				essiccamentoPesoCampioneLordo48H = c.getStringCellValue();
			} else if (c != null && (c.getCellTypeEnum() == CellType.FORMULA)) { 
				switch(c.getCachedFormulaResultType()) {
	            case Cell.CELL_TYPE_NUMERIC:
	            	essiccamentoPesoCampioneLordo48H = df2.format(c.getNumericCellValue());
	                break;
	            case Cell.CELL_TYPE_STRING:
	            	essiccamentoPesoCampioneLordo48H = c.getStringCellValue();
	                break;
		        }
			} else {
				log.warn("Impossibile determinare peso campione lordo 48h!");
				errorList.add("Impossibile determinare peso campione A1 lordo 48h!");
			}
		}
		log.info("* Peso campione lordo 48h: **{}**", essiccamentoPesoCampioneLordo48H);
		
		
		
//		if((siglaCampione == null || siglaCampione.isEmpty()) &&
//				dataAnalisi == null &&
//				essiccamentoPesoCampioneLordoIniziale == null) {
//			log.warn("Mancano sigla, data analisi e peso iniziale del campione, lo considero mancante e non lo inserisco nel db.");
//			errorList.add("Mancano sigla, data analisi e peso iniziale del campione A, lo considero mancante e non lo inserisco nel db.");
//
//			if(errorMap.get(currentPath) == null) {
//				errorMap.put(currentPath, errorList);
//			} else {
//				for(String err : errorList) {
//					errorMap.get(currentPath).add(err);
//				}
//			}
//			
//			return null;
//		}
		
		
		
		Campione campione = new Campione();
		campione.setTipoCampione(TipoCampione.A);
		campione.setCassetta(cassetta);
		
		if(!siglaCampione.isEmpty()) {
			try {
				campione.setSiglaCampione(Integer.valueOf(siglaCampione));
			} catch (Exception e) {
				log.debug("La sigla del campione non è un numero: {}", siglaCampione);
			}
		}
		
		campione.setRipartizioneQuartatura(ripartizioneQuartatura);
		campione.setEssiccamento(essiccamento);
		campione.setSetacciaturaSecco(setacciaturaSecco);
		campione.setLavaggioSetacciatura(lavaggioSetacciatura);
		
		campione.setSabbia(sabbia);
		campione.setMaterialeFine(materialeFine);
		campione.setMaterialeOrganico(materialeOrganico);
		campione.setElementiMagg125Mm(elementiMagg125Mm);
		campione.setDetritiConglomerato(detritiConglomerato);
		campione.setArgilla(argilla);
		campione.setElementiArrotondati(elementiArrotondati);
		campione.setElementiSpigolosi(elementiSpigolosi);
		campione.setSfabbricidi(sfabbricidi);
		
		campione.setDataAnalisi(dataAnalisi);
		campione.setOperatoreAnalisi(operatore);
		
		campione.setEssiccamentoNumTeglia(essiccamentoNumTeglia);
		if(essiccamentoPesoTeglia != null && !essiccamentoPesoTeglia.isEmpty())
			campione.setEssiccamentoPesoTeglia(new BigDecimal(essiccamentoPesoTeglia.replace(",", ".")));
		if(essiccamentoPesoCampioneLordoIniziale != null && !essiccamentoPesoCampioneLordoIniziale.isEmpty())
			campione.setEssiccamentoPesoCampioneLordoIniziale(new BigDecimal(essiccamentoPesoCampioneLordoIniziale.replace(",", ".")));
		if(essiccamentoPesoCampioneLordo24H != null && !essiccamentoPesoCampioneLordo24H.isEmpty())
			campione.setEssiccamentoPesoCampioneLordo24H(new BigDecimal(essiccamentoPesoCampioneLordo24H.replace(",", ".")));
		if(essiccamentoPesoCampioneLordo48H != null && !essiccamentoPesoCampioneLordo48H.isEmpty())
			campione.setEssiccamentoPesoCampioneLordo48H(new BigDecimal(essiccamentoPesoCampioneLordo48H.replace(",", ".")));
		
		if(operatore == null && operatoreText != null && !operatoreText.isEmpty()) {
			campione.setNote(noteA + " ###" + operatoreText);
		} else {
			campione.setNote(noteA);
		}
		
		campione.setLavorazioneConclusa(true);
		
		String codiceCampione = campione.getCassetta().getCodiceCassetta() + "-" + campione.getTipoCampione();
		campione.setCodiceCampione(codiceCampione);
		
		campione = campioneRepository.save(campione);
		
		
		// setacciatura
		log.info("#### Dati setacciatura");
		for(int numPesata = 1; numPesata < 6; numPesata++) {
			ArrayList<BigDecimal> seriePesate = new ArrayList<BigDecimal>();
			for(char col='B';col<='P';col++) {
				ref = new CellReference(Character.toString(col) + (numPesata + 3));
				row = lavorazioneSheet.getRow(ref.getRow());
				String peso = null;
				if (row != null) {
					Cell c = row.getCell(ref.getCol());
					//log.debug("Cella: {}", ref);
					if (c.getCellTypeEnum() == CellType.NUMERIC) {
						log.trace("Il campo è numerico (double): {}",
								df2.format(c.getNumericCellValue()));
						peso = df2.format(c.getNumericCellValue());
					} else if (c.getCellTypeEnum() == CellType.STRING) {
						log.trace("Il campo è stringa: {}", c.getStringCellValue());
						peso = c.getStringCellValue();
					} else if(c.getCellTypeEnum() == CellType.FORMULA) { 
						log.trace("La cella {}{} è stata impostata come formula: {}", col, numPesata +3, c.getCellFormula());
						switch(c.getCachedFormulaResultType()) {
				            case Cell.CELL_TYPE_NUMERIC:
				            	log.trace("Il campo è numerico (double): {}",
										df2.format(c.getNumericCellValue()));
								peso = df2.format(c.getNumericCellValue());
				                break;
				            case Cell.CELL_TYPE_STRING:
				            	log.trace("Il campo è stringa: {}", c.getStringCellValue());
								peso = c.getStringCellValue();
				                break;
				        }
					} else {
						
						if(col=='B') {
							break; // non ci sono più pesate
						} else {
							// probabilmente la cella è vuota
							peso = "0";
						}
					}
					
					if(peso != null) {
						seriePesate.add(new BigDecimal(peso.replace(",", ".")));
					} else {
						seriePesate.add(null);
					}
				}
				//log.info("* Peso netto essiccazione, pesata {}: **{}**", numPesata, peso);
			}
			if(seriePesate.size() > 0) {
				Pesata pesata = insertPesataAA1(campione, numPesata, seriePesate);
				log.info("* Inserita setacciatura n. {}", pesata.getNumPesata());
			}
			
		}
		
		
		ClassificazioneGeotecnicaDTO dto = null;
		try {
			dto = campioneService.generateClassificazioneGeotecnica(campione);
		} catch (CustomParameterizedException ex) {
			log.error("La sommatoria dei pesi di una setacciatura è uguale a 0!");
			errorList.add("La sommatoria dei pesi di una setacciatura del campione A è uguale a 0!");
		}
		
		if(dto != null) {
	    	campione.setClassificazioneGeotecnica(dto.getClassificazioneGeotecnica());
	    	campione = campioneRepository.save(campione);
		}
		
		//byte[] byteArray = null;
		try {
			currentCurvaGranulometrica = campioneService.generateCurvaGranulometrica(campione);
		} catch (CustomParameterizedException ex) {
			log.error("La sommatoria dei pesi di una setacciatura è uguale a 0!");
			errorList.add("La sommatoria dei pesi di una setacciatura del campione A è uguale a 0!");
		}
			
//		if(byteArray != null) {
//			campione.setCurva(byteArray);
//			campione.setCurvaContentType("image/png");
//			campione = campioneRepository.save(campione);
//		}
		
		if(errorList.size() > 0) {
			if(errorMap.get(currentPath) == null) {
				errorMap.put(currentPath, errorList);
			} else {
				for(String err : errorList) {
					errorMap.get(currentPath).add(err);
				}
			}
		}
		
		return campione;
	}
	
	@SuppressWarnings("deprecation")
	public Campione insertCampioneA1(Cassetta cassetta, XSSFWorkbook workbook) {
		List<String> errorList = new ArrayList<String>();
		
		log.info("### Creazione e inserimento campione A1 relativo alla cassetta {}", cassetta.getCodiceCassetta());
		
		//XSSFSheet accettazioneSheet = workbook.getSheet("Dati input accettazione");
		XSSFSheet pesaturaSheet = workbook.getSheet("Pesatura");
		XSSFSheet lavorazioneSheet = workbook.getSheet("Lavorazione");
		XSSFSheet rapportoSheet = workbook.getSheet("Rapporto di prova");
		
		DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(0);
        
        DecimalFormat df2 = new DecimalFormat("#");
        df2.setMaximumFractionDigits(2);
		
        /*
         * controllo presenza dati di lavorazione
         */
        CellReference ref = new CellReference("C262");
		Row row = rapportoSheet.getRow(ref.getRow());
		String sumMasse = null;
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if (c != null && (c.getCellTypeEnum() == CellType.NUMERIC)) {
				log.trace("Il campo è numerico (double): {}",
						df2.format(c.getNumericCellValue()));
				sumMasse = df2.format(c.getNumericCellValue());
			} else if (c != null && (c.getCellTypeEnum() == CellType.STRING)) {
				sumMasse = c.getStringCellValue();
			} else if (c != null && (c.getCellTypeEnum() == CellType.FORMULA)) { 
				switch(c.getCachedFormulaResultType()) {
	            case Cell.CELL_TYPE_NUMERIC:
	            	sumMasse = df2.format(c.getNumericCellValue());
	                break;
	            case Cell.CELL_TYPE_STRING:
	            	sumMasse = c.getStringCellValue();
	                break;
		        }
			} else {
				log.warn("Impossibile determinare sommatoria masse setacciatura!");
			}
		}
		log.debug("* Sommatoria masse setacciatura: **{}**", sumMasse);
        
        
		if(sumMasse == null || Double.valueOf(sumMasse.replace(",", ".")) < 1) {
			log.warn("Mancano i dati di setacciatura del campione, lo considero mancante e non lo inserisco nel db.");
			errorList.add("Mancano i dati di setacciatura del campione A1, lo considero mancante e non lo inserisco nel db.");

			if(errorMap.get(currentPath) == null) {
				errorMap.put(currentPath, errorList);
			} else {
				for(String err : errorList) {
					errorMap.get(currentPath).add(err);
				}
			}
			
			return null;
		}
        
		//sigla campione
		ref = new CellReference("F12");
		row = lavorazioneSheet.getRow(ref.getRow());
		String siglaCampione = "";
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c.getCellTypeEnum() == CellType.NUMERIC) {
				log.trace("Il campo è numerico (double), presumo sia senza virgola: {}", df.format(c.getNumericCellValue()));
				siglaCampione = df.format(c.getNumericCellValue());
			} else if(c.getCellTypeEnum() == CellType.STRING) {
				siglaCampione = c.getStringCellValue();
			} else if (c.getCellTypeEnum() == CellType.FORMULA) { 
				switch(c.getCachedFormulaResultType()) {
	            case Cell.CELL_TYPE_NUMERIC:
	            	siglaCampione = df.format(c.getNumericCellValue());
	                break;
	            case Cell.CELL_TYPE_STRING:
	            	siglaCampione = c.getStringCellValue();
	                break;
		        }
			} else {
				log.warn("Impossibile determinare sigla campione!");
				errorList.add("Impossibile determinare la sigla del campione A1");
			}
		}
		log.info("* Sigla campione: **{}**", siglaCampione);
		
		// data lavorazione
 		ref = new CellReference("P12");
 		LocalDate dataAnalisi = null;
 		row = lavorazioneSheet.getRow(ref.getRow());
 		if (row != null) {
 			Cell c = row.getCell(ref.getCol());
 			
 			if(c != null && c.getCellTypeEnum() == CellType.NUMERIC) {
 				if (DateUtil.isCellDateFormatted(c)) {
 					log.debug("Data di analisi estratta dal file: {}", c.getDateCellValue().toString());
 	                dataAnalisi = c.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
 	                log.debug("Data di analisi convertita in LocalDate: {}", dataAnalisi.toString());
 	            }
 			} else if(c.getCellTypeEnum() == CellType.STRING) {
 				log.debug("La data è in formato testuale e va convertita: {}", c.getStringCellValue());
 				DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALIAN);
 				DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd/MM/yy", Locale.ITALIAN);
 				
 				try {
 					dataAnalisi = LocalDate.parse(c.getStringCellValue(), formatter1);
 				} catch (DateTimeParseException e) {
 					log.debug("La data non è in formato dd/MM/yyyy");
 				}
 				
 				try {
 					dataAnalisi = LocalDate.parse(c.getStringCellValue(), formatter2);
 				} catch (DateTimeParseException e) {
 					log.debug("La data non è in formato dd/MM/yy");
 				}
 				
 				if(dataAnalisi != null)
 					log.debug("Data di consegna testuale convertita in LocalDate: {}", dataAnalisi.toString());
 				
 			} else {
 				log.warn("Impossibile determinare la data di lavorazione!");
 				errorList.add("Impossibile determinare la data di lavorazione del campione A1");
 			}
 		}
 		log.info("* Data analisi: **{}**", dataAnalisi);
 		
 		// operatore analisi
 		Operatore operatore = null;
 		String operatoreText = null;
		ref = new CellReference("B204");
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				operatoreText = c.getStringCellValue();
				log.debug("Operatore estratto da file Excel: {}", operatoreText);
				String[] operatoreTextParts = operatoreText.split(" ");
				
				String cognome = null;
				// caso
				if(operatoreTextParts.length > 2) {
					cognome = operatoreTextParts[2];
				} else if(operatoreTextParts.length == 2) {
					cognome = operatoreTextParts[1];
				} else if(operatoreTextParts.length == 1) {
					//potebbe esserci solo il cognome?
					cognome = operatoreTextParts[0];
				}
				
				if(cognome != null && !cognome.isEmpty()) {
					Optional<User> user = userRepository.findOneByLastNameIgnoreCase(cognome);
					if(user.isPresent()) {
						operatore = operatoreRepository.findOneByUserId(user.get().getId());
					} else {
						// nome e cognome invertiti
						cognome = operatoreTextParts[0];
						user = userRepository.findOneByLastNameIgnoreCase(cognome);
						if(user.isPresent()) {
							operatore = operatoreRepository.findOneByUserId(user.get().getId());
						} else {
							//più nomi
							cognome = operatoreTextParts[operatoreTextParts.length-1];
							user = userRepository.findOneByLastNameIgnoreCase(cognome);
							if(user.isPresent()) {
								operatore = operatoreRepository.findOneByUserId(user.get().getId());
							}
						}
					}
					if(user.isPresent())
						log.info("* Operatore analisi: **{}**", user.get().getLogin());
				}
					
			} else {
				log.warn("Operatore non indicato nel file Excel");
				errorList.add("Operatore analisi del campione A1 non inserito");
			}
		}
		if(operatore == null) {
			log.warn("Impossibile determinare operatore: {}. L'operatore indicato verrà inserito nelle note.", operatoreText);
		}
		
		// checkbox vari
		boolean trueFalse = false;
		
		ref = new CellReference("L31");
		Boolean ripartizioneQuartatura = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				ripartizioneQuartatura = trueFalse;
			}
		}
		log.info("* Ripartizione o quartatura: **{}**", ripartizioneQuartatura);
		ref = new CellReference("L32");
		Boolean essiccamento = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				essiccamento = trueFalse;
			}
		}
		log.info("* Essiccamento: **{}**", essiccamento);
		ref = new CellReference("L33");
		Boolean setacciaturaSecco = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				setacciaturaSecco = trueFalse;
			}
		}
		log.info("* Setacciatura a secco: **{}**", setacciaturaSecco);
		ref = new CellReference("L34");
		Boolean lavaggioSetacciatura = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				lavaggioSetacciatura = trueFalse;
			}
		}
		log.info("* Lavaggio e setacciatura: **{}**", lavaggioSetacciatura);
		
		//caratteristiche
		ref = new CellReference("L36");
		Boolean sabbia = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				sabbia = trueFalse;
			}
		}
		log.info("* Sabbia: **{}**", sabbia);
		ref = new CellReference("L37");
		Boolean materialeFine = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				materialeFine = trueFalse;
			}
		}
		log.info("* Materiale fine: **{}**", materialeFine);
		ref = new CellReference("L38");
		Boolean materialeOrganico = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				materialeOrganico = trueFalse;
			}
		}
		log.info("* Materiale organico: **{}**", materialeOrganico);
		ref = new CellReference("L39");
		Boolean elementiMagg125Mm = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				elementiMagg125Mm = trueFalse;
			}
		}
		log.info("* Elementi magg. 125: **{}**", elementiMagg125Mm);
		ref = new CellReference("L40");
		Boolean detritiConglomerato = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				detritiConglomerato = trueFalse;
			}
		}
		log.info("* Detriti conglomerato: **{}**", detritiConglomerato);
		ref = new CellReference("L41");
		Boolean argilla = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				argilla = trueFalse;
			}
		}
		log.info("* Argilla: **{}**", argilla);
		ref = new CellReference("L42");
		Boolean elementiArrotondati = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				elementiArrotondati = trueFalse;
			}
		}
		log.info("* Elementi arrotondati: **{}**", elementiArrotondati);
		ref = new CellReference("L43");
		Boolean elementiSpigolosi = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				elementiSpigolosi = trueFalse;
			}
		}
		log.info("* Elementi spigolosi: **{}**", elementiSpigolosi);
		ref = new CellReference("L44");
		Boolean sfabbricidi = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				sfabbricidi = trueFalse;
			}
		}
		log.info("* Sfabbricidi: **{}**", sfabbricidi);
		
		// foto sacchetto e campione
//		byte[] fotoSacchetto = null;
//		String fotoSacchettoContentType = null;
//		byte[] fotoCampione = null;
//		String fotoCampioneContentType = null;
		
		String noteA1 = "";
		ref = new CellReference("A302");
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK) && (c.getCellTypeEnum() == CellType.STRING)) {
				noteA1 = c.getStringCellValue();
			}
		}

		
		XSSFDrawing drawing = rapportoSheet.createDrawingPatriarch();
        for (XSSFShape shape : drawing.getShapes()) {
            if (shape instanceof XSSFPicture) {
                XSSFPicture picture = (XSSFPicture) shape;
                XSSFPictureData xssfPictureData = picture.getPictureData();
                
                if(xssfPictureData == null)
                	continue;
                
                ClientAnchor anchor = picture.getPreferredSize();
                int row1 = anchor.getRow1();
//                int row2 = anchor.getRow2();
                int col1 = anchor.getCol1();
//                int col2 = anchor.getCol2();
                
                if (row1 > 200 && row1 < 300 && col1 <= 2) { //foto sacchetto A1
                	currentFotoSacchettoContentType = "image/" + xssfPictureData.suggestFileExtension();
					log.debug("Foto sacchetto tipo: {}", currentFotoSacchettoContentType);
					currentFotoSacchetto = xssfPictureData.getData();
			        log.debug("Foto sacchetto length: {}", currentFotoSacchetto.length);
                }
                
                if (row1 > 200 && row1 < 300 && col1 > 2) { //foto campione A1
                	currentFotoCampioneContentType = "image/" + xssfPictureData.suggestFileExtension();
					log.debug("Foto campione tipo: {}", currentFotoCampioneContentType);
					currentFotoCampione = xssfPictureData.getData();
			        log.debug("Foto campione length: {}", currentFotoCampione.length);
                }
            }
            if (shape instanceof XSSFSimpleShape){
            	XSSFSimpleShape textBox = (XSSFSimpleShape) shape;
            	XSSFClientAnchor anchor = (XSSFClientAnchor) textBox.getAnchor();
            	int row1 = anchor.getRow1();
//                int row2 = anchor.getRow2();
//                int col1 = anchor.getCol1();
//                int col2 = anchor.getCol2();
            	
                if (row1 > 250 && row1 < 350) //note campione A1
                	if(!textBox.getText().startsWith("CURVA")) {
                		noteA1 = textBox.getText();
                		if(noteA1 != null && !noteA1.isEmpty())
                    		log.info("* Note campione A1: {}", noteA1);
                	}
                
            }
        }
        if(noteA1 == null || noteA1.isEmpty()) {
    		ref = new CellReference("A302");
    		row = rapportoSheet.getRow(ref.getRow());
    		if (row != null) {
    			Cell c = row.getCell(ref.getCol());
    			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
    				noteA1 = c.getStringCellValue();
    				log.info("* Note campione A1: {}", noteA1);
    			}
    		}
        }
		
		// pesatura
        log.info("#### Dati pesatura");
		ref = new CellReference("B4");
		row = pesaturaSheet.getRow(ref.getRow());
		String essiccamentoNumTeglia = null;
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if (c != null && (c.getCellTypeEnum() == CellType.NUMERIC)) {
				log.trace("Il campo è numerico (double), presumo sia senza virgola: {}",
						df.format(c.getNumericCellValue()));
				essiccamentoNumTeglia = df.format(c.getNumericCellValue());
			} else if (c != null && (c.getCellTypeEnum() == CellType.STRING)) {
				essiccamentoNumTeglia = c.getStringCellValue();
			} else if (c != null && (c.getCellTypeEnum() == CellType.FORMULA)) { 
				switch(c.getCachedFormulaResultType()) {
	            case Cell.CELL_TYPE_NUMERIC:
	            	essiccamentoNumTeglia = df2.format(c.getNumericCellValue());
	                break;
	            case Cell.CELL_TYPE_STRING:
	            	essiccamentoNumTeglia = c.getStringCellValue();
	                break;
		        }
			} else {
				log.trace("Impossibile determinare numero teglia!");
			}
		}
		log.info("* Numero teglia: **{}**", essiccamentoNumTeglia);
		
		ref = new CellReference("F4");
		row = pesaturaSheet.getRow(ref.getRow());
		String essiccamentoPesoTeglia = "";
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if (c != null && (c.getCellTypeEnum() == CellType.NUMERIC)) {
				log.trace("Il campo è numerico (double): {}",
						df2.format(c.getNumericCellValue()));
				essiccamentoPesoTeglia = df2.format(c.getNumericCellValue());
			} else if (c != null && (c.getCellTypeEnum() == CellType.STRING)) {
				essiccamentoPesoTeglia = c.getStringCellValue();
			} else if (c != null && (c.getCellTypeEnum() == CellType.FORMULA)) { 
				switch(c.getCachedFormulaResultType()) {
	            case Cell.CELL_TYPE_NUMERIC:
	            	essiccamentoPesoTeglia = df2.format(c.getNumericCellValue());
	                break;
	            case Cell.CELL_TYPE_STRING:
	            	essiccamentoPesoTeglia = c.getStringCellValue();
	                break;
		        }
			} else {
				log.warn("Impossibile determinare peso teglia!");
			}
		}
		log.info("* Peso teglia: **{}**", essiccamentoPesoTeglia);
		
		ref = new CellReference("G4");
		row = pesaturaSheet.getRow(ref.getRow());
		String essiccamentoPesoCampioneLordoIniziale = null;
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if (c != null && (c.getCellTypeEnum() == CellType.NUMERIC)) {
				log.trace("Il campo è numerico (double): {}",
						df2.format(c.getNumericCellValue()));
				essiccamentoPesoCampioneLordoIniziale = df2.format(c.getNumericCellValue());
			} else if (c != null && (c.getCellTypeEnum() == CellType.STRING)) {
				essiccamentoPesoCampioneLordoIniziale = c.getStringCellValue();
			} else if (c != null && (c.getCellTypeEnum() == CellType.FORMULA)) { 
				switch(c.getCachedFormulaResultType()) {
	            case Cell.CELL_TYPE_NUMERIC:
	            	essiccamentoPesoCampioneLordoIniziale = df2.format(c.getNumericCellValue());
	                break;
	            case Cell.CELL_TYPE_STRING:
	            	essiccamentoPesoCampioneLordoIniziale = c.getStringCellValue();
	                break;
		        }
			} else {
				log.warn("Impossibile determinare peso campione lordo iniziale!");
				errorList.add("Impossibile determinare peso campione A1 lordo iniziale!");
			}
		}
		log.info("* Peso campione lordo iniziale: **{}**", essiccamentoPesoCampioneLordoIniziale);
		
		ref = new CellReference("I4");
		row = pesaturaSheet.getRow(ref.getRow());
		String essiccamentoPesoCampioneLordo24H = null;
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if (c != null && (c.getCellTypeEnum() == CellType.NUMERIC)) {
				log.trace("Il campo è numerico (double): {}",
						df2.format(c.getNumericCellValue()));
				essiccamentoPesoCampioneLordo24H = df2.format(c.getNumericCellValue());
			} else if (c != null && (c.getCellTypeEnum() == CellType.STRING)) {
				essiccamentoPesoCampioneLordo24H = c.getStringCellValue();
			} else if (c != null && (c.getCellTypeEnum() == CellType.FORMULA)) { 
				switch(c.getCachedFormulaResultType()) {
	            case Cell.CELL_TYPE_NUMERIC:
	            	essiccamentoPesoCampioneLordo24H = df2.format(c.getNumericCellValue());
	                break;
	            case Cell.CELL_TYPE_STRING:
	            	essiccamentoPesoCampioneLordo24H = c.getStringCellValue();
	                break;
		        }
			} else {
				log.warn("Impossibile determinare peso campione lordo 24h!");
				errorList.add("Impossibile determinare peso campione A1 lordo 24h!");
			}
		}
		log.info("* Peso campione lordo 24h: **{}**", essiccamentoPesoCampioneLordo24H);
		
		ref = new CellReference("L4");
		row = pesaturaSheet.getRow(ref.getRow());
		String essiccamentoPesoCampioneLordo48H = null;
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if (c != null && (c.getCellTypeEnum() == CellType.NUMERIC)) {
				log.trace("Il campo è numerico (double): {}",
						df2.format(c.getNumericCellValue()));
				essiccamentoPesoCampioneLordo48H = df2.format(c.getNumericCellValue());
			} else if (c != null && (c.getCellTypeEnum() == CellType.STRING)) {
				essiccamentoPesoCampioneLordo48H = c.getStringCellValue();
			} else if (c != null && (c.getCellTypeEnum() == CellType.FORMULA)) { 
				switch(c.getCachedFormulaResultType()) {
	            case Cell.CELL_TYPE_NUMERIC:
	            	essiccamentoPesoCampioneLordo48H = df2.format(c.getNumericCellValue());
	                break;
	            case Cell.CELL_TYPE_STRING:
	            	essiccamentoPesoCampioneLordo48H = c.getStringCellValue();
	                break;
		        }
			} else {
				log.warn("Impossibile determinare peso campione lordo 48h!");
				errorList.add("Impossibile determinare peso campione A1 lordo 48h!");
			}
		}
		log.info("* Peso campione lordo 48h: **{}**", essiccamentoPesoCampioneLordo48H);
		
		
		
//		if((siglaCampione == null || siglaCampione.isEmpty()) &&
//				dataAnalisi == null &&
//				essiccamentoPesoCampioneLordoIniziale == null) {
//			log.warn("Mancano sigla, data analisi e peso iniziale del campione, lo considero mancante e non lo inserisco nel db.");
//			errorList.add("Mancano sigla, data analisi e peso iniziale del campione A1, lo considero mancante e non lo inserisco nel db.");
//
//			if(errorMap.get(currentPath) == null) {
//				errorMap.put(currentPath, errorList);
//			} else {
//				for(String err : errorList) {
//					errorMap.get(currentPath).add(err);
//				}
//			}
//			
//			return null;
//		}
		
		
		
		Campione campione = new Campione();
		campione.setTipoCampione(TipoCampione.A1);
		campione.setCassetta(cassetta);
		
		if(!siglaCampione.isEmpty()) {
			try {
				campione.setSiglaCampione(Integer.valueOf(siglaCampione));
			} catch (Exception e) {
				log.debug("La sigla del campione non è un numero: {}", siglaCampione);
			}
		}
		
		campione.setRipartizioneQuartatura(ripartizioneQuartatura);
		campione.setEssiccamento(essiccamento);
		campione.setSetacciaturaSecco(setacciaturaSecco);
		campione.setLavaggioSetacciatura(lavaggioSetacciatura);
		
		campione.setSabbia(sabbia);
		campione.setMaterialeFine(materialeFine);
		campione.setMaterialeOrganico(materialeOrganico);
		campione.setElementiMagg125Mm(elementiMagg125Mm);
		campione.setDetritiConglomerato(detritiConglomerato);
		campione.setArgilla(argilla);
		campione.setElementiArrotondati(elementiArrotondati);
		campione.setElementiSpigolosi(elementiSpigolosi);
		campione.setSfabbricidi(sfabbricidi);
		
		campione.setDataAnalisi(dataAnalisi);
		campione.setOperatoreAnalisi(operatore);
		
		campione.setEssiccamentoNumTeglia(essiccamentoNumTeglia);
		if(essiccamentoPesoTeglia != null && !essiccamentoPesoTeglia.isEmpty())
			campione.setEssiccamentoPesoTeglia(new BigDecimal(essiccamentoPesoTeglia.replace(",", ".")));
		if(essiccamentoPesoCampioneLordoIniziale != null && !essiccamentoPesoCampioneLordoIniziale.isEmpty())
			campione.setEssiccamentoPesoCampioneLordoIniziale(new BigDecimal(essiccamentoPesoCampioneLordoIniziale.replace(",", ".")));
		if(essiccamentoPesoCampioneLordo24H != null && !essiccamentoPesoCampioneLordo24H.isEmpty())
			campione.setEssiccamentoPesoCampioneLordo24H(new BigDecimal(essiccamentoPesoCampioneLordo24H.replace(",", ".")));
		if(essiccamentoPesoCampioneLordo48H != null && !essiccamentoPesoCampioneLordo48H.isEmpty())
			campione.setEssiccamentoPesoCampioneLordo48H(new BigDecimal(essiccamentoPesoCampioneLordo48H.replace(",", ".")));
		
		if(operatore == null && operatoreText != null && !operatoreText.isEmpty()) {
			campione.setNote(noteA1 + " ###" + operatoreText);
		} else {
			campione.setNote(noteA1);
		}
		
		campione.setLavorazioneConclusa(true);
		
		String codiceCampione = campione.getCassetta().getCodiceCassetta() + "-" + campione.getTipoCampione();
		campione.setCodiceCampione(codiceCampione);
		
		campione = campioneRepository.save(campione);
		
		
		// setacciatura
		log.info("#### Dati setacciatura");
		for(int numPesata = 1; numPesata < 6; numPesata++) {
			ArrayList<BigDecimal> seriePesate = new ArrayList<BigDecimal>();
			for(char col='B';col<='P';col++) {
				ref = new CellReference(Character.toString(col) + (numPesata + 14));
				row = lavorazioneSheet.getRow(ref.getRow());
				String peso = null;
				if (row != null) {
					Cell c = row.getCell(ref.getCol());
					//log.debug("Cella: {}", ref);
					if (c.getCellTypeEnum() == CellType.NUMERIC) {
						log.trace("Il campo è numerico (double): {}",
								df2.format(c.getNumericCellValue()));
						peso = df2.format(c.getNumericCellValue());
					} else if (c.getCellTypeEnum() == CellType.STRING) {
						log.trace("Il campo è stringa: {}", c.getStringCellValue());
						peso = c.getStringCellValue();
					} else if(c.getCellTypeEnum() == CellType.FORMULA) { 
						switch(c.getCachedFormulaResultType()) {
				            case Cell.CELL_TYPE_NUMERIC:
				            	log.trace("Il campo è numerico (double): {}",
										df2.format(c.getNumericCellValue()));
								peso = df2.format(c.getNumericCellValue());
				                break;
				            case Cell.CELL_TYPE_STRING:
				            	log.trace("Il campo è stringa: {}", c.getStringCellValue());
								peso = c.getStringCellValue();
				                break;
				        }
					} else {
						
						if(col=='B') {
							break; // non ci sono più pesate
						} else {
							// probabilmente la cella è vuota
							peso = "0";
						}
					}
					
					if(peso != null) {
						seriePesate.add(new BigDecimal(peso.replace(",", ".")));
					} else {
						seriePesate.add(null);
					}
				}
				//log.info("* Peso netto essiccazione, pesata {}: **{}**", numPesata, peso);
			}
			if(seriePesate.size() > 0) {
				Pesata pesata = insertPesataAA1(campione, numPesata, seriePesate);
				log.info("* Inserita setacciatura n. {}", pesata.getNumPesata());
			}
			
		}
		
		// blob
//		campione.setFotoSacchetto(fotoSacchetto);
//		campione.setFotoSacchettoContentType(fotoSacchettoContentType);
//		campione = campioneRepository.save(campione);
//		
//		campione.setFotoCampione(fotoCampione);
//		campione.setFotoCampioneContentType(fotoCampioneContentType);
//		campione = campioneRepository.save(campione);
		
		ClassificazioneGeotecnicaDTO dto = null;
		try {
			dto = campioneService.generateClassificazioneGeotecnica(campione);
		} catch (CustomParameterizedException ex) {
			log.error("La sommatoria dei pesi di una setacciatura è uguale a 0!");
			errorList.add("La sommatoria dei pesi di una setacciatura del campione A1 è uguale a 0");
		}
		
		if(dto != null) {
	    	campione.setClassificazioneGeotecnica(dto.getClassificazioneGeotecnica());
	    	campione = campioneRepository.save(campione);
		}
		
		//byte[] byteArray = null;
		try {
			currentCurvaGranulometrica = campioneService.generateCurvaGranulometrica(campione);
		} catch (CustomParameterizedException ex) {
			log.error("La sommatoria dei pesi di una setacciatura è uguale a 0!");
			errorList.add("La sommatoria dei pesi di una setacciatura del campione A1 è uguale a 0");
		}
			
//		if(byteArray != null) {
//			campione.setCurva(byteArray);
//			campione.setCurvaContentType("image/png");
//			campione = campioneRepository.save(campione);
//		}
		
		if(errorList.size() > 0) {
			if(errorMap.get(currentPath) == null) {
				errorMap.put(currentPath, errorList);
			} else {
				for(String err : errorList) {
					errorMap.get(currentPath).add(err);
				}
			}
		}
		
		return campione;
	}
	
	public Pesata insertPesataAA1(Campione campione, int numPesata, ArrayList<BigDecimal> seriePesate) {
		Pesata pesata = new Pesata();
		
		pesata.setCampione(campione);
		pesata.setNumPesata(numPesata);
		pesata.setPesoNetto(seriePesate.get(0));
		pesata.setTratt125Mm(seriePesate.get(1));
		pesata.setTratt63Mm(seriePesate.get(2));
		pesata.setTratt31V5Mm(seriePesate.get(3));
		pesata.setTratt16Mm(seriePesate.get(4));
		pesata.setTratt8Mm(seriePesate.get(5));
		pesata.setTratt6V3Mm(seriePesate.get(6));
		pesata.setTratt4Mm(seriePesate.get(7));
		pesata.setTratt2Mm(seriePesate.get(8));
		pesata.setTratt1Mm(seriePesate.get(9));
		pesata.setTratt0V5Mm(seriePesate.get(10));
		pesata.setTratt0V25Mm(seriePesate.get(11));
		pesata.setTratt0V125Mm(seriePesate.get(12));
		pesata.setTratt0V075Mm(seriePesate.get(13));
		pesata.setFondo(seriePesate.get(14));
		
		Pesata result = pesataRepository.save(pesata);
		
		return result;
	}
	
	@SuppressWarnings("deprecation")
	public Campione insertCampioneB(Cassetta cassetta, XSSFWorkbook workbook) {
		List<String> errorList = new ArrayList<String>();
		
		log.info("### Creazione e inserimento campione B relativo alla cassetta {}", cassetta.getCodiceCassetta());
		
		//XSSFSheet accettazioneSheet = workbook.getSheet("Dati input accettazione");
		XSSFSheet pesaturaSheet = workbook.getSheet("Pesatura");
		XSSFSheet lavorazioneSheet = workbook.getSheet("Lavorazione");
		XSSFSheet rapportoSheet = workbook.getSheet("Rapporto di prova");
		
		DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(0);
        
        DecimalFormat df2 = new DecimalFormat("#");
        df2.setMaximumFractionDigits(2);
		
		//sigla campione
		CellReference ref = new CellReference("F23");
		Row row = lavorazioneSheet.getRow(ref.getRow());
		String siglaCampione = "";
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c.getCellTypeEnum() == CellType.NUMERIC) {
				log.trace("Il campo è numerico (double), presumo sia senza virgola: {}", df.format(c.getNumericCellValue()));
				siglaCampione = df.format(c.getNumericCellValue());
			} else if(c.getCellTypeEnum() == CellType.STRING) {
				siglaCampione = c.getStringCellValue();
			} else if (c.getCellTypeEnum() == CellType.FORMULA) { 
				switch(c.getCachedFormulaResultType()) {
	            case Cell.CELL_TYPE_NUMERIC:
	            	siglaCampione = df.format(c.getNumericCellValue());
	                break;
	            case Cell.CELL_TYPE_STRING:
	            	siglaCampione = c.getStringCellValue();
	                break;
		        }
			} else {
				log.warn("Impossibile determinare sigla campione!");
				errorList.add("Impossibile determinare la sigla del campione B");
			}
		}
		log.info("* Sigla campione: **{}**", siglaCampione);
		
		// data lavorazione
 		ref = new CellReference("P23");
 		LocalDate dataAnalisi = null;
 		row = lavorazioneSheet.getRow(ref.getRow());
 		if (row != null) {
 			Cell c = row.getCell(ref.getCol());
 			
 			if(c != null && c.getCellTypeEnum() == CellType.NUMERIC) {
 				if (DateUtil.isCellDateFormatted(c)) {
 					log.debug("Data di analisi estratta dal file: {}", c.getDateCellValue().toString());
 	                dataAnalisi = c.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
 	                log.debug("Data di analisi convertita in LocalDate: {}", dataAnalisi.toString());
 	            }
 			} else if(c.getCellTypeEnum() == CellType.STRING) {
 				log.debug("La data è in formato testuale e va convertita: {}", c.getStringCellValue());
 				DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALIAN);
 				DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd/MM/yy", Locale.ITALIAN);
 				
 				try {
 					dataAnalisi = LocalDate.parse(c.getStringCellValue(), formatter1);
 				} catch (DateTimeParseException e) {
 					log.debug("La data non è in formato dd/MM/yyyy");
 				}
 				
 				try {
 					dataAnalisi = LocalDate.parse(c.getStringCellValue(), formatter2);
 				} catch (DateTimeParseException e) {
 					log.debug("La data non è in formato dd/MM/yy");
 				}
 				
 				if(dataAnalisi != null)
 					log.debug("Data di consegna testuale convertita in LocalDate: {}", dataAnalisi.toString());
 				
 			} else {
 				log.warn("Impossibile determinare la data di lavorazione!");
 				errorList.add("Impossibile determinare la data di lavorazione del campione B");
 			}
 		}
 		log.info("* Data analisi: **{}**", dataAnalisi);
 		
 		// operatore analisi
 		Operatore operatore = null;
 		String operatoreText = null;
		ref = new CellReference("B360");
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				operatoreText = c.getStringCellValue();
				log.debug("Operatore estratto da file Excel: {}", operatoreText);
				String[] operatoreTextParts = operatoreText.split(" ");
				
				String cognome = null;
				// caso
				if(operatoreTextParts.length > 2) {
					cognome = operatoreTextParts[2];
				} else if(operatoreTextParts.length == 2) {
					cognome = operatoreTextParts[1];
				} else if(operatoreTextParts.length == 1) {
					//potebbe esserci solo il cognome?
					cognome = operatoreTextParts[0];
				}
				
				if(cognome != null && !cognome.isEmpty()) {
					Optional<User> user = userRepository.findOneByLastNameIgnoreCase(cognome);
					if(user.isPresent()) {
						operatore = operatoreRepository.findOneByUserId(user.get().getId());
					} else {
						// nome e cognome invertiti
						cognome = operatoreTextParts[0];
						user = userRepository.findOneByLastNameIgnoreCase(cognome);
						if(user.isPresent()) {
							operatore = operatoreRepository.findOneByUserId(user.get().getId());
						} else {
							//più nomi
							cognome = operatoreTextParts[operatoreTextParts.length-1];
							user = userRepository.findOneByLastNameIgnoreCase(cognome);
							if(user.isPresent()) {
								operatore = operatoreRepository.findOneByUserId(user.get().getId());
							}
						}
					}
					if(user.isPresent())
						log.info("* Operatore analisi: **{}**", user.get().getLogin());
				}
					
			} else {
				log.warn("Operatore non indicato nel file Excel");
				errorList.add("Operatore analisi del campione B non inserito");
			}
		}
		if(operatore == null) {
			log.warn("Impossibile determinare operatore: {}. L'operatore indicato verrà inserito nelle note.", operatoreText);
		}
		
		// checkbox vari
		boolean trueFalse = false;
		
		ref = new CellReference("L46");
		Boolean ripartizioneQuartatura = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				ripartizioneQuartatura = trueFalse;
			}
		}
		log.info("* Ripartizione o quartatura: **{}**", ripartizioneQuartatura);
		ref = new CellReference("L47");
		Boolean essiccamento = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				essiccamento = trueFalse;
			}
		}
		log.info("* Essiccamento: **{}**", essiccamento);
		ref = new CellReference("L48");
		Boolean setacciaturaSecco = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				setacciaturaSecco = trueFalse;
			}
		}
		log.info("* Setacciatura a secco: **{}**", setacciaturaSecco);
		ref = new CellReference("L49");
		Boolean lavaggioSetacciatura = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				lavaggioSetacciatura = trueFalse;
			}
		}
		log.info("* Lavaggio e setacciatura: **{}**", lavaggioSetacciatura);
		
		//caratteristiche
		ref = new CellReference("L51");
		Boolean ghiaia = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				ghiaia = trueFalse;
			}
		}
		log.info("* Ghiaia: **{}**", ghiaia);
		
		ref = new CellReference("L52");
		Boolean matRisulta = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				matRisulta = trueFalse;
			}
		}
		log.info("* Materiale risulta: **{}**", matRisulta);
		
		ref = new CellReference("L53");
		Boolean detriti = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				detriti = trueFalse;
			}
		}
		log.info("* Detriti: **{}**", detriti);
		
		ref = new CellReference("L54");
		Boolean sabbia = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				sabbia = trueFalse;
			}
		}
		log.info("* Sabbia: **{}**", sabbia);
		
		ref = new CellReference("L55");
		Boolean materialeFine = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				materialeFine = trueFalse;
			}
		}
		log.info("* Materiale fine: **{}**", materialeFine);
		
		ref = new CellReference("L56");
		Boolean materialeOrganico = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				materialeOrganico = trueFalse;
			}
		}
		log.info("* Materiale organico: **{}**", materialeOrganico);
		
		ref = new CellReference("L57");
		Boolean detritiConglomerato = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				detritiConglomerato = trueFalse;
			}
		}
		log.info("* Detriti conglomerato: **{}**", detritiConglomerato);
		
		ref = new CellReference("L58");
		Boolean argillaAlt = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				argillaAlt = trueFalse;
			}
		}
		log.info("* Argilla: **{}**", argillaAlt);
		
		ref = new CellReference("L59");
		Boolean elementiArrotondati = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				elementiArrotondati = trueFalse;
			}
		}
		log.info("* Elementi arrotondati: **{}**", elementiArrotondati);
		
		ref = new CellReference("L60");
		Boolean elementiSpigolosi = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				elementiSpigolosi = trueFalse;
			}
		}
		log.info("* Elementi spigolosi: **{}**", elementiSpigolosi);
		
		ref = new CellReference("L61");
		Boolean sfabbricidi = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				sfabbricidi = trueFalse;
			}
		}
		log.info("* Sfabbricidi: **{}**", sfabbricidi);
		
		// foto sacchetto e campione
//		byte[] fotoSacchetto = null;
//		String fotoSacchettoContentType = null;
//		byte[] fotoCampione = null;
//		String fotoCampioneContentType = null;
		
		String noteB = "";
		
		String noteBLavorazione = "";
		ref = new CellReference("I26");
		row = lavorazioneSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				noteBLavorazione = c.getStringCellValue();
				log.info("* Note lavorazione: {}", noteBLavorazione);
			}
		}
		
		ref = new CellReference("A408");
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK) && (c.getCellTypeEnum() == CellType.STRING)) {
				noteB = noteBLavorazione + "   " + c.getStringCellValue();
			}
		}
		
		XSSFDrawing drawing = rapportoSheet.createDrawingPatriarch();
        for (XSSFShape shape : drawing.getShapes()) {
            if (shape instanceof XSSFPicture) {
                XSSFPicture picture = (XSSFPicture) shape;
                XSSFPictureData xssfPictureData = picture.getPictureData();
                
                if(xssfPictureData == null)
                	continue;
                
                ClientAnchor anchor = picture.getPreferredSize();
                int row1 = anchor.getRow1();
//                int row2 = anchor.getRow2();
                int col1 = anchor.getCol1();
//                int col2 = anchor.getCol2();
                
                if (row1 > 300 && row1 < 450 && col1 <= 2) { //foto sacchetto A1
                	currentFotoSacchettoContentType = "image/" + xssfPictureData.suggestFileExtension();
					log.debug("Foto sacchetto tipo: {}", currentFotoSacchettoContentType);
					currentFotoSacchetto = xssfPictureData.getData();
			        log.debug("Foto sacchetto length: {}", currentFotoSacchetto.length);
                }
                
                if (row1 > 300 && row1 < 450 && col1 > 2) { //foto campione A1
                	currentFotoCampioneContentType = "image/" + xssfPictureData.suggestFileExtension();
					log.debug("Foto campione tipo: {}", currentFotoCampioneContentType);
					currentFotoCampione = xssfPictureData.getData();
			        log.debug("Foto campione length: {}", currentFotoCampione.length);
                }
            }
            if (shape instanceof XSSFSimpleShape){
            	XSSFSimpleShape textBox = (XSSFSimpleShape) shape;
            	XSSFClientAnchor anchor = (XSSFClientAnchor) textBox.getAnchor();
            	int row1 = anchor.getRow1();
//                int row2 = anchor.getRow2();
//                int col1 = anchor.getCol1();
//                int col2 = anchor.getCol2();
            	
                if (row1 > 350 && row1 < 450) {
                	if(!textBox.getText().startsWith("CURVA")) {
                		noteB = noteBLavorazione + "   " + textBox.getText();
                		if(noteB != null && !noteB.isEmpty())
                    		log.info("* Note campione B: {}", noteB);
                	}
                }
                
            }
        }
        if(noteB == null || noteB.isEmpty()) {
    		ref = new CellReference("A408");
    		row = rapportoSheet.getRow(ref.getRow());
    		if (row != null) {
    			Cell c = row.getCell(ref.getCol());
    			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
    				noteB = c.getStringCellValue();
    				log.info("* Note campione B: {}", noteB);
    			}
    		}
        }
		
		// pesatura
        log.info("#### Dati pesatura");
		ref = new CellReference("B5");
		row = pesaturaSheet.getRow(ref.getRow());
		String essiccamentoNumTeglia = null;
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if (c != null && (c.getCellTypeEnum() == CellType.NUMERIC)) {
				log.trace("Il campo è numerico (double), presumo sia senza virgola: {}",
						df.format(c.getNumericCellValue()));
				essiccamentoNumTeglia = df.format(c.getNumericCellValue());
			} else if (c != null && (c.getCellTypeEnum() == CellType.STRING)) {
				essiccamentoNumTeglia = c.getStringCellValue();
			} else if (c != null && (c.getCellTypeEnum() == CellType.FORMULA)) { 
				switch(c.getCachedFormulaResultType()) {
	            case Cell.CELL_TYPE_NUMERIC:
	            	essiccamentoNumTeglia = df2.format(c.getNumericCellValue());
	                break;
	            case Cell.CELL_TYPE_STRING:
	            	essiccamentoNumTeglia = c.getStringCellValue();
	                break;
		        }
			} else {
				log.trace("Impossibile determinare numero teglia!");
			}
		}
		log.info("* Numero teglia: **{}**", essiccamentoNumTeglia);
		
		ref = new CellReference("F5");
		row = pesaturaSheet.getRow(ref.getRow());
		String essiccamentoPesoTeglia = "";
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if (c != null && (c.getCellTypeEnum() == CellType.NUMERIC)) {
				log.trace("Il campo è numerico (double): {}",
						df2.format(c.getNumericCellValue()));
				essiccamentoPesoTeglia = df2.format(c.getNumericCellValue());
			} else if (c != null && (c.getCellTypeEnum() == CellType.STRING)) {
				essiccamentoPesoTeglia = c.getStringCellValue();
			} else if (c != null && (c.getCellTypeEnum() == CellType.FORMULA)) { 
				switch(c.getCachedFormulaResultType()) {
	            case Cell.CELL_TYPE_NUMERIC:
	            	essiccamentoPesoTeglia = df2.format(c.getNumericCellValue());
	                break;
	            case Cell.CELL_TYPE_STRING:
	            	essiccamentoPesoTeglia = c.getStringCellValue();
	                break;
		        }
			} else {
				log.warn("Impossibile determinare peso teglia!");
			}
		}
		log.info("* Peso teglia: **{}**", essiccamentoPesoTeglia);
		
		ref = new CellReference("G5");
		row = pesaturaSheet.getRow(ref.getRow());
		String essiccamentoPesoCampioneLordoIniziale = null;
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if (c != null && (c.getCellTypeEnum() == CellType.NUMERIC)) {
				log.trace("Il campo è numerico (double): {}",
						df2.format(c.getNumericCellValue()));
				essiccamentoPesoCampioneLordoIniziale = df2.format(c.getNumericCellValue());
			} else if (c != null && (c.getCellTypeEnum() == CellType.STRING)) {
				essiccamentoPesoCampioneLordoIniziale = c.getStringCellValue();
			} else if (c != null && (c.getCellTypeEnum() == CellType.FORMULA)) { 
				switch(c.getCachedFormulaResultType()) {
	            case Cell.CELL_TYPE_NUMERIC:
	            	essiccamentoPesoCampioneLordoIniziale = df2.format(c.getNumericCellValue());
	                break;
	            case Cell.CELL_TYPE_STRING:
	            	essiccamentoPesoCampioneLordoIniziale = c.getStringCellValue();
	                break;
		        }
			} else {
				log.warn("Impossibile determinare peso campione lordo iniziale!");
				errorList.add("Impossibile determinare peso campione B lordo iniziale");
			}
		}
		log.info("* Peso campione lordo iniziale: **{}**", essiccamentoPesoCampioneLordoIniziale);
		
		ref = new CellReference("I5");
		row = pesaturaSheet.getRow(ref.getRow());
		String essiccamentoPesoCampioneLordo24H = null;
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if (c != null && (c.getCellTypeEnum() == CellType.NUMERIC)) {
				log.trace("Il campo è numerico (double): {}",
						df2.format(c.getNumericCellValue()));
				essiccamentoPesoCampioneLordo24H = df2.format(c.getNumericCellValue());
			} else if (c != null && (c.getCellTypeEnum() == CellType.STRING)) {
				essiccamentoPesoCampioneLordo24H = c.getStringCellValue();
			} else if (c != null && (c.getCellTypeEnum() == CellType.FORMULA)) { 
				switch(c.getCachedFormulaResultType()) {
	            case Cell.CELL_TYPE_NUMERIC:
	            	essiccamentoPesoCampioneLordo24H = df2.format(c.getNumericCellValue());
	                break;
	            case Cell.CELL_TYPE_STRING:
	            	essiccamentoPesoCampioneLordo24H = c.getStringCellValue();
	                break;
		        }
			} else {
				log.warn("Impossibile determinare peso campione lordo 24h!");
				errorList.add("Impossibile determinare peso campione B lordo 24h");
			}
		}
		log.info("* Peso campione lordo 24h: **{}**", essiccamentoPesoCampioneLordo24H);
		
		ref = new CellReference("L5");
		row = pesaturaSheet.getRow(ref.getRow());
		String essiccamentoPesoCampioneLordo48H = null;
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if (c != null && (c.getCellTypeEnum() == CellType.NUMERIC)) {
				log.trace("Il campo è numerico (double): {}",
						df2.format(c.getNumericCellValue()));
				essiccamentoPesoCampioneLordo48H = df2.format(c.getNumericCellValue());
			} else if (c != null && (c.getCellTypeEnum() == CellType.STRING)) {
				essiccamentoPesoCampioneLordo48H = c.getStringCellValue();
			} else if (c != null && (c.getCellTypeEnum() == CellType.FORMULA)) { 
				switch(c.getCachedFormulaResultType()) {
	            case Cell.CELL_TYPE_NUMERIC:
	            	essiccamentoPesoCampioneLordo48H = df2.format(c.getNumericCellValue());
	                break;
	            case Cell.CELL_TYPE_STRING:
	            	essiccamentoPesoCampioneLordo48H = c.getStringCellValue();
	                break;
		        }
			} else {
				log.warn("Impossibile determinare peso campione lordo 48h!");
				errorList.add("Impossibile determinare peso campione B lordo 48h");
			}
		}
		log.info("* Peso campione lordo 48h: **{}**", essiccamentoPesoCampioneLordo48H);
		
		
		
		if((siglaCampione == null || siglaCampione.isEmpty()) &&
				dataAnalisi == null &&
				essiccamentoPesoCampioneLordoIniziale == null) {
			log.warn("Mancano sigla, data analisi e peso iniziale del campione, lo considero mancante e non lo inserisco nel db.");
			errorList.add("Mancano sigla, data analisi e peso iniziale del campione B, lo considero mancante e non lo inserisco nel db.");

			if(errorMap.get(currentPath) == null) {
				errorMap.put(currentPath, errorList);
			} else {
				for(String err : errorList) {
					errorMap.get(currentPath).add(err);
				}
			}
			
			return null;
		}
		
		
		
		Campione campione = new Campione();
		campione.setTipoCampione(TipoCampione.B);
		campione.setCassetta(cassetta);
		
		if(!siglaCampione.isEmpty()) {
			try {
				campione.setSiglaCampione(Integer.valueOf(siglaCampione));
			} catch (Exception e) {
				log.debug("La sigla del campione non è un numero: {}", siglaCampione);
			}
		}
		
		campione.setRipartizioneQuartatura(ripartizioneQuartatura);
		campione.setEssiccamento(essiccamento);
		campione.setSetacciaturaSecco(setacciaturaSecco);
		campione.setLavaggioSetacciatura(lavaggioSetacciatura);
		
		campione.setGhiaia(ghiaia);
		campione.setMaterialeRisultaVagliato(matRisulta);
		campione.setDetriti(detriti);
		campione.setSabbia(sabbia);
		campione.setMaterialeFine(materialeFine);
		campione.setMaterialeOrganico(materialeOrganico);
		campione.setDetritiConglomerato(detritiConglomerato);
		campione.setArgillaMatAlterabile(argillaAlt);
		campione.setElementiArrotondati(elementiArrotondati);
		campione.setElementiSpigolosi(elementiSpigolosi);
		campione.setSfabbricidi(sfabbricidi);
		
		campione.setDataAnalisi(dataAnalisi);
		campione.setOperatoreAnalisi(operatore);
		
		campione.setEssiccamentoNumTeglia(essiccamentoNumTeglia);
		if(essiccamentoPesoTeglia != null && !essiccamentoPesoTeglia.isEmpty())
			campione.setEssiccamentoPesoTeglia(new BigDecimal(essiccamentoPesoTeglia.replace(",", ".")));
		if(essiccamentoPesoCampioneLordoIniziale != null && !essiccamentoPesoCampioneLordoIniziale.isEmpty())
			campione.setEssiccamentoPesoCampioneLordoIniziale(new BigDecimal(essiccamentoPesoCampioneLordoIniziale.replace(",", ".")));
		if(essiccamentoPesoCampioneLordo24H != null && !essiccamentoPesoCampioneLordo24H.isEmpty())
			campione.setEssiccamentoPesoCampioneLordo24H(new BigDecimal(essiccamentoPesoCampioneLordo24H.replace(",", ".")));
		if(essiccamentoPesoCampioneLordo48H != null && !essiccamentoPesoCampioneLordo48H.isEmpty())
			campione.setEssiccamentoPesoCampioneLordo48H(new BigDecimal(essiccamentoPesoCampioneLordo48H.replace(",", ".")));
		
		if(operatore == null && operatoreText != null && !operatoreText.isEmpty()) {
			campione.setNote(noteB + " ###" + operatoreText);
		} else {
			campione.setNote(noteB);
		}
		
		campione.setLavorazioneConclusa(true);
		
		String codiceCampione = campione.getCassetta().getCodiceCassetta() + "-" + campione.getTipoCampione();
		campione.setCodiceCampione(codiceCampione);
		
		campione = campioneRepository.save(campione);
		
		
		// setacciatura
		log.info("#### Dati setacciatura");
		int numPesata;
		boolean lastRow = false;
		for(numPesata = 1; numPesata < 6; numPesata++) {
			ArrayList<BigDecimal> seriePesate = new ArrayList<BigDecimal>();
			
			for(char col='B';col<='G';col++) {
				ref = new CellReference(Character.toString(col) + (numPesata + 25));
				row = lavorazioneSheet.getRow(ref.getRow());
				String peso = null;
				if (row != null) {
					Cell c = row.getCell(ref.getCol());
					//log.debug("Cella: {}", ref);
					if (c.getCellTypeEnum() == CellType.NUMERIC) {
						log.trace("Il campo è numerico (double): {}",
								df2.format(c.getNumericCellValue()));
						peso = df2.format(c.getNumericCellValue());
					} else if (c.getCellTypeEnum() == CellType.STRING) {
						log.trace("Il campo è stringa: {}", c.getStringCellValue());
						peso = c.getStringCellValue();
					} else if(c.getCellTypeEnum() == CellType.FORMULA) { 
						switch(c.getCachedFormulaResultType()) {
				            case Cell.CELL_TYPE_NUMERIC:
				            	log.trace("Il campo è numerico (double): {}",
										df2.format(c.getNumericCellValue()));
								peso = df2.format(c.getNumericCellValue());
				                break;
				            case Cell.CELL_TYPE_STRING:
				            	log.trace("Il campo è stringa: {}", c.getStringCellValue());
								peso = c.getStringCellValue();
				                break;
				        }
					} else {
						
						if(col=='B') {
							break; // non ci sono più pesate
						} else {
							// probabilmente la cella è vuota
							peso = "0";
						}
					}
					
					if(peso != null) {
						seriePesate.add(new BigDecimal(peso.replace(",", ".")));
					} else {
						seriePesate.add(BigDecimal.ZERO);
					}
				}
				//log.info("* Peso netto essiccazione, pesata {}: **{}**", numPesata, peso);
			}
			if(seriePesate.size() > 0) {
				Pesata pesata = insertPesataB(campione, numPesata, seriePesate);
				log.info("* Inserita setacciatura n. {}", pesata.getNumPesata());
			}
			
			if(lastRow)
				break;
			
		}
		
		
		/*
		 * Se per il campione B non ci sono setacciature o c'è qualche altro problema (peso netto non inserito...)
		 * bisogna prendere nel rapporto la cella D402 che contiene la percentuale di passante inserita manualmente, e da cui dipende la conformità indicata
		 * nel rapporto. In base alla percentuale generare una pesata farlocca con peso netto=100 e trattenuto a 100 = 100-percPassante
		 */
		if((numPesata - 1) == 0) {
			log.warn("Nessuna setacciatura inserita per il campione B. Dati mancanti o insufficienti.");
			
			ref = new CellReference("D402");
			row = rapportoSheet.getRow(ref.getRow());
			String perc100 = null;
			if (row != null) {
				Cell c = row.getCell(ref.getCol());
				if (c.getCellTypeEnum() == CellType.NUMERIC) {
					perc100 = df2.format(c.getNumericCellValue());
				} else if (c.getCellTypeEnum() == CellType.STRING) {
					perc100 = c.getStringCellValue();
				} else {
					log.error("Impossibile determinare la percentuale di passante a 100 mm indicata nella cella D402 del foglio Rapporto!");
					errorList.add("Nessuna setacciatura inserita per il campione B oppure dati insufficienti, inoltre è stato impossibile determinare la percentuale di passante a 100 mm indicata nella cella D402 del foglio Rapporto.");
				}
			}
			if(perc100 != null) {
				log.info("* Percentuale di passante a 100 mm indicata nella cella D402 del foglio Rapporto: {}", perc100);
				
				BigDecimal percTratt100 = new BigDecimal("100").subtract(new BigDecimal(perc100.replace(",", ".")));
				
				ArrayList<BigDecimal> serieFarloccaPesate = new ArrayList<BigDecimal>();
				serieFarloccaPesate.add(new BigDecimal("100"));
				serieFarloccaPesate.add(percTratt100);
				serieFarloccaPesate.add(BigDecimal.ZERO);
				serieFarloccaPesate.add(BigDecimal.ZERO);
				serieFarloccaPesate.add(BigDecimal.ZERO);
				serieFarloccaPesate.add(BigDecimal.ZERO);
				
				insertPesataB(campione, numPesata, serieFarloccaPesate);
				
				log.info("* Inserita setacciatura fittizia n. {}", numPesata);
				
				// classificazione geotecnica
		        String classGeoTecManuale = null;
				ref = new CellReference("A405");
				row = rapportoSheet.getRow(ref.getRow());
				if (row != null) {
					Cell c = row.getCell(ref.getCol());
					if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
						classGeoTecManuale = c.getStringCellValue();
						log.info("* Classificazione geotecnica estratta dal rapporto alla cella A405: {}", classGeoTecManuale);
						campione.setDescrizioneCampione(classGeoTecManuale);
						campione = campioneRepository.save(campione);
					}
				}
			}
			
		}
		
		if((numPesata - 1) > 0) {
			ClassificazioneGeotecnicaDTO dto = null;
			try {
				dto = campioneService.generateClassificazioneGeotecnica(campione);
			} catch (CustomParameterizedException ex) {
				log.warn("Non è stato possibile generare la classificazione geotecnica: {}", ex.getMessage());
				//errorList.add("Non è stato possibile generare la classificazione geotecnica del campione B in base alle setacciature riportate nel foglio Lavorazione");
			}
			
			if(dto != null) {
		    	campione.setClassificazioneGeotecnica(dto.getClassificazioneGeotecnica());
		    	campione = campioneRepository.save(campione);
			} else { // campione B con setacciatura solo a 100 o senza setacciature
				String classGeoTecManuale = null;
				ref = new CellReference("A405");
				row = rapportoSheet.getRow(ref.getRow());
				if (row != null) {
					Cell c = row.getCell(ref.getCol());
					if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
						classGeoTecManuale = c.getStringCellValue();
						log.info("* * Classificazione geotecnica estratta dal rapporto alla cella A405: {}", classGeoTecManuale);
						campione.setDescrizioneCampione(classGeoTecManuale);
						campione = campioneRepository.save(campione);
					}
				}
			}
		}
		
		// blob
//		campione.setFotoSacchetto(fotoSacchetto);
//		campione.setFotoSacchettoContentType(fotoSacchettoContentType);
//		campione = campioneRepository.save(campione);
//		
//		campione.setFotoCampione(fotoCampione);
//		campione.setFotoCampioneContentType(fotoCampioneContentType);
//		campione = campioneRepository.save(campione);
		
		
		
		if(errorList.size() > 0) {
			if(errorMap.get(currentPath) == null) {
				errorMap.put(currentPath, errorList);
			} else {
				for(String err : errorList) {
					errorMap.get(currentPath).add(err);
				}
			}
		}
		
		return campione;
	}
	
	public Pesata insertPesataB(Campione campione, int numPesata, ArrayList<BigDecimal> seriePesate) {
		Pesata pesata = new Pesata();
		
		pesata.setCampione(campione);
		pesata.setNumPesata(numPesata);
		pesata.setPesoNetto(seriePesate.get(0));
		pesata.setTrattB100Mm(seriePesate.get(1));
		pesata.setTrattB6V3Mm(seriePesate.get(2));
		pesata.setTrattB2Mm(seriePesate.get(3));
		pesata.setTrattB0V5Mm(seriePesate.get(4));
		pesata.setFondo(seriePesate.get(5));
		
		pesata.setTratt125Mm(new BigDecimal("0"));
		pesata.setTratt63Mm(new BigDecimal("0"));
		pesata.setTratt31V5Mm(new BigDecimal("0"));
		pesata.setTratt16Mm(new BigDecimal("0"));
		pesata.setTratt8Mm(new BigDecimal("0"));
		pesata.setTratt6V3Mm(new BigDecimal("0"));
		pesata.setTratt4Mm(new BigDecimal("0"));
		pesata.setTratt2Mm(new BigDecimal("0"));
		pesata.setTratt1Mm(new BigDecimal("0"));
		pesata.setTratt0V5Mm(new BigDecimal("0"));
		pesata.setTratt0V25Mm(new BigDecimal("0"));
		pesata.setTratt0V125Mm(new BigDecimal("0"));
		pesata.setTratt0V075Mm(new BigDecimal("0"));
		
		Pesata result = pesataRepository.save(pesata);
		
		return result;
	}
	
	@SuppressWarnings("deprecation")
	public Campione insertCampioneC(Cassetta cassetta, XSSFWorkbook workbook) {
		List<String> errorList = new ArrayList<String>();
		
		log.info("### Creazione e inserimento campione C relativo alla cassetta {}", cassetta.getCodiceCassetta());
		
//		XSSFSheet accettazioneSheet = workbook.getSheet("Dati input accettazione");
//		XSSFSheet pesaturaSheet = workbook.getSheet("Pesatura");
//		XSSFSheet lavorazioneSheet = workbook.getSheet("Lavorazione");
		XSSFSheet rapportoSheet = workbook.getSheet("Rapporto di prova");
	
		DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(0);
        
        DecimalFormat df2 = new DecimalFormat("#");
        df2.setMaximumFractionDigits(2);
		
        //sigla campione
        CellReference ref = new CellReference("F458");
        Row row = rapportoSheet.getRow(ref.getRow());
  		String siglaCampione = "";
  		if (row != null) {
  			Cell c = row.getCell(ref.getCol());
  			if(c.getCellTypeEnum() == CellType.NUMERIC) {
  				log.trace("Il campo è numerico (double), presumo sia senza virgola: {}", df.format(c.getNumericCellValue()));
  				siglaCampione = df.format(c.getNumericCellValue());
  			} else if(c.getCellTypeEnum() == CellType.STRING) {
  				siglaCampione = c.getStringCellValue();
  			} else if (c.getCellTypeEnum() == CellType.FORMULA) { 
  				switch(c.getCachedFormulaResultType()) {
  	            case Cell.CELL_TYPE_NUMERIC:
  	            	siglaCampione = df.format(c.getNumericCellValue());
  	                break;
  	            case Cell.CELL_TYPE_STRING:
  	            	siglaCampione = c.getStringCellValue();
  	                break;
  		        }
  			} else {
  				log.warn("Impossibile determinare sigla campione!");
  				errorList.add("Impossibile determinare la sigla del campione C");
  			}
  		}
  		log.info("* Sigla campione: **{}**", siglaCampione);
        
        // data lavorazione
        ref = new CellReference("F456");
  		LocalDate dataAnalisi = null;
  		row = rapportoSheet.getRow(ref.getRow());
  		if (row != null) {
  			Cell c = row.getCell(ref.getCol());
  			
  			if(c != null && c.getCellTypeEnum() == CellType.NUMERIC) {
  				if (DateUtil.isCellDateFormatted(c)) {
  					log.debug("Data di analisi estratta dal file: {}", c.getDateCellValue().toString());
  	                dataAnalisi = c.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
  	                log.debug("Data di analisi convertita in LocalDate: {}", dataAnalisi.toString());
  	            }
  			} else if(c.getCellTypeEnum() == CellType.STRING) {
  				log.debug("La data è in formato testuale e va convertita: {}", c.getStringCellValue());
  				DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALIAN);
  				DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd/MM/yy", Locale.ITALIAN);
  				
  				try {
 					dataAnalisi = LocalDate.parse(c.getStringCellValue(), formatter1);
 				} catch (DateTimeParseException e) {
 					log.debug("La data non è in formato dd/MM/yyyy");
 				}
 				
 				try {
 					dataAnalisi = LocalDate.parse(c.getStringCellValue(), formatter2);
 				} catch (DateTimeParseException e) {
 					log.debug("La data non è in formato dd/MM/yy");
 				}
 				
 				if(dataAnalisi != null)
 					log.debug("Data di consegna testuale convertita in LocalDate: {}", dataAnalisi.toString());
  				
  			} else {
  				log.warn("Impossibile determinare la data di lavorazione!");
  				errorList.add("Impossibile determinare la data di lavorazione del campione C");
  			}
  		}
  		log.info("* Data analisi: **{}**", dataAnalisi);
  		
  		// operatore analisi
  		Operatore operatore = null;
  		String operatoreText = null;
 		ref = new CellReference("B456");
 		row = rapportoSheet.getRow(ref.getRow());
 		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				operatoreText = c.getStringCellValue();
				log.debug("Operatore estratto da file Excel: {}", operatoreText);
				String[] operatoreTextParts = operatoreText.split(" ");
				
				String cognome = null;
				// caso
				if(operatoreTextParts.length > 2) {
					cognome = operatoreTextParts[2];
				} else if(operatoreTextParts.length == 2) {
					cognome = operatoreTextParts[1];
				} else if(operatoreTextParts.length == 1) {
					//potebbe esserci solo il cognome?
					cognome = operatoreTextParts[0];
				}
				
				if(cognome != null && !cognome.isEmpty()) {
					Optional<User> user = userRepository.findOneByLastNameIgnoreCase(cognome);
					if(user.isPresent()) {
						operatore = operatoreRepository.findOneByUserId(user.get().getId());
					} else {
						// nome e cognome invertiti
						cognome = operatoreTextParts[0];
						user = userRepository.findOneByLastNameIgnoreCase(cognome);
						if(user.isPresent()) {
							operatore = operatoreRepository.findOneByUserId(user.get().getId());
						} else {
							//più nomi
							cognome = operatoreTextParts[operatoreTextParts.length-1];
							user = userRepository.findOneByLastNameIgnoreCase(cognome);
							if(user.isPresent()) {
								operatore = operatoreRepository.findOneByUserId(user.get().getId());
							}
						}
					}
					if(user.isPresent())
						log.info("* Operatore analisi: **{}**", user.get().getLogin());
				}
					
			} else {
				log.warn("Operatore non indicato nel file Excel");
				errorList.add("Operatore analisi del campione C non inserito");
			}
		}
		if(operatore == null) {
			log.warn("Impossibile determinare operatore: {}. L'operatore indicato verrà inserito nelle note.", operatoreText);
		}
		
		
		//caratteristiche
		boolean trueFalse = false;
		
		ref = new CellReference("L63");
		Boolean ghiaia = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				ghiaia = trueFalse;
			}
		}
		log.info("* Ghiaia: **{}**", ghiaia);
		
		ref = new CellReference("L64");
		Boolean detriti = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				detriti = trueFalse;
			}
		}
		log.info("* Detriti: **{}**", detriti);
		
		ref = new CellReference("L65");
		Boolean sabbia = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				sabbia = trueFalse;
			}
		}
		log.info("* Sabbia: **{}**", sabbia);
		
		ref = new CellReference("L66");
		Boolean materialeOrganico = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				materialeOrganico = trueFalse;
			}
		}
		log.info("* Materiale organico: **{}**", materialeOrganico);
		
		ref = new CellReference("L67");
		Boolean detritiConglomerato = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				detritiConglomerato = trueFalse;
			}
		}
		log.info("* Detriti conglomerato: **{}**", detritiConglomerato);
		
		ref = new CellReference("L68");
		Boolean argillaMatAlt = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				argillaMatAlt = trueFalse;
			}
		}
		log.info("* Argilla e materiale alterabile: **{}**", argillaMatAlt);
		
		ref = new CellReference("L69");
		Boolean granuliCementati = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				granuliCementati = trueFalse;
			}
		}
		log.info("* Granuli cementati: **{}**", granuliCementati);
		
		ref = new CellReference("L70");
		Boolean elementiSpigolosi = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				elementiSpigolosi = trueFalse;
			}
		}
		log.info("* Elementi spigolosi: **{}**", elementiSpigolosi);
		
		ref = new CellReference("L71");
		Boolean sfabbricidi = null;
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				trueFalse = c.getBooleanCellValue();
				sfabbricidi = trueFalse;
			}
		}
		log.info("* Sfabbricidi: **{}**", sfabbricidi);
		
		// foto sacchetto e campione + note
//		byte[] fotoSacchetto = null;
//		String fotoSacchettoContentType = null;
//		byte[] fotoCampione = null;
//		String fotoCampioneContentType = null;
		
		String noteC = null;
		
		XSSFDrawing drawing = rapportoSheet.createDrawingPatriarch();
        for (XSSFShape shape : drawing.getShapes()) {
            if (shape instanceof XSSFPicture) {
                XSSFPicture picture = (XSSFPicture) shape;
                XSSFPictureData xssfPictureData = picture.getPictureData();
                
                if(xssfPictureData == null)
                	continue;
                
                ClientAnchor anchor = picture.getPreferredSize();
                int row1 = anchor.getRow1();
//                int row2 = anchor.getRow2();
                int col1 = anchor.getCol1();
//                int col2 = anchor.getCol2();
                
                if (row1 > 450 && row1 < 550 && col1 <= 2) { //foto sacchetto C
                	currentFotoSacchettoContentType = "image/" + xssfPictureData.suggestFileExtension();
					log.debug("Foto sacchetto tipo: {}", currentFotoSacchettoContentType);
					currentFotoSacchetto = xssfPictureData.getData();
			        log.debug("Foto sacchetto length: {}", currentFotoSacchetto.length);
                }
                
                if (row1 > 450 && row1 < 550 && col1 > 2) { //foto campione C
                	currentFotoCampioneContentType = "image/" + xssfPictureData.suggestFileExtension();
					log.debug("Foto campione tipo: {}", currentFotoCampioneContentType);
					currentFotoCampione = xssfPictureData.getData();
			        log.debug("Foto campione length: {}", currentFotoCampione.length);
                }
            }
            if (shape instanceof XSSFSimpleShape){
            	XSSFSimpleShape textBox = (XSSFSimpleShape) shape;
            	XSSFClientAnchor anchor = (XSSFClientAnchor) textBox.getAnchor();
            	int row1 = anchor.getRow1();
//                int row2 = anchor.getRow2();
//                int col1 = anchor.getCol1();
//                int col2 = anchor.getCol2();
            	
                if (row1 > 450 && row1 < 550) {
                	noteC = textBox.getText();
                	if(noteC != null && !noteC.isEmpty())
                		log.info("* Note campione C: {}", noteC);
                }
                
            }
        }
        if(noteC == null || noteC.isEmpty()) {
    		ref = new CellReference("A491");
    		row = rapportoSheet.getRow(ref.getRow());
    		if (row != null) {
    			Cell c = row.getCell(ref.getCol());
    			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
    				noteC = c.getStringCellValue();
    				log.info("* Note campione C: {}", noteC);
    			}
    		}
        }
        
        // classificazione geotecnica
        String classGeoTecManuale = null;
		ref = new CellReference("A488");
		row = rapportoSheet.getRow(ref.getRow());
		if (row != null) {
			Cell c = row.getCell(ref.getCol());
			if(c != null && (c.getCellTypeEnum() != CellType.BLANK)) {
				classGeoTecManuale = c.getStringCellValue();
				log.info("* Classificazione geotecnica: {}", classGeoTecManuale);
			}
		}
		
		if((siglaCampione == null || siglaCampione.isEmpty()) &&
				dataAnalisi == null &&
				classGeoTecManuale == null) {
			log.warn("Mancano sigla, data analisi e classificazione geotecnica del campione, lo considero mancante e non lo inserisco nel db.");
			errorList.add("Mancano sigla, data analisi e classificazione geotecnica del campione C, lo considero mancante e non lo inserisco nel db.");
			
			if(errorMap.get(currentPath) == null) {
				errorMap.put(currentPath, errorList);
			} else {
				for(String err : errorList) {
					errorMap.get(currentPath).add(err);
				}
			}
			
			return null;
		}
		
		Campione campione = new Campione();
		campione.setTipoCampione(TipoCampione.C);
		campione.setCassetta(cassetta);
		
		if(!siglaCampione.isEmpty()) {
			try {
				campione.setSiglaCampione(Integer.valueOf(siglaCampione));
			} catch (Exception e) {
				log.debug("La sigla del campione non è un numero: {}", siglaCampione);
			}
		}
		
		campione.setGhiaia(ghiaia);
		campione.setDetriti(detriti);
		campione.setSabbia(sabbia);
		campione.setMaterialeOrganico(materialeOrganico);
		campione.setDetritiConglomerato(detritiConglomerato);
		campione.setArgillaMatAlterabile(argillaMatAlt);
		campione.setGranuliCementati(granuliCementati);
		campione.setElementiSpigolosi(elementiSpigolosi);
		campione.setSfabbricidi(sfabbricidi);
		
		campione.setDataAnalisi(dataAnalisi);
		campione.setOperatoreAnalisi(operatore);
		
		campione.setDescrizioneCampione(classGeoTecManuale);
		
		if(operatore == null && operatoreText != null && !operatoreText.isEmpty()) {
			campione.setNote(noteC + " ###" + operatoreText);
		} else {
			campione.setNote(noteC);
		}
		
		campione.setLavorazioneConclusa(true);
		
		String codiceCampione = campione.getCassetta().getCodiceCassetta() + "-" + campione.getTipoCampione();
		campione.setCodiceCampione(codiceCampione);
		
		campione = campioneRepository.save(campione);
		
		// blob
//		campione.setFotoSacchetto(fotoSacchetto);
//		campione.setFotoSacchettoContentType(fotoSacchettoContentType);
//		campione = campioneRepository.save(campione);
//		
//		campione.setFotoCampione(fotoCampione);
//		campione.setFotoCampioneContentType(fotoCampioneContentType);
//		campione = campioneRepository.save(campione);
		
		if(errorList.size() > 0) {
			if(errorMap.get(currentPath) == null) {
				errorMap.put(currentPath, errorList);
			} else {
				for(String err : errorList) {
					errorMap.get(currentPath).add(err);
				}
			}
		}
		
		return campione;
	}
	
	
	// copiato da http://stackoverflow.com/questions/33437166/print-directory-like-tree-command
	
	public static String printDirectoryTree(File folder) {
	    if (!folder.isDirectory()) {
	        throw new IllegalArgumentException("folder is not a Directory");
	    }
	    int indent = 0;
	    StringBuilder sb = new StringBuilder();
	    printDirectoryTree(folder, indent, sb);
	    return sb.toString();
	}

	private static void printDirectoryTree(File folder, int indent,
	        StringBuilder sb) {
	    if (!folder.isDirectory()) {
	        throw new IllegalArgumentException("folder is not a Directory");
	    }
	    sb.append(getIndentString(indent));
	    sb.append("+--");
	    sb.append(folder.getName());
	    sb.append("/");
	    sb.append("\n");
	    for (File file : folder.listFiles()) {
	        if (file.isDirectory()) {
	            printDirectoryTree(file, indent + 1, sb);
	        } else {
	        	if(file.getName().toLowerCase().endsWith(".xlsm"))
	        		printFile(file, indent + 1, sb);
	        }
	    }

	}

	private static void printFile(File file, int indent, StringBuilder sb) {
	    sb.append(getIndentString(indent));
	    sb.append("+--");
	    sb.append(file.getName());
	    sb.append("\n");
	}

	private static String getIndentString(int indent) {
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < indent; i++) {
	        sb.append("|  ");
	    }
	    return sb.toString();
	}
	
}
