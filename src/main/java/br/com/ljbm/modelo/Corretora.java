package br.com.ljbm.modelo;

import java.io.Serial;
import java.io.Serializable;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

//@ToString(of = {"ide", "sigla"})
@Data
@NoArgsConstructor
@Entity
@Table(name = "Corretora")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Corretora implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;
	
	public static String cnpjAgora = "74014747000135";
	public static String cnpjBB = "00000000000191";

	@Id
	@Column(name = "ide", nullable = false, updatable = false, unique = true)
	private Integer ide;

	@Column(name = "cnpj", nullable = false, unique = true, length = 14, columnDefinition = "char(14)")
	private String cnpj;

	@Column(name = "razaoSocial", nullable = false, length = 70, columnDefinition = "varchar(70)")
	private String razaoSocial;

	@Column(name = "sigla", nullable = false, unique = true)
	private String sigla;

	@Version
	@Column(name = "versao", nullable = false)
	private Integer versao;
	
//	@JsonInclude(Include.NON_NULL)
//	@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
//	@OneToMany(mappedBy="corretora", fetch=FetchType.LAZY)
//	private List<FundoInvestimento> fundosInvestimento;

}
