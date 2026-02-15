package br.com.gameStore.gameStore.repository;

import br.com.gameStore.gameStore.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
// Interface de repositório para a entidade Usuario, estendendo JpaRepository para fornecer operações CRUD básicas
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    UserDetails findByEmail(String email);



}
