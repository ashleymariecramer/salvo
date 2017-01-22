package edu.example;

import org.hibernate.validator.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

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

    //2. List of All to be shown whether user logged in or not
    @RequestMapping(path = "/games", method = RequestMethod.GET)
    public List<Object> getAllGames() {
        return repo.findAll().stream().map(game -> makeGameDTO(game)).collect(toList());
    }

    private Map<String, Object> makeGameDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        long gameId = game.getId();
        dto.put("gameId", game.getId());
        dto.put("created", game.getCreationDate());
        dto.put("gamePlayers", game.getGamePlayers().stream().map(gamePlayer -> makeGamePlayerDTO(gamePlayer, gameId)).collect(toList()));
        //here we need stream because there are more than one game player per game
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

    //2. List of Games by Logged in player
    @RequestMapping(path = "/currentUserGames", method = RequestMethod.GET)
    public Map<String, Object> getUserGames(Authentication authentication) {
       if (!isGuest(authentication)) { //This checks there is not a guest user
           Player loggedInUser = pRepo.findByUsername(authentication.getName());
           return makeUserDTO(loggedInUser, authentication);
       }
       else{
           return makeGuestUserDTO();
       }
    }

    //this returns the players id number (long) or the number 0 for guests (if no one logged in)
    private String getLoggedInUser(Authentication authentication){
        if (!isGuest(authentication)) { //This checks there is not a guest user
            String loggedInUser = pRepo.findByUsername(authentication.getName()).getUsername();
            return loggedInUser;
        }
        else{
            String loggedInUser = "guest";
            return loggedInUser;
        }
    }

    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
        //this checks if authentication is null or is an instance the predefined spring security class "AnonymousAuthenticationToken"
    }

    private Map<String, Object> makeGuestUserDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("player", "guest");
        return dto;
    }

    private Map<String, Object> makeUserDTO(Player player, Authentication authentication) {
        String loggedInUser = getLoggedInUser(authentication);
//        Long gamePlayerId = player.getGamePlayers().stream().filter(gp -> gp.getPlayer().getUsername() == loggedInUser).findFirst().get().getId();;
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("loggedInPlayer", makeLoggedInPlayersDetailsDTO(player)); // don´t need to loop here cos a game player only has one player
        dto.put("games", player.getGamePlayers().stream().map(gp -> gp.getId()).map(gpId -> makePlayersGameDetailsDTO(gpId, authentication)).collect(toList()));
        return dto;
    }

    private Map<String, Object> makeLoggedInPlayersDetailsDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("playerId", player.getId());
        dto.put("username", player.getUsername());
        dto.put("nickname", player.getNickname());
        dto.put("gameIdsFinishedGames", player.getGameScores().stream().map(gs -> gs.getGame().getId()).collect(toList()));
        dto.put("gamePlayerIds", player.getGamePlayers().stream().map(gp -> gp.getId()).collect(toList()));
        dto.put("games", player.getGamePlayers().stream().map(gp -> gp.getGame().getId()).collect(toList()));
        return dto;
    }

//    private Map<String, Object> makePlayersGameDetailsDTO(Game game, String loggedInUser) {
//        Map<String, Object> dto = new LinkedHashMap<String, Object>();
//        Long gameId = game.getId();
//        dto.put("gameId", gameId);
////        dto.put("gamePlayers", game.getGamePlayers().stream().map(gamePlayer -> makePlayerAndOpponentDTO(gamePlayer, gameId, loggedInUser)).collect(toList()));
//        dto.put("gameId", game.getId());
//        dto.put("gamePlayers", game.getGamePlayers());
//        dto.put("gameScores", game.getGameScores());



    public Map<String, Object> makePlayersGameDetailsDTO(Long gamePlayerId, Authentication authentication) {
        GamePlayer gamePlayer = gpRepo.findOne(gamePlayerId);
        long gameId = gamePlayer.getGame().getId();
        String playerId = gamePlayer.getPlayer().getUsername();
        String loggedInUser = getLoggedInUser(authentication);
        if (playerId == loggedInUser) {//if player id for gameplayer & logged are the same -> return game view
            return makePlayersGamesViewsDTO(gamePlayer, gamePlayerId, gameId);
        }
        else {
            Map<String, Object> result = makeMap("Error", new ResponseEntity<String>("Sorry, you are not a player in this game", HttpStatus.UNAUTHORIZED));
            return result;
        }
    }
    private Map<String, Object> makePlayersGamesViewsDTO(GamePlayer gamePlayer, Long gamePlayerId, Long gameId) {
    Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("gameId", gamePlayer.getGame().getId());
        dto.put("you", gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() == gamePlayerId)
            .findFirst().map(gp -> makeGamePlayerDTO(gp, gameId)).get());
        dto.put("opponent", gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gamePlayerId)
            .findFirst().map(gp -> makeGamePlayerDTO(gp, gameId)).get());
        return dto;
}

