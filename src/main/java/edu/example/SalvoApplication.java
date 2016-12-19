package edu.example;


import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Date;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository PlayerRepo,
									  GameRepository GameRepo,
									  GamePlayerRepository GamePlayerRepo) {
		return (args) -> {
			// save a couple of customers
			Player player1 = new Player("JB", "jack.bauer@hotmail.com");
			Player player2 = new Player("Chloe", "chloe_o_brian@gmail.com");
			Player player3 = new Player("Kimmie B", "kim.bauer@hotmail.com");
			Player player4 = new Player("Almeida", "tony.almeida@ctu.gov");
			// save players to their repository
			PlayerRepo.save(player1);
			PlayerRepo.save(player2);
			PlayerRepo.save(player3);
			PlayerRepo.save(player4);
			// create a few games - offsets times by seconds
			Game game1 = new Game(0);
			Game game2 = new Game(3600);
			Game game3 = new Game(7200);
			Game game4 = new Game(10800);
			Game game5 = new Game(14400);
			Game game6 = new Game(18000);
			Game game7 = new Game(21600);
			Game game8 = new Game(25200);
			// save games to repository
			GameRepo.save(game1);
			GameRepo.save(game2);
			GameRepo.save(game3);
			GameRepo.save(game4);
			GameRepo.save(game5);
			GameRepo.save(game6);
			GameRepo.save(game7);
			GameRepo.save(game8);
			// create game players - 2 per game
			GamePlayer gamePlayer1 = new GamePlayer(0, game1, player1);
			GamePlayer gamePlayer2 = new GamePlayer(30, game1, player2);
			GamePlayer gamePlayer3 = new GamePlayer(0, game2, player1);
			GamePlayer gamePlayer4 = new GamePlayer(650, game2, player2);
			GamePlayer gamePlayer5 = new GamePlayer(450, game3, player2);
			GamePlayer gamePlayer6 = new GamePlayer(0, game3, player4);
			GamePlayer gamePlayer7 = new GamePlayer(280, game4, player2);
			GamePlayer gamePlayer8 = new GamePlayer(0, game4, player1);
			GamePlayer gamePlayer9 = new GamePlayer(0, game5, player4);
			GamePlayer gamePlayer10 = new GamePlayer(310, game5, player1);
			GamePlayer gamePlayer11 = new GamePlayer(0, game6, player3);
			GamePlayer gamePlayer12 = new GamePlayer(0, game7, player4);
			GamePlayer gamePlayer13 = new GamePlayer(0, game8, player3);
			GamePlayer gamePlayer14 = new GamePlayer(860, game8, player4);
			// save game players to repository
			GamePlayerRepo.save(gamePlayer1);
			GamePlayerRepo.save(gamePlayer2);
			GamePlayerRepo.save(gamePlayer3);
			GamePlayerRepo.save(gamePlayer4);
			GamePlayerRepo.save(gamePlayer5);
			GamePlayerRepo.save(gamePlayer6);
			GamePlayerRepo.save(gamePlayer7);
			GamePlayerRepo.save(gamePlayer8);
			GamePlayerRepo.save(gamePlayer9);
			GamePlayerRepo.save(gamePlayer10);
			GamePlayerRepo.save(gamePlayer11);
			GamePlayerRepo.save(gamePlayer12);
			GamePlayerRepo.save(gamePlayer13);
			GamePlayerRepo.save(gamePlayer14);
		};
	}
}
