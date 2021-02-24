package it.cnr.igag.italgas.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import it.cnr.igag.italgas.domain.enumeration.StatoContenitore;

import it.cnr.igag.italgas.domain.enumeration.StatoAttualeCassetta;

/**
 * A Cassetta.
 */
@Entity
@Table(name = "cassetta")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "cassetta")
public class Cassetta extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codice_cassetta")
    private String codiceCassetta;

    @Column(name = "odl")
    private String odl;

    @Column(name = "rif_geografo")
    private String rifGeografo;

    @Column(name = "prioritario")
    private Boolean prioritario;

    @Column(name = "indirizzo_scavo")
    private String indirizzoScavo;

    @Column(name = "denom_cantiere")
    private String denomCantiere;

    @Column(name = "codice_rdc")
    private String codiceRdc;

    @Column(name = "data_scavo")
    private LocalDate dataScavo;

    @Column(name = "coord_gps_n")
    private String coordGpsN;

    @Column(name = "coord_gps_e")
    private String coordGpsE;

    @Column(name = "coord_x")
    private Double coordX;

    @Column(name = "coord_y")
    private Double coordY;

    @Column(name = "centro_operativo")
    private String centroOperativo;

    @Column(name = "impresa_appaltatrice")
    private String impresaAppaltatrice;

    @Column(name = "incaricato_appaltatore")
    private String incaricatoAppaltatore;

    @Column(name = "tecnico_itg_resp")
    private String tecnicoItgResp;

    @Column(name = "num_campioni")
    private String numCampioni;

    @Column(name = "presenza_campione_10")
    private Boolean presenzaCampione10;

    @Column(name = "presenza_campione_11")
    private Boolean presenzaCampione11;

    @Column(name = "presenza_campione_12")
    private Boolean presenzaCampione12;

    @Column(name = "presenza_campione_13")
    private Boolean presenzaCampione13;

    @Column(name = "presenza_campione_14")
    private Boolean presenzaCampione14;

    @Column(name = "presenza_campione_15")
    private Boolean presenzaCampione15;

    @Column(name = "presenza_campione_16")
    private Boolean presenzaCampione16;

    @Column(name = "presenza_campione_17")
    private Boolean presenzaCampione17;

    @Enumerated(EnumType.STRING)
    @Column(name = "stato_contenitore")
    private StatoContenitore statoContenitore;

    @Enumerated(EnumType.STRING)
    @Column(name = "stato_attuale_cassetta")
    private StatoAttualeCassetta statoAttualeCassetta;

    @Column(name = "contenuto_inquinato")
    private Boolean contenutoInquinato;

    @Column(name = "conglomerato_bituminoso")
    private Boolean conglomeratoBituminoso;
    
    @Column(name = "presenza_cbu")
    private Boolean presenzaCbu;
    
    @Column(name = "presenza_cbb1s")
    private Boolean presenzaCbb1s;
    
    @Column(name = "presenza_cbb2s")
    private Boolean presenzaCbb2s;

    @Lob
    @Column(name = "foto_contenitore")
    private byte[] fotoContenitore;

    @Column(name = "foto_contenitore_content_type")
    private String fotoContenitoreContentType;

    @Lob
    @Column(name = "foto_contenuto")
    private byte[] fotoContenuto;

    @Column(name = "foto_contenuto_content_type")
    private String fotoContenutoContentType;

    @Column(name = "ms_sismicita_locale")
    private String msSismicitaLocale;

    @Column(name = "ms_val_accelerazione")
    private String msValAccelerazione;

    @Size(max = 65534)
    @Column(name = "note", length = 65534)
    private String note;

    @Lob
    @Column(name = "certificato_pdf")
    private byte[] certificatoPdf;

    @Column(name = "certificato_pdf_content_type")
    private String certificatoPdfContentType;

    @Column(name = "num_protocollo_certificato")
    private String numProtocolloCertificato;

    @ManyToOne
    private CodiceIstat codiceIstat;

    @ManyToOne
    private Consegna consegna;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodiceCassetta() {
        return codiceCassetta;
    }

    public void setCodiceCassetta(String codiceCassetta) {
        this.codiceCassetta = codiceCassetta;
    }

    public String getOdl() {
        return odl;
    }

    public void setOdl(String odl) {
        this.odl = odl;
    }

    public String getRifGeografo() {
        return rifGeografo;
    }

    public void setRifGeografo(String rifGeografo) {
        this.rifGeografo = rifGeografo;
    }

    public Boolean isPrioritario() {
        return prioritario;
    }
    public Boolean getPrioritario() {
        return prioritario;
    }

    public void setPrioritario(Boolean prioritario) {
        this.prioritario = prioritario;
    }

    public String getIndirizzoScavo() {
        return indirizzoScavo;
    }

    public void setIndirizzoScavo(String indirizzoScavo) {
        this.indirizzoScavo = indirizzoScavo;
    }

    public String getDenomCantiere() {
        return denomCantiere;
    }

    public void setDenomCantiere(String denomCantiere) {
        this.denomCantiere = denomCantiere;
    }

    public String getCodiceRdc() {
        return codiceRdc;
    }

    public void setCodiceRdc(String codiceRdc) {
        this.codiceRdc = codiceRdc;
    }

    public LocalDate getDataScavo() {
        return dataScavo;
    }

    public void setDataScavo(LocalDate dataScavo) {
        this.dataScavo = dataScavo;
    }

    public String getCoordGpsN() {
        return coordGpsN;
    }

    public void setCoordGpsN(String coordGpsN) {
        this.coordGpsN = coordGpsN;
    }

    public String getCoordGpsE() {
        return coordGpsE;
    }

    public void setCoordGpsE(String coordGpsE) {
        this.coordGpsE = coordGpsE;
    }

    public Double getCoordX() {
        return coordX;
    }

    public void setCoordX(Double coordX) {
        this.coordX = coordX;
    }

    public Double getCoordY() {
        return coordY;
    }

    public void setCoordY(Double coordY) {
        this.coordY = coordY;
    }

    public String getCentroOperativo() {
        return centroOperativo;
    }

    public void setCentroOperativo(String centroOperativo) {
        this.centroOperativo = centroOperativo;
    }

    public String getImpresaAppaltatrice() {
        return impresaAppaltatrice;
    }

    public void setImpresaAppaltatrice(String impresaAppaltatrice) {
        this.impresaAppaltatrice = impresaAppaltatrice;
    }

    public String getIncaricatoAppaltatore() {
        return incaricatoAppaltatore;
    }

    public void setIncaricatoAppaltatore(String incaricatoAppaltatore) {
        this.incaricatoAppaltatore = incaricatoAppaltatore;
    }

    public String getTecnicoItgResp() {
        return tecnicoItgResp;
    }

    public void setTecnicoItgResp(String tecnicoItgResp) {
        this.tecnicoItgResp = tecnicoItgResp;
    }

    public String getNumCampioni() {
        return numCampioni;
    }

    public void setNumCampioni(String numCampioni) {
        this.numCampioni = numCampioni;
    }

    public Boolean isPresenzaCampione10() {
        return presenzaCampione10;
    }
    public Boolean getPresenzaCampione10() {
        return presenzaCampione10;
    }

    public void setPresenzaCampione10(Boolean presenzaCampione10) {
        this.presenzaCampione10 = presenzaCampione10;
    }

    public Boolean isPresenzaCampione11() {
        return presenzaCampione11;
    }

    public void setPresenzaCampione11(Boolean presenzaCampione11) {
        this.presenzaCampione11 = presenzaCampione11;
    }

    public Boolean isPresenzaCampione12() {
        return presenzaCampione12;
    }
    public Boolean getPresenzaCampione12() {
        return presenzaCampione12;
    }

    public void setPresenzaCampione12(Boolean presenzaCampione12) {
        this.presenzaCampione12 = presenzaCampione12;
    }

    public Boolean isPresenzaCampione13() {
        return presenzaCampione13;
    }

    public void setPresenzaCampione13(Boolean presenzaCampione13) {
        this.presenzaCampione13 = presenzaCampione13;
    }

    public Boolean isPresenzaCampione14() {
        return presenzaCampione14;
    }
    public Boolean getPresenzaCampione14() {
        return presenzaCampione14;
    }

    public void setPresenzaCampione14(Boolean presenzaCampione14) {
        this.presenzaCampione14 = presenzaCampione14;
    }

    public Boolean isPresenzaCampione15() {
        return presenzaCampione15;
    }

    public void setPresenzaCampione15(Boolean presenzaCampione15) {
        this.presenzaCampione15 = presenzaCampione15;
    }

    public Boolean isPresenzaCampione16() {
        return presenzaCampione16;
    }
    public Boolean getPresenzaCampione16() {
        return presenzaCampione16;
    }

    public void setPresenzaCampione16(Boolean presenzaCampione16) {
        this.presenzaCampione16 = presenzaCampione16;
    }

    public Boolean isPresenzaCampione17() {
        return presenzaCampione17;
    }

    public void setPresenzaCampione17(Boolean presenzaCampione17) {
        this.presenzaCampione17 = presenzaCampione17;
    }

    public StatoContenitore getStatoContenitore() {
        return statoContenitore;
    }

    public void setStatoContenitore(StatoContenitore statoContenitore) {
        this.statoContenitore = statoContenitore;
    }

    public StatoAttualeCassetta getStatoAttualeCassetta() {
        return statoAttualeCassetta;
    }

    public void setStatoAttualeCassetta(StatoAttualeCassetta statoAttualeCassetta) {
        this.statoAttualeCassetta = statoAttualeCassetta;
    }

    public Boolean isContenutoInquinato() {
        return contenutoInquinato;
    }

    public void setContenutoInquinato(Boolean contenutoInquinato) {
        this.contenutoInquinato = contenutoInquinato;
    }

    public Boolean isConglomeratoBituminoso() {
        return conglomeratoBituminoso;
    }
    public Boolean getConglomeratoBituminoso() {
        return conglomeratoBituminoso;
    }

    public void setConglomeratoBituminoso(Boolean conglomeratoBituminoso) {
        this.conglomeratoBituminoso = conglomeratoBituminoso;
    }

    public Boolean getPresenzaCbu() {
		return presenzaCbu;
	}

	public void setPresenzaCbu(Boolean presenzaCbu) {
		this.presenzaCbu = presenzaCbu;
	}

	public Boolean getPresenzaCbb1s() {
		return presenzaCbb1s;
	}

	public void setPresenzaCbb1s(Boolean presenzaCbb1s) {
		this.presenzaCbb1s = presenzaCbb1s;
	}

	public Boolean getPresenzaCbb2s() {
		return presenzaCbb2s;
	}

	public void setPresenzaCbb2s(Boolean presenzaCbb2s) {
		this.presenzaCbb2s = presenzaCbb2s;
	}

	public byte[] getFotoContenitore() {
        return fotoContenitore;
    }

    public void setFotoContenitore(byte[] fotoContenitore) {
        this.fotoContenitore = fotoContenitore;
    }

    public String getFotoContenitoreContentType() {
        return fotoContenitoreContentType;
    }

    public void setFotoContenitoreContentType(String fotoContenitoreContentType) {
        this.fotoContenitoreContentType = fotoContenitoreContentType;
    }

    public byte[] getFotoContenuto() {
        return fotoContenuto;
    }

    public void setFotoContenuto(byte[] fotoContenuto) {
        this.fotoContenuto = fotoContenuto;
    }

    public String getFotoContenutoContentType() {
        return fotoContenutoContentType;
    }

    public void setFotoContenutoContentType(String fotoContenutoContentType) {
        this.fotoContenutoContentType = fotoContenutoContentType;
    }

    public String getMsSismicitaLocale() {
        return msSismicitaLocale;
    }

    public void setMsSismicitaLocale(String msSismicitaLocale) {
        this.msSismicitaLocale = msSismicitaLocale;
    }

    public String getMsValAccelerazione() {
        return msValAccelerazione;
    }

    public void setMsValAccelerazione(String msValAccelerazione) {
        this.msValAccelerazione = msValAccelerazione;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public byte[] getCertificatoPdf() {
        return certificatoPdf;
    }

    public void setCertificatoPdf(byte[] certificatoPdf) {
        this.certificatoPdf = certificatoPdf;
    }

    public String getCertificatoPdfContentType() {
        return certificatoPdfContentType;
    }

    public void setCertificatoPdfContentType(String certificatoPdfContentType) {
        this.certificatoPdfContentType = certificatoPdfContentType;
    }

    public String getNumProtocolloCertificato() {
        return numProtocolloCertificato;
    }

    public void setNumProtocolloCertificato(String numProtocolloCertificato) {
        this.numProtocolloCertificato = numProtocolloCertificato;
    }

    public CodiceIstat getCodiceIstat() {
        return codiceIstat;
    }

    public void setCodiceIstat(CodiceIstat codiceIstat) {
        this.codiceIstat = codiceIstat;
    }

    public Consegna getConsegna() {
        return consegna;
    }

    public void setConsegna(Consegna consegna) {
        this.consegna = consegna;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Cassetta cassetta = (Cassetta) o;
        if(cassetta.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, cassetta.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Cassetta{" +
            "id=" + id +
            ", codiceCassetta='" + codiceCassetta + "'" +
            ", odl='" + odl + "'" +
            ", rifGeografo='" + rifGeografo + "'" +
            ", prioritario='" + prioritario + "'" +
            ", indirizzoScavo='" + indirizzoScavo + "'" +
            ", denomCantiere='" + denomCantiere + "'" +
            ", codiceRdc='" + codiceRdc + "'" +
            ", dataScavo='" + dataScavo + "'" +
            ", coordGpsN='" + coordGpsN + "'" +
            ", coordGpsE='" + coordGpsE + "'" +
            ", coordX='" + coordX + "'" +
            ", coordY='" + coordY + "'" +
            ", centroOperativo='" + centroOperativo + "'" +
            ", impresaAppaltatrice='" + impresaAppaltatrice + "'" +
            ", incaricatoAppaltatore='" + incaricatoAppaltatore + "'" +
            ", tecnicoItgResp='" + tecnicoItgResp + "'" +
            ", numCampioni='" + numCampioni + "'" +
            ", presenzaCampione10='" + presenzaCampione10 + "'" +
            ", presenzaCampione11='" + presenzaCampione11 + "'" +
            ", presenzaCampione12='" + presenzaCampione12 + "'" +
            ", presenzaCampione13='" + presenzaCampione13 + "'" +
            ", presenzaCampione14='" + presenzaCampione14 + "'" +
            ", presenzaCampione15='" + presenzaCampione15 + "'" +
            ", presenzaCampione16='" + presenzaCampione16 + "'" +
            ", presenzaCampione17='" + presenzaCampione17 + "'" +
            ", statoContenitore='" + statoContenitore + "'" +
            ", statoAttualeCassetta='" + statoAttualeCassetta + "'" +
            ", contenutoInquinato='" + contenutoInquinato + "'" +
            ", conglomeratoBituminoso='" + conglomeratoBituminoso + "'" +
            ", fotoContenitore='" + fotoContenitore + "'" +
            ", fotoContenitoreContentType='" + fotoContenitoreContentType + "'" +
            ", fotoContenuto='" + fotoContenuto + "'" +
            ", fotoContenutoContentType='" + fotoContenutoContentType + "'" +
            ", msSismicitaLocale='" + msSismicitaLocale + "'" +
            ", msValAccelerazione='" + msValAccelerazione + "'" +
            ", note='" + note + "'" +
            ", certificatoPdf='" + certificatoPdf + "'" +
            ", certificatoPdfContentType='" + certificatoPdfContentType + "'" +
            ", numProtocolloCertificato='" + numProtocolloCertificato + "'" +
            '}';
    }
}
