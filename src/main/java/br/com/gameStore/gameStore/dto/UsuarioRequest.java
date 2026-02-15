package br.com.gameStore.gameStore.dto;

import br.com.gameStore.gameStore.mode.enums.Papel;

public record UsuarioRequest(String nome,
                             String email,
                             String senha) {
}
