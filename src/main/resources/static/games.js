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
            var date = ISODateString(data[i].created);
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

  /* use a function for the exact format desired... */
  function ISODateString(date){
      function pad(n){return n<10 ? '0'+n : n}
      return date.year +'-'
      + pad(date.monthValue)+'-'
      + pad(date.dayOfMonth)+'@'
      + pad(date.hour)+':'
      + pad(date.minute)+':'
      + pad(date.second)
  }

  var d = new Date();
  console.log(ISODateString(d)); // prints something like 2009-09-28T19:03:12Z
// get games data
var object;


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