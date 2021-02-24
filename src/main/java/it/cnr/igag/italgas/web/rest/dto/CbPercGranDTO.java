package it.cnr.igag.italgas.web.rest.dto;

import java.math.BigDecimal;

public class CbPercGranDTO {

	private BigDecimal ghiaiaPerc;
	
	private BigDecimal sabbiaPerc;
	
	private BigDecimal limoArgillaPerc;
	
	public CbPercGranDTO(BigDecimal ghiaiaPerc, BigDecimal sabbiaPerc, BigDecimal limoArgillaPerc) {
		super();
		this.ghiaiaPerc = ghiaiaPerc;
		this.sabbiaPerc = sabbiaPerc;
		this.limoArgillaPerc = limoArgillaPerc;
	}

	public BigDecimal getGhiaiaPerc() {
		return ghiaiaPerc;
	}

	public void setGhiaiaPerc(BigDecimal ghiaiaPerc) {
		this.ghiaiaPerc = ghiaiaPerc;
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
	
}
