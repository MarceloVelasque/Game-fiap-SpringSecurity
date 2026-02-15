package br.com.gameStore.gameStore.config.security;

import br.com.gameStore.gameStore.dto.UserDTO;
import br.com.gameStore.gameStore.model.Usuario;
import br.com.gameStore.gameStore.repository.UsuarioRepository;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {
    private final UsuarioRepository usuarioRepository;


    //esse método é o construtor da classe TokenService, que recebe um objeto do tipo UsuarioRepository como parâmetro e o atribui a um campo privado da classe.
    // O UsuarioRepository é uma interface que estende JpaRepository e é usada para acessar os dados dos usuários no banco de dados.
    // Ao injetar o UsuarioRepository no construtor, a classe TokenService pode usar seus métodos para realizar operações relacionadas aos usuários,
    // como salvar um novo usuário ou buscar um usuário existente.
    public TokenService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }


    //esse método é responsável por gerar um token JWT para um usuário autenticado.
    // Ele recebe um objeto do tipo Usuario como parâmetro, que representa o usuário para o qual o token será gerado.
    // O método utiliza a biblioteca Auth0 JWT para criar o token, definindo o algoritmo de assinatura (HMAC256) e as informações do token,
    //  como o emissor (issuer), o assunto (subject) e a data de expiração.
    // O token é assinado com a chave secreta "fiap" e retornado como uma string.
    // O token gerado pode ser usado para autenticar o usuário em requisições subsequentes, permitindo o acesso a recursos protegidos do sistema.
    // O método generationToken é chamado durante o processo de login,
    //  onde as credenciais do usuário são verificadas e, se forem válidas, um token é gerado para o usuário autenticado.
    // O token gerado contém informações sobre o usuário e pode ser usado para autenticação em requisições futuras, permitindo o acesso a recursos protegidos do sistema.
    public String generationToken(Usuario user) {
        Algorithm algorithm = Algorithm.HMAC256("fiap");

        String token = JWT.create().withIssuer("game")
                .withSubject(user.getEmail())
                .withExpiresAt(expirationTime())
                .sign(algorithm);
        return token;


    }
    //esse método é responsável por validar um token JWT recebido em uma requisição.
    // Ele recebe o token como parâmetro e utiliza a biblioteca Auth0 JWT para verificar sua validade.
    // O método define o mesmo algoritmo de assinatura (HMAC256) e as mesmas informações do token (emissor e assunto) que foram usadas para gerar o token.
    // Se o token for válido, o método retorna o assunto (subject) do token, que é o email do usuário.
    //  Caso contrário, se o token for inválido ou expirado, o método captura a exceção JWTVerificationException e retorna uma string vazia.
    // O método validateToken é chamado durante o processo de autenticação de requisições protegidas,
    //  onde o token recebido na requisição é validado para garantir que o usuário esteja autenticado e autorizado a acessar os recursos solicitados.
    public static String validateToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256("fiap");
        try {
            return JWT.require(algorithm)
                    .withIssuer("game")
                    .build()
                    .verify(token)
                    .getSubject();

        } catch (JWTVerificationException e) {
            return "";
        }
    }

    //esse metodo define a data de expiração do token, que é 15 minutos a partir do momento em que o token é gerado
    // a data de expiração é calculada usando a classe LocalDateTime, adicionando 15 minutos ao horário atual, e convertendo para Instant com o fuso horário de -03:00
    // o token expira após 15 minutos, garantindo que os tokens sejam válidos apenas por um período limitado, aumentando a segurança do sistema
    public Instant expirationTime() {
        return LocalDateTime.now().plusMinutes(15).toInstant(ZoneOffset.of("-03:00"));
    }

    @PostMapping("/register")
    public ResponseEntity register(Usuario user) {
        String cryptoPass= new BCryptPasswordEncoder().encode(user.getPassword());
        user.setSenha(cryptoPass);
        Usuario newUser = usuarioRepository.save(user);
        return ResponseEntity.created(null).body(newUser);


    }
}
