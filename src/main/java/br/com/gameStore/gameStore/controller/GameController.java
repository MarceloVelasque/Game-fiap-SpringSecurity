package br.com.gameStore.gameStore.controller;

import br.com.gameStore.gameStore.model.Game;
import br.com.gameStore.gameStore.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/games")
public class GameController {

    @Autowired
    private GameService gameService;


    // GET /games - Retorna uma lista de todos os jogos
    @GetMapping
    public ResponseEntity<List<Game>> getGames() {
        List<Game> games = gameService.findAll();
        return ResponseEntity.ok().body(games);
    }

    // POST /games - Cria um novo jogo
    @PostMapping
    public ResponseEntity<Game> save(@RequestBody Game game) {
      Game newGame = gameService.save(game);
        return ResponseEntity.created(null).body(newGame);
    }

    // GET /games/{id} - Retorna um jogo específico pelo ID
    public ResponseEntity<Game> getById(@PathVariable Long id) {
        Optional<Game> game = gameService.findById(id);
        if (game.isPresent()) {
            return ResponseEntity.ok(game.get());
        }
        return ResponseEntity.notFound().build();
    }


}
