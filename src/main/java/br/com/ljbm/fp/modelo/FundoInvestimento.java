package br.com.ljbm.fp.modelo;

import java.io.Serial;
import java.io.Serializable;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@Entity
@Table(name = "FundoInvestimento")
//, uniqueConstraints = @UniqueConstraint(columnNames = "nome"))
//@Cacheable
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class FundoInvestimento implements Serializable {

    @Serial
    private static final long serialVersionUID = 3905125413892087441L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ide", nullable = false)
    private Long ide;

    @Column(name = "cnpj", length = 14)
    private String cnpj;

    @Column(name = "nome", nullable = false, length = 90)
    @NotNull
    @Size(min = 5, max = 90)
    private String nome;

	@Column(name = "tipoFundoInvestimento", nullable = true)
	@Enumerated(EnumType.ORDINAL)
	private TipoFundoInvestimento tipoFundoInvestimento;

	@ManyToOne(optional = false, fetch=FetchType.LAZY)
	private Corretora corretora;

}
