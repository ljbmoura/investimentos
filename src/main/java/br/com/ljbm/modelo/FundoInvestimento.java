package br.com.ljbm.modelo;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode
@ToString (of = {"ide", "nome", "corretora"})
@Getter
@Setter
@Entity
@Table(name = "FundoInvestimento")
@Cacheable
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class FundoInvestimento implements Serializable {

    @Serial
    private static final long serialVersionUID = 3905125413892087441L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ide", nullable = false)
    private Long ide;

    @Column(name = "cnpj", length = 14)
    @NotEmpty
    private String cnpj;

    @Column(name = "nome", nullable = false, length = 90)
    @NotEmpty
    @Size(min = 5, max = 90)
    private String nome;

	@Column(name = "tipoFundoInvestimento", nullable = false)
	@Enumerated(EnumType.ORDINAL)
    @NotNull
	private TipoFundoInvestimento tipoFundoInvestimento;

	@ManyToOne(optional = false, fetch=FetchType.LAZY)
    @JoinColumn(name = "corretora_ide", nullable = false)
    @NotNull
    private Corretora corretora;

    @OneToMany(mappedBy = "fundoInvestimento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Aplicacao> aplicacoes;

}
