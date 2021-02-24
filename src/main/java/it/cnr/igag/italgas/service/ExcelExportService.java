package it.cnr.igag.italgas.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import it.cnr.igag.italgas.domain.Campione;
import it.cnr.igag.italgas.domain.Cassetta;
import it.cnr.igag.italgas.domain.Pesata;
import it.cnr.igag.italgas.domain.enumeration.TipoCampione;
import it.cnr.igag.italgas.repository.CampioneRepository;
import it.cnr.igag.italgas.repository.CassettaRepository;
import it.cnr.igag.italgas.repository.PesataRepository;

@Service
public class ExcelExportService {

	private final Logger log = LoggerFactory.getLogger(ExcelExportService.class);
	
	@Inject
    private CassettaRepository cassettaRepository;
	
	@Inject
    private CampioneRepository campioneRepository;
	
	@Inject
    private CampioneService campioneService;
	
	@Inject
    private PesataRepository pesataRepository;
	
	
	public byte[] exportData(long idCassetta) throws IOException {
		
		DecimalFormat df2 = new DecimalFormat("#");
        df2.setMaximumFractionDigits(2);
		
		Cassetta cassetta = cassettaRepository.findOne(idCassetta);
		
		ClassPathResource excelBitumeFileResource = new ClassPathResource("Lavorazione_DEFAULT_BITUME.xlsm");
		File excelBitumeFile = excelBitumeFileResource.getFile();
		
		// creo una copia del file per evitare accessi concorrenti
		String destBitumeTempDir = "/tmp/";
		String destBitumeFileName = "_" + cassetta.getCodiceIstat().getComune().replace(" ", "") + "_MsLink_" + cassetta.getRifGeografo()
			+ "_OdS_" + cassetta.getOdl() + "_ID_" + cassetta.getId() + ".xlsm";
		File destBitumeTempFile = new File(destBitumeTempDir + destBitumeFileName);
		Files.copy(excelBitumeFile.toPath(), destBitumeTempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		
		FileInputStream excelBitumeInputStream = new FileInputStream(destBitumeTempFile);
		XSSFWorkbook workbook = new XSSFWorkbook(excelBitumeInputStream);
		
		/*
		 * popolamento workbook
		 * eventualmente foto: http://www.mysamplecode.com/2012/06/apache-poi-excel-insert-image.html
		 */
		XSSFSheet accettazioneSheet = workbook.getSheet("Dati input accettazione");
		XSSFSheet pesaturaSheet = workbook.getSheet("Pesatura");
		XSSFSheet lavorazioneSheet = workbook.getSheet("Lavorazione");
		XSSFSheet rapportoSheet = workbook.getSheet("Rapporto di prova");
		XSSFSheet campioneCbASheet = workbook.getSheet("Campione CB_A");
		XSSFSheet campioneCbBSheet = workbook.getSheet("Campione CB_B");
		XSSFSheet rapportoBitumeCbASheet = workbook.getSheet("Rapporto Bitume CB_A");
		XSSFSheet rapportoBitumeCbBSheet = workbook.getSheet("Rapporto Bitume CB_B");
		
		NumberFormat bigDecimalFormatter = NumberFormat.getNumberInstance(Locale.ENGLISH);
		
		//accettazione
		XSSFCell cell = accettazioneSheet.getRow(2).getCell(4);
		cell.setCellValue(cassetta.getPrioritario() != null && cassetta.getPrioritario() ? "SI" : "NO");
		
		cell = accettazioneSheet.getRow(4).getCell(4);
		if(cassetta.getConsegna().getNumProtocolloAccettazione() != null)
			cell.setCellValue(cassetta.getConsegna().getNumProtocolloAccettazione());
		
		cell = accettazioneSheet.getRow(6).getCell(4);
		if(cassetta.getTecnicoItgResp() != null)
			cell.setCellValue(cassetta.getTecnicoItgResp());
		
		cell = accettazioneSheet.getRow(8).getCell(4);
		if(cassetta.getImpresaAppaltatrice() != null)
			cell.setCellValue(cassetta.getImpresaAppaltatrice());
		
		cell = accettazioneSheet.getRow(10).getCell(4);
		if(cassetta.getOdl() != null)
			cell.setCellValue(cassetta.getOdl());
		
		cell = accettazioneSheet.getRow(12).getCell(4);
		if(cassetta.getIncaricatoAppaltatore() != null)
			cell.setCellValue(cassetta.getIncaricatoAppaltatore());
		
		cell = accettazioneSheet.getRow(14).getCell(4);
		if(cassetta.getConsegna().getDataConsegna() != null)
			cell.setCellValue(cassetta.getConsegna().getDataConsegna().format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALIAN)));
		
		
		
		cell = accettazioneSheet.getRow(4).getCell(8);
		if(cassetta.getCentroOperativo() != null)
			cell.setCellValue(cassetta.getCentroOperativo());
		
		cell = accettazioneSheet.getRow(6).getCell(8);
		if(cassetta.getDenomCantiere() != null)
			cell.setCellValue(cassetta.getDenomCantiere());
		
		cell = accettazioneSheet.getRow(8).getCell(8);
		if(cassetta.getCodiceIstat() != null)
			cell.setCellValue(cassetta.getCodiceIstat().getComune());
		
		cell = accettazioneSheet.getRow(10).getCell(8);
		if(cassetta.getCodiceIstat() != null)
			cell.setCellValue(cassetta.getCodiceIstat().getRegione());
		
		cell = accettazioneSheet.getRow(12).getCell(8);
		if(cassetta.getCodiceIstat() != null)
			cell.setCellValue(cassetta.getCodiceIstat().getProvincia());
		
		cell = accettazioneSheet.getRow(14).getCell(8);
		if(cassetta.getRifGeografo() != null)
			cell.setCellValue(cassetta.getRifGeografo());
		
		cell = accettazioneSheet.getRow(16).getCell(8);
		if(cassetta.getNumCampioni() != null)
			cell.setCellValue(cassetta.getNumCampioni());
		
		
		
		cell = accettazioneSheet.getRow(4).getCell(12);
		if(cassetta.getCoordGpsN() != null)
			cell.setCellValue(cassetta.getCoordGpsN());
		
		cell = accettazioneSheet.getRow(6).getCell(12);
		if(cassetta.getCoordGpsE() != null)
			cell.setCellValue(cassetta.getCoordGpsE());
		
