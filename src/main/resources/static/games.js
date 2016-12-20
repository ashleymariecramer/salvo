$(function() {

//Main functions:
   loadData();


  // display text in the output area
  function showOutput(text) {
        $("#output").text(text);
    }

  function addListItem(data) {
        for (var i = 0; i < data.length; i++){
            var id = data[i].gameId;
            var date = new Date(data[i].created);
            var player1 = data[i].gamePlayers[0].player.username;
            var player2;
            if(data[i].gamePlayers[1] == undefined){
                player2 = "";}
            else {
                player2 = " & " + data[i].gamePlayers[1].player.username;
                }
            $("#output").append("<li>" + "game id: " + id + ", created on: " + date + ", played by: " + player1 + player2 + "</li>");
    }
  }


  function loadData() {
    $.getJSON("/api/games")
    .done(function(data) {

          addListItem(data);
          })
    .fail(function( jqXHR, textStatus ) {
      showOutput( "Failed: " + textStatus );
    });
  }





});