package br.com.gameStore.gameStore.config.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private VerificarToken verificarToken;

    // o método securityFilterChain é responsável por configurar as regras de segurança para as requisições HTTP.
    //  Ele desabilita o CSRF, define a política de criação de sessão como stateless, e configura as regras de autorização para diferentes endpoints.
    //  Ele também adiciona o filtro de verificação de token antes do filtro de autenticação padrão do Spring Security.

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                    .requestMatchers(HttpMethod.GET, "/games").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/games/*").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/games").hasRole("ADMIN")
                .anyRequest().authenticated())
                .addFilterBefore(verificarToken, UsernamePasswordAuthenticationFilter.class)
                .build();

    }






//esse método é responsável por fornecer o AuthenticationManager, que é um componente central do Spring Security para autenticação de usuários.
// Ele é necessário para o processo de login, onde as credenciais do usuário são verificadas.
// O método recebe uma instância de AuthenticationConfiguration, que é usada para obter o AuthenticationManager configurado no contexto de segurança do Spring.
//  O método lança uma exceção caso haja algum problema ao obter o AuthenticationManager.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

// esse método é responsável por fornecer um bean do tipo PasswordEncoder, que é usado para criptografar as senhas dos usuários antes de armazená-las
//  no banco de dados.
// Ele retorna uma instância de BCryptPasswordEncoder, que é uma implementação do PasswordEncoder que utiliza o algoritmo
// de hash bcrypt para criptografar as senhas. O uso do BCryptPasswordEncoder é recomendado para garantir a segurança das senhas,
// pois ele aplica um processo de hashing adaptativo que torna mais difícil para os atacantes quebrarem as senhas mesmo que tenham acesso ao
// banco de dados. O método é anotado com @Bean para que o Spring possa gerenciar a instância do PasswordEncoder e injetá-la
//  onde for necessário no aplicativo.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
