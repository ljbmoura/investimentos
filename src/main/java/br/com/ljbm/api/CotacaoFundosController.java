package br.com.ljbm.api;

import java.io.IOException;

import br.com.ljbm.servico.CotacaoFundosIntegracao;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cotacao-fundos")
@RequiredArgsConstructor
public class CotacaoFundosController {

	private final CotacaoFundosIntegracao servico;

    @GetMapping("atualizar")
    public void obtemCotacoesAtuais() throws IOException {
    	servico.obtemCotacaoFundosBB();
    	servico.obtemCotacaoFundosTesouroDireto();
    }

}