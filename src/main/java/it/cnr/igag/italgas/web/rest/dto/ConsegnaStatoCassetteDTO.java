package it.cnr.igag.italgas.web.rest.dto;

import it.cnr.igag.italgas.domain.Consegna;

public class ConsegnaStatoCassetteDTO {
	
	private Consegna consegna;
	
	private Integer numCassette;
	
	private Integer numCassetteInserite;
	
	private Integer numCassetteInLavorazione;
	
	private Integer numCassetteRifiutate;
	
	private Integer numCassetteInTrattamento;
	
	private Integer numCassetteLavorazioneTerminata;
	
	private Integer numCassetteRestituite;
	
	private Integer numCassetteStatoSconosciuto;

	public Consegna getConsegna() {
		return consegna;
	}

	public void setConsegna(Consegna consegna) {
		this.consegna = consegna;
	}

	public Integer getNumCassette() {
		return numCassette;
	}

	public void setNumCassette(Integer numCassette) {
		this.numCassette = numCassette;
	}

	public Integer getNumCassetteInserite() {
		return numCassetteInserite;
	}

	public void setNumCassetteInserite(Integer numCassetteInserite) {
		this.numCassetteInserite = numCassetteInserite;
	}

	public Integer getNumCassetteInLavorazione() {
		return numCassetteInLavorazione;
	}

	public void setNumCassetteInLavorazione(Integer numCassetteInLavorazione) {
		this.numCassetteInLavorazione = numCassetteInLavorazione;
	}

	public Integer getNumCassetteRifiutate() {
		return numCassetteRifiutate;
	}

	public void setNumCassetteRifiutate(Integer numCassetteRifiutate) {
		this.numCassetteRifiutate = numCassetteRifiutate;
	}

	public Integer getNumCassetteInTrattamento() {
		return numCassetteInTrattamento;
	}

	public void setNumCassetteInTrattamento(Integer numCassetteInTrattamento) {
		this.numCassetteInTrattamento = numCassetteInTrattamento;
	}

	public Integer getNumCassetteLavorazioneTerminata() {
		return numCassetteLavorazioneTerminata;
	}

	public void setNumCassetteLavorazioneTerminata(Integer numCassetteLavorazioneTerminata) {
		this.numCassetteLavorazioneTerminata = numCassetteLavorazioneTerminata;
	}

	public Integer getNumCassetteRestituite() {
		return numCassetteRestituite;
	}

	public void setNumCassetteRestituite(Integer numCassetteRestituite) {
		this.numCassetteRestituite = numCassetteRestituite;
	}

	public Integer getNumCassetteStatoSconosciuto() {
		return numCassetteStatoSconosciuto;
	}

	public void setNumCassetteStatoSconosciuto(Integer numCassetteStatoSconosciuto) {
		this.numCassetteStatoSconosciuto = numCassetteStatoSconosciuto;
	}

}
