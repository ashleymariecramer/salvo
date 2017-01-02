package edu.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Created by ashleymariecramer on 14/12/16.
 */
@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired //this injects an instance of the class GameRepository for use by this controller (Dependency Injection)
    private GameRepository repo;
    @Autowired //this injects an instance of the class GamePlayerRepository for use by this controller (Dependency Injection)
    private GamePlayerRepository gpRepo;

    //1. List of Games
    @RequestMapping("/games")
    public List<Object> getAllGames() {
        return repo.findAll().stream().map(game -> makeGameDTO(game)).collect(toList());
    }

    private Map<String, Object> makeGameDTO(Game game) {

        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("gameId", game.getId());
        dto.put("created", game.getCreationDate());
        dto.put("gamePlayers", game.getGamePlayers().stream().map(gamePlayer -> makeGamePlayerDTO(gamePlayer)).collect(toList()));
        //here we need stream because there are more than one game player per game
        return dto;
    }

    private Map<String, Object> makeGamePlayerDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("gamePlayerId", gamePlayer.getId());
        dto.put("player", makePlayerDTO(gamePlayer.getPlayer())); // don´t need to loop here cos a game player only has one player
        return dto;
    }

    private Map<String, Object> makePlayerDTO(Player player) {

        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("playerId", player.getId());
        dto.put("username", player.getUsername());
        dto.put("nickname", player.getNickname());
        return dto;
    }


    //2. Game View based on gameplayerId
    @RequestMapping("/game_view/{gamePlayerId}")
    public List<Object> getGamesbyPlayer(@PathVariable Long gamePlayerId){
        return gpRepo.findAll().stream()  //loop through all gamePlayer repo to find data of gamePlayer with same id
                .filter(gp -> gp.getId() == gamePlayerId) //only returns data which corresponds to the gamePlayer id in url
                .map(gp -> makeGameViewDTO(gp, gamePlayerId)) //use a map to display only the data needed
                .collect(toList()); //TODO: Don´t need this to be list as there's only one game - but not sure how to change it
    }

//    public Long getCurrentPlayer(@PathVariable Long gamePlayerId){
//        return gamePlayerId;
//    }


    private Map<String, Object> makeGameViewDTO(GamePlayer gamePlayer, Long gamePlayerId) {

        //TODO: YOU & OPPONENT details do not need to be inside a list - how to remove it  - get parallel:false error
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
            dto.put("GameView", makeGameDetailsDTO(gamePlayer.getGame()));
//            dto.put("Game View", makeGameDTO(gamePlayer.getGame())); // Uses makeGame DTO but with only the game from the gamePlayer id
//            dto.put("You", makePlayerDTO(gamePlayer.getPlayer())); // reuse the DTO for makePlayer from games api
            dto.put("You", gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() == gamePlayerId).map(gp -> makeGamePlayerDTO(gp)).collect(toList()));
            dto.put("YourShips", gamePlayer.getShip().stream().map(ship -> makeShipDTO(ship)).collect(toList()));
            dto.put("Opponent", gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gamePlayerId).map(gp -> makeGamePlayerDTO(gp)).collect(toList()));
        //here we need stream because there are more than one ship per game player
            return dto;
    }

    private Map<String, Object> makeGameDetailsDTO(Game game) {

        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("gameId", game.getId());
        dto.put("created", game.getCreationDate());
        return dto;
    }


    private Map<String, Object> makeShipDTO(Ship ship) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("type", ship.getType());
        dto.put("locations", ship.getLocations());
        return dto;
    }




}
