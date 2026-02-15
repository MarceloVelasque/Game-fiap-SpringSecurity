package br.com.gameStore.gameStore.dto;

public record GameRequest(
        String nome,
        String titulo,
        String descricao,
        Double preco) {
}
