package br.com.ljbm.fp.api;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.ljbm.fp.servico.RentabilidadeFundosInvestimento;

@RestController
@RequestMapping("/rentabilidade")
public class CotacaoFundosController {

	private final RentabilidadeFundosInvestimento servico;

    public CotacaoFundosController (@Autowired RentabilidadeFundosInvestimento servico) {
        this.servico = servico;
    }

//    @PostMapping("/")
//    public void obtemRentabilidade() {
//       this.servico.tabelaRentabilidade();
//    }
    
    @GetMapping("/")
    public void obtemCotacoesAtuais() throws IOException {
    	servico.obtemCotacaoFundosBB();
    	servico.obtemCotacaoTesouroDireto();
    }

}