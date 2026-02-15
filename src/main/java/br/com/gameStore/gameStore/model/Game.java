    package br.com.gameStore.gameStore.model;

    import jakarta.persistence.Entity;
    import jakarta.persistence.GeneratedValue;
    import jakarta.persistence.Id;
    import jakarta.persistence.Table;

    import java.util.Objects;

    // Modelo de entidade para representar um jogo no sistema de loja de jogos.
    // A classe Game possui atributos como id, título, descrição e preço, além de métodos para acessar e modificar esses atributos.

    @Entity
    @Table(name = "tb_game")
    public class Game {

        @Id
        @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
        private Long id;

        private String titulo;

        private String descricao;

        private double preco;


            public Game() {
            }

        public Game(Long id, String titulo, String descricao, double preco) {
            this.id = id;
            this.titulo = titulo;
            this.descricao = descricao;
            this.preco = preco;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitulo() {
            return titulo;
        }

        public void setTitulo(String titulo) {
            this.titulo = titulo;
        }

        public String getDescricao() {
            return descricao;
        }

        public void setDescricao(String descricao) {
            this.descricao = descricao;
        }

        public double getPreco() {
            return preco;
        }

        public void setPreco(double preco) {
            this.preco = preco;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Game game = (Game) o;
            return Double.compare(preco, game.preco) == 0 && Objects.equals(id, game.id) && Objects.equals(titulo, game.titulo) && Objects.equals(descricao, game.descricao);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, titulo, descricao, preco);
        }

        @Override
        public String toString() {
            return "Game{" +
                    "id=" + id +
                    ", titulo='" + titulo + '\'' +
                    ", descricao='" + descricao + '\'' +
                    ", preco=" + preco +
                    '}';
        }
    }
