package br.com.ljbm.dto;

import br.com.ljbm.utilitarios.JSONSerdeCompatible;

import java.time.LocalDate;
public record PeriodoRemuneracaoSELICDTO (LocalDate inicio, LocalDate fim) implements JSONSerdeCompatible {
}
