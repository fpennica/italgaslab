package it.cnr.igag.italgas.web.rest.dto;

import java.math.BigDecimal;

public class ClassificazioneGeotecnicaDTO {
	
	private BigDecimal ghiaiaPerc;
	
	private BigDecimal sabbiaGrossaPerc;
	
	private BigDecimal sabbiaPerc;
	
	private BigDecimal limoArgillaPerc;
	
	private BigDecimal sabbiaFineLimoArgillaPerc;
	
	private String classificazioneGeotecnica;

	public ClassificazioneGeotecnicaDTO(BigDecimal ghiaiaPerc, BigDecimal sabbiaGrossaPerc, BigDecimal sabbiaPerc,
			BigDecimal limoArgillaPerc) {
		super();
		this.ghiaiaPerc = ghiaiaPerc;
		this.sabbiaGrossaPerc = sabbiaGrossaPerc;
		this.sabbiaPerc = sabbiaPerc;
		this.limoArgillaPerc = limoArgillaPerc;
	}

	public ClassificazioneGeotecnicaDTO(BigDecimal ghiaiaPerc, BigDecimal sabbiaGrossaPerc,
			BigDecimal sabbiaFineLimoArgillaPerc) {
		super();
		this.ghiaiaPerc = ghiaiaPerc;
		this.sabbiaGrossaPerc = sabbiaGrossaPerc;
		this.sabbiaFineLimoArgillaPerc = sabbiaFineLimoArgillaPerc;
	}

	public BigDecimal getGhiaiaPerc() {
		return ghiaiaPerc;
	}

	public void setGhiaiaPerc(BigDecimal ghiaiaPerc) {
		this.ghiaiaPerc = ghiaiaPerc;
	}

	public BigDecimal getSabbiaGrossaPerc() {
		return sabbiaGrossaPerc;
	}

	public void setSabbiaGrossaPerc(BigDecimal sabbiaGrossaPerc) {
		this.sabbiaGrossaPerc = sabbiaGrossaPerc;
	}

	public BigDecimal getSabbiaPerc() {
		return sabbiaPerc;
	}

	public void setSabbiaPerc(BigDecimal sabbiaPerc) {
		this.sabbiaPerc = sabbiaPerc;
	}

	public BigDecimal getLimoArgillaPerc() {
		return limoArgillaPerc;
	}

	public void setLimoArgillaPerc(BigDecimal limoArgillaPerc) {
		this.limoArgillaPerc = limoArgillaPerc;
	}

	public BigDecimal getSabbiaFineLimoArgillaPerc() {
		return sabbiaFineLimoArgillaPerc;
	}

	public void setSabbiaFineLimoArgillaPerc(BigDecimal sabbiaFineLimoArgillaPerc) {
		this.sabbiaFineLimoArgillaPerc = sabbiaFineLimoArgillaPerc;
	}

	public String getClassificazioneGeotecnica() {
		return classificazioneGeotecnica;
	}

	public void setClassificazioneGeotecnica(String classificazioneGeotecnica) {
		this.classificazioneGeotecnica = classificazioneGeotecnica;
	}

}
