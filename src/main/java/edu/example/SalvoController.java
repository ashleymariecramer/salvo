package edu.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Autowired //this injects an instance of the classes to be used by this controller (Dependency Injection)
    private GameRepository repo;
    @Autowired
    private GamePlayerRepository gpRepo;
    @Autowired
    private GameScoreRepository gsRepo;
    @Autowired
    private PlayerRepository pRepo;
    @Autowired
    private SalvoService salvoService;
    @Autowired
    private ValidationService validationService;

    /****************************** API /GAMES **************************************/
    //1. List of All games to be shown whether user logged in or not
    @RequestMapping(path = "/games", method = RequestMethod.GET)
    public List<Object> getAllGames() {
        return repo.findAll().stream().map(game -> salvoService.makeGameDTO(game)).collect(toList());
    }


    /******************************* API /CURRENT USER GAMES ********************************************/
    @RequestMapping(path = "/currentUserGames", method = RequestMethod.GET)
    public Map<String, Object> getUserGames(Authentication authentication) {
        if (!salvoService.isGuest(authentication)) { //This checks there is not a guest user
            Player loggedInUser = pRepo.findByUsername(authentication.getName());
            return salvoService.makeUserDTO(loggedInUser, authentication);
        }
        return salvoService.makeGuestUserDTO();
    }


    /*********************************** API /GAME VIEW ****************************************/
    //3. Game View based on gameplayerId
    //passing gameId as a parameter allows individual scores to be added to game view for completed games
    @RequestMapping("/game_view/{gamePlayerId}")
    public Map<String, Object> getGamesbyPlayer(@PathVariable Long gamePlayerId, Authentication authentication) {
        GamePlayer gamePlayer = gpRepo.findOne(gamePlayerId);
        long gameId = gamePlayer.getGame().getId();
        String playerId = gamePlayer.getPlayer().getUsername();
        String loggedInUser = salvoService.getUsername(authentication);
        if (playerId == loggedInUser) {//if player id for gameplayer & logged are the same -> return game view
                return salvoService.makeGameViewDTO(gamePlayer, gamePlayerId, gameId);
            } else {
                Map<String, Object> result = salvoService.makeMap("Error", new ResponseEntity<String>("Sorry, you are not a player in this game", HttpStatus.UNAUTHORIZED));
                return result;
        }
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
                String result = salvoService.returnResult(gameScore.getScore());
                if (result.equals("won")) {
                    won = (Integer) playerScore.get("won");
                    won++;
                } else if (result.equals("lost")) {
                    lost = (Integer) playerScore.get("lost");
                    lost++;
                } else {
                    tied = (Integer) playerScore.get("tied");
                    tied++;
                }

                playerScore.put("won", won);
                playerScore.put("lost", lost);
                playerScore.put("tied", tied);

            } else { //If no player exists for this id then create initial data with playerId, nickname, and scores
                scoreStats.put(id, salvoService.makeScoreStatsDTO(gameScore));
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


    /*********************************** API /PLAYERS ****************************************/
    //5. Create new players
    //Because some of the conditions have to return a Map ResponseEntity<Map<String,Object>> then all of them have to
    //@RequestParam is necessary before each parameter
    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createPlayer(@RequestParam String nickname,
                                                            @RequestParam String username,
                                                            @RequestParam String password) {
        Player player = pRepo.findByUsername(username); //gives a 409 Conflict Error

        return validationService.validatePlayer(player, nickname, username, password);

    }


    /*********************************** API /NEW GAME ****************************************/
    //6. Create new game
    @RequestMapping(path = "/newGame", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createGame(Authentication authentication) {
        Player player = pRepo.findByUsername(salvoService.getUsername(authentication));
        Game game = repo.save(new Game(0l));
        GamePlayer gamePlayer = gpRepo.save(new GamePlayer(game, player));

        return new ResponseEntity<>(salvoService.makeMap("gamePlayerId", gamePlayer.getId()), HttpStatus.CREATED);
    }


    /************************* API /JOIN GAME (add player to existing game ********************************/
    //7. Add new player to an existing game, saving new gameplayer id to the gpRepo and updating game in repo too.
    @RequestMapping(path = "/games/{gameId}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> checkGamebyId(@PathVariable Long gameId, Authentication authentication) {

        Player player = pRepo.findByUsername(salvoService.getUsername(authentication)); //to check if player loggedin

        return validationService.validateGame(player, gameId);

    }


    /************************* API /ADD SHIPS (add ships to existing game for specific gameplayer id) ********************************/
    //8. Add ships
    @RequestMapping(path = "/games/players/{gpId}/ships", method = RequestMethod.POST)
    //the ships will be passed as a list from the front end
    public ResponseEntity<Map<String, Object>> addShip(@PathVariable Long gpId, @RequestBody List<Ship> ships, Authentication authentication) {
        GamePlayer gamePlayer = gpRepo.findOne(gpId);
        validationService.verifyGamePlayer(gpId, authentication);

        return validationService.validateShips(gamePlayer, ships);

    }


    /************************* API /ADD SALVOES(add salvoes to existing game for specific gameplayer id) ********************************/
    //9. Add salvoes
    @RequestMapping(path = "/games/players/{gpId}/salvoes", method = RequestMethod.POST)
    //the ships will be passed as a list from the front end
    public ResponseEntity<Map<String, Object>> addSalvo(@PathVariable Long gpId, @RequestBody Salvo salvo, Authentication authentication) {

        GamePlayer gamePlayer = gpRepo.findOne(gpId);
        validationService.verifyGamePlayer(gpId, authentication);
        int turn = gamePlayer.getSalvo().size() + 1; //gets turn  umber based on current no. of salvos saved to repo

        salvo.setGamePlayer(gamePlayer);
        salvo.setTurn(turn);

        return validationService.validateSalvoes(gamePlayer, turn, salvo);

    }



    /************************* API /GAME HISTORY (get details by turn) ********************************/
    //10. Game History
    @RequestMapping(path = "/gameHistory/{gamePlayerId}", method = RequestMethod.GET)
    public Map<String, Object> getGameHistoryByPlayer(@PathVariable Long gamePlayerId, Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        GamePlayer gamePlayer = gpRepo.findOne(gamePlayerId);
        long gameId = gamePlayer.getGame().getId();
        String playerId = gamePlayer.getPlayer().getUsername();
        Long yourPlayerId = gamePlayer.getPlayer().getId();
        String loggedInUser = salvoService.getUsername(authentication);
        if (playerId != loggedInUser) {//if player id for gameplayer & logged are not the same give error
            Map<String, Object> result = salvoService.makeMap("Error", new ResponseEntity<String>("Sorry, you are not a player in this game", HttpStatus.UNAUTHORIZED));
            return result;

        } // TODO: this is duplicated in game view so can be extracted to a new method

            List<String> hitsOverallYou = new ArrayList<>(); //this will have cumulative list of hit ships per game so far
            Integer sunkShipsYou = 0; //This needs to be outside of the turn loops
            List<String> previouslySunkYou = new ArrayList<>(); //This will determine ships sunk on previous turns so not repeated
            List<String> hitsOverallOpp = new ArrayList<>(); //this will have cumulative list of hit ships per game so far
            Integer sunkShipsOpp = 0; //This needs to be outside of the turn loops so that it gets added to each time
            List<String> previouslySunkOpp = new ArrayList<>(); //This will determine ships sunk on previous turns so not repeated

            Set<Salvo> salvoOpps;
            Long opponentPlayerId;

        Optional<GamePlayer> optionalGamePlayer = gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gamePlayerId)
                    .findFirst();
            if (optionalGamePlayer.isPresent()) {
                salvoOpps = optionalGamePlayer.get().getSalvo();
                opponentPlayerId = optionalGamePlayer.get().getPlayer().getId();
            }

            else{
                salvoOpps = null;
                opponentPlayerId = null;
            }

            dto.put("gameId", gameId);
            dto.put("playerId", yourPlayerId);
            dto.put("opponentPlayerId", opponentPlayerId);

            dto.put("hits", gamePlayer.getSalvo().stream().sorted(Comparator.comparing(Salvo::getTurn))
                    .map(salvoYou -> salvoService.makeTurnStatsDTO(salvoYou, salvoOpps,
                            hitsOverallYou, hitsOverallOpp, previouslySunkYou, previouslySunkOpp,
                            sunkShipsYou, sunkShipsOpp)).collect(toList()));

          return dto;
    }


    /************************* API /ADD SCORE (add score to to player repo using player Id & game Id) ********************************/
    //11. Add score to finished game
    @RequestMapping(path = "/players/{playerId}/gameScores", method = RequestMethod.POST)
    //the ships will be passed as a list from the front end
//    public ResponseEntity<Map<String, Object>> addGameScore(@PathVariable Long playerId, @RequestBody Long gameId, @RequestBody double score) {
    public ResponseEntity<Map<String, Object>> addGameScore(@PathVariable Long playerId,
                                                            @RequestParam(value = "gameId") Long gameId,
                                                            @RequestParam(value = "score") Double score) {
        //using RequestParam because with RequestBody can only pass one object
        //ASK: how to pass two separate variables using RequestBody - can make a map string, object but how to get the values back here

        Player player = pRepo.findOne(playerId);
        Game game = repo.findOne(gameId);


        GameScore gameScore = new GameScore(0, game, player, score);
//        GameScore gameScore = new GameScore(0, game, player, score);
        gsRepo.save(gameScore);

        return new ResponseEntity<>(salvoService.makeMap("gameScoreId", gameScore.getId()), HttpStatus.CREATED); //201
    }
//TODO: make it so you can only access your own game scores




/** End of all functions **/
} //Do not delete!! End of function
