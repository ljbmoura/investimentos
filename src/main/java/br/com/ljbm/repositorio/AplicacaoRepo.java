package br.com.ljbm.repositorio;

import br.com.ljbm.modelo.Aplicacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AplicacaoRepo extends JpaRepository<Aplicacao, Long> { }
