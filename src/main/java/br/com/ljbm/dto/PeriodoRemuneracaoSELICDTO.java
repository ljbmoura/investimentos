package br.com.ljbm.dto;

import br.com.ljbm.utilitarios.JSONSerdeCompatible;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PeriodoRemuneracaoSELICDTO implements JSONSerdeCompatible {
    public String inicio;
    public String fim;
}
