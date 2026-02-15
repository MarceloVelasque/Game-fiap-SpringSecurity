package br.com.gameStore.gameStore.config.security;

import br.com.gameStore.gameStore.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class VerificarToken  extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;
    @Autowired
    private UsuarioRepository usuarioRepository;




    //esse método é chamado para cada requisição, ele verifica se o token é válido e, se for, autentica o usuário no contexto de segurança do Spring Security.
    // Ele extrai o token do cabeçalho Authorization, valida o token usando o TokenService, e se o token for válido, ele busca os detalhes
    // do usuário no banco de dados usando o UsuarioRepository.
    // Em seguida, ele cria um objeto UsernamePasswordAuthenticationToken com os detalhes do usuário e o define no contexto de segurança do Spring Security,
    // permitindo que o usuário seja autenticado para a requisição atual.

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");
        String token = "";
        if(authorizationHeader == null){
            token = null;
        } else {
            token = authorizationHeader.replace("Bearer ", "").trim();
            String login =  TokenService.validateToken(token);
            UserDetails usuario = usuarioRepository.findByEmail(login);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

        }
        filterChain.doFilter(request, response);
    }
}
