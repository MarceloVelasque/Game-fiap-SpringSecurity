package br.com.gameStore.gameStore.dto;

public record GameResponse(
        Long id,
        String titulo,
        String descricao,
        Double preco) {
}
