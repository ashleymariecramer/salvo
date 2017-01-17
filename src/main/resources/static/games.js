$(function() {

//Main functions:
   loadCurrentUser();
   loadGameData();
   loadLeaderBoard();
});

//Auxiliary Functions

  // display text in the output area
  function showOutput(text) {
        $("#output").text(text);
        $("#leaderBoard").text(text);
        $("#currentUser").text(text);
    }


//ajax call to the api to get the JSON data - if successful it uses data to draw a list of games if not it returns an error
  function loadCurrentUser() {
    $.getJSON("/api/currentUserGames")
    .done(function(data) {
          loggedInUserMap(data);
          })
    .fail(function( jqXHR, textStatus ) {
      showOutput( "Failed: " + textStatus );
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

  //get data from JSON and create a new variable which contains the logged in player's name & their games.
  function loggedInUserMap(data) {
        if (data.player == "guest"){
            $("#currentUser").append("<h3 class='warning'>" + "Log in to see the games you are playing in" + "</h3>");
        }
        else{
            $("#currentUser").append("<h2>" + "Hi there " + "<b>" + data.player.nickname + "</b>" + "</h2>");
                    $("#currentUser").append("<h4>" + "Here are your games: ");
                    for (var i = 0; i < data.games.length; i++){
                        $("#currentUser").append("<button class='games'>" + data.games[i] + "</button>");
                    }
        }

  }

  //get data from JSON and list of all games with game id, creation date, players emails
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
//     .done(...) //TODO: need to define what happens on successful login eg. 'Welcome "name"' or '"name" is logged in.'
//     .fail(...); /TODO: need to define what happens if login fails
//  }

////possible option for failed login - redirects to login page:
//.fail(function (jqXHR, textStatus, errorThrown) {
//   if (jqXHR.status === 401) { // HTTP Status 401: Unauthorized
//            var preLoginInfo = JSON.stringify({method: 'GET', url: '/'});
//            $.cookie('restsecurity.pre.login.request', preLoginInfo);
//            window.location = '/api/login.html';
//
//        } else {
//            alert('Houston, we have a problem...');
//        }
// }
//
//  function logout(evt) {
//    evt.preventDefault();
//    $.post("/api/logout)
//     .done(...) //TODO: need to define what happens on successful logout. 'You are currently logged out"
//     .fail(...); //TODO: need to define what happens if logout fails
//  }



//Code for handling submission process of the login form
//jQuery(document).ready(function ($) {
//    $('#loginform').submit(function (event) {
//        event.preventDefault();
//        var data = 'username=' + $('#username').val() + '&password=' + $('#password').val();
//        $.ajax({
//            data: data,
//            timeout: 1000,
//            type: 'POST',
//            url: '/api/login'
//
//        }).done(function(data, textStatus, jqXHR) {
//            var preLoginInfo = JSON.parse($.cookie('dashboard.pre.login.request'));
//            window.location = preLoginInfo.url;
//
//        }).fail(function(jqXHR, textStatus, errorThrown) {
//            alert('Booh! Wrong credentials, try again!');
//        });
//    });
//});