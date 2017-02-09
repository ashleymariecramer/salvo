package edu.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.stream.Collectors.toList;


@Service
public class SalvoService {

    @Autowired
    private GamePlayerRepository gpRepo;
    @Autowired
    private GameScoreRepository gsRepo;
    @Autowired
    private PlayerRepository pRepo;


    public Map<String, Object> makeGameDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<>();
        long gameId = game.getId();
        dto.put("gameId", game.getId());
        dto.put("created", game.getCreationDate());
        dto.put("gamePlayers", game.getGamePlayers().stream().map(gamePlayer -> makeGamePlayerDTO(gamePlayer, gameId))
                .collect(toList()));
        //here we need stream because there are more than one game player per game
        return dto;
    }

    //for each gameplayer it returns their id and player details
    public Map<String, Object> makeGamePlayerDTO(GamePlayer gamePlayer, long gameId) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("gamePlayerId", gamePlayer.getId());
        dto.put("player", makePlayerDTO(gamePlayer.getPlayer(), gameId)); //don´t need to loop here cos a game player only has one player
        return dto;
    }

    // the gameplayers details include their playerId, username(email), nickname and score(if game is finished)
    private Map<String, Object> makePlayerDTO(Player player, long gameId) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("playerId", player.getId());
        dto.put("username", player.getUsername());
        dto.put("nickname", player.getNickname());
        dto.put("score", player.getGameScores().stream().filter(gs -> gs.getGame().getId() == gameId).findFirst()
                .map(g -> g.getScore()).orElse(null));
        //adding score here makes it clear who the score belongs to
        return dto;
    }

    /* this returns the players username or 'guest' (if no one logged in) */
    public String getUsername(Authentication authentication) {
        if (!isGuest(authentication)) { //This checks there is not a guest user
            String loggedInUser = pRepo.findByUsername(authentication.getName()).getUsername();
            return loggedInUser;
        } else {
            String loggedInUser = "guest";
            return loggedInUser;
        }
    }

    public boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
        //this checks if authentication is null or is an instance the predefined spring security class "AnonymousAuthenticationToken"
    }

    public Map<String, Object> makeGuestUserDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("player", "guest");
        return dto;
    }

    public Map<String, Object> makeUserDTO(Player player, Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("loggedInPlayer", makeLoggedInPlayersDetailsDTO(player)); // don´t need to loop here cos a game player only has one player
        dto.put("games", player.getGamePlayers().stream().map(gp -> gp.getId())
                .map(gpId -> makePlayersGameDetailsDTO(gpId, authentication)).collect(toList()));
        return dto;
    }

    private Map<String, Object> makeLoggedInPlayersDetailsDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<>();
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
        if (playerId == loggedInUser) {//if player id for gamePlayer & logged are the same -> return game view
            return makePlayersGamesViewsDTO(gamePlayer, gamePlayerId, gameId);
        } else {
            Map<String, Object> result = makeMap("Error",
                    new ResponseEntity<>("Sorry, you are not a player in this game", HttpStatus.UNAUTHORIZED));
            return result;
        }
    }

    private Map<String, Object> makePlayersGamesViewsDTO(GamePlayer gamePlayer, Long gamePlayerId, Long gameId) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("gameId", gamePlayer.getGame().getId());
        dto.put("you", gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() == gamePlayerId)
                .findFirst().map(gp -> makeGamePlayerDTO(gp, gameId)).get());

        Optional<Map<String, Object>> opponent = gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gamePlayerId)
                .findFirst().map(gp -> makeGamePlayerDTO(gp, gameId));
        if (opponent.isPresent()) {
            dto.put("opponent", opponent.get()); // findFirst returns an Optional, as it could be that there is no first to find
            //So for this reason we can add the conditional to see if it isPresent (ie. has a value) before executing the code
        } //if there is no optional then this part of the code is skipped

        return dto;
    }

    public Map<String, Object> makeGameViewDTO(GamePlayer gamePlayer, Long gamePlayerId, Long gameId) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();

        dto.put("gameView", makeGameDetailsDTO(gamePlayer.getGame()));
        dto.put("you", gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() == gamePlayerId)
                .findFirst().map(gp -> makeGamePlayerDTO(gp, gameId)).get());
        dto.put("yourShips", gamePlayer.getShip().stream().map(ship -> makeShipDTO(ship)).collect(toList()));
        dto.put("yourSalvoes", gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() == gamePlayerId)
                .findFirst().get().getSalvo().stream().map(salvo -> makeSalvoDTO(salvo)).collect(toList()));

        Optional<Map<String, Object>> opponent = gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gamePlayerId)
                .findFirst().map(gp -> makeGamePlayerDTO(gp, gameId));
        if (opponent.isPresent()) {
            dto.put("opponent", opponent.get());
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

    public Map<String, Object> makeScoreStatsDTO(GameScore gameScore) {
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
        } else if (result.equals("lost")) {
            dto.put("lost", 1);
        } else {
            dto.put("tied", 1);
        }
        return dto;
    }

    public String returnResult(double score) {
        if (score < 0.5) {
            return "lost";
        } else if (score > 0.5) {
            return "won";
        } else {
            return "tied";
        }
    }

    public Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    public Map<String, Object> makeTurnStatsDTO(Salvo salvoYou, Set<Salvo> salvoOpps, List hitsOverallYou, List hitsOverallOpp,
                                                List previouslySunkYou, List previouslySunkOpp,
                                                Integer sunkShipsYou, Integer sunkShipsOpp) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn", salvoYou.getTurn());
        int turn = salvoYou.getTurn();
        dto.put("hitsOnOpp", makeHitStatsDTO(salvoYou, hitsOverallYou, sunkShipsYou, previouslySunkYou));

        Optional<Salvo> optionalSalvoes = salvoOpps.stream().filter(s -> s.getTurn() == turn).findFirst();
        if (optionalSalvoes.isPresent()) {
            dto.put("hitsOnYou", makeHitStatsDTO(optionalSalvoes.get(), hitsOverallOpp, sunkShipsOpp, previouslySunkOpp));
        }

        return dto;
    }

    //this passes through all the data of one players salvos and the opposing player ships
    private Map<String, Object> makeHitStatsDTO(Salvo salvo, List hitsOverall, Integer sunkShips, List previouslySunkShips) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        Long gpId = salvo.getGamePlayer().getId();
        List<String> hitsPerTurn = new ArrayList<>();
        List<String> sunkShipList = new ArrayList();
        List<String> mySalvoLocations = salvo.getLocations();
        Set<Ship> oppShips = salvo.getGamePlayer().getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gpId)
                .findFirst().get().getShip();

        for (String mySalvoLocation : mySalvoLocations) {
            for (Ship oppShip : oppShips) {
                String type = oppShip.getType();
                List shipLocations = oppShip.getLocations();
                if (shipLocations.contains(mySalvoLocation)) { //check if ship locations contains same location as the salvo
                    hitsPerTurn.add(type); //if so push ship type to the hits array for this turn
                    hitsOverall.add(type); //if so push ship type to the hits array for all turns so far
                }
            }
        } //ASK: How can this be written as a stream????
        if (Collections.frequency(hitsOverall, "aircraftCarrier") == 5) {
            sunkShips += 1;
            if (previouslySunkShips.contains("aircraftCarrier") == false){
                sunkShipList.add(" Aircraft Carrier");
                previouslySunkShips.add("aircraftCarrier");
            }
        }
        if (Collections.frequency(hitsOverall, "battleship") == 4) {
            sunkShips += 1;
            if (previouslySunkShips.contains("battleship") == false) {
                sunkShipList.add(" Battleship");
                previouslySunkShips.add("battleship");
            }
        }
        if (Collections.frequency(hitsOverall, "submarine") == 3) {
            sunkShips += 1;
            if (previouslySunkShips.contains("submarine") == false) {
                sunkShipList.add(" Submarine");
                previouslySunkShips.add("submarine");
            }
        }
        if (Collections.frequency(hitsOverall, "destroyer") == 3) {
            sunkShips += 1;
            if (previouslySunkShips.contains("destroyer") == false) {
                sunkShipList.add(" Destroyer");
                previouslySunkShips.add("destroyer");
            }
        }
        if (Collections.frequency(hitsOverall, "patrolBoat") == 2) {
            sunkShips += 1;
            if (previouslySunkShips.contains("patrolBoat") == false) {
                sunkShipList.add(" Patrol Boat");
                previouslySunkShips.add("patrolBoat");
            }
        }

        dto.put("shipsLeft", 5 - sunkShips); //TODO: remove this and use 5 - sunkShipsList.size
        dto.put("shipsSunk", sunkShipList);
        dto.put("hitsPerTurn", makeTurnHitsDTO(hitsPerTurn));
        return dto;
    }


    //make new object to record only the ships hit per turn
    private Map<String, Object> makeTurnHitsDTO(List hitsPerTurn) {
        Map<String, Integer> shipsHit = new LinkedHashMap<String, Integer>();
        for (Object ship : hitsPerTurn) {
            String type = ship.toString();
            if (shipsHit.containsKey(type)){ //if ship name is already included in the 'shipsHit' array
                int existingVal = shipsHit.get(type); //get the number if hits
                int newVal = existingVal + 1; // increase them by 1
                shipsHit.remove(type);
                shipsHit.put(type, newVal); //
            } else {
                shipsHit.put(type, 1);
            }
        }

        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("shipsHit", shipsHit);
        return dto;
    }




}
