$(function() {

//Main functions:
   loadData();


  // display text in the output area
  function showOutput(text) {
        $("#output").text(text);
    }

//  function addListItem(data) {
//        for (var i = 0; i < data.length; i++){
//            var id = data[i].gameId;
//            var date = new Date(data[i].created);
//            var player1 = data[i].gamePlayers[0].player.username;
//            var player2;
//            if(data[i].gamePlayers[1] == undefined){
//                player2 = "";}
//            else {
//                player2 = " & " + data[i].gamePlayers[1].player.username;
//                }
//            $("#output").append("<li>" + "game id: " + id + ", created on: " + date + ", played by: " + player1 + player2 + "</li>");
//    }
//  }

  function testMap(data) {
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
          testMap(data);
          })
    .fail(function( jqXHR, textStatus ) {
      showOutput( "Failed: " + textStatus );
    });
  }





});