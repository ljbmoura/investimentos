package br.com.ljbm.fp.modelo;

public enum TipoFundoInvestimento {

	TesouroDireto("TesouroDireto", 1L), RendaFixa("RendaFixa", 2L), Acoes("Acoes", 3L), CDB("CDB", 4L);

	private String nome;
	private Long id;

	TipoFundoInvestimento(String nome, Long id) {
		this.nome = nome;
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public Long getId() {
		return id;
	}
}
