package edu.example;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication //This is needed to use SpringBoot
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository playerRepo,
									  GameRepository gameRepo,
									  GamePlayerRepository gamePlayerRepo,
									  ShipRepository shipRepo,
									  SalvoRepository salvoRepo,
									  GameScoreRepository gameScoreRepo) {
		return (args) -> {
			// save a couple of customers
			Player player1 = new Player("JB", "j.bauer@ctu.gov", "24");
			Player player2 = new Player("Chloe", "c.obrian@ctu.gov", "42");
			Player player3 = new Player("Kimmie B", "kim_bauer@gmail.com", "kb");
			Player player4 = new Player("Almeida", "t.almeida@ctu.gov", "mole");
			// save players to their repository
			playerRepo.save(player1);
			playerRepo.save(player2);
			playerRepo.save(player3);
			playerRepo.save(player4);
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
			gameRepo.save(game1);
			gameRepo.save(game2);
			gameRepo.save(game3);
			gameRepo.save(game4);
			gameRepo.save(game5);
			gameRepo.save(game6);
			gameRepo.save(game7);
			gameRepo.save(game8);
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
			gamePlayerRepo.save(gamePlayer1);
			gamePlayerRepo.save(gamePlayer2);
			gamePlayerRepo.save(gamePlayer3);
			gamePlayerRepo.save(gamePlayer4);
			gamePlayerRepo.save(gamePlayer5);
			gamePlayerRepo.save(gamePlayer6);
			gamePlayerRepo.save(gamePlayer7);
			gamePlayerRepo.save(gamePlayer8);
			gamePlayerRepo.save(gamePlayer9);
			gamePlayerRepo.save(gamePlayer10);
			gamePlayerRepo.save(gamePlayer11);
			gamePlayerRepo.save(gamePlayer12);
			gamePlayerRepo.save(gamePlayer13);
			gamePlayerRepo.save(gamePlayer14);
			// create new locations using Array.asList
			List<String> locations1 = Arrays.asList("H2", "H3", "H4");
			List<String> locations2 = Arrays.asList("E1", "F1", "G1");
			List<String> locations3 = Arrays.asList("B4", "B5");
			List<String> locations4 = Arrays.asList("B5", "C5", "D5");
			List<String> locations5 = Arrays.asList("F1", "F2");
			List<String> locations6 = Arrays.asList("C6","C7");
			List<String> locations7 = Arrays.asList("A2","A3","A4");
			List<String> locations8 = Arrays.asList("G6","H6");
			// create new ships (max 5 per player) & assign locations to these
			//could also combine these 2 steps in 1:
			// Ship ship1 = new Ship("Destroyer", gamePlayer1, Arrays.asList("H2", "H3", "H4"));
			Ship ship1 = new Ship("Destroyer", gamePlayer1, locations1);
			Ship ship2 = new Ship("Submarine", gamePlayer1, locations2);
			Ship ship3 = new Ship("Patrol Boat", gamePlayer1, locations3);
			Ship ship4 = new Ship("Destroyer", gamePlayer2, locations4);
			Ship ship5 = new Ship("Patrol Boat", gamePlayer2, locations5);
			Ship ship6 = new Ship("Destroyer", gamePlayer3, locations4);
			Ship ship7 = new Ship("Patrol Boat", gamePlayer3, locations6);
			Ship ship8 = new Ship("Submarine", gamePlayer4, locations7);
			Ship ship9 = new Ship("Patrol Boat", gamePlayer4, locations8);
			Ship ship10 = new Ship("Destroyer", gamePlayer5, locations4);
			Ship ship11 = new Ship("Patrol Boat", gamePlayer5, locations6);
			Ship ship12 = new Ship("Submarine", gamePlayer6, locations7);
			Ship ship13 = new Ship("Patrol Boat", gamePlayer6, locations8);
			Ship ship14 = new Ship("Destroyer", gamePlayer7, locations4);
			Ship ship15 = new Ship("Patrol Boat", gamePlayer7, locations6);
			Ship ship16 = new Ship("Submarine", gamePlayer8, locations7);
			Ship ship17 = new Ship("Patrol Boat", gamePlayer8, locations8);
			// add ships to repo
			shipRepo.save(ship1);
			shipRepo.save(ship2);
			shipRepo.save(ship3);
			shipRepo.save(ship4);
			shipRepo.save(ship5);
			shipRepo.save(ship6);
			shipRepo.save(ship7);
			shipRepo.save(ship8);
			shipRepo.save(ship9);
			shipRepo.save(ship10);
			shipRepo.save(ship11);
			shipRepo.save(ship12);
			shipRepo.save(ship13);
			shipRepo.save(ship14);
			shipRepo.save(ship15);
			shipRepo.save(ship16);
			shipRepo.save(ship17);
			//create salvos
			Salvo salvo1 = new Salvo(1, gamePlayer1, Arrays.asList("B5", "C5", "F1"));
			Salvo salvo2 = new Salvo(1, gamePlayer2, Arrays.asList("B4", "B5", "B6"));
			Salvo salvo3 = new Salvo(2, gamePlayer1, Arrays.asList("F2", "D5"));
			Salvo salvo4 = new Salvo(2, gamePlayer2, Arrays.asList("E1", "H3", "A2"));
			Salvo salvo5 = new Salvo(1, gamePlayer3, Arrays.asList("A2", "A4", "G6"));
			Salvo salvo6 = new Salvo(1, gamePlayer4, Arrays.asList("B5", "D5", "C7"));
			Salvo salvo7 = new Salvo(2, gamePlayer3, Arrays.asList("A3", "H6"));
			Salvo salvo8 = new Salvo(2, gamePlayer4, Arrays.asList("C5", "C6"));
			Salvo salvo9 = new Salvo(1, gamePlayer5, Arrays.asList("H6", "A4", "G6"));
			Salvo salvo10 = new Salvo(1, gamePlayer6, Arrays.asList("H1", "H2", "H3"));
			Salvo salvo11 = new Salvo(2, gamePlayer5, Arrays.asList("A2", "A3", "D8"));
			Salvo salvo12 = new Salvo(2, gamePlayer6, Arrays.asList("E1", "F2", "G3"));
			Salvo salvo13 = new Salvo(1, gamePlayer7, Arrays.asList("A3", "A4", "F7"));
			Salvo salvo14 = new Salvo(1, gamePlayer8, Arrays.asList("B5", "C6", "H1"));
			Salvo salvo15 = new Salvo(2, gamePlayer7, Arrays.asList("A2", "G6", "H6"));
			Salvo salvo16 = new Salvo(2, gamePlayer8, Arrays.asList("C5", "C7", "D5"));
			//save Salvoes to repo
			salvoRepo.save(salvo1);
			salvoRepo.save(salvo2);
			salvoRepo.save(salvo3);
			salvoRepo.save(salvo4);
			salvoRepo.save(salvo5);
			salvoRepo.save(salvo6);
			salvoRepo.save(salvo7);
			salvoRepo.save(salvo8);
			salvoRepo.save(salvo9);
			salvoRepo.save(salvo10);
			salvoRepo.save(salvo11);
			salvoRepo.save(salvo12);
			salvoRepo.save(salvo13);
			salvoRepo.save(salvo14);
			salvoRepo.save(salvo15);
			salvoRepo.save(salvo16);
			// create gameScores per gamePlayer (finish time offset, Game game, Player player, float score)
			GameScore gameScore1 = new GameScore(1800, game1, player1, 1);
			GameScore gameScore2 = new GameScore(1800, game1, player2, 0);
			GameScore gameScore3 = new GameScore(5400, game2, player1, 0.5);
			GameScore gameScore4 = new GameScore(5400, game2, player2, 0.5);
			GameScore gameScore5 = new GameScore(9000, game3, player2, 1);
			GameScore gameScore6 = new GameScore(9000, game3, player4, 0);
			GameScore gameScore7 = new GameScore(12600, game4, player2, 0.5);
			GameScore gameScore8 = new GameScore(12600, game4, player1, 0.5);
			//save scores to repo
			gameScoreRepo.save(gameScore1);
			gameScoreRepo.save(gameScore2);
			gameScoreRepo.save(gameScore3);
			gameScoreRepo.save(gameScore4);
			gameScoreRepo.save(gameScore1);
			gameScoreRepo.save(gameScore5);
			gameScoreRepo.save(gameScore6);
			gameScoreRepo.save(gameScore7);
			gameScoreRepo.save(gameScore8);
		};
	}
}


