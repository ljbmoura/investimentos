package br.com.ljbm.fp.modelo;

import java.util.List;


import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "Corretora")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Corretora implements java.io.Serializable {

	private static final long serialVersionUID = 5872063361515729800L;
	
	public static String cnpjAgora = "74014747000135";
	public static String cnpjBB = "00000000000191";

	private Long ide;

	private String cnpj;

	private String razaoSocial;

	private String sigla;

	private Integer versao;
	
	@JsonInclude(Include.NON_NULL)
	private List<FundoInvestimento> fundosSobCustodia;

	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ide")
	public Long getIde() {
		return ide;
	}

	public void setIde(Long ide) {
		this.ide = ide;
	}

	@Column(name = "cnpj", nullable = false, unique = true, columnDefinition = "char(14)")
	public String getCnpj() {
		return cnpj;
	}

	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}

	@Column(name = "razaoSocial", nullable = false, length = 70, columnDefinition = "varchar(70)")
	public String getRazaoSocial() {
		return razaoSocial;
	}

	public void setRazaoSocial(String razaoSocial) {
		this.razaoSocial = razaoSocial;
	}

	@Column(name = "sigla", nullable = false, columnDefinition = "char(10)")
	public String getSigla() {
		return sigla;
	}

	public void setSigla(String sigla) {
		this.sigla = sigla;
	}

	@Version
	@Column(name = "versao", nullable = false)
	public Integer getVersao() {
		return versao;
	}

	public void setVersao(Integer version) {
		this.versao = version;
	}
	
	@OneToMany(mappedBy="corretora", fetch=FetchType.LAZY)
//	@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public List<FundoInvestimento> getFundosSobCustodia() {
		return fundosSobCustodia;
	}
	
	public void setFundosSobCustodia(List<FundoInvestimento> fundosSobCustodia) {
		this.fundosSobCustodia = fundosSobCustodia;
	}

	@Override
	public String toString() {
		return "Corretora [ide=" + ide + ", cnpj=" + cnpj + ", razaoSocial=" + razaoSocial + ", sigla=" + sigla
				+ ", versao=" + versao + "]";
	}
}
