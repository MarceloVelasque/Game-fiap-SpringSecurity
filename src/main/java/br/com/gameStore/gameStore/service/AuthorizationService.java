package br.com.gameStore.gameStore.service;

import br.com.gameStore.gameStore.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


// Classe de serviço para autenticação e autorização de usuários
// Implementa a interface UserDetailsService do Spring Security para carregar os detalhes do usuário
// O método loadUserByUsername é responsável por buscar um usuário pelo email (username) e retornar um objeto UserDetails
// O método utiliza o repositório UsuarioRepository para acessar os dados do usuário no banco de dados
// Se o usuário não for encontrado, uma exceção UsernameNotFoundException é lançada


@Service
public class AuthorizationService implements UserDetailsService {

    @Autowired
    UsuarioRepository usuarioRepository;

    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(username);
    }
}
