package it.cnr.igag.italgas.web.rest.dto;

import java.math.BigDecimal;

public class SetaccioDTO {
	
	private BigDecimal setaccio;
	
	private BigDecimal massa;
	
	private BigDecimal trattenutoPerc;
	
	private BigDecimal trattenutoCumPerc;
	
	private BigDecimal passanteCumPerc;

	public SetaccioDTO(BigDecimal setaccio, BigDecimal massa, BigDecimal trattenutoPerc, BigDecimal trattenutoCumPerc,
			BigDecimal passanteCumPerc) {
		super();
		this.setaccio = setaccio;
		this.massa = massa;
		this.trattenutoPerc = trattenutoPerc;
		this.trattenutoCumPerc = trattenutoCumPerc;
		this.passanteCumPerc = passanteCumPerc;
	}

	public BigDecimal getSetaccio() {
		return setaccio;
	}

	public void setSetaccio(BigDecimal setaccio) {
		this.setaccio = setaccio;
	}

	public BigDecimal getMassa() {
		return massa;
	}

	public void setMassa(BigDecimal massa) {
		this.massa = massa;
	}

	public BigDecimal getTrattenutoPerc() {
		return trattenutoPerc;
	}

	public void setTrattenutoPerc(BigDecimal trattenutoPerc) {
		this.trattenutoPerc = trattenutoPerc;
	}

	public BigDecimal getTrattenutoCumPerc() {
		return trattenutoCumPerc;
	}

	public void setTrattenutoCumPerc(BigDecimal trattenutoCumPerc) {
		this.trattenutoCumPerc = trattenutoCumPerc;
	}

	public BigDecimal getPassanteCumPerc() {
		return passanteCumPerc;
	}

	public void setPassanteCumPerc(BigDecimal passanteCumPerc) {
		this.passanteCumPerc = passanteCumPerc;
	}
	
}
