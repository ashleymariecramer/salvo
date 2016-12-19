package edu.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.toList;

/**
 * Created by ashleymariecramer on 14/12/16.
 */
@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository repo;
    //this creates an instace of the class GameRepository for use by this controller

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
        return dto;
    }
}
