package it.cnr.igag.italgas.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import it.cnr.igag.italgas.domain.enumeration.TipoCampione;

import it.cnr.igag.italgas.domain.enumeration.ProceduraDetMassaVol;

/**
 * A Campione.
 */
@Entity
@Table(name = "campione")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "campione")
public class Campione extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codice_campione")
    private String codiceCampione;

    @Column(name = "sigla_campione")
    private Integer siglaCampione;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_campione", nullable = false)
    private TipoCampione tipoCampione;

    @Size(max = 65534)
    @Column(name = "descrizione_campione", length = 65534)
    private String descrizioneCampione;

    @Lob
    @Column(name = "foto_sacchetto")
    private byte[] fotoSacchetto;

    @Column(name = "foto_sacchetto_content_type")
    private String fotoSacchettoContentType;

    @Lob
    @Column(name = "foto_campione")
    private byte[] fotoCampione;

    @Column(name = "foto_campione_content_type")
    private String fotoCampioneContentType;

    @Column(name = "data_analisi")
    private LocalDate dataAnalisi;

    @Column(name = "ripartizione_quartatura")
    private Boolean ripartizioneQuartatura;

    @Column(name = "essiccamento")
    private Boolean essiccamento;

    @Column(name = "setacciatura_secco")
    private Boolean setacciaturaSecco;

    @Column(name = "lavaggio_setacciatura")
    private Boolean lavaggioSetacciatura;

    @Column(name = "essiccamento_num_teglia")
    private String essiccamentoNumTeglia;

    @Column(name = "essiccamento_peso_teglia", precision=10, scale=2)
    private BigDecimal essiccamentoPesoTeglia;

    @Column(name = "essiccamento_peso_campione_lordo_iniziale", precision=10, scale=2)
    private BigDecimal essiccamentoPesoCampioneLordoIniziale;

    @Column(name = "essiccamento_peso_campione_lordo_24_h", precision=10, scale=2)
    private BigDecimal essiccamentoPesoCampioneLordo24H;

    @Column(name = "essiccamento_peso_campione_lordo_48_h", precision=10, scale=2)
    private BigDecimal essiccamentoPesoCampioneLordo48H;

    @Column(name = "sabbia")
    private Boolean sabbia;

    @Column(name = "ghiaia")
    private Boolean ghiaia;

    @Column(name = "materiale_risulta_vagliato")
    private Boolean materialeRisultaVagliato;

    @Column(name = "detriti")
    private Boolean detriti;

    @Column(name = "materiale_fine")
    private Boolean materialeFine;

    @Column(name = "materiale_organico")
    private Boolean materialeOrganico;

    @Column(name = "elementi_magg_125_mm")
    private Boolean elementiMagg125Mm;

    @Column(name = "detriti_conglomerato")
    private Boolean detritiConglomerato;

    @Column(name = "argilla")
    private Boolean argilla;

    @Column(name = "argilla_mat_alterabile")
    private Boolean argillaMatAlterabile;

    @Column(name = "granuli_cementati")
    private Boolean granuliCementati;

    @Column(name = "elementi_arrotondati")
    private Boolean elementiArrotondati;

    @Column(name = "elementi_spigolosi")
    private Boolean elementiSpigolosi;

    @Column(name = "sfabbricidi")
    private Boolean sfabbricidi;

    @Column(name = "tipo_b_conforme")
    private Boolean tipoBConforme;

    
    /*
     * Spessori
     */
    @Column(name = "cbu_spess_est1_p1", precision=10, scale=2)
    private BigDecimal cbuSpessEst1p1;
    
    @Column(name = "cbu_spess_est1_p2", precision=10, scale=2)
    private BigDecimal cbuSpessEst1p2;
    
    @Column(name = "cbb_spess_est1_p1", precision=10, scale=2)
    private BigDecimal cbbSpessEst1p1;
    
    @Column(name = "cbb_spess_est1_p2", precision=10, scale=2)
    private BigDecimal cbbSpessEst1p2;
    
    
    
    @Column(name = "cbu_spess_med_p1", precision=10, scale=2)
    private BigDecimal cbuSpessMedp1;
    
    @Column(name = "cbu_spess_med_p2", precision=10, scale=2)
    private BigDecimal cbuSpessMedp2;
    
    @Column(name = "cbb_spess_med_p1", precision=10, scale=2)
    private BigDecimal cbbSpessMedp1;
    
    @Column(name = "cbb_spess_med_p2", precision=10, scale=2)
    private BigDecimal cbbSpessMedp2;
    
    
    
    @Column(name = "cbu_spess_est2_p1", precision=10, scale=2)
    private BigDecimal cbuSpessEst2p1;
    
    @Column(name = "cbu_spess_est2_p2", precision=10, scale=2)
    private BigDecimal cbuSpessEst2p2;
    
    @Column(name = "cbb_spess_est2_p1", precision=10, scale=2)
    private BigDecimal cbbSpessEst2p1;
    
    @Column(name = "cbb_spess_est2_p2", precision=10, scale=2)
    private BigDecimal cbbSpessEst2p2;
    
    
    /*
     * SSD
     */
    @Column(name = "cbu_ssd_m1_p1", precision=10, scale=2)
    private BigDecimal cbuSsdM1p1;
    
    @Column(name = "cbu_ssd_m1_p2", precision=10, scale=2)
    private BigDecimal cbuSsdM1p2;
    
    @Column(name = "cbb_ssd_m1_p1", precision=10, scale=2)
    private BigDecimal cbbSsdM1p1;
    
    @Column(name = "cbb_ssd_m1_p2", precision=10, scale=2)
    private BigDecimal cbbSsdM1p2;
    
    
    
    @Column(name = "cbu_ssd_m2_p1", precision=10, scale=2)
    private BigDecimal cbuSsdM2p1;
    
    @Column(name = "cbu_ssd_m2_p2", precision=10, scale=2)
    private BigDecimal cbuSsdM2p2;
    
    @Column(name = "cbb_ssd_m2_p1", precision=10, scale=2)
    private BigDecimal cbbSsdM2p1;
    
    @Column(name = "cbb_ssd_m2_p2", precision=10, scale=2)
    private BigDecimal cbbSsdM2p2;
    
    
    
    @Column(name = "cbu_ssd_m3_p1", precision=10, scale=2)
    private BigDecimal cbuSsdM3p1;
    
    @Column(name = "cbu_ssd_m3_p2", precision=10, scale=2)
    private BigDecimal cbuSsdM3p2;
    
    @Column(name = "cbb_ssd_m3_p1", precision=10, scale=2)
    private BigDecimal cbbSsdM3p1;
    
    @Column(name = "cbb_ssd_m3_p2", precision=10, scale=2)
    private BigDecimal cbbSsdM3p2;
    
    
    
    @Column(name = "cbu_ssd_t_p1", precision=10, scale=2)
    private BigDecimal cbuSsdTp1;
    
    @Column(name = "cbu_ssd_t_p2", precision=10, scale=2)
    private BigDecimal cbuSsdTp2;
    
    @Column(name = "cbb_ssd_t_p1", precision=10, scale=2)
    private BigDecimal cbbSsdTp1;
    
    @Column(name = "cbb_ssd_t_p2", precision=10, scale=2)
    private BigDecimal cbbSsdTp2;
    
    
    /*
     * Legante
     */
    @Column(name = "cbu_leg_mm_p1", precision=10, scale=2)
    private BigDecimal cbuLegMmp1;
    
    @Column(name = "cbb_leg_mm_p1", precision=10, scale=2)
    private BigDecimal cbbLegMmp1;
    
    @Column(name = "cbb_leg_mm_p2", precision=10, scale=2)
    private BigDecimal cbbLegMmp2;
    
    @Column(name = "cbu_leg_ma_p1", precision=10, scale=2)
    private BigDecimal cbuLegMap1;
    
    @Column(name = "cbb_leg_ma_p1", precision=10, scale=2)
    private BigDecimal cbbLegMap1;
    
    @Column(name = "cbb_leg_ma_p2", precision=10, scale=2)
    private BigDecimal cbbLegMap2;
    
    
    /*
     * MVA
     */
    @Column(name = "cbu_mva_m0_pic1", precision=10, scale=2)
    private BigDecimal cbuMvaM0pic1;
    
    @Column(name = "cbu_mva_m0_pic2", precision=10, scale=2)
    private BigDecimal cbuMvaM0pic2;
    
    
    @Column(name = "cbb_mva_m0_pic1_p1", precision=10, scale=2)
    private BigDecimal cbbMvaM0pic1p1;
    
    @Column(name = "cbb_mva_m0_pic1_p2", precision=10, scale=2)
    private BigDecimal cbbMvaM0pic1p2;
    
    @Column(name = "cbb_mva_m0_pic2_p1", precision=10, scale=2)
    private BigDecimal cbbMvaM0pic2p1;
    
    @Column(name = "cbb_mva_m0_pic2_p2", precision=10, scale=2)
    private BigDecimal cbbMvaM0pic2p2;
    
    
    
    @Column(name = "cbu_mva_mpw_pic1", precision=10, scale=2)
    private BigDecimal cbuMvaMpwpic1;
    
    @Column(name = "cbu_mva_mpw_pic2", precision=10, scale=2)
    private BigDecimal cbuMvaMpwpic2;
    
    
    @Column(name = "cbb_mva_mpw_pic1_p1", precision=10, scale=2)
    private BigDecimal cbbMvaMpwpic1p1;
    
    @Column(name = "cbb_mva_mpw_pic1_p2", precision=10, scale=2)
    private BigDecimal cbbMvaMpwpic1p2;
    
    @Column(name = "cbb_mva_mpw_pic2_p1", precision=10, scale=2)
    private BigDecimal cbbMvaMpwpic2p1;
    
    @Column(name = "cbb_mva_mpw_pic2_p2", precision=10, scale=2)
    private BigDecimal cbbMvaMpwpic2p2;
    
    
    
    @Column(name = "cbu_mva_t1_pic1", precision=10, scale=2)
    private BigDecimal cbuMvaT1pic1;
    
    @Column(name = "cbu_mva_t1_pic2", precision=10, scale=2)
    private BigDecimal cbuMvaT1pic2;
    
    
    @Column(name = "cbb_mva_t1_pic1_p1", precision=10, scale=2)
    private BigDecimal cbbMvaT1pic1p1;
    
    @Column(name = "cbb_mva_t1_pic1_p2", precision=10, scale=2)
    private BigDecimal cbbMvaT1pic1p2;
    
    @Column(name = "cbb_mva_t1_pic2_p1", precision=10, scale=2)
    private BigDecimal cbbMvaT1pic2p1;
    
    @Column(name = "cbb_mva_t1_pic2_p2", precision=10, scale=2)
    private BigDecimal cbbMvaT1pic2p2;
    
    
    
    @Column(name = "cbu_mva_mpa_pic1", precision=10, scale=2)
    private BigDecimal cbuMvaMpapic1;
    
    @Column(name = "cbu_mva_mpa_pic2", precision=10, scale=2)
    private BigDecimal cbuMvaMpapic2;
    
    
    @Column(name = "cbb_mva_mpa_pic1_p1", precision=10, scale=2)
    private BigDecimal cbbMvaMpapic1p1;
    
    @Column(name = "cbb_mva_mpa_pic1_p2", precision=10, scale=2)
    private BigDecimal cbbMvaMpapic1p2;
    
    @Column(name = "cbb_mva_mpa_pic2_p1", precision=10, scale=2)
    private BigDecimal cbbMvaMpapic2p1;
    
    @Column(name = "cbb_mva_mpa_pic2_p2", precision=10, scale=2)
    private BigDecimal cbbMvaMpapic2p2;
    
    
    
    
    @Column(name = "cbu_mva_m2_pic1", precision=10, scale=2)
    private BigDecimal cbuMvaM2pic1;
    
    @Column(name = "cbu_mva_m2_pic2", precision=10, scale=2)
    private BigDecimal cbuMvaM2pic2;
    
    
    @Column(name = "cbb_mva_m2_pic1_p1", precision=10, scale=2)
    private BigDecimal cbbMvaM2pic1p1;
    
    @Column(name = "cbb_mva_m2_pic1_p2", precision=10, scale=2)
    private BigDecimal cbbMvaM2pic1p2;
    
    @Column(name = "cbb_mva_m2_pic2_p1", precision=10, scale=2)
    private BigDecimal cbbMvaM2pic2p1;
    
    @Column(name = "cbb_mva_m2_pic2_p2", precision=10, scale=2)
    private BigDecimal cbbMvaM2pic2p2;
    
    
    
    @Column(name = "cbu_mva_t2_pic1", precision=10, scale=2)
    private BigDecimal cbuMvaT2pic1;
    
    @Column(name = "cbu_mva_t2_pic2", precision=10, scale=2)
    private BigDecimal cbuMvaT2pic2;
    
    
    @Column(name = "cbb_mva_t2_pic1_p1", precision=10, scale=2)
    private BigDecimal cbbMvaT2pic1p1;
    
    @Column(name = "cbb_mva_t2_pic1_p2", precision=10, scale=2)
    private BigDecimal cbbMvaT2pic1p2;
    
    @Column(name = "cbb_mva_t2_pic2_p1", precision=10, scale=2)
    private BigDecimal cbbMvaT2pic2p1;
    
    @Column(name = "cbb_mva_t2_pic2_p2", precision=10, scale=2)
    private BigDecimal cbbMvaT2pic2p2;
    
    
    /*
     * Lavaggio
     */
    @Column(name = "cbu_p_post_lav", precision=10, scale=2)
    private BigDecimal cbuPPostLav;
    
    @Column(name = "cbb_p_post_lav", precision=10, scale=2)
    private BigDecimal cbbPPostLav;
    
    
    /*
     * Foto etichetta
     */
    @Lob
    @Column(name = "foto_etichetta")
    private byte[] fotoEtichetta;

    @Column(name = "foto_etichetta_content_type")
    private String fotoEtichettaContentType;
    
    /*
     * Curva Binder
     */
    @Lob
    @Column(name = "curva_binder")
    private byte[] curvaBinder;

    @Column(name = "curva_binder_content_type")
    private String curvaBinderContentType;
    
    
    
    

    @Lob
    @Column(name = "curva")
    private byte[] curva;

    @Column(name = "curva_content_type")
    private String curvaContentType;

    @Size(max = 65534)
    @Column(name = "classificazione_geotecnica", length = 65534)
    private String classificazioneGeotecnica;

    @Size(max = 65534)
    @Column(name = "note", length = 65534)
    private String note;

    @Column(name = "lavorazione_conclusa")
    private Boolean lavorazioneConclusa;

    @ManyToOne
    private Cassetta cassetta;

    @ManyToOne
    private Operatore operatoreAnalisi;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodiceCampione() {
        return codiceCampione;
    }

    public void setCodiceCampione(String codiceCampione) {
        this.codiceCampione = codiceCampione;
    }

    public Integer getSiglaCampione() {
        return siglaCampione;
    }

    public void setSiglaCampione(Integer siglaCampione) {
        this.siglaCampione = siglaCampione;
    }

    public TipoCampione getTipoCampione() {
        return tipoCampione;
    }

    public void setTipoCampione(TipoCampione tipoCampione) {
        this.tipoCampione = tipoCampione;
    }

    public String getDescrizioneCampione() {
        return descrizioneCampione;
    }

    public void setDescrizioneCampione(String descrizioneCampione) {
        this.descrizioneCampione = descrizioneCampione;
    }

    public byte[] getFotoSacchetto() {
        return fotoSacchetto;
    }

    public void setFotoSacchetto(byte[] fotoSacchetto) {
        this.fotoSacchetto = fotoSacchetto;
    }

    public String getFotoSacchettoContentType() {
        return fotoSacchettoContentType;
    }

    public void setFotoSacchettoContentType(String fotoSacchettoContentType) {
        this.fotoSacchettoContentType = fotoSacchettoContentType;
    }

    public byte[] getFotoCampione() {
        return fotoCampione;
    }

    public void setFotoCampione(byte[] fotoCampione) {
        this.fotoCampione = fotoCampione;
    }

    public String getFotoCampioneContentType() {
        return fotoCampioneContentType;
    }

    public void setFotoCampioneContentType(String fotoCampioneContentType) {
        this.fotoCampioneContentType = fotoCampioneContentType;
    }

    public LocalDate getDataAnalisi() {
        return dataAnalisi;
    }

    public void setDataAnalisi(LocalDate dataAnalisi) {
        this.dataAnalisi = dataAnalisi;
    }

    public Boolean isRipartizioneQuartatura() {
        return ripartizioneQuartatura;
    }

    public void setRipartizioneQuartatura(Boolean ripartizioneQuartatura) {
        this.ripartizioneQuartatura = ripartizioneQuartatura;
    }

    public Boolean isEssiccamento() {
        return essiccamento;
    }

    public void setEssiccamento(Boolean essiccamento) {
        this.essiccamento = essiccamento;
    }

    public Boolean isSetacciaturaSecco() {
        return setacciaturaSecco;
    }

    public void setSetacciaturaSecco(Boolean setacciaturaSecco) {
        this.setacciaturaSecco = setacciaturaSecco;
    }

    public Boolean isLavaggioSetacciatura() {
        return lavaggioSetacciatura;
    }

    public void setLavaggioSetacciatura(Boolean lavaggioSetacciatura) {
        this.lavaggioSetacciatura = lavaggioSetacciatura;
    }

    public String getEssiccamentoNumTeglia() {
        return essiccamentoNumTeglia;
    }

    public void setEssiccamentoNumTeglia(String essiccamentoNumTeglia) {
        this.essiccamentoNumTeglia = essiccamentoNumTeglia;
    }

    public BigDecimal getEssiccamentoPesoTeglia() {
        return essiccamentoPesoTeglia;
    }

    public void setEssiccamentoPesoTeglia(BigDecimal essiccamentoPesoTeglia) {
        this.essiccamentoPesoTeglia = essiccamentoPesoTeglia;
    }

    public BigDecimal getEssiccamentoPesoCampioneLordoIniziale() {
        return essiccamentoPesoCampioneLordoIniziale;
    }

    public void setEssiccamentoPesoCampioneLordoIniziale(BigDecimal essiccamentoPesoCampioneLordoIniziale) {
        this.essiccamentoPesoCampioneLordoIniziale = essiccamentoPesoCampioneLordoIniziale;
    }

    public BigDecimal getEssiccamentoPesoCampioneLordo24H() {
        return essiccamentoPesoCampioneLordo24H;
    }

    public void setEssiccamentoPesoCampioneLordo24H(BigDecimal essiccamentoPesoCampioneLordo24H) {
        this.essiccamentoPesoCampioneLordo24H = essiccamentoPesoCampioneLordo24H;
    }

    public BigDecimal getEssiccamentoPesoCampioneLordo48H() {
        return essiccamentoPesoCampioneLordo48H;
    }

    public void setEssiccamentoPesoCampioneLordo48H(BigDecimal essiccamentoPesoCampioneLordo48H) {
        this.essiccamentoPesoCampioneLordo48H = essiccamentoPesoCampioneLordo48H;
    }

    public Boolean isSabbia() {
        return sabbia;
    }

    public void setSabbia(Boolean sabbia) {
        this.sabbia = sabbia;
    }

    public Boolean isGhiaia() {
        return ghiaia;
    }

    public void setGhiaia(Boolean ghiaia) {
        this.ghiaia = ghiaia;
    }

    public Boolean isMaterialeRisultaVagliato() {
        return materialeRisultaVagliato;
    }

    public void setMaterialeRisultaVagliato(Boolean materialeRisultaVagliato) {
        this.materialeRisultaVagliato = materialeRisultaVagliato;
    }

    public Boolean isDetriti() {
        return detriti;
    }

    public void setDetriti(Boolean detriti) {
        this.detriti = detriti;
    }

    public Boolean isMaterialeFine() {
        return materialeFine;
    }

    public void setMaterialeFine(Boolean materialeFine) {
        this.materialeFine = materialeFine;
    }

    public Boolean isMaterialeOrganico() {
        return materialeOrganico;
    }

    public void setMaterialeOrganico(Boolean materialeOrganico) {
        this.materialeOrganico = materialeOrganico;
    }

    public Boolean isElementiMagg125Mm() {
        return elementiMagg125Mm;
    }

    public void setElementiMagg125Mm(Boolean elementiMagg125Mm) {
        this.elementiMagg125Mm = elementiMagg125Mm;
    }

    public Boolean isDetritiConglomerato() {
        return detritiConglomerato;
    }

    public void setDetritiConglomerato(Boolean detritiConglomerato) {
        this.detritiConglomerato = detritiConglomerato;
    }

    public Boolean isArgilla() {
        return argilla;
    }

    public void setArgilla(Boolean argilla) {
        this.argilla = argilla;
    }

    public Boolean isArgillaMatAlterabile() {
        return argillaMatAlterabile;
    }

    public void setArgillaMatAlterabile(Boolean argillaMatAlterabile) {
        this.argillaMatAlterabile = argillaMatAlterabile;
    }

    public Boolean isGranuliCementati() {
        return granuliCementati;
    }

    public void setGranuliCementati(Boolean granuliCementati) {
        this.granuliCementati = granuliCementati;
    }

    public Boolean isElementiArrotondati() {
        return elementiArrotondati;
    }

    public void setElementiArrotondati(Boolean elementiArrotondati) {
        this.elementiArrotondati = elementiArrotondati;
    }

    public Boolean isElementiSpigolosi() {
        return elementiSpigolosi;
    }

    public void setElementiSpigolosi(Boolean elementiSpigolosi) {
        this.elementiSpigolosi = elementiSpigolosi;
    }

    public Boolean isSfabbricidi() {
        return sfabbricidi;
    }

    public void setSfabbricidi(Boolean sfabbricidi) {
        this.sfabbricidi = sfabbricidi;
    }

    public Boolean isTipoBConforme() {
        return tipoBConforme;
    }

    public void setTipoBConforme(Boolean tipoBConforme) {
        this.tipoBConforme = tipoBConforme;
    }

    

    public BigDecimal getCbuSpessEst1p1() {
		return cbuSpessEst1p1;
	}

	public void setCbuSpessEst1p1(BigDecimal cbuSpessEst1p1) {
		this.cbuSpessEst1p1 = cbuSpessEst1p1;
	}

	public BigDecimal getCbuSpessEst1p2() {
		return cbuSpessEst1p2;
	}

	public void setCbuSpessEst1p2(BigDecimal cbuSpessEst1p2) {
		this.cbuSpessEst1p2 = cbuSpessEst1p2;
	}

	public BigDecimal getCbbSpessEst1p1() {
		return cbbSpessEst1p1;
	}

	public void setCbbSpessEst1p1(BigDecimal cbbSpessEst1p1) {
		this.cbbSpessEst1p1 = cbbSpessEst1p1;
	}

	public BigDecimal getCbbSpessEst1p2() {
		return cbbSpessEst1p2;
	}

	public void setCbbSpessEst1p2(BigDecimal cbbSpessEst1p2) {
		this.cbbSpessEst1p2 = cbbSpessEst1p2;
	}

	public BigDecimal getCbuSpessMedp1() {
		return cbuSpessMedp1;
	}

	public void setCbuSpessMedp1(BigDecimal cbuSpessMedp1) {
		this.cbuSpessMedp1 = cbuSpessMedp1;
	}

	public BigDecimal getCbuSpessMedp2() {
		return cbuSpessMedp2;
	}

	public void setCbuSpessMedp2(BigDecimal cbuSpessMedp2) {
		this.cbuSpessMedp2 = cbuSpessMedp2;
	}

	public BigDecimal getCbbSpessMedp1() {
		return cbbSpessMedp1;
	}

	public void setCbbSpessMedp1(BigDecimal cbbSpessMedp1) {
		this.cbbSpessMedp1 = cbbSpessMedp1;
	}

	public BigDecimal getCbbSpessMedp2() {
		return cbbSpessMedp2;
	}

	public void setCbbSpessMedp2(BigDecimal cbbSpessMedp2) {
		this.cbbSpessMedp2 = cbbSpessMedp2;
	}

	public BigDecimal getCbuSpessEst2p1() {
		return cbuSpessEst2p1;
	}

	public void setCbuSpessEst2p1(BigDecimal cbuSpessEst2p1) {
		this.cbuSpessEst2p1 = cbuSpessEst2p1;
	}

	public BigDecimal getCbuSpessEst2p2() {
		return cbuSpessEst2p2;
	}

	public void setCbuSpessEst2p2(BigDecimal cbuSpessEst2p2) {
		this.cbuSpessEst2p2 = cbuSpessEst2p2;
	}

	public BigDecimal getCbbSpessEst2p1() {
		return cbbSpessEst2p1;
	}

	public void setCbbSpessEst2p1(BigDecimal cbbSpessEst2p1) {
		this.cbbSpessEst2p1 = cbbSpessEst2p1;
	}

	public BigDecimal getCbbSpessEst2p2() {
		return cbbSpessEst2p2;
	}

	public void setCbbSpessEst2p2(BigDecimal cbbSpessEst2p2) {
		this.cbbSpessEst2p2 = cbbSpessEst2p2;
	}

	public BigDecimal getCbuSsdM1p1() {
		return cbuSsdM1p1;
	}

	public void setCbuSsdM1p1(BigDecimal cbuSsdM1p1) {
		this.cbuSsdM1p1 = cbuSsdM1p1;
	}

	public BigDecimal getCbuSsdM1p2() {
		return cbuSsdM1p2;
	}

	public void setCbuSsdM1p2(BigDecimal cbuSsdM1p2) {
		this.cbuSsdM1p2 = cbuSsdM1p2;
	}

	public BigDecimal getCbbSsdM1p1() {
		return cbbSsdM1p1;
	}

	public void setCbbSsdM1p1(BigDecimal cbbSsdM1p1) {
		this.cbbSsdM1p1 = cbbSsdM1p1;
	}

	public BigDecimal getCbbSsdM1p2() {
		return cbbSsdM1p2;
	}

	public void setCbbSsdM1p2(BigDecimal cbbSsdM1p2) {
		this.cbbSsdM1p2 = cbbSsdM1p2;
	}

	public BigDecimal getCbuSsdM2p1() {
		return cbuSsdM2p1;
	}

	public void setCbuSsdM2p1(BigDecimal cbuSsdM2p1) {
		this.cbuSsdM2p1 = cbuSsdM2p1;
	}

	public BigDecimal getCbuSsdM2p2() {
		return cbuSsdM2p2;
	}

	public void setCbuSsdM2p2(BigDecimal cbuSsdM2p2) {
		this.cbuSsdM2p2 = cbuSsdM2p2;
	}

	public BigDecimal getCbbSsdM2p1() {
		return cbbSsdM2p1;
	}

	public void setCbbSsdM2p1(BigDecimal cbbSsdM2p1) {
		this.cbbSsdM2p1 = cbbSsdM2p1;
	}

	public BigDecimal getCbbSsdM2p2() {
		return cbbSsdM2p2;
	}

	public void setCbbSsdM2p2(BigDecimal cbbSsdM2p2) {
		this.cbbSsdM2p2 = cbbSsdM2p2;
	}

	public BigDecimal getCbuSsdM3p1() {
		return cbuSsdM3p1;
	}

	public void setCbuSsdM3p1(BigDecimal cbuSsdM3p1) {
		this.cbuSsdM3p1 = cbuSsdM3p1;
	}

	public BigDecimal getCbuSsdM3p2() {
		return cbuSsdM3p2;
	}

	public void setCbuSsdM3p2(BigDecimal cbuSsdM3p2) {
		this.cbuSsdM3p2 = cbuSsdM3p2;
	}

	public BigDecimal getCbbSsdM3p1() {
		return cbbSsdM3p1;
	}

	public void setCbbSsdM3p1(BigDecimal cbbSsdM3p1) {
		this.cbbSsdM3p1 = cbbSsdM3p1;
	}

	public BigDecimal getCbbSsdM3p2() {
		return cbbSsdM3p2;
	}

	public void setCbbSsdM3p2(BigDecimal cbbSsdM3p2) {
		this.cbbSsdM3p2 = cbbSsdM3p2;
	}

	public BigDecimal getCbuSsdTp1() {
		return cbuSsdTp1;
	}

	public void setCbuSsdTp1(BigDecimal cbuSsdTp1) {
		this.cbuSsdTp1 = cbuSsdTp1;
	}

	public BigDecimal getCbuSsdTp2() {
		return cbuSsdTp2;
	}

	public void setCbuSsdTp2(BigDecimal cbuSsdTp2) {
		this.cbuSsdTp2 = cbuSsdTp2;
	}

	public BigDecimal getCbbSsdTp1() {
		return cbbSsdTp1;
	}

	public void setCbbSsdTp1(BigDecimal cbbSsdTp1) {
		this.cbbSsdTp1 = cbbSsdTp1;
	}

	public BigDecimal getCbbSsdTp2() {
		return cbbSsdTp2;
	}

	public void setCbbSsdTp2(BigDecimal cbbSsdTp2) {
		this.cbbSsdTp2 = cbbSsdTp2;
	}

	public BigDecimal getCbuLegMmp1() {
		return cbuLegMmp1;
	}

	public void setCbuLegMmp1(BigDecimal cbuLegMmp1) {
		this.cbuLegMmp1 = cbuLegMmp1;
	}

	public BigDecimal getCbbLegMmp1() {
		return cbbLegMmp1;
	}

	public void setCbbLegMmp1(BigDecimal cbbLegMmp1) {
		this.cbbLegMmp1 = cbbLegMmp1;
	}

	public BigDecimal getCbbLegMmp2() {
		return cbbLegMmp2;
	}

	public void setCbbLegMmp2(BigDecimal cbbLegMmp2) {
		this.cbbLegMmp2 = cbbLegMmp2;
	}

	public BigDecimal getCbuLegMap1() {
		return cbuLegMap1;
	}

	public void setCbuLegMap1(BigDecimal cbuLegMap1) {
		this.cbuLegMap1 = cbuLegMap1;
	}

	public BigDecimal getCbbLegMap1() {
		return cbbLegMap1;
	}

	public void setCbbLegMap1(BigDecimal cbbLegMap1) {
		this.cbbLegMap1 = cbbLegMap1;
	}

	public BigDecimal getCbbLegMap2() {
		return cbbLegMap2;
	}

	public void setCbbLegMap2(BigDecimal cbbLegMap2) {
		this.cbbLegMap2 = cbbLegMap2;
	}

	public BigDecimal getCbuMvaM0pic1() {
		return cbuMvaM0pic1;
	}

	public void setCbuMvaM0pic1(BigDecimal cbuMvaM0pic1) {
		this.cbuMvaM0pic1 = cbuMvaM0pic1;
	}

	public BigDecimal getCbuMvaM0pic2() {
		return cbuMvaM0pic2;
	}

	public void setCbuMvaM0pic2(BigDecimal cbuMvaM0pic2) {
		this.cbuMvaM0pic2 = cbuMvaM0pic2;
	}

	public BigDecimal getCbbMvaM0pic1p1() {
		return cbbMvaM0pic1p1;
	}

	public void setCbbMvaM0pic1p1(BigDecimal cbbMvaM0pic1p1) {
		this.cbbMvaM0pic1p1 = cbbMvaM0pic1p1;
	}

	public BigDecimal getCbbMvaM0pic1p2() {
		return cbbMvaM0pic1p2;
	}

	public void setCbbMvaM0pic1p2(BigDecimal cbbMvaM0pic1p2) {
		this.cbbMvaM0pic1p2 = cbbMvaM0pic1p2;
	}

	public BigDecimal getCbbMvaM0pic2p1() {
		return cbbMvaM0pic2p1;
	}

	public void setCbbMvaM0pic2p1(BigDecimal cbbMvaM0pic2p1) {
		this.cbbMvaM0pic2p1 = cbbMvaM0pic2p1;
	}

	public BigDecimal getCbbMvaM0pic2p2() {
		return cbbMvaM0pic2p2;
	}

	public void setCbbMvaM0pic2p2(BigDecimal cbbMvaM0pic2p2) {
		this.cbbMvaM0pic2p2 = cbbMvaM0pic2p2;
	}

	public BigDecimal getCbuMvaMpwpic1() {
		return cbuMvaMpwpic1;
	}

	public void setCbuMvaMpwpic1(BigDecimal cbuMvaMpwpic1) {
		this.cbuMvaMpwpic1 = cbuMvaMpwpic1;
	}

	public BigDecimal getCbuMvaMpwpic2() {
		return cbuMvaMpwpic2;
	}

	public void setCbuMvaMpwpic2(BigDecimal cbuMvaMpwpic2) {
		this.cbuMvaMpwpic2 = cbuMvaMpwpic2;
	}

	public BigDecimal getCbbMvaMpwpic1p1() {
		return cbbMvaMpwpic1p1;
	}

	public void setCbbMvaMpwpic1p1(BigDecimal cbbMvaMpwpic1p1) {
		this.cbbMvaMpwpic1p1 = cbbMvaMpwpic1p1;
	}

	public BigDecimal getCbbMvaMpwpic1p2() {
		return cbbMvaMpwpic1p2;
	}

	public void setCbbMvaMpwpic1p2(BigDecimal cbbMvaMpwpic1p2) {
		this.cbbMvaMpwpic1p2 = cbbMvaMpwpic1p2;
	}

	public BigDecimal getCbbMvaMpwpic2p1() {
		return cbbMvaMpwpic2p1;
	}

	public void setCbbMvaMpwpic2p1(BigDecimal cbbMvaMpwpic2p1) {
		this.cbbMvaMpwpic2p1 = cbbMvaMpwpic2p1;
	}

	public BigDecimal getCbbMvaMpwpic2p2() {
		return cbbMvaMpwpic2p2;
	}

	public void setCbbMvaMpwpic2p2(BigDecimal cbbMvaMpwpic2p2) {
		this.cbbMvaMpwpic2p2 = cbbMvaMpwpic2p2;
	}

	public BigDecimal getCbuMvaT1pic1() {
		return cbuMvaT1pic1;
	}

	public void setCbuMvaT1pic1(BigDecimal cbuMvaT1pic1) {
		this.cbuMvaT1pic1 = cbuMvaT1pic1;
	}

	public BigDecimal getCbuMvaT1pic2() {
		return cbuMvaT1pic2;
	}

	public void setCbuMvaT1pic2(BigDecimal cbuMvaT1pic2) {
		this.cbuMvaT1pic2 = cbuMvaT1pic2;
	}

	public BigDecimal getCbbMvaT1pic1p1() {
		return cbbMvaT1pic1p1;
	}

	public void setCbbMvaT1pic1p1(BigDecimal cbbMvaT1pic1p1) {
		this.cbbMvaT1pic1p1 = cbbMvaT1pic1p1;
	}

	public BigDecimal getCbbMvaT1pic1p2() {
		return cbbMvaT1pic1p2;
	}

	public void setCbbMvaT1pic1p2(BigDecimal cbbMvaT1pic1p2) {
		this.cbbMvaT1pic1p2 = cbbMvaT1pic1p2;
	}

	public BigDecimal getCbbMvaT1pic2p1() {
		return cbbMvaT1pic2p1;
	}

	public void setCbbMvaT1pic2p1(BigDecimal cbbMvaT1pic2p1) {
		this.cbbMvaT1pic2p1 = cbbMvaT1pic2p1;
	}

	public BigDecimal getCbbMvaT1pic2p2() {
		return cbbMvaT1pic2p2;
	}

	public void setCbbMvaT1pic2p2(BigDecimal cbbMvaT1pic2p2) {
		this.cbbMvaT1pic2p2 = cbbMvaT1pic2p2;
	}

	public BigDecimal getCbuMvaMpapic1() {
		return cbuMvaMpapic1;
	}

	public void setCbuMvaMpapic1(BigDecimal cbuMvaMpapic1) {
		this.cbuMvaMpapic1 = cbuMvaMpapic1;
	}

	public BigDecimal getCbuMvaMpapic2() {
		return cbuMvaMpapic2;
	}

	public void setCbuMvaMpapic2(BigDecimal cbuMvaMpapic2) {
		this.cbuMvaMpapic2 = cbuMvaMpapic2;
	}

	public BigDecimal getCbbMvaMpapic1p1() {
		return cbbMvaMpapic1p1;
	}

	public void setCbbMvaMpapic1p1(BigDecimal cbbMvaMpapic1p1) {
		this.cbbMvaMpapic1p1 = cbbMvaMpapic1p1;
	}

	public BigDecimal getCbbMvaMpapic1p2() {
		return cbbMvaMpapic1p2;
	}

	public void setCbbMvaMpapic1p2(BigDecimal cbbMvaMpapic1p2) {
		this.cbbMvaMpapic1p2 = cbbMvaMpapic1p2;
	}

	public BigDecimal getCbbMvaMpapic2p1() {
		return cbbMvaMpapic2p1;
	}

	public void setCbbMvaMpapic2p1(BigDecimal cbbMvaMpapic2p1) {
		this.cbbMvaMpapic2p1 = cbbMvaMpapic2p1;
	}

	public BigDecimal getCbbMvaMpapic2p2() {
		return cbbMvaMpapic2p2;
	}

	public void setCbbMvaMpapic2p2(BigDecimal cbbMvaMpapic2p2) {
		this.cbbMvaMpapic2p2 = cbbMvaMpapic2p2;
	}

	public BigDecimal getCbuMvaM2pic1() {
		return cbuMvaM2pic1;
	}

	public void setCbuMvaM2pic1(BigDecimal cbuMvaM2pic1) {
		this.cbuMvaM2pic1 = cbuMvaM2pic1;
	}

	public BigDecimal getCbuMvaM2pic2() {
		return cbuMvaM2pic2;
	}

	public void setCbuMvaM2pic2(BigDecimal cbuMvaM2pic2) {
		this.cbuMvaM2pic2 = cbuMvaM2pic2;
	}

	public BigDecimal getCbbMvaM2pic1p1() {
		return cbbMvaM2pic1p1;
	}

	public void setCbbMvaM2pic1p1(BigDecimal cbbMvaM2pic1p1) {
		this.cbbMvaM2pic1p1 = cbbMvaM2pic1p1;
	}

	public BigDecimal getCbbMvaM2pic1p2() {
		return cbbMvaM2pic1p2;
	}

	public void setCbbMvaM2pic1p2(BigDecimal cbbMvaM2pic1p2) {
		this.cbbMvaM2pic1p2 = cbbMvaM2pic1p2;
	}

	public BigDecimal getCbbMvaM2pic2p1() {
		return cbbMvaM2pic2p1;
	}

	public void setCbbMvaM2pic2p1(BigDecimal cbbMvaM2pic2p1) {
		this.cbbMvaM2pic2p1 = cbbMvaM2pic2p1;
	}

	public BigDecimal getCbbMvaM2pic2p2() {
		return cbbMvaM2pic2p2;
	}

	public void setCbbMvaM2pic2p2(BigDecimal cbbMvaM2pic2p2) {
		this.cbbMvaM2pic2p2 = cbbMvaM2pic2p2;
	}

	public BigDecimal getCbuMvaT2pic1() {
		return cbuMvaT2pic1;
	}

	public void setCbuMvaT2pic1(BigDecimal cbuMvaT2pic1) {
		this.cbuMvaT2pic1 = cbuMvaT2pic1;
	}

	public BigDecimal getCbuMvaT2pic2() {
		return cbuMvaT2pic2;
	}

	public void setCbuMvaT2pic2(BigDecimal cbuMvaT2pic2) {
		this.cbuMvaT2pic2 = cbuMvaT2pic2;
	}

	public BigDecimal getCbbMvaT2pic1p1() {
		return cbbMvaT2pic1p1;
	}

	public void setCbbMvaT2pic1p1(BigDecimal cbbMvaT2pic1p1) {
		this.cbbMvaT2pic1p1 = cbbMvaT2pic1p1;
	}

	public BigDecimal getCbbMvaT2pic1p2() {
		return cbbMvaT2pic1p2;
	}

	public void setCbbMvaT2pic1p2(BigDecimal cbbMvaT2pic1p2) {
		this.cbbMvaT2pic1p2 = cbbMvaT2pic1p2;
	}

	public BigDecimal getCbbMvaT2pic2p1() {
		return cbbMvaT2pic2p1;
	}

	public void setCbbMvaT2pic2p1(BigDecimal cbbMvaT2pic2p1) {
		this.cbbMvaT2pic2p1 = cbbMvaT2pic2p1;
	}

	public BigDecimal getCbbMvaT2pic2p2() {
		return cbbMvaT2pic2p2;
	}

	public void setCbbMvaT2pic2p2(BigDecimal cbbMvaT2pic2p2) {
		this.cbbMvaT2pic2p2 = cbbMvaT2pic2p2;
	}

	public BigDecimal getCbuPPostLav() {
		return cbuPPostLav;
	}

	public void setCbuPPostLav(BigDecimal cbuPPostLav) {
		this.cbuPPostLav = cbuPPostLav;
	}

	public BigDecimal getCbbPPostLav() {
		return cbbPPostLav;
	}

	public void setCbbPPostLav(BigDecimal cbbPPostLav) {
		this.cbbPPostLav = cbbPPostLav;
	}

	public byte[] getFotoEtichetta() {
		return fotoEtichetta;
	}

	public void setFotoEtichetta(byte[] fotoEtichetta) {
		this.fotoEtichetta = fotoEtichetta;
	}

	public String getFotoEtichettaContentType() {
		return fotoEtichettaContentType;
	}

	public void setFotoEtichettaContentType(String fotoEtichettaContentType) {
		this.fotoEtichettaContentType = fotoEtichettaContentType;
	}

	public byte[] getCurvaBinder() {
		return curvaBinder;
	}

	public void setCurvaBinder(byte[] curvaBinder) {
		this.curvaBinder = curvaBinder;
	}

	public String getCurvaBinderContentType() {
		return curvaBinderContentType;
	}

	public void setCurvaBinderContentType(String curvaBinderContentType) {
		this.curvaBinderContentType = curvaBinderContentType;
	}

	public byte[] getCurva() {
        return curva;
    }

    public void setCurva(byte[] curva) {
        this.curva = curva;
    }

    public String getCurvaContentType() {
        return curvaContentType;
    }

    public void setCurvaContentType(String curvaContentType) {
        this.curvaContentType = curvaContentType;
    }

    public String getClassificazioneGeotecnica() {
        return classificazioneGeotecnica;
    }

    public void setClassificazioneGeotecnica(String classificazioneGeotecnica) {
        this.classificazioneGeotecnica = classificazioneGeotecnica;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Boolean isLavorazioneConclusa() {
        return lavorazioneConclusa;
    }

    public void setLavorazioneConclusa(Boolean lavorazioneConclusa) {
        this.lavorazioneConclusa = lavorazioneConclusa;
    }

    public Cassetta getCassetta() {
        return cassetta;
    }

    public void setCassetta(Cassetta cassetta) {
        this.cassetta = cassetta;
    }

    public Operatore getOperatoreAnalisi() {
        return operatoreAnalisi;
    }

    public void setOperatoreAnalisi(Operatore operatore) {
        this.operatoreAnalisi = operatore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Campione campione = (Campione) o;
        if(campione.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, campione.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Campione{" +
            "id=" + id +
            ", codiceCampione='" + codiceCampione + "'" +
            ", siglaCampione='" + siglaCampione + "'" +
            ", tipoCampione='" + tipoCampione + "'" +
            ", descrizioneCampione='" + descrizioneCampione + "'" +
            ", fotoSacchetto='" + fotoSacchetto + "'" +
            ", fotoSacchettoContentType='" + fotoSacchettoContentType + "'" +
            ", fotoCampione='" + fotoCampione + "'" +
            ", fotoCampioneContentType='" + fotoCampioneContentType + "'" +
            ", dataAnalisi='" + dataAnalisi + "'" +
            ", ripartizioneQuartatura='" + ripartizioneQuartatura + "'" +
            ", essiccamento='" + essiccamento + "'" +
            ", setacciaturaSecco='" + setacciaturaSecco + "'" +
            ", lavaggioSetacciatura='" + lavaggioSetacciatura + "'" +
            ", essiccamentoNumTeglia='" + essiccamentoNumTeglia + "'" +
            ", essiccamentoPesoTeglia='" + essiccamentoPesoTeglia + "'" +
            ", essiccamentoPesoCampioneLordoIniziale='" + essiccamentoPesoCampioneLordoIniziale + "'" +
            ", essiccamentoPesoCampioneLordo24H='" + essiccamentoPesoCampioneLordo24H + "'" +
            ", essiccamentoPesoCampioneLordo48H='" + essiccamentoPesoCampioneLordo48H + "'" +
            ", sabbia='" + sabbia + "'" +
            ", ghiaia='" + ghiaia + "'" +
            ", materialeRisultaVagliato='" + materialeRisultaVagliato + "'" +
            ", detriti='" + detriti + "'" +
            ", materialeFine='" + materialeFine + "'" +
            ", materialeOrganico='" + materialeOrganico + "'" +
            ", elementiMagg125Mm='" + elementiMagg125Mm + "'" +
            ", detritiConglomerato='" + detritiConglomerato + "'" +
            ", argilla='" + argilla + "'" +
            ", argillaMatAlterabile='" + argillaMatAlterabile + "'" +
            ", granuliCementati='" + granuliCementati + "'" +
            ", elementiArrotondati='" + elementiArrotondati + "'" +
            ", elementiSpigolosi='" + elementiSpigolosi + "'" +
            ", sfabbricidi='" + sfabbricidi + "'" +
            ", tipoBConforme='" + tipoBConforme + "'" +
            
            ", curva='" + curva + "'" +
            ", curvaContentType='" + curvaContentType + "'" +
            ", classificazioneGeotecnica='" + classificazioneGeotecnica + "'" +
            ", note='" + note + "'" +
            ", lavorazioneConclusa='" + lavorazioneConclusa + "'" +
            '}';
    }
}