		cell = accettazioneSheet.getRow(8).getCell(12);
		if(cassetta.getDataScavo() != null)
			cell.setCellValue(cassetta.getDataScavo().format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALIAN)));
		
		
		
		//pesatura
		List<Campione> campioneList = campioneRepository.findByCassetta(cassetta);
		Campione campioneA = null;
		Campione campioneA1 = null;
		Campione campioneB = null;
		Campione campioneC = null;
		Campione campioneCB_A = null;
		Campione campioneCB_B = null;
		
		for(Campione c : campioneList) {
			if(c.getTipoCampione() == TipoCampione.A) {
				campioneA = c;
			}
			if(c.getTipoCampione() == TipoCampione.A1) {
				campioneA1 = c;
			}
			if(c.getTipoCampione() == TipoCampione.B) {
				campioneB = c;
			}
			if(c.getTipoCampione() == TipoCampione.C) {
				campioneC = c;
			}
			if(c.getTipoCampione() == TipoCampione.CB_A) {
				campioneCB_A = c;
			}
			if(c.getTipoCampione() == TipoCampione.CB_B) {
				campioneCB_B = c;
			}
		}
		
		try {
			if(campioneA != null) {
				cell = pesaturaSheet.getRow(2).getCell(1);
				if(campioneA.getEssiccamentoNumTeglia() != null)
					cell.setCellValue(campioneA.getEssiccamentoNumTeglia());
				
				cell = pesaturaSheet.getRow(2).getCell(3);
				if(campioneA.getSiglaCampione() != null)
					cell.setCellValue(campioneA.getSiglaCampione());
				
				cell = pesaturaSheet.getRow(2).getCell(4);
				if(campioneA.getTipoCampione() != null)
					cell.setCellValue(campioneA.getTipoCampione().toString());
				
				cell = pesaturaSheet.getRow(2).getCell(5);
				if(campioneA.getEssiccamentoPesoTeglia() != null)
					cell.setCellValue(campioneA.getEssiccamentoPesoTeglia().doubleValue());
				
				cell = pesaturaSheet.getRow(2).getCell(6);
				if(campioneA.getEssiccamentoPesoCampioneLordoIniziale() != null)
					cell.setCellValue(campioneA.getEssiccamentoPesoCampioneLordoIniziale().doubleValue());
				
				
				cell = pesaturaSheet.getRow(2).getCell(8);
				if(campioneA.getEssiccamentoPesoCampioneLordo24H() != null)
					cell.setCellValue(campioneA.getEssiccamentoPesoCampioneLordo24H().doubleValue());
				
				cell = pesaturaSheet.getRow(2).getCell(11);
				if(campioneA.getEssiccamentoPesoCampioneLordo48H() != null)
					cell.setCellValue(campioneA.getEssiccamentoPesoCampioneLordo48H().doubleValue());
			}
			
			if(campioneA1 != null) {
				cell = pesaturaSheet.getRow(3).getCell(1);
				if(campioneA1.getEssiccamentoNumTeglia() != null)
					cell.setCellValue(campioneA1.getEssiccamentoNumTeglia());
				
				cell = pesaturaSheet.getRow(3).getCell(3);
				if(campioneA1.getSiglaCampione() != null)
					cell.setCellValue(campioneA1.getSiglaCampione());
				
				cell = pesaturaSheet.getRow(3).getCell(4);
				if(campioneA1.getTipoCampione() != null)
					cell.setCellValue(campioneA1.getTipoCampione().toString());
				
				cell = pesaturaSheet.getRow(3).getCell(5);
				if(campioneA1.getEssiccamentoPesoTeglia() != null)
					cell.setCellValue(campioneA1.getEssiccamentoPesoTeglia().doubleValue());
				
				cell = pesaturaSheet.getRow(3).getCell(6);
				if(campioneA1.getEssiccamentoPesoCampioneLordoIniziale() != null)
					cell.setCellValue(campioneA1.getEssiccamentoPesoCampioneLordoIniziale().doubleValue());
				
				cell = pesaturaSheet.getRow(3).getCell(8);
				if(campioneA1.getEssiccamentoPesoCampioneLordo24H() != null)
					cell.setCellValue(campioneA1.getEssiccamentoPesoCampioneLordo24H().doubleValue());
				
				cell = pesaturaSheet.getRow(3).getCell(11);
				if(campioneA1.getEssiccamentoPesoCampioneLordo48H() != null)
					cell.setCellValue(campioneA1.getEssiccamentoPesoCampioneLordo48H().doubleValue());
			}
			
			if(campioneB != null) {
				cell = pesaturaSheet.getRow(4).getCell(1);
				if(campioneB.getEssiccamentoNumTeglia() != null)
					cell.setCellValue(campioneB.getEssiccamentoNumTeglia());
				
				cell = pesaturaSheet.getRow(4).getCell(3);
				if(campioneB.getSiglaCampione() != null)
					cell.setCellValue(campioneB.getSiglaCampione());
				
				cell = pesaturaSheet.getRow(4).getCell(4);
				if(campioneB.getTipoCampione() != null)
					cell.setCellValue(campioneB.getTipoCampione().toString());
				
				cell = pesaturaSheet.getRow(4).getCell(5);
				if(campioneB.getEssiccamentoPesoTeglia() != null)
					cell.setCellValue(campioneB.getEssiccamentoPesoTeglia().doubleValue());
				
				cell = pesaturaSheet.getRow(4).getCell(6);
				if(campioneB.getEssiccamentoPesoCampioneLordoIniziale() != null)
					cell.setCellValue(campioneB.getEssiccamentoPesoCampioneLordoIniziale().doubleValue());
				
				cell = pesaturaSheet.getRow(4).getCell(8);
				if(campioneB.getEssiccamentoPesoCampioneLordo24H() != null)
					cell.setCellValue(campioneB.getEssiccamentoPesoCampioneLordo24H().doubleValue());
				
				cell = pesaturaSheet.getRow(4).getCell(11);
				if(campioneB.getEssiccamentoPesoCampioneLordo48H() != null)
					cell.setCellValue(campioneB.getEssiccamentoPesoCampioneLordo48H().doubleValue());
			}
			
			
			
			// lavorazione
			if(campioneA != null) {
				cell = lavorazioneSheet.getRow(0).getCell(15);
				if(campioneA.getDataAnalisi() != null)
					cell.setCellValue(campioneA.getDataAnalisi().format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALIAN)));
				
				List<Pesata> pesateA = pesataRepository.findByCampioneOrderByNumPesataAsc(campioneA);
				
				int rowNum = 3;
				for(Pesata p : pesateA) {
					cell = lavorazioneSheet.getRow(rowNum).getCell(1);
					cell.setCellValue(p.getPesoNetto().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(2);
					cell.setCellValue(p.getTratt125Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(3);
					cell.setCellValue(p.getTratt63Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(4);
					cell.setCellValue(p.getTratt31V5Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(5);
					cell.setCellValue(p.getTratt16Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(6);
					cell.setCellValue(p.getTratt8Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(7);
					cell.setCellValue(p.getTratt6V3Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(8);
					cell.setCellValue(p.getTratt4Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(9);
					cell.setCellValue(p.getTratt2Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(10);
					cell.setCellValue(p.getTratt1Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(11);
					cell.setCellValue(p.getTratt0V5Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(12);
					cell.setCellValue(p.getTratt0V25Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(13);
					cell.setCellValue(p.getTratt0V125Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(14);
					cell.setCellValue(p.getTratt0V075Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(15);
					cell.setCellValue(p.getFondo().doubleValue());
					
					rowNum++;
				}
				
				cell = rapportoSheet.getRow(46).getCell(1);
				if(campioneA.getOperatoreAnalisi() != null) {
					cell.setCellValue(campioneA.getOperatoreAnalisi().getUser().getFirstName() + " " + campioneA.getOperatoreAnalisi().getUser().getLastName());
				} else if(campioneA.getNote() != null && !campioneA.getNote().isEmpty() && campioneA.getNote().indexOf("###") > -1) {
					String operatore = campioneA.getNote().substring(campioneA.getNote().indexOf("###")).replace("###", "");
					cell.setCellValue(operatore);
				}
				
				cell = rapportoSheet.getRow(145).getCell(0);
				if(campioneA.getNote() != null && !campioneA.getNote().isEmpty()) {
					if(campioneA.getNote().contains("###")) {
						cell.setCellValue(campioneA.getNote().substring(0, campioneA.getNote().indexOf("###")));
					} else {
						cell.setCellValue(campioneA.getNote());
					}
				}
				
			}
			
			if(campioneA1 != null) {
				cell = lavorazioneSheet.getRow(11).getCell(15);
				if(campioneA1.getDataAnalisi() != null)
					cell.setCellValue(campioneA1.getDataAnalisi().format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALIAN)));
				
				List<Pesata> pesateA1 = pesataRepository.findByCampioneOrderByNumPesataAsc(campioneA1);
				
				int rowNum = 14;
				for(Pesata p : pesateA1) {
					cell = lavorazioneSheet.getRow(rowNum).getCell(1);
					cell.setCellValue(p.getPesoNetto().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(2);
					cell.setCellValue(p.getTratt125Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(3);
					cell.setCellValue(p.getTratt63Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(4);
					cell.setCellValue(p.getTratt31V5Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(5);
					cell.setCellValue(p.getTratt16Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(6);
					cell.setCellValue(p.getTratt8Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(7);
					cell.setCellValue(p.getTratt6V3Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(8);
					cell.setCellValue(p.getTratt4Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(9);
					cell.setCellValue(p.getTratt2Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(10);
					cell.setCellValue(p.getTratt1Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(11);
					cell.setCellValue(p.getTratt0V5Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(12);
					cell.setCellValue(p.getTratt0V25Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(13);
					cell.setCellValue(p.getTratt0V125Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(14);
					cell.setCellValue(p.getTratt0V075Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(15);
					cell.setCellValue(p.getFondo().doubleValue());
					
					rowNum++;
				}
				
				cell = rapportoSheet.getRow(203).getCell(1);
				if(campioneA1.getOperatoreAnalisi() != null) {
					cell.setCellValue(campioneA1.getOperatoreAnalisi().getUser().getFirstName() + " " + campioneA1.getOperatoreAnalisi().getUser().getLastName());
				} else if(campioneA1.getNote() != null && !campioneA1.getNote().isEmpty() && campioneA1.getNote().indexOf("###") > -1) {
					String operatore = campioneA1.getNote().substring(campioneA1.getNote().indexOf("###")).replace("###", "");
					cell.setCellValue(operatore);
				}
				
				cell = rapportoSheet.getRow(301).getCell(0);
				if(campioneA1.getNote() != null && !campioneA1.getNote().isEmpty()) {
					if(campioneA1.getNote().contains("###")) {
						cell.setCellValue(campioneA1.getNote().substring(0, campioneA1.getNote().indexOf("###")));
					} else {
						cell.setCellValue(campioneA1.getNote());
					}
				}
				
			}
			
			if(campioneB != null) {
				cell = lavorazioneSheet.getRow(22).getCell(15);
				if(campioneB.getDataAnalisi() != null)
					cell.setCellValue(campioneB.getDataAnalisi().format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALIAN)));
				
				List<Pesata> pesateB = pesataRepository.findByCampioneOrderByNumPesataAsc(campioneB);
				
				int rowNum = 25;
				for(Pesata p : pesateB) {
					cell = lavorazioneSheet.getRow(rowNum).getCell(1);
					cell.setCellValue(p.getPesoNetto().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(2);
					cell.setCellValue(p.getTrattB100Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(3);
					cell.setCellValue(p.getTrattB6V3Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(4);
					cell.setCellValue(p.getTrattB2Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(5);
					cell.setCellValue(p.getTrattB0V5Mm().doubleValue());
					
					cell = lavorazioneSheet.getRow(rowNum).getCell(6);
					cell.setCellValue(p.getFondo().doubleValue());
					
					rowNum++;
				}
				
				cell = rapportoSheet.getRow(359).getCell(1);
				if(campioneB.getOperatoreAnalisi() != null) {
					cell.setCellValue(campioneB.getOperatoreAnalisi().getUser().getFirstName() + " " + campioneB.getOperatoreAnalisi().getUser().getLastName());
				} else if(campioneB.getNote() != null && !campioneB.getNote().isEmpty() && campioneB.getNote().indexOf("###") > -1) {
					String operatore = campioneB.getNote().substring(campioneB.getNote().indexOf("###")).replace("###", "");
					cell.setCellValue(operatore);
				}
				
				BigDecimal passB = campioneService.checkPassanteB(campioneB);
				
				if(passB != null) {
					cell = rapportoSheet.getRow(401).getCell(3);
					cell.setCellValue(passB.setScale(0).intValue());
				}
				
				//la classificazione geotecnica di B è nel campo descrizioneCampione se setacciatura solo a 100
				cell = rapportoSheet.getRow(404).getCell(0);
				if(campioneB.getDescrizioneCampione() != null && !campioneB.getDescrizioneCampione().trim().isEmpty())
					cell.setCellValue(campioneB.getDescrizioneCampione());
				
				cell = rapportoSheet.getRow(407).getCell(0);
				if(campioneB.getNote() != null && !campioneB.getNote().trim().isEmpty()) {
					if(campioneB.getNote().contains("###")) {
						cell.setCellValue(campioneB.getNote().substring(0, campioneB.getNote().indexOf("###")));
					} else {
						cell.setCellValue(campioneB.getNote());
					}
				}
				
			}
			
			if(campioneC != null) {
				
				cell = rapportoSheet.getRow(455).getCell(1);
				if(campioneC.getOperatoreAnalisi() != null) {
					cell.setCellValue(campioneC.getOperatoreAnalisi().getUser().getFirstName() + " " + campioneC.getOperatoreAnalisi().getUser().getLastName());
				} else if(campioneC.getNote() != null && !campioneC.getNote().isEmpty() && campioneC.getNote().indexOf("###") > -1) {
					String operatore = campioneC.getNote().substring(campioneC.getNote().indexOf("###")).replace("###", "");
					cell.setCellValue(operatore);
				}
				
				cell = rapportoSheet.getRow(455).getCell(5);
				if(campioneC.getDataAnalisi() != null)
					cell.setCellValue(campioneC.getDataAnalisi().format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALIAN)));
				
				cell = rapportoSheet.getRow(457).getCell(5);
				if(campioneC.getSiglaCampione() != null)
					cell.setCellValue(campioneC.getSiglaCampione());
				
				cell = rapportoSheet.getRow(458).getCell(5);
				cell.setCellValue("C");
				
				cell = rapportoSheet.getRow(487).getCell(0);
				if(campioneC.getDescrizioneCampione() != null && !campioneC.getDescrizioneCampione().trim().isEmpty())
					cell.setCellValue(campioneC.getDescrizioneCampione());
				
				cell = rapportoSheet.getRow(490).getCell(0);
				if(campioneC.getNote() != null && !campioneC.getNote().trim().isEmpty()) {
					if(campioneC.getNote().contains("###")) {
						cell.setCellValue(campioneC.getNote().substring(0, campioneC.getNote().indexOf("###")));
					} else {
						cell.setCellValue(campioneC.getNote());
					}
				}
				
			}
			
			//sismicità
			cell = rapportoSheet.getRow(561).getCell(2);
			if(cassetta.getMsSismicitaLocale() != null && !cassetta.getMsSismicitaLocale().trim().isEmpty())
				cell.setCellValue(cassetta.getMsSismicitaLocale());
			
			cell = rapportoSheet.getRow(563).getCell(5);
			if(cassetta.getMsValAccelerazione() != null && !cassetta.getMsValAccelerazione().trim().isEmpty())
				cell.setCellValue(cassetta.getMsValAccelerazione());
			
			
			/*
			 * ######## CB_A
			 */
			if(campioneCB_A != null) {
				cell = campioneCbASheet.getRow(2).getCell(3);
				if(campioneCB_A.getDataAnalisi() != null)
					cell.setCellValue(campioneCB_A.getDataAnalisi().format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALIAN)));
				
				//USURA
				//spessore p1
				cell = campioneCbASheet.getRow(10).getCell(2);
				if(campioneCB_A.getCbuSpessEst1p1() != null)
					cell.setCellValue(campioneCB_A.getCbuSpessEst1p1().doubleValue());
				
				cell = campioneCbASheet.getRow(10).getCell(3);
				if(campioneCB_A.getCbuSpessMedp1() != null)
					cell.setCellValue(campioneCB_A.getCbuSpessMedp1().doubleValue());
				
				cell = campioneCbASheet.getRow(10).getCell(4);
				if(campioneCB_A.getCbuSpessEst2p1() != null)
					cell.setCellValue(campioneCB_A.getCbuSpessEst2p1().doubleValue());
				
				//spessore p2
				cell = campioneCbASheet.getRow(11).getCell(2);
				if(campioneCB_A.getCbuSpessEst1p2() != null)
					cell.setCellValue(campioneCB_A.getCbuSpessEst1p2().doubleValue());
				
				cell = campioneCbASheet.getRow(11).getCell(3);
				if(campioneCB_A.getCbuSpessMedp2() != null)
					cell.setCellValue(campioneCB_A.getCbuSpessMedp2().doubleValue());
				
				cell = campioneCbASheet.getRow(11).getCell(4);
				if(campioneCB_A.getCbuSpessEst2p2() != null)
					cell.setCellValue(campioneCB_A.getCbuSpessEst2p2().doubleValue());
				
				//ssd p1
				cell = campioneCbASheet.getRow(17).getCell(2);
				if(campioneCB_A.getCbuSsdM1p1() != null)
					cell.setCellValue(campioneCB_A.getCbuSsdM1p1().doubleValue());
				
				cell = campioneCbASheet.getRow(17).getCell(3);
				if(campioneCB_A.getCbuSsdM2p1() != null)
					cell.setCellValue(campioneCB_A.getCbuSsdM2p1().doubleValue());
				
				cell = campioneCbASheet.getRow(17).getCell(4);
				if(campioneCB_A.getCbuSsdM3p1() != null)
					cell.setCellValue(campioneCB_A.getCbuSsdM3p1().doubleValue());
				
				cell = campioneCbASheet.getRow(17).getCell(5);
				if(campioneCB_A.getCbuSsdTp1() != null)
					cell.setCellValue(campioneCB_A.getCbuSsdTp1().doubleValue());
				
				//ssd p2
				cell = campioneCbASheet.getRow(18).getCell(2);
				if(campioneCB_A.getCbuSsdM1p2() != null)
					cell.setCellValue(campioneCB_A.getCbuSsdM1p2().doubleValue());
				
				cell = campioneCbASheet.getRow(18).getCell(3);
				if(campioneCB_A.getCbuSsdM2p2() != null)
					cell.setCellValue(campioneCB_A.getCbuSsdM2p2().doubleValue());
				
				cell = campioneCbASheet.getRow(18).getCell(4);
				if(campioneCB_A.getCbuSsdM3p2() != null)
					cell.setCellValue(campioneCB_A.getCbuSsdM3p2().doubleValue());
				
				cell = campioneCbASheet.getRow(18).getCell(5);
				if(campioneCB_A.getCbuSsdTp2() != null)
					cell.setCellValue(campioneCB_A.getCbuSsdTp2().doubleValue());
				
				//legante p1
				cell = campioneCbASheet.getRow(26).getCell(2);
				if(campioneCB_A.getCbuLegMmp1() != null)
					cell.setCellValue(campioneCB_A.getCbuLegMmp1().doubleValue());
				
				cell = campioneCbASheet.getRow(26).getCell(3);
				if(campioneCB_A.getCbuLegMap1() != null)
					cell.setCellValue(campioneCB_A.getCbuLegMap1().doubleValue());
				
				// mva p1
				cell = campioneCbASheet.getRow(39).getCell(2);
				if(campioneCB_A.getCbuMvaM0pic1() != null)
					cell.setCellValue(campioneCB_A.getCbuMvaM0pic1().doubleValue());
				
				cell = campioneCbASheet.getRow(39).getCell(3);
				if(campioneCB_A.getCbuMvaMpwpic1() != null)
					cell.setCellValue(campioneCB_A.getCbuMvaMpwpic1().doubleValue());
				
				cell = campioneCbASheet.getRow(39).getCell(5);
				if(campioneCB_A.getCbuMvaT1pic1() != null)
					cell.setCellValue(campioneCB_A.getCbuMvaT1pic1().doubleValue());
				
				cell = campioneCbASheet.getRow(40).getCell(2);
				if(campioneCB_A.getCbuMvaM0pic2() != null)
					cell.setCellValue(campioneCB_A.getCbuMvaM0pic2().doubleValue());
				
				cell = campioneCbASheet.getRow(40).getCell(3);
				if(campioneCB_A.getCbuMvaMpwpic2() != null)
					cell.setCellValue(campioneCB_A.getCbuMvaMpwpic2().doubleValue());
				
				cell = campioneCbASheet.getRow(40).getCell(5);
				if(campioneCB_A.getCbuMvaT1pic2() != null)
					cell.setCellValue(campioneCB_A.getCbuMvaT1pic2().doubleValue());
				
				
				cell = campioneCbASheet.getRow(45).getCell(3);
				if(campioneCB_A.getCbuMvaMpapic1() != null)
					cell.setCellValue(campioneCB_A.getCbuMvaMpapic1().doubleValue());
				
				cell = campioneCbASheet.getRow(45).getCell(5);
				if(campioneCB_A.getCbuMvaM2pic1() != null)
					cell.setCellValue(campioneCB_A.getCbuMvaM2pic1().doubleValue());
				
				cell = campioneCbASheet.getRow(45).getCell(7);
				if(campioneCB_A.getCbuMvaT2pic1() != null)
					cell.setCellValue(campioneCB_A.getCbuMvaT2pic1().doubleValue());
				
				cell = campioneCbASheet.getRow(46).getCell(3);
				if(campioneCB_A.getCbuMvaMpapic2() != null)
					cell.setCellValue(campioneCB_A.getCbuMvaMpapic2().doubleValue());
				
				cell = campioneCbASheet.getRow(46).getCell(5);
				if(campioneCB_A.getCbuMvaM2pic2() != null)
					cell.setCellValue(campioneCB_A.getCbuMvaM2pic2().doubleValue());
				
				cell = campioneCbASheet.getRow(46).getCell(7);
				if(campioneCB_A.getCbuMvaT2pic2() != null)
					cell.setCellValue(campioneCB_A.getCbuMvaT2pic2().doubleValue());
				
				//lavaggio
				cell = campioneCbASheet.getRow(62).getCell(4);
				if(campioneCB_A.getCbuPPostLav() != null)
					cell.setCellValue(campioneCB_A.getCbuPPostLav().doubleValue());
				
				
				
				//BINDER
				//spessore p1
				cell = campioneCbASheet.getRow(10).getCell(12);
				if(campioneCB_A.getCbbSpessEst1p1() != null)
					cell.setCellValue(campioneCB_A.getCbbSpessEst1p1().doubleValue());
				
				cell = campioneCbASheet.getRow(10).getCell(13);
				if(campioneCB_A.getCbbSpessMedp1() != null)
					cell.setCellValue(campioneCB_A.getCbbSpessMedp1().doubleValue());
				
				cell = campioneCbASheet.getRow(10).getCell(14);
				if(campioneCB_A.getCbbSpessEst2p1() != null)
					cell.setCellValue(campioneCB_A.getCbbSpessEst2p1().doubleValue());
				
				//spessore p2
				cell = campioneCbASheet.getRow(11).getCell(12);
				if(campioneCB_A.getCbbSpessEst1p2() != null)
					cell.setCellValue(campioneCB_A.getCbbSpessEst1p2().doubleValue());
				
				cell = campioneCbASheet.getRow(11).getCell(13);
				if(campioneCB_A.getCbbSpessMedp2() != null)
					cell.setCellValue(campioneCB_A.getCbbSpessMedp2().doubleValue());
				
				cell = campioneCbASheet.getRow(11).getCell(14);
				if(campioneCB_A.getCbbSpessEst2p2() != null)
					cell.setCellValue(campioneCB_A.getCbbSpessEst2p2().doubleValue());
				
				//ssd p1
				cell = campioneCbASheet.getRow(17).getCell(12);
				if(campioneCB_A.getCbbSsdM1p1() != null)
					cell.setCellValue(campioneCB_A.getCbbSsdM1p1().doubleValue());
				
				cell = campioneCbASheet.getRow(17).getCell(13);
				if(campioneCB_A.getCbbSsdM2p1() != null)
					cell.setCellValue(campioneCB_A.getCbbSsdM2p1().doubleValue());
				
				cell = campioneCbASheet.getRow(17).getCell(14);
				if(campioneCB_A.getCbbSsdM3p1() != null)
					cell.setCellValue(campioneCB_A.getCbbSsdM3p1().doubleValue());
				
				cell = campioneCbASheet.getRow(17).getCell(15);
				if(campioneCB_A.getCbbSsdTp1() != null)
					cell.setCellValue(campioneCB_A.getCbbSsdTp1().doubleValue());
				
				//ssd p2
				cell = campioneCbASheet.getRow(18).getCell(12);
				if(campioneCB_A.getCbbSsdM1p2() != null)
					cell.setCellValue(campioneCB_A.getCbbSsdM1p2().doubleValue());
				
				cell = campioneCbASheet.getRow(18).getCell(13);
				if(campioneCB_A.getCbbSsdM2p2() != null)
					cell.setCellValue(campioneCB_A.getCbbSsdM2p2().doubleValue());
				
				cell = campioneCbASheet.getRow(18).getCell(14);
				if(campioneCB_A.getCbbSsdM3p2() != null)
					cell.setCellValue(campioneCB_A.getCbbSsdM3p2().doubleValue());
				
				cell = campioneCbASheet.getRow(18).getCell(15);
				if(campioneCB_A.getCbbSsdTp2() != null)
					cell.setCellValue(campioneCB_A.getCbbSsdTp2().doubleValue());
				
				//legante p1
				cell = campioneCbASheet.getRow(26).getCell(12);
				if(campioneCB_A.getCbbLegMmp1() != null)
					cell.setCellValue(campioneCB_A.getCbbLegMmp1().doubleValue());
				
				cell = campioneCbASheet.getRow(26).getCell(13);
				if(campioneCB_A.getCbbLegMap1() != null)
					cell.setCellValue(campioneCB_A.getCbbLegMap1().doubleValue());
				
				//legante p2
				cell = campioneCbASheet.getRow(27).getCell(12);
				if(campioneCB_A.getCbbLegMmp2() != null)
					cell.setCellValue(campioneCB_A.getCbbLegMmp2().doubleValue());
				
				cell = campioneCbASheet.getRow(27).getCell(13);
				if(campioneCB_A.getCbbLegMap2() != null)
					cell.setCellValue(campioneCB_A.getCbbLegMap2().doubleValue());
				
				// mva p1
				cell = campioneCbASheet.getRow(39).getCell(12);
				if(campioneCB_A.getCbbMvaM0pic1p1() != null)
					cell.setCellValue(campioneCB_A.getCbbMvaM0pic1p1().doubleValue());
				
				cell = campioneCbASheet.getRow(39).getCell(13);
				if(campioneCB_A.getCbbMvaMpwpic1p1() != null)
					cell.setCellValue(campioneCB_A.getCbbMvaMpwpic1p1().doubleValue());
				
				cell = campioneCbASheet.getRow(39).getCell(15);
				if(campioneCB_A.getCbbMvaT1pic1p1() != null)
					cell.setCellValue(campioneCB_A.getCbbMvaT1pic1p1().doubleValue());
				
				cell = campioneCbASheet.getRow(40).getCell(12);
				if(campioneCB_A.getCbbMvaM0pic2p1() != null)
					cell.setCellValue(campioneCB_A.getCbbMvaM0pic2p1().doubleValue());
				
				cell = campioneCbASheet.getRow(40).getCell(13);
				if(campioneCB_A.getCbbMvaMpwpic2p1() != null)
					cell.setCellValue(campioneCB_A.getCbbMvaMpwpic2p1().doubleValue());
				
				cell = campioneCbASheet.getRow(40).getCell(15);
				if(campioneCB_A.getCbbMvaT1pic2p1() != null)
					cell.setCellValue(campioneCB_A.getCbbMvaT1pic2p1().doubleValue());
				
				
				cell = campioneCbASheet.getRow(45).getCell(13);
				if(campioneCB_A.getCbbMvaMpapic1p1() != null)
					cell.setCellValue(campioneCB_A.getCbbMvaMpapic1p1().doubleValue());
				
				cell = campioneCbASheet.getRow(45).getCell(15);
				if(campioneCB_A.getCbbMvaM2pic1p1() != null)
					cell.setCellValue(campioneCB_A.getCbbMvaM2pic1p1().doubleValue());
				
				cell = campioneCbASheet.getRow(45).getCell(17);
				if(campioneCB_A.getCbbMvaT2pic1p1() != null)
					cell.setCellValue(campioneCB_A.getCbbMvaT2pic1p1().doubleValue());
				
				cell = campioneCbASheet.getRow(46).getCell(13);
				if(campioneCB_A.getCbbMvaMpapic2p1() != null)
					cell.setCellValue(campioneCB_A.getCbbMvaMpapic2p1().doubleValue());
				
				cell = campioneCbASheet.getRow(46).getCell(15);
				if(campioneCB_A.getCbbMvaM2pic2p1() != null)
					cell.setCellValue(campioneCB_A.getCbbMvaM2pic2p1().doubleValue());
				
				cell = campioneCbASheet.getRow(46).getCell(17);
				if(campioneCB_A.getCbbMvaT2pic2p1() != null)
					cell.setCellValue(campioneCB_A.getCbbMvaT2pic2p1().doubleValue());
				
				
				// mva p2
				cell = campioneCbASheet.getRow(39).getCell(22);
				if(campioneCB_A.getCbbMvaM0pic1p2() != null)
					cell.setCellValue(campioneCB_A.getCbbMvaM0pic1p2().doubleValue());
				
				cell = campioneCbASheet.getRow(39).getCell(23);
				if(campioneCB_A.getCbbMvaMpwpic1p2() != null)
					cell.setCellValue(campioneCB_A.getCbbMvaMpwpic1p2().doubleValue());
				
				cell = campioneCbASheet.getRow(39).getCell(25);
				if(campioneCB_A.getCbbMvaT1pic1p2() != null)
					cell.setCellValue(campioneCB_A.getCbbMvaT1pic1p2().doubleValue());
				
				cell = campioneCbASheet.getRow(40).getCell(22);
				if(campioneCB_A.getCbbMvaM0pic2p2() != null)
					cell.setCellValue(campioneCB_A.getCbbMvaM0pic2p2().doubleValue());
				
				cell = campioneCbASheet.getRow(40).getCell(23);
				if(campioneCB_A.getCbbMvaMpwpic2p2() != null)
					cell.setCellValue(campioneCB_A.getCbbMvaMpwpic2p2().doubleValue());
				
				cell = campioneCbASheet.getRow(40).getCell(25);
				if(campioneCB_A.getCbbMvaT1pic2p2() != null)
					cell.setCellValue(campioneCB_A.getCbbMvaT1pic2p2().doubleValue());
				
				
				cell = campioneCbASheet.getRow(45).getCell(23);
				if(campioneCB_A.getCbbMvaMpapic1p2() != null)
					cell.setCellValue(campioneCB_A.getCbbMvaMpapic1p2().doubleValue());
				
				cell = campioneCbASheet.getRow(45).getCell(25);
				if(campioneCB_A.getCbbMvaM2pic1p2() != null)
					cell.setCellValue(campioneCB_A.getCbbMvaM2pic1p2().doubleValue());
				
				cell = campioneCbASheet.getRow(45).getCell(27);
				if(campioneCB_A.getCbbMvaT2pic1p2() != null)
					cell.setCellValue(campioneCB_A.getCbbMvaT2pic1p2().doubleValue());
				
				cell = campioneCbASheet.getRow(46).getCell(23);
				if(campioneCB_A.getCbbMvaMpapic2p2() != null)
					cell.setCellValue(campioneCB_A.getCbbMvaMpapic2p2().doubleValue());
				
				cell = campioneCbASheet.getRow(46).getCell(25);
				if(campioneCB_A.getCbbMvaM2pic2p2() != null)
					cell.setCellValue(campioneCB_A.getCbbMvaM2pic2p2().doubleValue());
				
				cell = campioneCbASheet.getRow(46).getCell(27);
				if(campioneCB_A.getCbbMvaT2pic2p2() != null)
					cell.setCellValue(campioneCB_A.getCbbMvaT2pic2p2().doubleValue());
				
				//lavaggio
				cell = campioneCbASheet.getRow(62).getCell(13);
				if(campioneCB_A.getCbbPPostLav() != null)
					cell.setCellValue(campioneCB_A.getCbbPPostLav().doubleValue());
				
				
				//PESATE USURA
				List<Pesata> pesateCbAUsura = pesataRepository.findByCampioneAndBinderFalseOrderByNumPesataAsc(campioneCB_A);
				
				int rowNum = 67;
				for(Pesata p : pesateCbAUsura) {
					cell = campioneCbASheet.getRow(rowNum).getCell(3);
					cell.setCellValue(p.getPesoNetto().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(4);
					cell.setCellValue(p.getTrattCb63Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(5);
					cell.setCellValue(p.getTrattCb40Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(6);
					cell.setCellValue(p.getTrattCb31V5Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(7);
					cell.setCellValue(p.getTrattCb20Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(8);
					cell.setCellValue(p.getTrattCb16Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(9);
					cell.setCellValue(p.getTrattCb14Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(10);
					cell.setCellValue(p.getTrattCb12V5Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(11);
					cell.setCellValue(p.getTrattCb10Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(12);
					cell.setCellValue(p.getTrattCb8Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(13);
					cell.setCellValue(p.getTrattCb6V3Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(14);
					cell.setCellValue(p.getTrattCb4Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(15);
					cell.setCellValue(p.getTrattCb2Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(16);
					cell.setCellValue(p.getTrattCb1Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(17);
					cell.setCellValue(p.getTrattCb0V5Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(18);
					cell.setCellValue(p.getTrattCb0V25Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(19);
					cell.setCellValue(p.getTrattCb0V063Mm().doubleValue());
					
					
					cell = campioneCbASheet.getRow(rowNum).getCell(20);
					cell.setCellValue(p.getFondo().doubleValue());
					
					rowNum++;
				}
				
				//PESATE BINDER
				List<Pesata> pesateCbABinder = pesataRepository.findByCampioneAndBinderTrueOrderByNumPesataAsc(campioneCB_A);
				
				rowNum = 79;
				for(Pesata p : pesateCbABinder) {
					cell = campioneCbASheet.getRow(rowNum).getCell(3);
					cell.setCellValue(p.getPesoNetto().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(4);
					cell.setCellValue(p.getTrattCb63Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(5);
					cell.setCellValue(p.getTrattCb40Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(6);
					cell.setCellValue(p.getTrattCb31V5Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(7);
					cell.setCellValue(p.getTrattCb20Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(8);
					cell.setCellValue(p.getTrattCb16Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(9);
					cell.setCellValue(p.getTrattCb14Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(10);
					cell.setCellValue(p.getTrattCb12V5Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(11);
					cell.setCellValue(p.getTrattCb10Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(12);
					cell.setCellValue(p.getTrattCb8Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(13);
					cell.setCellValue(p.getTrattCb6V3Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(14);
					cell.setCellValue(p.getTrattCb4Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(15);
					cell.setCellValue(p.getTrattCb2Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(16);
					cell.setCellValue(p.getTrattCb1Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(17);
					cell.setCellValue(p.getTrattCb0V5Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(18);
					cell.setCellValue(p.getTrattCb0V25Mm().doubleValue());
					
					cell = campioneCbASheet.getRow(rowNum).getCell(19);
					cell.setCellValue(p.getTrattCb0V063Mm().doubleValue());
					
					
					cell = campioneCbASheet.getRow(rowNum).getCell(20);
					cell.setCellValue(p.getFondo().doubleValue());
					
					rowNum++;
				}
				
				// operatore e note CB_A
				cell = rapportoBitumeCbASheet.getRow(11).getCell(1);
				if(campioneCB_A.getOperatoreAnalisi() != null) {
					cell.setCellValue(campioneCB_A.getOperatoreAnalisi().getUser().getFirstName() + " " + campioneCB_A.getOperatoreAnalisi().getUser().getLastName());
				} else if(campioneCB_A.getNote() != null && !campioneCB_A.getNote().isEmpty() && campioneCB_A.getNote().indexOf("###") > -1) {
					String operatore = campioneCB_A.getNote().substring(campioneCB_A.getNote().indexOf("###")).replace("###", "");
					cell.setCellValue(operatore);
				}
				
				cell = rapportoBitumeCbASheet.getRow(241).getCell(0);
				if(campioneCB_A.getNote() != null && !campioneCB_A.getNote().isEmpty()) {
					if(campioneCB_A.getNote().contains("###")) {
						cell.setCellValue(campioneCB_A.getNote().substring(0, campioneCB_A.getNote().indexOf("###")));
					} else {
						cell.setCellValue(campioneCB_A.getNote());
					}
				}
				
			}
			
			
			/*
			 * ####################### CB_B
			 */
			if(campioneCB_B != null) {
				cell = campioneCbBSheet.getRow(2).getCell(3);
				if(campioneCB_B.getDataAnalisi() != null)
					cell.setCellValue(campioneCB_B.getDataAnalisi().format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALIAN)));
				
				//USURA
				//spessore p1
				cell = campioneCbBSheet.getRow(10).getCell(2);
				if(campioneCB_B.getCbuSpessEst1p1() != null)
					cell.setCellValue(campioneCB_B.getCbuSpessEst1p1().doubleValue());
				
				cell = campioneCbBSheet.getRow(10).getCell(3);
				if(campioneCB_B.getCbuSpessMedp1() != null)
					cell.setCellValue(campioneCB_B.getCbuSpessMedp1().doubleValue());
				
				cell = campioneCbBSheet.getRow(10).getCell(4);
				if(campioneCB_B.getCbuSpessEst2p1() != null)
					cell.setCellValue(campioneCB_B.getCbuSpessEst2p1().doubleValue());
				
				//spessore p2
				cell = campioneCbBSheet.getRow(11).getCell(2);
				if(campioneCB_B.getCbuSpessEst1p2() != null)
					cell.setCellValue(campioneCB_B.getCbuSpessEst1p2().doubleValue());
				
				cell = campioneCbBSheet.getRow(11).getCell(3);
				if(campioneCB_B.getCbuSpessMedp2() != null)
					cell.setCellValue(campioneCB_B.getCbuSpessMedp2().doubleValue());
				
				cell = campioneCbBSheet.getRow(11).getCell(4);
				if(campioneCB_B.getCbuSpessEst2p2() != null)
					cell.setCellValue(campioneCB_B.getCbuSpessEst2p2().doubleValue());
				
				//ssd p1
				cell = campioneCbBSheet.getRow(17).getCell(2);
				if(campioneCB_B.getCbuSsdM1p1() != null)
					cell.setCellValue(campioneCB_B.getCbuSsdM1p1().doubleValue());
				
				cell = campioneCbBSheet.getRow(17).getCell(3);
				if(campioneCB_B.getCbuSsdM2p1() != null)
					cell.setCellValue(campioneCB_B.getCbuSsdM2p1().doubleValue());
				
				cell = campioneCbBSheet.getRow(17).getCell(4);
				if(campioneCB_B.getCbuSsdM3p1() != null)
					cell.setCellValue(campioneCB_B.getCbuSsdM3p1().doubleValue());
				
				cell = campioneCbBSheet.getRow(17).getCell(5);
				if(campioneCB_B.getCbuSsdTp1() != null)
					cell.setCellValue(campioneCB_B.getCbuSsdTp1().doubleValue());
				
				//ssd p2
				cell = campioneCbBSheet.getRow(18).getCell(2);
				if(campioneCB_B.getCbuSsdM1p2() != null)
					cell.setCellValue(campioneCB_B.getCbuSsdM1p2().doubleValue());
				
				cell = campioneCbBSheet.getRow(18).getCell(3);
				if(campioneCB_B.getCbuSsdM2p2() != null)
					cell.setCellValue(campioneCB_B.getCbuSsdM2p2().doubleValue());
				
				cell = campioneCbBSheet.getRow(18).getCell(4);
				if(campioneCB_B.getCbuSsdM3p2() != null)
					cell.setCellValue(campioneCB_B.getCbuSsdM3p2().doubleValue());
				
				cell = campioneCbBSheet.getRow(18).getCell(5);
				if(campioneCB_B.getCbuSsdTp2() != null)
					cell.setCellValue(campioneCB_B.getCbuSsdTp2().doubleValue());
				
				//legante p1
				cell = campioneCbBSheet.getRow(26).getCell(2);
				if(campioneCB_B.getCbuLegMmp1() != null)
					cell.setCellValue(campioneCB_B.getCbuLegMmp1().doubleValue());
				
				cell = campioneCbBSheet.getRow(26).getCell(3);
				if(campioneCB_B.getCbuLegMap1() != null)
					cell.setCellValue(campioneCB_B.getCbuLegMap1().doubleValue());
				
				// mva p1
				cell = campioneCbBSheet.getRow(39-2).getCell(2);
				if(campioneCB_B.getCbuMvaM0pic1() != null)
					cell.setCellValue(campioneCB_B.getCbuMvaM0pic1().doubleValue());
				
				cell = campioneCbBSheet.getRow(39-2).getCell(3);
				if(campioneCB_B.getCbuMvaMpwpic1() != null)
					cell.setCellValue(campioneCB_B.getCbuMvaMpwpic1().doubleValue());
				
				cell = campioneCbBSheet.getRow(39-2).getCell(5);
				if(campioneCB_B.getCbuMvaT1pic1() != null)
					cell.setCellValue(campioneCB_B.getCbuMvaT1pic1().doubleValue());
				
				cell = campioneCbBSheet.getRow(40-2).getCell(2);
				if(campioneCB_B.getCbuMvaM0pic2() != null)
					cell.setCellValue(campioneCB_B.getCbuMvaM0pic2().doubleValue());
				
				cell = campioneCbBSheet.getRow(40-2).getCell(3);
				if(campioneCB_B.getCbuMvaMpwpic2() != null)
					cell.setCellValue(campioneCB_B.getCbuMvaMpwpic2().doubleValue());
				
				cell = campioneCbBSheet.getRow(40-2).getCell(5);
				if(campioneCB_B.getCbuMvaT1pic2() != null)
					cell.setCellValue(campioneCB_B.getCbuMvaT1pic2().doubleValue());
				
				
				cell = campioneCbBSheet.getRow(45-2).getCell(3);
				if(campioneCB_B.getCbuMvaMpapic1() != null)
					cell.setCellValue(campioneCB_B.getCbuMvaMpapic1().doubleValue());
				
				cell = campioneCbBSheet.getRow(45-2).getCell(5);
				if(campioneCB_B.getCbuMvaM2pic1() != null)
					cell.setCellValue(campioneCB_B.getCbuMvaM2pic1().doubleValue());
				
				cell = campioneCbBSheet.getRow(45-2).getCell(7);
				if(campioneCB_B.getCbuMvaT2pic1() != null)
					cell.setCellValue(campioneCB_B.getCbuMvaT2pic1().doubleValue());
				
				cell = campioneCbBSheet.getRow(46-2).getCell(3);
				if(campioneCB_B.getCbuMvaMpapic2() != null)
					cell.setCellValue(campioneCB_B.getCbuMvaMpapic2().doubleValue());
				
				cell = campioneCbBSheet.getRow(46-2).getCell(5);
				if(campioneCB_B.getCbuMvaM2pic2() != null)
					cell.setCellValue(campioneCB_B.getCbuMvaM2pic2().doubleValue());
				
				cell = campioneCbBSheet.getRow(46-2).getCell(7);
				if(campioneCB_B.getCbuMvaT2pic2() != null)
					cell.setCellValue(campioneCB_B.getCbuMvaT2pic2().doubleValue());
				
				//lavaggio
				cell = campioneCbBSheet.getRow(62-2).getCell(4);
				if(campioneCB_B.getCbuPPostLav() != null)
					cell.setCellValue(campioneCB_B.getCbuPPostLav().doubleValue());
				
				
				
				//BINDER
				//spessore p1
				cell = campioneCbBSheet.getRow(10).getCell(12);
				if(campioneCB_B.getCbbSpessEst1p1() != null)
					cell.setCellValue(campioneCB_B.getCbbSpessEst1p1().doubleValue());
				
				cell = campioneCbBSheet.getRow(10).getCell(13);
				if(campioneCB_B.getCbbSpessMedp1() != null)
					cell.setCellValue(campioneCB_B.getCbbSpessMedp1().doubleValue());
				
				cell = campioneCbBSheet.getRow(10).getCell(14);
				if(campioneCB_B.getCbbSpessEst2p1() != null)
					cell.setCellValue(campioneCB_B.getCbbSpessEst2p1().doubleValue());
				
				//spessore p2
				cell = campioneCbBSheet.getRow(11).getCell(12);
				if(campioneCB_B.getCbbSpessEst1p2() != null)
					cell.setCellValue(campioneCB_B.getCbbSpessEst1p2().doubleValue());
				
				cell = campioneCbBSheet.getRow(11).getCell(13);
				if(campioneCB_B.getCbbSpessMedp2() != null)
					cell.setCellValue(campioneCB_B.getCbbSpessMedp2().doubleValue());
				
				cell = campioneCbBSheet.getRow(11).getCell(14);
				if(campioneCB_B.getCbbSpessEst2p2() != null)
					cell.setCellValue(campioneCB_B.getCbbSpessEst2p2().doubleValue());
				
				//ssd p1
				cell = campioneCbBSheet.getRow(17).getCell(12);
				if(campioneCB_B.getCbbSsdM1p1() != null)
					cell.setCellValue(campioneCB_B.getCbbSsdM1p1().doubleValue());
				
				cell = campioneCbBSheet.getRow(17).getCell(13);
				if(campioneCB_B.getCbbSsdM2p1() != null)
					cell.setCellValue(campioneCB_B.getCbbSsdM2p1().doubleValue());
				
				cell = campioneCbBSheet.getRow(17).getCell(14);
				if(campioneCB_B.getCbbSsdM3p1() != null)
					cell.setCellValue(campioneCB_B.getCbbSsdM3p1().doubleValue());
				
				cell = campioneCbBSheet.getRow(17).getCell(15);
				if(campioneCB_B.getCbbSsdTp1() != null)
					cell.setCellValue(campioneCB_B.getCbbSsdTp1().doubleValue());
				
				//ssd p2
				cell = campioneCbBSheet.getRow(18).getCell(12);
				if(campioneCB_B.getCbbSsdM1p2() != null)
					cell.setCellValue(campioneCB_B.getCbbSsdM1p2().doubleValue());
				
				cell = campioneCbBSheet.getRow(18).getCell(13);
				if(campioneCB_B.getCbbSsdM2p2() != null)
					cell.setCellValue(campioneCB_B.getCbbSsdM2p2().doubleValue());
				
				cell = campioneCbBSheet.getRow(18).getCell(14);
				if(campioneCB_B.getCbbSsdM3p2() != null)
					cell.setCellValue(campioneCB_B.getCbbSsdM3p2().doubleValue());
				
				cell = campioneCbBSheet.getRow(18).getCell(15);
				if(campioneCB_B.getCbbSsdTp2() != null)
					cell.setCellValue(campioneCB_B.getCbbSsdTp2().doubleValue());
				
				//legante p1
				cell = campioneCbBSheet.getRow(26).getCell(12);
				if(campioneCB_B.getCbbLegMmp1() != null)
					cell.setCellValue(campioneCB_B.getCbbLegMmp1().doubleValue());
				
				cell = campioneCbBSheet.getRow(26).getCell(13);
				if(campioneCB_B.getCbbLegMap1() != null)
					cell.setCellValue(campioneCB_B.getCbbLegMap1().doubleValue());
				
				//legante p2
				cell = campioneCbBSheet.getRow(27).getCell(12);
				if(campioneCB_B.getCbbLegMmp2() != null)
					cell.setCellValue(campioneCB_B.getCbbLegMmp2().doubleValue());
				
				cell = campioneCbBSheet.getRow(27).getCell(13);
				if(campioneCB_B.getCbbLegMap2() != null)
					cell.setCellValue(campioneCB_B.getCbbLegMap2().doubleValue());
				
				// mva p1
				cell = campioneCbBSheet.getRow(39-2).getCell(12);
				if(campioneCB_B.getCbbMvaM0pic1p1() != null)
					cell.setCellValue(campioneCB_B.getCbbMvaM0pic1p1().doubleValue());
				
				cell = campioneCbBSheet.getRow(39-2).getCell(13);
				if(campioneCB_B.getCbbMvaMpwpic1p1() != null)
					cell.setCellValue(campioneCB_B.getCbbMvaMpwpic1p1().doubleValue());
				
				cell = campioneCbBSheet.getRow(39-2).getCell(15);
				if(campioneCB_B.getCbbMvaT1pic1p1() != null)
					cell.setCellValue(campioneCB_B.getCbbMvaT1pic1p1().doubleValue());
				
				cell = campioneCbBSheet.getRow(40-2).getCell(12);
				if(campioneCB_B.getCbbMvaM0pic2p1() != null)
					cell.setCellValue(campioneCB_B.getCbbMvaM0pic2p1().doubleValue());
				
				cell = campioneCbBSheet.getRow(40-2).getCell(13);
				if(campioneCB_B.getCbbMvaMpwpic2p1() != null)
					cell.setCellValue(campioneCB_B.getCbbMvaMpwpic2p1().doubleValue());
				
				cell = campioneCbBSheet.getRow(40-2).getCell(15);
				if(campioneCB_B.getCbbMvaT1pic2p1() != null)
					cell.setCellValue(campioneCB_B.getCbbMvaT1pic2p1().doubleValue());
				
				
				cell = campioneCbBSheet.getRow(45-2).getCell(13);
				if(campioneCB_B.getCbbMvaMpapic1p1() != null)
					cell.setCellValue(campioneCB_B.getCbbMvaMpapic1p1().doubleValue());
				
				cell = campioneCbBSheet.getRow(45-2).getCell(15);
				if(campioneCB_B.getCbbMvaM2pic1p1() != null)
					cell.setCellValue(campioneCB_B.getCbbMvaM2pic1p1().doubleValue());
				
				cell = campioneCbBSheet.getRow(45-2).getCell(17);
				if(campioneCB_B.getCbbMvaT2pic1p1() != null)
					cell.setCellValue(campioneCB_B.getCbbMvaT2pic1p1().doubleValue());
				
				cell = campioneCbBSheet.getRow(46-2).getCell(13);
				if(campioneCB_B.getCbbMvaMpapic2p1() != null)
					cell.setCellValue(campioneCB_B.getCbbMvaMpapic2p1().doubleValue());
				
				cell = campioneCbBSheet.getRow(46-2).getCell(15);
				if(campioneCB_B.getCbbMvaM2pic2p1() != null)
					cell.setCellValue(campioneCB_B.getCbbMvaM2pic2p1().doubleValue());
				
				cell = campioneCbBSheet.getRow(46-2).getCell(17);
				if(campioneCB_B.getCbbMvaT2pic2p1() != null)
					cell.setCellValue(campioneCB_B.getCbbMvaT2pic2p1().doubleValue());
				
				
				// mva p2
				cell = campioneCbBSheet.getRow(39-2).getCell(22);
				if(campioneCB_B.getCbbMvaM0pic1p2() != null)
					cell.setCellValue(campioneCB_B.getCbbMvaM0pic1p2().doubleValue());
				
				cell = campioneCbBSheet.getRow(39-2).getCell(23);
				if(campioneCB_B.getCbbMvaMpwpic1p2() != null)
					cell.setCellValue(campioneCB_B.getCbbMvaMpwpic1p2().doubleValue());
				
				cell = campioneCbBSheet.getRow(39-2).getCell(25);
				if(campioneCB_B.getCbbMvaT1pic1p2() != null)
					cell.setCellValue(campioneCB_B.getCbbMvaT1pic1p2().doubleValue());
				
				cell = campioneCbBSheet.getRow(40-2).getCell(22);
				if(campioneCB_B.getCbbMvaM0pic2p2() != null)
					cell.setCellValue(campioneCB_B.getCbbMvaM0pic2p2().doubleValue());
				
				cell = campioneCbBSheet.getRow(40-2).getCell(23);
				if(campioneCB_B.getCbbMvaMpwpic2p2() != null)
					cell.setCellValue(campioneCB_B.getCbbMvaMpwpic2p2().doubleValue());
				
				cell = campioneCbBSheet.getRow(40-2).getCell(25);
				if(campioneCB_B.getCbbMvaT1pic2p2() != null)
					cell.setCellValue(campioneCB_B.getCbbMvaT1pic2p2().doubleValue());
				
				
				cell = campioneCbBSheet.getRow(45-2).getCell(23);
				if(campioneCB_B.getCbbMvaMpapic1p2() != null)
					cell.setCellValue(campioneCB_B.getCbbMvaMpapic1p2().doubleValue());
				
				cell = campioneCbBSheet.getRow(45-2).getCell(25);
				if(campioneCB_B.getCbbMvaM2pic1p2() != null)
					cell.setCellValue(campioneCB_B.getCbbMvaM2pic1p2().doubleValue());
				
				cell = campioneCbBSheet.getRow(45-2).getCell(27);
				if(campioneCB_B.getCbbMvaT2pic1p2() != null)
					cell.setCellValue(campioneCB_B.getCbbMvaT2pic1p2().doubleValue());
				
				cell = campioneCbBSheet.getRow(46-2).getCell(23);
				if(campioneCB_B.getCbbMvaMpapic2p2() != null)
					cell.setCellValue(campioneCB_B.getCbbMvaMpapic2p2().doubleValue());
				
				cell = campioneCbBSheet.getRow(46-2).getCell(25);
				if(campioneCB_B.getCbbMvaM2pic2p2() != null)
					cell.setCellValue(campioneCB_B.getCbbMvaM2pic2p2().doubleValue());
				
				cell = campioneCbBSheet.getRow(46-2).getCell(27);
				if(campioneCB_B.getCbbMvaT2pic2p2() != null)
					cell.setCellValue(campioneCB_B.getCbbMvaT2pic2p2().doubleValue());
				
				//lavaggio
				cell = campioneCbBSheet.getRow(62-2).getCell(13);
				if(campioneCB_B.getCbbPPostLav() != null)
					cell.setCellValue(campioneCB_B.getCbbPPostLav().doubleValue());
				
				
				//PESATE USURA
				List<Pesata> pesateCbBUsura = pesataRepository.findByCampioneAndBinderFalseOrderByNumPesataAsc(campioneCB_B);
				
				int rowNum = 67-2;
				for(Pesata p : pesateCbBUsura) {
					cell = campioneCbBSheet.getRow(rowNum).getCell(3);
					cell.setCellValue(p.getPesoNetto().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(4);
					cell.setCellValue(p.getTrattCb63Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(5);
					cell.setCellValue(p.getTrattCb40Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(6);
					cell.setCellValue(p.getTrattCb31V5Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(7);
					cell.setCellValue(p.getTrattCb20Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(8);
					cell.setCellValue(p.getTrattCb16Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(9);
					cell.setCellValue(p.getTrattCb14Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(10);
					cell.setCellValue(p.getTrattCb12V5Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(11);
					cell.setCellValue(p.getTrattCb10Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(12);
					cell.setCellValue(p.getTrattCb8Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(13);
					cell.setCellValue(p.getTrattCb6V3Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(14);
					cell.setCellValue(p.getTrattCb4Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(15);
					cell.setCellValue(p.getTrattCb2Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(16);
					cell.setCellValue(p.getTrattCb1Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(17);
					cell.setCellValue(p.getTrattCb0V5Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(18);
					cell.setCellValue(p.getTrattCb0V25Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(19);
					cell.setCellValue(p.getTrattCb0V063Mm().doubleValue());
					
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(20);
					cell.setCellValue(p.getFondo().doubleValue());
					
					rowNum++;
				}
				
				//PESATE BINDER
				List<Pesata> pesateCbBBinder = pesataRepository.findByCampioneAndBinderTrueOrderByNumPesataAsc(campioneCB_B);
				
				rowNum = 79-2;
				for(Pesata p : pesateCbBBinder) {
					cell = campioneCbBSheet.getRow(rowNum).getCell(3);
					cell.setCellValue(p.getPesoNetto().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(4);
					cell.setCellValue(p.getTrattCb63Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(5);
					cell.setCellValue(p.getTrattCb40Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(6);
					cell.setCellValue(p.getTrattCb31V5Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(7);
					cell.setCellValue(p.getTrattCb20Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(8);
					cell.setCellValue(p.getTrattCb16Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(9);
					cell.setCellValue(p.getTrattCb14Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(10);
					cell.setCellValue(p.getTrattCb12V5Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(11);
					cell.setCellValue(p.getTrattCb10Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(12);
					cell.setCellValue(p.getTrattCb8Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(13);
					cell.setCellValue(p.getTrattCb6V3Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(14);
					cell.setCellValue(p.getTrattCb4Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(15);
					cell.setCellValue(p.getTrattCb2Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(16);
					cell.setCellValue(p.getTrattCb1Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(17);
					cell.setCellValue(p.getTrattCb0V5Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(18);
					cell.setCellValue(p.getTrattCb0V25Mm().doubleValue());
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(19);
					cell.setCellValue(p.getTrattCb0V063Mm().doubleValue());
					
					
					cell = campioneCbBSheet.getRow(rowNum).getCell(20);
					cell.setCellValue(p.getFondo().doubleValue());
					
					rowNum++;
				}
				
				// operatore e note CB_B
				cell = rapportoBitumeCbBSheet.getRow(11).getCell(1);
				if(campioneCB_B.getOperatoreAnalisi() != null) {
					cell.setCellValue(campioneCB_B.getOperatoreAnalisi().getUser().getFirstName() + " " + campioneCB_B.getOperatoreAnalisi().getUser().getLastName());
				} else if(campioneCB_B.getNote() != null && !campioneCB_B.getNote().isEmpty() && campioneCB_B.getNote().indexOf("###") > -1) {
					String operatore = campioneCB_B.getNote().substring(campioneCB_B.getNote().indexOf("###")).replace("###", "");
					cell.setCellValue(operatore);
				}
				
				cell = rapportoBitumeCbBSheet.getRow(241).getCell(0);
				if(campioneCB_B.getNote() != null && !campioneCB_B.getNote().isEmpty()) {
					if(campioneCB_B.getNote().contains("###")) {
						cell.setCellValue(campioneCB_B.getNote().substring(0, campioneCB_B.getNote().indexOf("###")));
					} else {
						cell.setCellValue(campioneCB_B.getNote());
					}
				}
				
			}
		
		
		} catch(NullPointerException e) {
			log.debug("####### NullPointerException alla riga: {}", e.getStackTrace()[0].getLineNumber());
		}
		
		
		
		//intestazioni
		//VANNO UNITE LE CELLE NEL MODELLO
		String nomeIstituto = cassetta.getConsegna().getLaboratorio().getNome();
		nomeIstituto = nomeIstituto.substring(0, nomeIstituto.indexOf("-") - 1);
        String indirizzo = cassetta.getConsegna().getLaboratorio().getIndirizzo().replace("-", "\r\n");
        String strind = nomeIstituto + "\r\n" + indirizzo;
		
        cell = rapportoSheet.getRow(2).getCell(2);
		cell.setCellValue(strind);
		cell = rapportoBitumeCbASheet.getRow(2).getCell(2);
		cell.setCellValue(strind);
		cell = rapportoBitumeCbBSheet.getRow(2).getCell(2);
		cell.setCellValue(strind);
		
		
		
		
		
		
		
		
		
		
		//marchio esportazione
		Row row = rapportoSheet.getRow(72);
		cell = (XSSFCell) row.createCell(10);
		cell.setCellValue("idCassetta");
		
		cell = (XSSFCell) row.createCell(11);
		cell.setCellValue(cassetta.getId());
		
		
		
		
		
		
		
		XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
		
		
		
		
		//bisogna chiudere il fileInputStream prima di salvare il file
		excelBitumeInputStream.close();
		
		FileOutputStream outFile = new FileOutputStream(destBitumeTempFile);
		workbook.write(outFile);
		outFile.close();
		
		
		
		
		
		//test
//		byte[] buffer = new byte[1024];
//		FileOutputStream fos = new FileOutputStream(destBitumeTempFile.getPath().replace(".xlsm", ".zip"));
//		ZipOutputStream zos = new ZipOutputStream(fos);
//		FileInputStream fis = new FileInputStream(destBitumeTempFile);
//		zos.putNextEntry(new ZipEntry(destBitumeTempFile.getName()));
//		
//		int length;
//        while ((length = fis.read(buffer)) > 0) {
//            zos.write(buffer, 0, length);
//        }
//
//        zos.closeEntry();
// 
//        fis.close();
//        zos.close();
//		
//        
//        
//        
//		
//		//copia file in destinazione AnnumiNAS
//		String exportDirName = "/srv/PROGETTO_ITALGAS/" + cassetta.getConsegna().getLaboratorio().getIstituto();
//		File exportDir = new File(exportDirName);
//		if(!exportDir.exists()) {
//			exportDir.mkdirs();
//		}
//		
//		File annuminasDestBitumeFile = new File(exportDir +  "/" + destBitumeFileName);
//		Files.copy(destBitumeTempFile.toPath(), annuminasDestBitumeFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//		
//		
//		
//		
//		
//		//test
//		File f = new File(destBitumeTempFile.getPath().replace(".xlsm", ".zip"));
//		Files.copy(f.toPath(), new File(exportDir +  "/" + f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
//		
//		
//		
//		
//		
//		log.debug("##### ESPORTAZIONE CONCLUSA");
//		
//		return annuminasDestBitumeFile.getPath();
		
		
		
		// devo ritornare array byte[]:
		byte[] data = Files.readAllBytes(destBitumeTempFile.toPath());
		Files.delete(destBitumeTempFile.toPath());
		
		return data;
	}
	
	
}
