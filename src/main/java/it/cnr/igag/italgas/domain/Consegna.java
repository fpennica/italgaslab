package it.cnr.igag.italgas.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A Consegna.
 */
@Entity
@Table(name = "consegna")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "consegna")
public class Consegna extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "data_consegna", nullable = false)
    private LocalDate dataConsegna;

    @Column(name = "trasportatore")
    private String trasportatore;

    @Column(name = "num_cassette")
    private Integer numCassette;

    @Column(name = "num_protocollo_accettazione")
    private String numProtocolloAccettazione;

    @Lob
    @Column(name = "protocollo_accettazione")
    private byte[] protocolloAccettazione;

    @Column(name = "protocollo_accettazione_content_type")
    private String protocolloAccettazioneContentType;

    @ManyToOne
    private Laboratorio laboratorio;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDataConsegna() {
        return dataConsegna;
    }

    public void setDataConsegna(LocalDate dataConsegna) {
        this.dataConsegna = dataConsegna;
    }

    public String getTrasportatore() {
        return trasportatore;
    }

    public void setTrasportatore(String trasportatore) {
        this.trasportatore = trasportatore;
    }

    public Integer getNumCassette() {
        return numCassette;
    }

    public void setNumCassette(Integer numCassette) {
        this.numCassette = numCassette;
    }

    public String getNumProtocolloAccettazione() {
        return numProtocolloAccettazione;
    }

    public void setNumProtocolloAccettazione(String numProtocolloAccettazione) {
        this.numProtocolloAccettazione = numProtocolloAccettazione;
    }

    public byte[] getProtocolloAccettazione() {
        return protocolloAccettazione;
    }

    public void setProtocolloAccettazione(byte[] protocolloAccettazione) {
        this.protocolloAccettazione = protocolloAccettazione;
    }

    public String getProtocolloAccettazioneContentType() {
        return protocolloAccettazioneContentType;
    }

    public void setProtocolloAccettazioneContentType(String protocolloAccettazioneContentType) {
        this.protocolloAccettazioneContentType = protocolloAccettazioneContentType;
    }

    public Laboratorio getLaboratorio() {
        return laboratorio;
    }

    public void setLaboratorio(Laboratorio laboratorio) {
        this.laboratorio = laboratorio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Consegna consegna = (Consegna) o;
        if(consegna.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, consegna.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Consegna{" +
            "id=" + id +
            ", dataConsegna='" + dataConsegna + "'" +
            ", trasportatore='" + trasportatore + "'" +
            ", numCassette='" + numCassette + "'" +
            ", numProtocolloAccettazione='" + numProtocolloAccettazione + "'" +
            ", protocolloAccettazione='" + protocolloAccettazione + "'" +
            ", protocolloAccettazioneContentType='" + protocolloAccettazioneContentType + "'" +
            '}';
    }
}
