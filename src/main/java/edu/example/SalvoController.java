package edu.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

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
    @Autowired
    //this injects an instance of the class GamePlayerRepository for use by this controller (Dependency Injection)
    private GamePlayerRepository gpRepo;
    @Autowired
    //this injects an instance of the class GamePlayerRepository for use by this controller (Dependency Injection)
    private GameScoreRepository gsRepo;
    @Autowired
    //this injects an instance of the class PlayerRepository for use by this controller (Dependency Injection)
    private PlayerRepository pRepo;

// This gives the username (email of the logged in player)
//    private String getUserName(Authentication authentication) {
//        String details = authentication.getName();
//        return details; /
//    }

    //1. List of Games by Logged in player
   @RequestMapping("/games")

    public Map<String, Object> getUserGames(Authentication authentication) {
       Player loggedInUser = pRepo.findByUsername(authentication.getName());
       if (loggedInUser != null) {
           return makeUserDTO(loggedInUser);
       }
       else { //TODO: this is not getting triggered - Why?
           return makeNullUserDTO(); //this returns { "player": null, "games": null } which is good
       }

    }

    private Map<String, Object> makeNullUserDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("player", null);
        dto.put("games", null);
        return dto;
    }




    private Map<String, Object> makeUserDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("player", makeUserDetailsDTO(player)); // don´t need to loop here cos a game player only has one player
        dto.put("games", player.getGameScores().stream().map(gs -> gs.getGame().getId()).collect(toList()));
        return dto;
    }


    private Map<String, Object> makeUserDetailsDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("playerId", player.getId()); //TODO: this the id we want to filter by
        dto.put("username", player.getUsername());
        dto.put("nickname", player.getNickname());
        return dto;
    }





    //2. Game View based on gameplayerId
    @RequestMapping("/game_view/{gamePlayerId}")
    public Map<String, Object> getGamesbyPlayer(@PathVariable Long gamePlayerId) {
        GamePlayer gamePlayer = gpRepo.findOne(gamePlayerId);
        long gameId = gamePlayer.getGame().getId();
        return makeGameViewDTO(gamePlayer, gamePlayerId, gameId);
        //passing gameId as a parameter allows individual scores to be added to game view for completed games
    }


    private Map<String, Object> makeGameViewDTO(GamePlayer gamePlayer, Long gamePlayerId, Long gameId) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("gameView", makeGameDetailsDTO(gamePlayer.getGame()));
        dto.put("you", gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() == gamePlayerId).findFirst().map(gp -> makeGamePlayerDTO(gp, gameId)).get());
        dto.put("yourShips", gamePlayer.getShip().stream().map(ship -> makeShipDTO(ship)).collect(toList()));
        dto.put("yourSalvoes", gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() == gamePlayerId).findFirst().get().getSalvo().stream().map(salvo -> makeSalvoDTO(salvo)).collect(toList()));
        dto.put("opponent", gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gamePlayerId).findFirst().map(gp -> makeGamePlayerDTO(gp, gameId)).get());
        dto.put("opponentShips", gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gamePlayerId).findFirst().get().getShip().stream().map(ship -> makeShipDTO(ship)).collect(toList()));
        dto.put("opponentSalvoes", gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gamePlayerId).findFirst().get().getSalvo().stream().map(salvo -> makeSalvoDTO(salvo)).collect(toList()));

        return dto;
    }

    private Map<String, Object> makeGameDetailsDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("gameId", game.getId());
        dto.put("created", game.getCreationDate());
        return dto;
    }

    private Map<String, Object> makeGamePlayerDTO(GamePlayer gamePlayer, long gameId) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("gamePlayerId", gamePlayer.getId());
        dto.put("player", makePlayerDTO(gamePlayer.getPlayer(), gameId)); // don´t need to loop here cos a game player only has one player
        return dto;
    }


    private Map<String, Object> makePlayerDTO(Player player, long gameId) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("playerId", player.getId());
        dto.put("username", player.getUsername());
        dto.put("nickname", player.getNickname());
        dto.put("score", player.getGameScores().stream().filter(gs -> gs.getGame().getId() == gameId).findFirst().map(g -> g.getScore()).orElse(null));
        //adding score here makes it clear who the score belongs to
        return dto;
    }

    private Map<String, Object> makeShipDTO(Ship ship) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("type", ship.getType());
        dto.put("locations", ship.getLocations());
        return dto;
    }

    private Map<String, Object> makeSalvoDTO(Salvo salvo) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn", salvo.getTurn());
        dto.put("locations", salvo.getLocations());
        return dto;
    }


//    3. List of GamePlayer results
    @RequestMapping("/scores")

    public List<Map<String, Object>> getAllScoreStats() {

        Map<Long, Map<String, Object>> scoreStats = new HashMap<>();

        List<GameScore> gameScores = gsRepo.findAll();

        for (GameScore gameScore : gameScores) {
            long id = gameScore.getPlayer().getId();
            if (scoreStats.containsKey(id)) { //if player already exists in the map

                Map<String, Object> playerScore = scoreStats.get(id);
                //add new score to existing score
                double existingScore = (Double) playerScore.get("score");
                double newScore = existingScore + gameScore.getScore();
                playerScore.put("score", newScore);
                Integer won = 0;
                Integer lost = 0;
                Integer tied = 0;
                //This checks the game result and updates the count for the relevant result (won, lost, or tied)
                String result = returnResult(gameScore.getScore());
                if (result.equals("won")) {
                    won = (Integer) playerScore.get("won");
                    won++;
                }
                else if (result.equals("lost")) {
                    lost = (Integer) playerScore.get("lost");
                    lost++;
                }
                else{
                    tied = (Integer) playerScore.get("tied");
                    tied++;
                }

                playerScore.put("won", won);
                playerScore.put("lost", lost);
                playerScore.put("tied", tied);

            } else { //If no player exists for this id then create initial data with playerId, nickname, and scores
                scoreStats.put(id, makeScoreStatsDTO(gameScore));
            }
        }
        // This converts the map within a map with the long id into a simple array of the maps for the player score stats
        List<Map<String, Object>> result = new ArrayList<>();
        //This set takes the long ids for each player
        Set<Long> keys = scoreStats.keySet();
        for (Long key : keys) { //this loops through the long ids to get their keys (which is the whole map key/value pairs for nickname: JB, score:1 etc)

            result.add(scoreStats.get(key));
        }

        return result;

    }

    private Map<String, Object> makeScoreStatsDTO(GameScore gameScore) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("playerId", gameScore.getPlayer().getId());
        dto.put("nickname", gameScore.getPlayer().getNickname());
        dto.put("score", gameScore.getScore());
        dto.put("won", 0);
        dto.put("lost", 0);
        dto.put("tied", 0);
        String result = returnResult(gameScore.getScore());

        //Here I need to put something which decides which result to populate with value 1
        if (result.equals("won")) {
            dto.put("won", 1);
        }
        else if (result.equals("lost")) {
            dto.put("lost", 1);
        }
        else{
            dto.put("tied", 1);
        }
        return dto;
    }

    private String returnResult(double score){
        if (score < 0.5 ){
            return "lost";
        }
        else if (score > 0.5 ){
            return "won";
        }
        else {
            return "tied";
        }
    }



} //Do not delete!! End of function