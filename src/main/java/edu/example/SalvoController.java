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
    public Map<String, Object> getGamesbyPlayer(@PathVariable Long gamePlayerId){
        GamePlayer gamePlayer = gpRepo.findOne(gamePlayerId);
        return makeGameViewDTO(gamePlayer, gamePlayerId);
    }


    private Map<String, Object> makeGameViewDTO(GamePlayer gamePlayer, Long gamePlayerId) {

        Map<String, Object> dto = new LinkedHashMap<String, Object>();
            dto.put("gameView", makeGameDetailsDTO(gamePlayer.getGame()));
            dto.put("you", gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() == gamePlayerId).findFirst().map(gp -> makeGamePlayerDTO(gp)).get());
            dto.put("yourShips", gamePlayer.getShip().stream().map(ship -> makeShipDTO(ship)).collect(toList()));
            dto.put("opponent", gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gamePlayerId).findFirst().map(gp -> makeGamePlayerDTO(gp)).get());
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
