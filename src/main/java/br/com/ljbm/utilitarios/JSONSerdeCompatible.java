package br.com.ljbm.utilitarios;

import br.com.ljbm.dto.CotacaoFundoDTO;
import br.com.ljbm.dto.PeriodoRemuneracaoSELICDTO;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * An interface for registering types that can be de/serialized with {@link JSONSerde}.
 */
@SuppressWarnings("DefaultAnnotationParam") // being explicit for the example
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_t")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CotacaoFundoDTO.class, name = "pv"),
        @JsonSubTypes.Type(value = PeriodoRemuneracaoSELICDTO.class, name = "rc")
})
public interface JSONSerdeCompatible {

}