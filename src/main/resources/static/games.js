$(function() {

//Main functions:
   loadData();


  // display text in the output area
  function showOutput(text) {
        $("#output").text(text);
    }

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