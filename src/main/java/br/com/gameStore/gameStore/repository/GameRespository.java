package br.com.gameStore.gameStore.repository;

import br.com.gameStore.gameStore.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

// Interface de repositório para a entidade Game, estendendo JpaRepository para fornecer operações CRUD básicas
public interface GameRespository extends JpaRepository<Game, Long> {
}
