package br.com.ljbm.modelo;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode
@ToString (of = {"ide", "dataCotacao", "valorCota", "ideFundoInvestimento"})
@Getter
@Setter
@Entity
@Table(name = "CotacaoFundo")
public class CotacaoFundo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "cotacao_fundo_seq")
    @SequenceGenerator(name = "cotacao_fundo_seq", sequenceName = "CotacaoFundo_ide", allocationSize = 1, schema = "dbo")
    @Column(name="ide")
    private Long ide;

    @Column(name = "dataCotacao", nullable = false)
    @NotNull
    private LocalDate dataCotacao;

    @Column(name = "valorCota", nullable = false, precision = 16, scale = 9)
    @NotNull
    private BigDecimal valorCota;

	@ManyToOne(optional = false, fetch=FetchType.LAZY)
    @JoinColumn(name="fundoInvestimento_ide",  nullable = false)
    private FundoInvestimento fundoInvestimento;

    @Column(name="fundoInvestimento_ide", insertable=false, updatable=false)
    private Long ideFundoInvestimento;

    public CotacaoFundo(LocalDate dataCotacao, BigDecimal valorCota, FundoInvestimento fundoInvestimento) {
        this.dataCotacao = dataCotacao;
        this.valorCota = valorCota;
        this.fundoInvestimento = fundoInvestimento;
    }

    public CotacaoFundo() {
        // org.hibernate.InstantiationException: No default constructor for entity 'br.com.ljbm.modelo.CotacaoFundo'
    }
}
