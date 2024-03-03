package br.com.ljbm.api;

import java.io.IOException;

import br.com.ljbm.servico.CotacaoFundosIntegracao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rentabilidade")
public class CotacaoFundosController {

	private final CotacaoFundosIntegracao servico;

    public CotacaoFundosController (@Autowired CotacaoFundosIntegracao servico) {
        this.servico = servico;
    }

//    @PostMapping("/")
//    public void obtemRentabilidade() {
//       this.servico.tabelaRentabilidade();
//    }
    
    @GetMapping("/")
    public void obtemCotacoesAtuais() throws IOException {
    	servico.obtemCotacaoFundosBB();
    	servico.obtemCotacaoFundosTesouroDireto();
    }

}