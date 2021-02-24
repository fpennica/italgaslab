package it.cnr.igag.italgas.web.rest.dto;

import it.cnr.igag.italgas.domain.Laboratorio;

public class LaboratorioStatoCassetteDTO {
	
	private Laboratorio laboratorio;
	
	private Integer numConsegneInserite;
	
	private Integer numCampioniInseriti;
	
	private Integer numCassetteInserite;
	
	private Integer numCassetteInLavorazione;
	
	private Integer numCassetteRifiutate;
	
	private Integer numCassetteInTrattamento;
	
	private Integer numCassetteLavorazioneTerminata;
	
	private Integer numCassetteRestituite;
	
	private Integer numCassetteStatoSconosciuto;


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

	public Laboratorio getLaboratorio() {
		return laboratorio;
	}

	public void setLaboratorio(Laboratorio laboratorio) {
		this.laboratorio = laboratorio;
	}

	public Integer getNumConsegneInserite() {
		return numConsegneInserite;
	}

	public void setNumConsegneInserite(Integer numConsegneInserite) {
		this.numConsegneInserite = numConsegneInserite;
	}

	public Integer getNumCampioniInseriti() {
		return numCampioniInseriti;
	}

	public void setNumCampioniInseriti(Integer numcampioniInseriti) {
		this.numCampioniInseriti = numcampioniInseriti;
	}

}
