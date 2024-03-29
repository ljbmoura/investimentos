package br.com.ljbm.modelo;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Aplicação entity
 * 
 * @author ljbm
 * @since 27/03/2012
 *        um evento de aplicação financeira. Ex:
 *        -------------------------------------------------------------------
 *        Data Documento Valor aplicado Quantidade Cotas Saldo Cotas
 *        -------------------------------------------------------------------
 *        05/07/2006 105173037 1.867,00 309,394394 309,394394
 * 
 */

@Data
@AllArgsConstructor
@Entity
@Table(name = "Aplicacao")
//@Cacheable
public class Aplicacao implements Serializable {

	@Serial
	private static final long serialVersionUID = -2999371111194178089L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="ide")
	private Long ide;

	@ManyToOne(optional = false, fetch=FetchType.LAZY)
	@JoinColumn(name = "fundoInvestimento_ide", nullable = false)
	private FundoInvestimento fundoInvestimento;
//	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
	@JsonDeserialize(using = LocalDateDeserializer.class)
	@JsonSerialize(using = LocalDateSerializer.class)
	@Column(name = "data", nullable = false)
	@NotNull
	private LocalDate dataCompra;

	@Column(name = "documento")
	private Long documento;

	@Column(name = "valorAplicado", nullable = false, precision = 19, scale = 2)
	@NotNull
	private BigDecimal valorAplicado;

	@Column(name = "quantidadeCotas", nullable = false, precision = 19, scale = 6)
	@NotNull
	private BigDecimal quantidadeCotas;

	@Column(name = "saldoCotas", nullable = false, precision = 19, scale = 6)
	@NotNull
	private BigDecimal saldoCotas;

	@Transient
	public BigDecimal getValorAplicadoRemanescente() {
		return saldoCotas.multiply(getValorCotaAplicacao());
	}

	@Transient
	public BigDecimal getValorCotaAplicacao() {
		return valorAplicado.divide(quantidadeCotas, 9, RoundingMode.DOWN);
	}

//	public Aplicacao(LocalDate data, Long documento, BigDecimal valorAplicado,
//					 BigDecimal quantidadeCotas, BigDecimal saldoCotas) {
//		this.dataCompra = data;
//		this.documento = documento;
//		this.valorAplicado = valorAplicado;
//		this.quantidadeCotas = quantidadeCotas;
//		this.saldoCotas = saldoCotas;
//	}

}
