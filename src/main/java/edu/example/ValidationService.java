package edu.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Service
public class ValidationService{

    @Autowired private SalvoService salvoService;
    @Autowired private ShipRepository shRepo;
    @Autowired private PlayerRepository pRepo;
    @Autowired private GamePlayerRepository gpRepo;
    @Autowired private SalvoRepository slRepo;
    @Autowired private GameRepository repo;


    //Auxiliary function to check player is logged in and is a gamePlayer in the game
    public ResponseEntity<Map<String, Object>> verifyGamePlayer(Long gpId, Authentication authentication) {
        Player player = pRepo.findByUsername(salvoService.getUsername(authentication)); //to check if player loggedin
        if (player == null) {
            return new ResponseEntity<>(salvoService.makeMap("error", "Not logged in"), HttpStatus.UNAUTHORIZED); //401 Works! :)
        }
        GamePlayer gamePlayer = gpRepo.findOne(gpId); //check if gamePlayer id exists
        if (gamePlayer == null) {
            return new ResponseEntity<>(salvoService.makeMap("error", "No such gamePlayer"), HttpStatus.UNAUTHORIZED); //401 Works! :)
        }//
        //Check current player in open game is not same logged in user - comparing usernames //TODO: extract this to be used for salvoes too
        Long gameId = gamePlayer.getGame().getId();
        String currentPlayerUsername = gamePlayer.getPlayer().getUsername();
        String playerUsername = player.getUsername();
        if (playerUsername != currentPlayerUsername) {
            return new ResponseEntity<>(salvoService.makeMap("error", "You are not the gamePlayer in this game"), HttpStatus.UNAUTHORIZED); //401 Works! :)
        }
        return new ResponseEntity<>(salvoService.makeMap("status", "Player & gamePlayer authorized"), HttpStatus.OK);
    }


    public ResponseEntity<Map<String, Object>> validateShips(GamePlayer gamePlayer, List<Ship> ships){

        if (gamePlayer.getShip().size() == 5) { // If 5 ships have already been placed
            return new ResponseEntity<>(salvoService.makeMap("error", "You have already placed ships for this game"), HttpStatus.FORBIDDEN); //403 Works! :)
        }

        for (Ship ship : ships) {
            ship.setGamePlayer(gamePlayer);
        }

        List<Ship> saved = shRepo.save(ships);

        return new ResponseEntity<>(salvoService.makeMap("shipIds", saved.stream().map(s -> s.getId()).collect(toList())), HttpStatus.CREATED); //201

    }

    public ResponseEntity<Map<String, Object>> validateSalvoes(GamePlayer gamePlayer, int turn, Salvo salvo){
        //Validation to make sure turn is not repeated
        List<Integer> turns = gamePlayer.getSalvo().stream().map(sl -> sl.getTurn()).collect(toList());
        if (turns.contains(turn)) {
            return new ResponseEntity<>(salvoService.makeMap("error", "You have already fired salvoes for this turn"), HttpStatus.FORBIDDEN); //403 Working :)
        }

        //Validation to make sure no salvoes are fired after game is complete
        if (gamePlayer.getGame().getGameScores().size() > 0) {
            return new ResponseEntity<>(salvoService.makeMap("error", "Game Over! Stop trying to fire Salvoes"), HttpStatus.FORBIDDEN); //403  Works! :)
        }

        slRepo.save(salvo);
        return new ResponseEntity<>(salvoService.makeMap("salvoIds", salvo.getId()), HttpStatus.CREATED); //201
    }

    public ResponseEntity<Map<String, Object>> validateGame(Player player, Long gameId) {


        if (player == null) {
            return new ResponseEntity<>(salvoService.makeMap("error", "Not logged in"), HttpStatus.UNAUTHORIZED); //401 Works! :)
        }
        Game game = repo.findOne(gameId); //check if game id exists
        if (game == null) {
            return new ResponseEntity<>(salvoService.makeMap("error", "No such game"), HttpStatus.FORBIDDEN); //403 Works! :)
        }
        Integer players = repo.findOne(gameId).getGamePlayers().size(); // check less than 2 players in the game
        if (players == 2) {
            return new ResponseEntity<>(salvoService.makeMap("error", "Game is full"), HttpStatus.FORBIDDEN); //403 Works! :)
        }
        //add player as a gamePlayer in the game - Check current player in open game is not same logged in user - compare usernames
        Player currentPlayer = repo.findOne(gameId).getGamePlayers().stream().map(gps -> gps.getPlayer()).findFirst().get();
        String currentPlayerUsername = currentPlayer.getUsername();
        String playerUsername = player.getUsername();
        if (playerUsername == currentPlayerUsername) {
            return new ResponseEntity<>(salvoService.makeMap("error", "You are already playing in this game"), HttpStatus.FORBIDDEN); //403 Works :)
        }
        GamePlayer gamePlayer = gpRepo.save(new GamePlayer(game, player));
        return new ResponseEntity<>(salvoService.makeMap("gamePlayerId", gamePlayer.getId()), HttpStatus.CREATED); //201 Works! :)
    }

    public ResponseEntity<Map<String, Object>> validatePlayer(Player player, String nickname, String username, String password){

        if (player != null) {
            return new ResponseEntity<Map<String, Object>>(salvoService.makeMap("error", "Username(email) already in use"), HttpStatus.CONFLICT);
        } else {
            player = pRepo.save(new Player(nickname, username, password)); //gives a 201 Created message
            return new ResponseEntity<Map<String, Object>>(salvoService.makeMap("player", player.getUsername()), HttpStatus.CREATED);
        }
    }

}
