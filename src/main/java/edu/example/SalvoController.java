package edu.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
    @Autowired
    //this injects an instance of the class ShipRepository for use by this controller (Dependency Injection)
    private ShipRepository shRepo;
    @Autowired
    //this injects an instance of the class SalvoRepository for use by this controller (Dependency Injection)
    private SalvoRepository slRepo;



    /****************************** API /GAMES **************************************/
    //1. List of All games to be shown whether user logged in or not
    @RequestMapping(path = "/games", method = RequestMethod.GET)
    public List<Object> getAllGames() {
        return repo.findAll().stream().map(game -> makeGameDTO(game)).collect(toList());
    }

    //for each game it returns the id, creation date and the players: with their gpId & player details
    private Map<String, Object> makeGameDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        long gameId = game.getId();
        dto.put("gameId", game.getId());
        dto.put("created", game.getCreationDate());
        dto.put("gamePlayers", game.getGamePlayers().stream().map(gamePlayer -> makeGamePlayerDTO(gamePlayer, gameId)).collect(toList()));
        //here we need stream because there are more than one game player per game
        return dto;
    }

    //for each gameplayer it returns their id and player details
    private Map<String, Object> makeGamePlayerDTO(GamePlayer gamePlayer, long gameId) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("gamePlayerId", gamePlayer.getId());
        dto.put("player", makePlayerDTO(gamePlayer.getPlayer(), gameId)); // don´t need to loop here cos a game player only has one player
        return dto;
    }

    // the gameplayers details include their playerId, username(email), nickname and score(if game is finished)
    private Map<String, Object> makePlayerDTO(Player player, long gameId) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("playerId", player.getId());
        dto.put("username", player.getUsername());
        dto.put("nickname", player.getNickname());
        dto.put("score", player.getGameScores().stream().filter(gs -> gs.getGame().getId() == gameId).findFirst().map(g -> g.getScore()).orElse(null));
        //adding score here makes it clear who the score belongs to
        return dto;
    }


    /******************************* API /CURRENT USER GAMES ********************************************/
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
    private String getUsername(Authentication authentication){
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

    public Map<String, Object> makePlayersGameDetailsDTO(Long gamePlayerId, Authentication authentication) {
        GamePlayer gamePlayer = gpRepo.findOne(gamePlayerId);
        long gameId = gamePlayer.getGame().getId();
        String playerId = gamePlayer.getPlayer().getUsername();
        String loggedInUser = getUsername(authentication);
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

        Optional<Map<String, Object>> oponent = gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gamePlayerId)
                .findFirst().map(gp -> makeGamePlayerDTO(gp, gameId));
        if (oponent.isPresent()) {
            dto.put("opponent", oponent.get()); // findFirst returns an Optional, as it could be that there is no first to find
            //So for this reason we can add the conditional to see if it isPresent (ie. has a value) before executing the code
        } //if there is no optional then this part of the code is skipped

        return dto;
}

    /*********************************** API /GAME VIEW ****************************************/
    //3. Game View based on gameplayerId
    //passing gameId as a parameter allows individual scores to be added to game view for completed games
    @RequestMapping("/game_view/{gamePlayerId}")
    public Map<String, Object> getGamesbyPlayer(@PathVariable Long gamePlayerId, Authentication authentication) {
        GamePlayer gamePlayer = gpRepo.findOne(gamePlayerId);
        long gameId = gamePlayer.getGame().getId();
        String playerId = gamePlayer.getPlayer().getUsername();
        String loggedInUser = getUsername(authentication);
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
        Optional<Map<String, Object>> oponent = gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gamePlayerId)
                .findFirst().map(gp -> makeGamePlayerDTO(gp, gameId));
        if (oponent.isPresent()) {
            dto.put("opponent", oponent.get());
            dto.put("opponentShips", gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gamePlayerId)
                    .findFirst().get().getShip().stream().map(ship -> makeShipDTO(ship)).collect(toList()));
            dto.put("opponentSalvoes", gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gamePlayerId)
                    .findFirst().get().getSalvo().stream().map(salvo -> makeSalvoDTO(salvo)).collect(toList()));
        }

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

    /******************************** API /SCORES *******************************************/
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

    /*********************************** API /PLAYERS ****************************************/
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

    /*********************************** API /NEW GAME ****************************************/
    //6. Create new game
    @RequestMapping(path = "/newGame", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>>createGame(Authentication authentication){
        Player player = pRepo.findByUsername(getUsername(authentication));
        Game game = repo.save(new Game(0l));
        GamePlayer gamePlayer = gpRepo.save(new GamePlayer(game, player));
        return new ResponseEntity<>(makeMap("gamePlayerId", gamePlayer.getId()), HttpStatus.CREATED);
    }


    /************************* API /JOIN GAME (add player to existing game ********************************/
    //7. Add new player to an existing game, saving new gameplayer id to the gpRepo and updating game in repo too.
    @RequestMapping(path = "/games/{gameId}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> checkGamebyId(@PathVariable Long gameId, Authentication authentication) {
        Player player = pRepo.findByUsername(getUsername(authentication)); //to check if player loggedin
        if (player == null) {
            return new ResponseEntity<>(makeMap("error", "Not logged in"), HttpStatus.UNAUTHORIZED); //401 Works! :)
        }
        Game game = repo.findOne(gameId); //check if game id exists
        if (game == null) {
            return new ResponseEntity<>(makeMap("error", "No such game"), HttpStatus.FORBIDDEN); //403 Works! :)
        }
        Integer players = repo.findOne(gameId).getGamePlayers().size(); // check less than 2 players in the game
        if (players == 2) {
            return new ResponseEntity<>(makeMap("error", "Game is full"), HttpStatus.FORBIDDEN); //403 Works! :)
        }
        //add player as a gamePlayer in the game - Check current player in open game is not same logged in user - compare usernames
        Player currentPlayer = repo.findOne(gameId).getGamePlayers().stream().map(gps -> gps.getPlayer()).findFirst().get();
        String currentPlayerUsername = currentPlayer.getUsername();
        String playerUsername = player.getUsername();
        if (playerUsername == currentPlayerUsername){
            return new ResponseEntity<>(makeMap("error", "You are already playing in this game"), HttpStatus.FORBIDDEN); //403 Works :)
        }
        GamePlayer gamePlayer = gpRepo.save(new GamePlayer(game, player));
        return new ResponseEntity<>(makeMap("gamePlayerId", gamePlayer.getId()), HttpStatus.CREATED); //201 Works! :)
    }

    /************************* API /ADD SHIPS (add ships to existing game for specific gameplayer id) ********************************/
    //8. Add ships
    @RequestMapping(path = "/games/players/{gpId}/ships", method = RequestMethod.POST)
    //the ships will be passed as a list from the front end
   public ResponseEntity<Map<String, Object>> addShip(@PathVariable Long gpId, @RequestBody List<Ship> ships, Authentication authentication) {
        Player player = pRepo.findByUsername(getUsername(authentication)); //to check if player loggedin
        if (player == null) {
            return new ResponseEntity<>(makeMap("error", "Not logged in"), HttpStatus.UNAUTHORIZED); //401 Works! :)
        }
        GamePlayer gamePlayer = gpRepo.findOne(gpId); //check if gamePlayer id exists
        if (gamePlayer == null) {
            return new ResponseEntity<>(makeMap("error", "No such gamePlayer"), HttpStatus.UNAUTHORIZED); //401 Works! :)
        }//
        //Check current player in open game is not same logged in user - comparing usernames
        Long gameId = gamePlayer.getGame().getId();
        String currentPlayerUsername = gamePlayer.getPlayer().getUsername();
        String playerUsername = player.getUsername();
        if (playerUsername != currentPlayerUsername){
            return new ResponseEntity<>(makeMap("error", "You are not the gamePlayer in this game"), HttpStatus.UNAUTHORIZED); //401 Works! :)
        }
        if (gamePlayer.getShip().size() == 5){ // If 5 ships have already been placed
            return new ResponseEntity<>(makeMap("error", "You have already placed ships for this game"), HttpStatus.FORBIDDEN); //403 Works! :)
        }
        List<Long> newShipIds = new ArrayList<>(); //this makes the variable 'initialized'
        //for loop for List of 'ships' to get all the ships added
        for (Ship ship : ships) {
            String name = ship.getType();
            List<String> locations = ship.getLocations();
            Ship shipNew = shRepo.save(new Ship(name, gamePlayer, locations)); //this saves each ship
            Long shipId = shipNew.getId();
            newShipIds.add(shipId);
        }

        return new ResponseEntity<>(makeMap("shipIds", newShipIds), HttpStatus.CREATED); //201
    }



/** End of all functions **/
} //Do not delete!! End of function