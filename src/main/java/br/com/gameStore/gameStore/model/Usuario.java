package br.com.gameStore.gameStore.model;

import br.com.gameStore.gameStore.mode.enums.Papel;
import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
// Classe de entidade que representa um usuário do sistema, implementando a interface UserDetails para integração com o Spring Security.
// A classe possui atributos como id, nome, email, senha e papel (função do usuário), além de métodos para acessar e modificar esses atributos.
// o userDetails é uma interface do Spring Security que fornece informações de autenticação e autorização para um usuário.
// Ela define métodos para obter as autoridades (permissões) do usuário,
// a senha, o nome de usuário e o status da conta (se está expirada, bloqueada, etc.). Ao implementar essa interface,
// a classe Usuario pode ser usada diretamente pelo Spring Security para autenticar e autorizar usuários no sistema.

@Entity
@Table(name = "tb_usuario")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(unique = true)
    private String email;

    @Column(length = 255)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Papel papel;

    public Usuario() {
    }

    public Usuario( String nome, String email, String senha, Papel papel) {

        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.papel = papel;
    }

    public Usuario(String nome, Long id, String email, String senha, Papel papel) {
        this.nome = nome;
        this.id = id;
        this.email = email;
        this.senha = senha;
        this.papel = papel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Papel getPapel() {
        return papel;
    }

    public void setPapel(Papel papel) {
        this.papel = papel;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id) && Objects.equals(nome, usuario.nome) && Objects.equals(email, usuario.email) && Objects.equals(senha, usuario.senha) && papel == usuario.papel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, email, senha, papel);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", senha='" + senha + '\'' +
                ", papel=" + papel +
                '}';
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(papel == Papel.ADMIN) {
            return List.of( new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USUARIO"));
        }

        return List.of( new SimpleGrantedAuthority("ROLE_USUARIO"));
    }

    @Override
    public @Nullable String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
