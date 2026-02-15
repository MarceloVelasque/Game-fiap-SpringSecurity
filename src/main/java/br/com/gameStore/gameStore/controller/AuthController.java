package br.com.gameStore.gameStore.controller;

import br.com.gameStore.gameStore.config.security.TokenService;
import br.com.gameStore.gameStore.dto.TokenDTO;
import br.com.gameStore.gameStore.dto.UserDTO;
import br.com.gameStore.gameStore.model.Usuario;
import br.com.gameStore.gameStore.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.StreamingHttpOutputMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;
    @Autowired
    private UsuarioRepository usuarioRepository;


    // O método login é responsável por autenticar o usuário e gerar um token JWT.
    //  Ele recebe um objeto UserDTO contendo o email e a senha do usuário,
    //  cria um objeto UsernamePasswordAuthenticationToken com essas informações,
    //  e usa o AuthenticationManager para autenticar o usuário. Se a autenticação for bem-sucedida,
    //  ele gera um token JWT usando o TokenService e retorna esse token em um objeto TokenDTO.

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody UserDTO userDTO) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDTO.email(), userDTO.password());
        Authentication authentication = authenticationManager.authenticate(auth);

        String token = tokenService.generationToken((Usuario) Objects.requireNonNull(authentication.getPrincipal()));

        return  ResponseEntity.ok(new TokenDTO(token));
    }

    // O método register é responsável por registrar um novo usuário no sistema.
    // Ele recebe um objeto Usuario contendo as informações do usuário a ser registrado,
    //  criptografa a senha usando BCryptPasswordEncoder, salva o usuário no banco de dados usando o UsuarioRepository,
    //  e retorna o usuário recém-criado em uma resposta HTTP com status 201 Created
    // O método register é chamado quando um novo usuário deseja se registrar no sistema, fornecendo suas informações, como email e senha.

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody Usuario user) {

        String cryptPass  = new BCryptPasswordEncoder().encode(user.getPassword());
        user.setSenha(cryptPass);
        Usuario newUser = usuarioRepository.save(user);
        return ResponseEntity.created(null).body(newUser);
    }



}
