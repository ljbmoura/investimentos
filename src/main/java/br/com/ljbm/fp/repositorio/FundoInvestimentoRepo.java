package br.com.ljbm.fp.repositorio;

import br.com.ljbm.fp.modelo.FundoInvestimento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundoInvestimentoRepo extends JpaRepository<FundoInvestimento, Long>
{
}
