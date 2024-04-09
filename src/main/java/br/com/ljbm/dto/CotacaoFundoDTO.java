package br.com.ljbm.dto;

import br.com.ljbm.utilitarios.JSONSerdeCompatible;

import java.math.BigDecimal;
import java.time.LocalDate;
public record CotacaoFundoDTO (String nomeFundo, LocalDate dataCotacao, BigDecimal valorCota)  implements JSONSerdeCompatible {
}
