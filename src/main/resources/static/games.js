$(function() {

//Main functions:
   loadData();


//Auxiliary Functions

  // display text in the output area
  function showOutput(text) {
        $("#output").text(text);
    }

  //get data from JSON and create a new variable which contains the game Id, creation date and players and present this in a string
  function GamesMap(data) {
         data.map(function(gameData) {
            var game = {};
            game.gameId = gameData.gameId;
            game.created = new Date(gameData.created);
            game.players = gameData.gamePlayers.map(function(gp) {
                return gp.player.username;
            });
            $("#output").append("<li>" + "Id: " + game.gameId + ", Created on: " + game.created + ", Players: " + game.players + "</li>");
         });
  }

//ajax call to the api to get the JSON data - if successful it uses data to draw a list of games if not it returns an error
  function loadData() {
    $.getJSON("/api/games")
    .done(function(data) {
          GamesMap(data);
          })
    .fail(function( jqXHR, textStatus ) {
      showOutput( "Failed: " + textStatus );
    });
  }





});