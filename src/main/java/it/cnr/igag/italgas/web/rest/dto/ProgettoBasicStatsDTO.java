package it.cnr.igag.italgas.web.rest.dto;

import java.util.HashMap;

public class ProgettoBasicStatsDTO {
	
	private Long numTotConsegneInserite;
	
	private Long numTotCassetteInserite;
	
	private Long numTotCampioniInseriti;
	
	private HashMap<String, LaboratorioStatoCassetteDTO> labMap = new HashMap<String, LaboratorioStatoCassetteDTO>();

	public Long getNumTotConsegneInserite() {
		return numTotConsegneInserite;
	}

	public void setNumTotConsegneInserite(Long numTotConsegneInserite) {
		this.numTotConsegneInserite = numTotConsegneInserite;
	}

	public Long getNumTotCassetteInserite() {
		return numTotCassetteInserite;
	}

	public void setNumTotCassetteInserite(Long numTotCassetteInserite) {
		this.numTotCassetteInserite = numTotCassetteInserite;
	}

	public Long getNumTotCampioniInseriti() {
		return numTotCampioniInseriti;
	}

	public void setNumTotCampioniInseriti(Long numTotCampioniInseriti) {
		this.numTotCampioniInseriti = numTotCampioniInseriti;
	}

	public HashMap<String, LaboratorioStatoCassetteDTO> getLabMap() {
		return labMap;
	}

	public void setLabMap(HashMap<String, LaboratorioStatoCassetteDTO> labMap) {
		this.labMap = labMap;
	}

	
}
