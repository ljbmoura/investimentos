package br.com.ljbm.modelo;

import lombok.Getter;

@Getter
public enum TipoFundoInvestimento {

	TesouroDireto("TesouroDireto", 1L),
	RendaFixa("RendaFixa", 2L),
	Acoes("Acoes", 3L),
	CDB("CDB", 4L);

	private final String nome;
	private final Long id;

	TipoFundoInvestimento(String nome, Long id) {
		this.nome = nome;
		this.id = id;
	}

}
