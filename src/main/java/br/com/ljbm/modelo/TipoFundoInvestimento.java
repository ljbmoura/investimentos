package br.com.ljbm.modelo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TipoFundoInvestimento {

	TesouroDireto("TesouroDireto", 1L),
	RendaFixa("RendaFixa", 2L),
	Acoes("Acoes", 3L),
	CDB("CDB", 4L);

	private final String nome;
	private final Long id;

}
