package br.com.gameStore.gameStore.dto;

import br.com.gameStore.gameStore.mode.enums.Papel;

public record UsuarioResponse(Long id,
                              String nome,
                              String email,
                              Papel papel) {
}