@Configuration //Allows spring to find these classes even though they are not public
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {
////The job of this new class is to take the email entered at login
//// and search the database to return a UserDetails object (if one exists)

	@Autowired
	PlayerRepository playerRepository;

	@Override
	public void init(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService());
	}

	@Bean
	UserDetailsService userDetailsService() {
		return new UserDetailsService() {

// changed findByEmail(name) to findByUsername(name) NB: name cannot be changed to email
// This method looks up a user by name in your repository, and, if found, creates and returns a
// org.springframework.security.core.userdetails.User object, with the stored user name, the stored password
// for that user, and the role or roles that user has.
 			@Override
			public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
              List<Player> players = playerRepository.findPlayersByUsername(name);
				if (!players.isEmpty()) {
					Player player = players.get(0);
					return new User(player.getUsername(), player.getPassword(),
							AuthorityUtils.commaSeparatedStringToAuthorityList("USER,ADMIN"));
							//only one role here = USER, to add multiple roles e.g., "INSTRUCTOR,STUDENT"
 							//use: AuthorityUtils.commaSeparatedStringToAuthorityList("INSTRUCTOR,STUDENT"));
				} else {
					throw new UsernameNotFoundException("Unknown user: " + name);
				}
			}
		};
	}
}


//Add in Configuration Security to define which authentication method is used & which pages are restricted to USERS
@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
 				.antMatchers("/game.html").hasAuthority("USER")
//				.antMatchers("/rest/**" ).hasAuthority("ADMIN") //TODO: change this back after testing uncomment here & removes from permitALL
				.antMatchers("/games.html", "gameStyle.css", "games.js", "/api/scores",
						"/api/games", "game.js", "/api/logout", "/rest/**", "/game_view/**", "game_view" ).permitAll()//For pages that can be seen by all
				.and()
			.formLogin() //This shows it uses form-based authentication
				.usernameParameter("username") //have changed name to email
				.passwordParameter("password") //Nothing changed
				.loginPage("/api/login")
				.permitAll() // technically not needed but logical to have it here
				.and()
				.logout()
				.logoutUrl("/api/logout")
				.permitAll(); // technically not needed but logical to have it here

//  This code disables the CSFR tokens - and
		  //turn off checking for CSRF tokens
		  http.csrf().disable();

		  //This overrides default settings that send HTML forms when unauthenticated access happens and when someone logs in or out.
		  // With these changes, Spring just sents HTTP success and response codes, no HTML pages.
		  // if user is not authenticated, just send an authentication failure response
		  http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		  // if login is successful, just clear the flags asking for authentication
		  http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

		  // if login fails, just send an authentication failure response
		  http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		  // if logout is successful, just send a success response
		  http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
		  }
//This is a utility function, defined to remove the flag Spring sets when an unauthenticated user attempts to access some resource.
	private void clearAuthenticationAttributes(HttpServletRequest request) {
			HttpSession session = request.getSession(false);
			if (session != null) {
			session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
			}

		}
}
