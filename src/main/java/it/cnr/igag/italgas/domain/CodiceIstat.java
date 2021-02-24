package it.cnr.igag.italgas.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A CodiceIstat.
 */
@Entity
@Table(name = "codice_istat")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "codiceistat")
public class CodiceIstat extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "codice_istat")
    private Integer codiceIstat;

    @Column(name = "comune")
    private String comune;

    @Column(name = "provincia")
    private String provincia;

    @Column(name = "regione")
    private String regione;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCodiceIstat() {
        return codiceIstat;
    }

    public void setCodiceIstat(Integer codiceIstat) {
        this.codiceIstat = codiceIstat;
    }

    public String getComune() {
        return comune;
    }

    public void setComune(String comune) {
        this.comune = comune;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getRegione() {
        return regione;
    }

    public void setRegione(String regione) {
        this.regione = regione;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CodiceIstat codiceIstat = (CodiceIstat) o;
        if(codiceIstat.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, codiceIstat.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "CodiceIstat{" +
            "id=" + id +
            ", codiceIstat='" + codiceIstat + "'" +
            ", comune='" + comune + "'" +
            ", provincia='" + provincia + "'" +
            ", regione='" + regione + "'" +
            '}';
    }
}
