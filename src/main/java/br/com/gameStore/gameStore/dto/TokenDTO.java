package br.com.gameStore.gameStore.dto;
// DTO para representar o token de autenticação retornado após o login bem-sucedido.
// Ele contém o token JWT que será usado para autenticar as requisições subsequentes do cliente.
// O TokenDTO é utilizado como resposta na API de autenticação, permitindo que o cliente receba o token JWT
// necessário para acessar os recursos protegidos do sistema.
public record TokenDTO(
        String token) {
}
