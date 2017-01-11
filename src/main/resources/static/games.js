$(function() {

//Main functions:
   loadGameData();
   loadLeaderBoard()
});

//Auxiliary Functions

  // display text in the output area
  function showOutput(text) {
        $("#output").text(text);
        $("#leaderBoard").text(text);
    }

  //get data from JSON and create a new variable which contains the game Id, creation date and players and present this in a string
  function gamesMap(data) {
         data.map(function(gameData) {
            var game = {};
            game.gameId = gameData.gameId;
            game.created = new Date(gameData.created);
            game.players = gameData.gamePlayers.map(function(gp) {
                return gp.player.username;
            });
            $("#output").append("<li>" + "Game " + game.gameId + ": Created on: " + game.created + "<br>" + "Players: " + "<b>" + game.players + "</b>" + "</li>");
         });
  }

//ajax call to the api to get the JSON data - if successful it uses data to draw a list of games if not it returns an error
  function loadGameData() {
    $.getJSON("/api/games")
    .done(function(data) {
          gamesMap(data);
          })
    .fail(function( jqXHR, textStatus ) {
      showOutput( "Failed: " + textStatus );
    });
  }

//ajax call to the api to get the JSON data - if successful it uses data to create a leaderboard if not it returns an error
    function loadLeaderBoard() {
      $.getJSON("/api/scores")
      .done(function(data) {
            scoresMap(data);
            })
      .fail(function( jqXHR, textStatus ) {
        showOutput( "Failed: " + textStatus );
      });
    }


  //get data from JSON and create a new variable which contains the game Id, creation date and players and present this in a string
  function scoresMap(data) {
         data.map(function(scoreData) {
            var player = {};
            player.nickname = scoreData.nickname;
            player.score = scoreData.score;
            player.won = scoreData.won;
            player.lost = scoreData.lost;
            player.tied = scoreData.tied;
            $("#leaderBoard").append("<tr>" + "<td>" + player.nickname + "</td>"
                                            + "<td>" + player.score + "</td>"
                                            + "<td>" + player.won + "</td>"
                                            + "<td>" + player.lost + "</td>"
                                            + "<td>" + player.tied + "</td>"
                                     + "</tr>");
         });

  }



  //TODO: JS for login - keeping username as it is required in the Application class and not sure whether it should be the same here
//  function login(evt) {
//    evt.preventDefault();
//    var form = evt.target.form;
//    $.post("/login",
//           { username: form["username"].value,
//             password: form["password"].value })
//     .done(...)
//     .fail(...);
//  }
//
//  function logout(evt) {
//    evt.preventDefault();
//    $.post("/logout)
//     .done(...)
//     .fail(...);
//  }
