package it.cnr.igag.italgas.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Laboratorio.
 */
@Entity
@Table(name = "laboratorio")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "laboratorio")
public class Laboratorio extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome")
    private String nome;

    @NotNull
    @Column(name = "istituto", nullable = false)
    private String istituto;

    @NotNull
    @Column(name = "responsabile", nullable = false)
    private String responsabile;

    @Column(name = "indirizzo")
    private String indirizzo;

    @Size(max = 65534)
    @Column(name = "teglie", length = 65534)
    private String teglie;
    
    @Size(max = 65534)
    @Column(name = "setacci", length = 65534)
    private String setacci;
    
    @Size(max = 65534)
    @Column(name = "coperchi", length = 65534)
    private String coperchi;
    
    @Lob
    @Column(name = "logo")
    private byte[] logo;

    @Column(name = "logo_content_type")
    private String logoContentType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getIstituto() {
        return istituto;
    }

    public void setIstituto(String istituto) {
        this.istituto = istituto;
    }

    public String getResponsabile() {
        return responsabile;
    }

    public void setResponsabile(String responsabile) {
        this.responsabile = responsabile;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public String getTeglie() {
		return teglie;
	}

	public void setTeglie(String teglie) {
		this.teglie = teglie;
	}

	public String getSetacci() {
		return setacci;
	}

	public void setSetacci(String setacci) {
		this.setacci = setacci;
	}

	public String getCoperchi() {
		return coperchi;
	}

	public void setCoperchi(String coperchi) {
		this.coperchi = coperchi;
	}

	public byte[] getLogo() {
        return logo;
    }

    public void setLogo(byte[] logo) {
        this.logo = logo;
    }

    public String getLogoContentType() {
        return logoContentType;
    }

    public void setLogoContentType(String logoContentType) {
        this.logoContentType = logoContentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Laboratorio laboratorio = (Laboratorio) o;
        if(laboratorio.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, laboratorio.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Laboratorio{" +
            "id=" + id +
            ", nome='" + nome + "'" +
            ", istituto='" + istituto + "'" +
            ", responsabile='" + responsabile + "'" +
            ", indirizzo='" + indirizzo + "'" +
            ", logo='" + logo + "'" +
            ", logoContentType='" + logoContentType + "'" +
            '}';
    }
}
