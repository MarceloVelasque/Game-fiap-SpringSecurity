package br.com.gameStore.gameStore.service;

import br.com.gameStore.gameStore.model.Game;
import br.com.gameStore.gameStore.repository.GameRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


// Classe de serviço para a entidade Game, responsável por implementar a lógica de negócios relacionada aos jogos
// O GameService utiliza o GameRespository para acessar os dados dos jogos no banco de dados
@Service
public class GameService {

    @Autowired
    private GameRespository gameRespository;

    public Game save(Game game){
        return gameRespository.save(game);
    }


    public List<Game> findAll(){
        return gameRespository.findAll();
    }


    public Optional<Game> findById(Long id) {
            return null;
    }
}
