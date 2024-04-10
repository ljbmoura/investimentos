package br.com.ljbm.modelo;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "seriecoeficienteselic")
@Cacheable
public class SerieCoeficienteSELIC implements java.io.Serializable {

	@Serial
	private static final long serialVersionUID = 6143557619299084513L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="ide", nullable = false)
	private Long ide;

	@Column(name = "datainicio", nullable = false)
	@NotNull
	//	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate dataInicio;

	@Column(name = "datafim", nullable = false)
	@NotNull
	//	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate dataFim;

	@Column(name = "fator", nullable = false, precision = 19, scale = 6)
	@NotNull
	private BigDecimal fator;

	public SerieCoeficienteSELIC(LocalDate dataInicio, LocalDate dataFim, BigDecimal fator) {
		this.dataInicio = dataInicio;
		this.dataFim = dataFim;
		this.fator = fator;
	}

}
