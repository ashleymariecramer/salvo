package edu.example;


import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository PlayerRepo, GameRepository GameRepo) {
		return (args) -> {
			// save a couple of customers
			Player player1 = new Player("JB", "jack.bauer@hotmail.com");
			Player player2 = new Player("Chloe", "chloe_o_brian@gmail.com");
			Player player3 = new Player("Kimmie B", "kim.bauer@hotmail.com");
			Player player4 = new Player("Dave", "d.palmer@yahoo.com");
			Player player5 = new Player("Dessie", "michdess@aol.com");
			PlayerRepo.save(player1);
			PlayerRepo.save(player2);
			PlayerRepo.save(player3);
			PlayerRepo.save(player4);
			PlayerRepo.save(player5);
			Game game1 = new Game();
			Game game2 = new Game();
			Game game3 = new Game();
			Game game4 = new Game();
			GameRepo.save(game1);
			GameRepo.save(game2);
			GameRepo.save(game3);
			GameRepo.save(game4);
		};
	}
}