//
//    private Map<String, Object> makePlayerAndOpponentDTO(GamePlayer gamePlayer, long gameId, String loggedInUser) {
//            Map<String, Object> dto = new LinkedHashMap<String, Object>();
//            Long gamePlayerId = gamePlayer.getId();
//            String gpUsername = gpRepo.findOne(gamePlayerId).getPlayer().getUsername();
//            //If loggedInUsername and username for the gameplayer are the same create dto for you
//            //if not create dto for opponent
//            dto.put("player", makeGamePlayerDTO(gamePlayer, gameId));
//
//            return dto;
//        }

    //3. Game View based on gameplayerId
    //passing gameId as a parameter allows individual scores to be added to game view for completed games
    @RequestMapping("/game_view/{gamePlayerId}")
    public Map<String, Object> getGamesbyPlayer(@PathVariable Long gamePlayerId, Authentication authentication) {
        GamePlayer gamePlayer = gpRepo.findOne(gamePlayerId);
        long gameId = gamePlayer.getGame().getId();
        String playerId = gamePlayer.getPlayer().getUsername();
        String loggedInUser = getLoggedInUser(authentication);
        if (playerId == loggedInUser) {//if player id for gameplayer & logged are the same -> return game view
            return makeGameViewDTO(gamePlayer, gamePlayerId, gameId);
        }
        else {
            Map<String, Object> result = makeMap("Error", new ResponseEntity<String>("Sorry, you are not a player in this game", HttpStatus.UNAUTHORIZED));
            return result;
        }
    }

    private Map<String, Object> makeGameViewDTO(GamePlayer gamePlayer, Long gamePlayerId, Long gameId) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("gameView", makeGameDetailsDTO(gamePlayer.getGame()));
        dto.put("you", gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() == gamePlayerId)
                .findFirst().map(gp -> makeGamePlayerDTO(gp, gameId)).get());
        dto.put("yourShips", gamePlayer.getShip().stream().map(ship -> makeShipDTO(ship)).collect(toList()));
        dto.put("yourSalvoes", gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() == gamePlayerId)
                .findFirst().get().getSalvo().stream().map(salvo -> makeSalvoDTO(salvo)).collect(toList()));
        dto.put("opponent", gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gamePlayerId)
                .findFirst().map(gp -> makeGamePlayerDTO(gp, gameId)).get());
        dto.put("opponentShips", gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gamePlayerId)
                .findFirst().get().getShip().stream().map(ship -> makeShipDTO(ship)).collect(toList()));
        dto.put("opponentSalvoes", gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gamePlayerId)
                .findFirst().get().getSalvo().stream().map(salvo -> makeSalvoDTO(salvo)).collect(toList()));
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

    private Map<String, Object> makeSalvoDTO(Salvo salvo) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn", salvo.getTurn());
        dto.put("locations", salvo.getLocations());
        return dto;
    }


//   4. List of GamePlayer results
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

    //5. Create new players
    //Because some of the conditions have to return a Map ResponseEntity<Map<String,Object>> then all of them have to
    //@RequestParam is necessary before each parameter
    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>>createPlayer(@RequestParam String nickname,
                                                          @RequestParam String username,
                                                          @RequestParam String password){
        Player player = pRepo.findByUsername(username); //gives a 409 Conflict Error
        if (player != null) {
            return new ResponseEntity<Map<String, Object>>(makeMap("error", "Username(email) already in use"), HttpStatus.CONFLICT);
        } else {
            player = pRepo.save(new Player(nickname, username, password)); //gives a 201 Created message
            return new ResponseEntity<Map<String, Object>>(makeMap("player", player.getUsername()), HttpStatus.CREATED);
        }

    }

    //this is needed to build the method makeMap
    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }


//code to test in console:
// $.post("/api/players", { nickname: "jon", password: "1234" }) --> should trigger 403 error username missing
// $.post("/api/players", { nickname: "jon", username: "j.bauer@ctu.gov", password: "1234" }) --> should trigger 409 error
// $.post("/api/players", { nickname: "jon", username: "newbie@aol.com", password: "1234" }) --> should work and give 201 response status



} //Do not delete!! End of function