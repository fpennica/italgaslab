package it.cnr.igag.italgas.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A Pesata.
 */
@Entity
@Table(name = "pesata")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "pesata")
public class Pesata extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "binder")
    private Boolean binder;
    
    @Column(name = "num_pesata")
    private Integer numPesata;

    @Column(name = "peso_netto", precision=10, scale=2)
    private BigDecimal pesoNetto;

    @Column(name = "tratt_125_mm", precision=10, scale=2)
    private BigDecimal tratt125Mm;

    @Column(name = "tratt_63_mm", precision=10, scale=2)
    private BigDecimal tratt63Mm;

    @Column(name = "tratt_31_v_5_mm", precision=10, scale=2)
    private BigDecimal tratt31V5Mm;

    @Column(name = "tratt_16_mm", precision=10, scale=2)
    private BigDecimal tratt16Mm;

    @Column(name = "tratt_8_mm", precision=10, scale=2)
    private BigDecimal tratt8Mm;

    @Column(name = "tratt_6_v_3_mm", precision=10, scale=2)
    private BigDecimal tratt6V3Mm;

    @Column(name = "tratt_4_mm", precision=10, scale=2)
    private BigDecimal tratt4Mm;

    @Column(name = "tratt_2_mm", precision=10, scale=2)
    private BigDecimal tratt2Mm;

    @Column(name = "tratt_1_mm", precision=10, scale=2)
    private BigDecimal tratt1Mm;

    @Column(name = "tratt_0_v_5_mm", precision=10, scale=2)
    private BigDecimal tratt0V5Mm;

    @Column(name = "tratt_0_v_25_mm", precision=10, scale=2)
    private BigDecimal tratt0V25Mm;

    @Column(name = "tratt_0_v_125_mm", precision=10, scale=2)
    private BigDecimal tratt0V125Mm;

    @Column(name = "tratt_0_v_075_mm", precision=10, scale=2)
    private BigDecimal tratt0V075Mm;

    @Column(name = "tratt_b_100_mm", precision=10, scale=2)
    private BigDecimal trattB100Mm;

    @Column(name = "tratt_b_6_v_3_mm", precision=10, scale=2)
    private BigDecimal trattB6V3Mm;

    @Column(name = "tratt_b_2_mm", precision=10, scale=2)
    private BigDecimal trattB2Mm;

    @Column(name = "tratt_b_0_v_5_mm", precision=10, scale=2)
    private BigDecimal trattB0V5Mm;

    /*
     * CBCBCBCBCBCBCBCBCB
     */
    @Column(name = "tratt_cb_63_mm", precision=10, scale=2)
    private BigDecimal trattCb63Mm;
    
    @Column(name = "tratt_cb_40_mm", precision=10, scale=2)
    private BigDecimal trattCb40Mm;
    
    @Column(name = "tratt_cb_31_v_5_mm", precision=10, scale=2)
    private BigDecimal trattCb31V5Mm;
    
    @Column(name = "tratt_cb_20_mm", precision=10, scale=2)
    private BigDecimal trattCb20Mm;
    
    @Column(name = "tratt_cb_16_mm", precision=10, scale=2)
    private BigDecimal trattCb16Mm;
    
    @Column(name = "tratt_cb_14_mm", precision=10, scale=2)
    private BigDecimal trattCb14Mm;
    
    @Column(name = "tratt_cb_12_v_5_mm", precision=10, scale=2)
    private BigDecimal trattCb12V5Mm;
    
    @Column(name = "tratt_cb_10_mm", precision=10, scale=2)
    private BigDecimal trattCb10Mm;
    
    @Column(name = "tratt_cb_8_mm", precision=10, scale=2)
    private BigDecimal trattCb8Mm;
    
    @Column(name = "tratt_cb_6_v_3_mm", precision=10, scale=2)
    private BigDecimal trattCb6V3Mm;
    
    @Column(name = "tratt_cb_4_mm", precision=10, scale=2)
    private BigDecimal trattCb4Mm;
    
    @Column(name = "tratt_cb_2_mm", precision=10, scale=2)
    private BigDecimal trattCb2Mm;
    
    @Column(name = "tratt_cb_1_mm", precision=10, scale=2)
    private BigDecimal trattCb1Mm;
    
    @Column(name = "tratt_cb_0_v_5_mm", precision=10, scale=2)
    private BigDecimal trattCb0V5Mm;
    
    @Column(name = "tratt_cb_0_v_25_mm", precision=10, scale=2)
    private BigDecimal trattCb0V25Mm;
    
    @Column(name = "tratt_cb_0_v_063_mm", precision=10, scale=2)
    private BigDecimal trattCb0V063Mm;
    
    
    
    
    @Column(name = "fondo", precision=10, scale=2)
    private BigDecimal fondo;

    @ManyToOne
    private Campione campione;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getBinder() {
		return binder;
	}

	public void setBinder(Boolean binder) {
		this.binder = binder;
	}

	public Integer getNumPesata() {
        return numPesata;
    }

    public void setNumPesata(Integer numPesata) {
        this.numPesata = numPesata;
    }

    public BigDecimal getPesoNetto() {
        return pesoNetto;
    }

    public void setPesoNetto(BigDecimal pesoNetto) {
        this.pesoNetto = pesoNetto;
    }

    public BigDecimal getTratt125Mm() {
        return tratt125Mm;
    }

    public void setTratt125Mm(BigDecimal tratt125Mm) {
        this.tratt125Mm = tratt125Mm;
    }

    public BigDecimal getTratt63Mm() {
        return tratt63Mm;
    }

    public void setTratt63Mm(BigDecimal tratt63Mm) {
        this.tratt63Mm = tratt63Mm;
    }

    public BigDecimal getTratt31V5Mm() {
        return tratt31V5Mm;
    }

    public void setTratt31V5Mm(BigDecimal tratt31V5Mm) {
        this.tratt31V5Mm = tratt31V5Mm;
    }

    public BigDecimal getTratt16Mm() {
        return tratt16Mm;
    }

    public void setTratt16Mm(BigDecimal tratt16Mm) {
        this.tratt16Mm = tratt16Mm;
    }

    public BigDecimal getTratt8Mm() {
        return tratt8Mm;
    }

    public void setTratt8Mm(BigDecimal tratt8Mm) {
        this.tratt8Mm = tratt8Mm;
    }

    public BigDecimal getTratt6V3Mm() {
        return tratt6V3Mm;
    }

    public void setTratt6V3Mm(BigDecimal tratt6V3Mm) {
        this.tratt6V3Mm = tratt6V3Mm;
    }

    public BigDecimal getTratt4Mm() {
        return tratt4Mm;
    }

    public void setTratt4Mm(BigDecimal tratt4Mm) {
        this.tratt4Mm = tratt4Mm;
    }

    public BigDecimal getTratt2Mm() {
        return tratt2Mm;
    }

    public void setTratt2Mm(BigDecimal tratt2Mm) {
        this.tratt2Mm = tratt2Mm;
    }

    public BigDecimal getTratt1Mm() {
        return tratt1Mm;
    }

    public void setTratt1Mm(BigDecimal tratt1Mm) {
        this.tratt1Mm = tratt1Mm;
    }

    public BigDecimal getTratt0V5Mm() {
        return tratt0V5Mm;
    }

    public void setTratt0V5Mm(BigDecimal tratt0V5Mm) {
        this.tratt0V5Mm = tratt0V5Mm;
    }

    public BigDecimal getTratt0V25Mm() {
        return tratt0V25Mm;
    }

    public void setTratt0V25Mm(BigDecimal tratt0V25Mm) {
        this.tratt0V25Mm = tratt0V25Mm;
    }

    public BigDecimal getTratt0V125Mm() {
        return tratt0V125Mm;
    }

    public void setTratt0V125Mm(BigDecimal tratt0V125Mm) {
        this.tratt0V125Mm = tratt0V125Mm;
    }

    public BigDecimal getTratt0V075Mm() {
        return tratt0V075Mm;
    }

    public void setTratt0V075Mm(BigDecimal tratt0V075Mm) {
        this.tratt0V075Mm = tratt0V075Mm;
    }

    public BigDecimal getTrattB100Mm() {
        return trattB100Mm;
    }

    public void setTrattB100Mm(BigDecimal trattB100Mm) {
        this.trattB100Mm = trattB100Mm;
    }

    public BigDecimal getTrattB6V3Mm() {
        return trattB6V3Mm;
    }

    public void setTrattB6V3Mm(BigDecimal trattB6V3Mm) {
        this.trattB6V3Mm = trattB6V3Mm;
    }

    public BigDecimal getTrattB2Mm() {
        return trattB2Mm;
    }

    public void setTrattB2Mm(BigDecimal trattB2Mm) {
        this.trattB2Mm = trattB2Mm;
    }

    public BigDecimal getTrattB0V5Mm() {
        return trattB0V5Mm;
    }

    public void setTrattB0V5Mm(BigDecimal trattB0V5Mm) {
        this.trattB0V5Mm = trattB0V5Mm;
    }

    public BigDecimal getTrattCb63Mm() {
		return trattCb63Mm;
	}

	public void setTrattCb63Mm(BigDecimal trattCb63Mm) {
		this.trattCb63Mm = trattCb63Mm;
	}

	public BigDecimal getTrattCb40Mm() {
		return trattCb40Mm;
	}

	public void setTrattCb40Mm(BigDecimal trattCb40Mm) {
		this.trattCb40Mm = trattCb40Mm;
	}

	public BigDecimal getTrattCb31V5Mm() {
		return trattCb31V5Mm;
	}

	public void setTrattCb31V5Mm(BigDecimal trattCb31V5Mm) {
		this.trattCb31V5Mm = trattCb31V5Mm;
	}

	public BigDecimal getTrattCb20Mm() {
		return trattCb20Mm;
	}

	public void setTrattCb20Mm(BigDecimal trattCb20Mm) {
		this.trattCb20Mm = trattCb20Mm;
	}

	public BigDecimal getTrattCb16Mm() {
		return trattCb16Mm;
	}

	public void setTrattCb16Mm(BigDecimal trattCb16Mm) {
		this.trattCb16Mm = trattCb16Mm;
	}

	public BigDecimal getTrattCb14Mm() {
		return trattCb14Mm;
	}

	public void setTrattCb14Mm(BigDecimal trattCb14Mm) {
		this.trattCb14Mm = trattCb14Mm;
	}

	public BigDecimal getTrattCb12V5Mm() {
		return trattCb12V5Mm;
	}

	public void setTrattCb12V5Mm(BigDecimal trattCb12V5Mm) {
		this.trattCb12V5Mm = trattCb12V5Mm;
	}

	public BigDecimal getTrattCb10Mm() {
		return trattCb10Mm;
	}

	public void setTrattCb10Mm(BigDecimal trattCb10Mm) {
		this.trattCb10Mm = trattCb10Mm;
	}

	public BigDecimal getTrattCb8Mm() {
		return trattCb8Mm;
	}

	public void setTrattCb8Mm(BigDecimal trattCb8Mm) {
		this.trattCb8Mm = trattCb8Mm;
	}

	public BigDecimal getTrattCb6V3Mm() {
		return trattCb6V3Mm;
	}

	public void setTrattCb6V3Mm(BigDecimal trattCb6V3Mm) {
		this.trattCb6V3Mm = trattCb6V3Mm;
	}

	public BigDecimal getTrattCb4Mm() {
		return trattCb4Mm;
	}

	public void setTrattCb4Mm(BigDecimal trattCb4Mm) {
		this.trattCb4Mm = trattCb4Mm;
	}

	public BigDecimal getTrattCb2Mm() {
		return trattCb2Mm;
	}

	public void setTrattCb2Mm(BigDecimal trattCb2Mm) {
		this.trattCb2Mm = trattCb2Mm;
	}

	public BigDecimal getTrattCb1Mm() {
		return trattCb1Mm;
	}

	public void setTrattCb1Mm(BigDecimal trattCb1Mm) {
		this.trattCb1Mm = trattCb1Mm;
	}

	public BigDecimal getTrattCb0V5Mm() {
		return trattCb0V5Mm;
	}

	public void setTrattCb0V5Mm(BigDecimal trattCb0V5Mm) {
		this.trattCb0V5Mm = trattCb0V5Mm;
	}

	public BigDecimal getTrattCb0V25Mm() {
		return trattCb0V25Mm;
	}

	public void setTrattCb0V25Mm(BigDecimal trattCb0V25Mm) {
		this.trattCb0V25Mm = trattCb0V25Mm;
	}

	public BigDecimal getTrattCb0V063Mm() {
		return trattCb0V063Mm;
	}

	public void setTrattCb0V063Mm(BigDecimal trattCb0V063Mm) {
		this.trattCb0V063Mm = trattCb0V063Mm;
	}

	public BigDecimal getFondo() {
        return fondo;
    }

    public void setFondo(BigDecimal fondo) {
        this.fondo = fondo;
    }

    public Campione getCampione() {
        return campione;
    }

    public void setCampione(Campione campione) {
        this.campione = campione;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Pesata pesata = (Pesata) o;
        if(pesata.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, pesata.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Pesata{" +
            "id=" + id +
            ", numPesata='" + numPesata + "'" +
            ", pesoNetto='" + pesoNetto + "'" +
            ", tratt125Mm='" + tratt125Mm + "'" +
            ", tratt63Mm='" + tratt63Mm + "'" +
            ", tratt31V5Mm='" + tratt31V5Mm + "'" +
            ", tratt16Mm='" + tratt16Mm + "'" +
            ", tratt8Mm='" + tratt8Mm + "'" +
            ", tratt6V3Mm='" + tratt6V3Mm + "'" +
            ", tratt4Mm='" + tratt4Mm + "'" +
            ", tratt2Mm='" + tratt2Mm + "'" +
            ", tratt1Mm='" + tratt1Mm + "'" +
            ", tratt0V5Mm='" + tratt0V5Mm + "'" +
            ", tratt0V25Mm='" + tratt0V25Mm + "'" +
            ", tratt0V125Mm='" + tratt0V125Mm + "'" +
            ", tratt0V075Mm='" + tratt0V075Mm + "'" +
            ", trattB100Mm='" + trattB100Mm + "'" +
            ", trattB6V3Mm='" + trattB6V3Mm + "'" +
            ", trattB2Mm='" + trattB2Mm + "'" +
            ", trattB0V5Mm='" + trattB0V5Mm + "'" +
            ", fondo='" + fondo + "'" +
            '}';
    }
}
