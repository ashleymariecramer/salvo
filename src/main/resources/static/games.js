$(function() {

//Main functions:
   loadCurrentUser();
   loadGameData();
   loadLeaderBoard();
   activateUserAccountFunctions();
   activateGameFunctions();


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
          hideJoinGameForFullGames();
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
            $("#login_form").show(); //show login
            $("#logout_form").hide(); //hides logout button
            $("#currentUser").append("<h3 class='warning'>" + "Log in to see the games you are playing in" + "</h3>");
            $("#create_game").hide();
        }
        else{
            $("#login_form").hide(); //hides login
            $("#logout_form").show(); //shows logout button
            $("#currentUser").append("<h2>" + "Hi there " + "<b>" + data.loggedInPlayer.nickname + "</b>" + "</h2>");
            if (data.games.length < 1){
                 $("#currentUser").append("<h4>" + "Time to start playing!");
            }
            else{
                 $("#currentUser").append("<h4>" + "Here are your games: ");
                    for (var i = 0; i < data.games.length ; i++){
                        var score = data.games[i].you.player.score;
                        var status = "completedGame";
                        if (data.games[i].you.player.score < 0.5){
                        score = "over: You lost";}
                        if (data.games[i].you.player.score > 0.5){
                        score = "over: You won!";}
                        if (data.games[i].you.player.score == 0.5){
                        score = "over: You tied";}
                        else{
                        score = "still in play";
                        status = "inPlay"}
                        var opponentNickname = "No-one (awaiting opponent)";
                        if (data.games[i].opponent != undefined){
                            opponentNickname = data.games[i].opponent.player.nickname};
                        $("#currentUser").append('<a '
                        + ' data-gameId=' + data.games[i].gameId
                        + ' data-gpId=' + data.games[i].you.gamePlayerId
//                        + ' data-opponentGpId=' + data.games[i].opponent.gamePlayerId
                        + ' href="/game.html?gp=' + data.games[i].you.gamePlayerId // btn-lg active
                        + '" class="btn ' + status + '" role="button">'
                        + 'Game ' + data.games[i].gameId
                        + '<br> You vs ' + opponentNickname
                        + '<br> Game ' + score +
                        '</a>');
                    }
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
                  return gp.player.nickname;
              });
              game.usernames = gameData.gamePlayers.map(function(gp) {
                  return gp.player.username;
              });
              $("#output").append("<li>" + "Game " + game.gameId + ", Players: " + "<b>" + game.players + "</b>" + ", Created on: " + game.created
              + "<button class='join_game' data-game-id='" + game.gameId + "' data-num-players='" + game.usernames.length
              + "' data-usernames='" + game.usernames + "'>Join game</button>" + "</li>");
           });
    }

  //get data from JSON
  function scoresMap(data) {
         data.map(function(scoreData) {//two layers because we are looping through data to get each entry
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

//Groups together all the  functions related to creating, accessing user accounts
  function activateUserAccountFunctions(){
    logIn();
    logOut();
    showCreateAccountForm();
    signUp();
  }

  //** Login **
  function logIn() {
     $("#login_button").click(function(evt) {
        evt.preventDefault(); //used with forms to prevent them getting submitted automatically - used with 'onsubmit="return false"' in html
        var form = evt.target.form; //this is needed later to gets the values from the form
        $.post("/api/login",
               { username: form["username"].value,
                 password: form["password"].value })
         .done(function() {
            console.log("logged in!"); //to check login has worked
            location.reload();//Refreshes page to update with logged in user
            })
         .fail(function(jqXHR, textStatus, errorThrown) {
              alert('Booh! Wrong credentials, try again!');
              })
      });
  }

  //** Logout **
  function logOut() {
    $("#logout_button").click(function(evt) {
    evt.preventDefault(); //used with forms to prevent them getting submitted automatically - used with 'onsubmit="return false"' in html
    var form = evt.target.form; //this is needed later to gets the values from the form
    $.post("/api/logout")
     .done(function() {
        console.log("logged out!"); //to check login has worked
        location.reload();//Refreshes page to update with logged in user
        })
        .fail(function() {
            alert('Booh! Something went wrong with the logout. Please try again!');
        })
    });
  }

  //** Show New User account creation form **
  function showCreateAccountForm() {
    $("#create_account_button").click(function(evt) {
        $("#login_form").hide(); //hides login
        $("#logout_form").hide(); //hides logout button
        $("#signup_form").show(); //shows sign up
    });
  }

  //** Sign up  **
  function signUp() {
        $("#signup_button").click(function(evt) {
        evt.preventDefault(); //used with forms to prevent them getting submitted automatically - used with 'onsubmit="return false"' in html
        var form = evt.target.form; //this is needed later to gets the values from the form
        if (validateForm() == true) {
                $.post("/api/players",
                       { nickname: form["nickname"].value,
                         username: form["username"].value,
                         password: form["password"].value })
                 .done(function() {
                    console.log("new account created!"); //to check login has worked
                    location.reload();//Refreshes page to update with logged in user
                    })
                 .fail(function(jqXHR, textStatus, errorThrown) {
                      alert("Oops, seems there was a problem! Please try again");
                      })
        }
        });
  }

  //Groups functions related to creating and joining games
  function activateGameFunctions() {
    createNewGame();
    joinGame();
  }

    //** Create new game  **
    function createNewGame() {
        $("#create_game").click(function(evt) {
            $.post("/api/newGame")
             .done(function() {
                  console.log("new game created!"); //to check login has worked
                  location.reload();//Refreshes page to update with logged in user
                  })
             .fail(function(jqXHR, textStatus, errorThrown) {
                  alert("Oops, seems there was a problem! Please try again");
                  })
        });
    }

    //** Join game  **
    function joinGame() {
         $("body").on("click",".join_game",function(){
            var gameId = $(this).attr("data-game-id");
            $.post("/api/games/"+ gameId + "/players")
             .done(function(data) { //putting 'data' here gets whatever was returned as JSON body in response entity
                  console.log("game " + gameId + " joined!"); //to check login has worked
                  var gpId = data.gamePlayerId;
                  var url = 'game.html?gp='+ gpId;
                  location.assign(url);//Takes user to game view for new game
                  //Need to obtain the new gamePlayer id and redirect to that page
                  })
             .fail(function(data, jqXHR, textStatus, errorThrown) {
                  alert(data.responseJSON.error); //links the with error message texts we put in the controller so it shows the specific error
                  })
        });
    }


    //** Hide "Join game" button if game is full **
    function hideJoinGameForFullGames() {
        $('button[class="join_game"][data-num-players=2]').hide();
        // hides buttons, with class "join_game" & data-num-players with a value of 2
    }




 //** Validate form for signup **
//check all fields are filled in and email has correct format
function validateForm() {
    var name = document.forms["signup_form"]["nickname"].value;
        if (name == "") {
            alert("Nickname must be filled out");
            return false;
        }
    var email = document.forms["signup_form"]["username"].value;
        if (email == "") {
            alert("Username(email) must be filled out");
            return false;
            }
    var pass = document.forms["signup_form"]["password"].value;
        if (pass == "") {
            alert("Password must be filled out");
            return false;
        }
    var atpos = email.indexOf("@");
    var dotpos = email.lastIndexOf(".");
        if (atpos<1 || dotpos<atpos+2 || dotpos+2>=email.length) {
            alert("Not a valid e-mail address");
            return false;
        }
    return true;
}