package br.com.ljbm.repositorio;

import br.com.ljbm.modelo.FundoInvestimento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundoInvestimentoRepo extends JpaRepository<FundoInvestimento, Long>
{
}
