package br.com.ljbm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CotacaoFundoDTO(String nomeFundo, LocalDate dataCotacao, BigDecimal valorCota) {

}
