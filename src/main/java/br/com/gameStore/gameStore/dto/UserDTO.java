package br.com.gameStore.gameStore.dto;

// DTO (Data Transfer Object) para representar os dados de um usuário em uma requisição de login.
// Ele contém os campos necessários para autenticar um usuário, como email e senha.
// O UserDTO é utilizado como entrada na API de autenticação, permitindo que o cliente
// forneça as credenciais necessárias para realizar o login e obter um token de autenticação válido para acessar os recursos protegidos do sistema.
public record UserDTO(String email, String password) {


}
