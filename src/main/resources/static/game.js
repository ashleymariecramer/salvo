$(function() {

//Main functions:
   buildGrid();
   loadData();


});

//Auxiliary functions

//builds a 11 x 11 grid where each cell has a unique id identifying it.
//in this same method I can also add key or board class and only show text for key class
//based on combined length of letter and number)

  function buildGrid() {
    var rows = [];
    var colStr = null;
    for(var j = 0; j < 11; j++) {
        colStr = "";
      for (var i = 0; i < 11; i++){
          var characters = ["","A","B","C","D","E","F","G","H","I","J"]; //array for vertical axis - 1st is blank
          var numbers = ["","1","2","3","4","5","6","7","8","9","10"]; //array for horizontal axis - 1st is blank
          var gridRef = characters[i] + numbers[j];
          if (gridRef.length < 2 || gridRef === "10"){
            var cell = '<td  ' + 'id="'+gridRef+'" '+' class="key"' + '>' + gridRef + '</td>';
          } else {
            var cell = '<td  ' + 'id="'+gridRef+'" '+' class="board"' + '>' + '</td>';
            }
          colStr += cell;
      }
      rows.push('<tr>' + colStr + '</tr>');
    }
    document.getElementById("grid").innerHTML += rows.join("");

  }


  // This gets the gp query value from the url. document.location.search gives the query e.g. "?gp=1"
  // document.location.search.substr(1) returns the parameter & its value without the & e.g. "gp=1"
  function getGamePlayerIdFromURL(){
      var query = document.location.search.substr(1).split('=') //e.g. takes "gp=1" & splits into ["gp", "1"]
      var gp = query[1];
      return gp;
    }

  // display text in the output area
  function showOutput(text) {
    $("#output").text(text);
  }

  //get data from JSON and create a new variable which contains the game Id, creation date and players and present this in a string
  function gameView(data) {
         data.map(function(gameData) {
            var game = {};
            game.id = gameData.GameView.gameId;
            game.created = new Date(gameData.GameView.created);
            game.you = gameData.You.map(function(you) {
                return you.player.nickname;
            });
            game.opp = gameData.Opponent.map(function(opp) {
                return opp.player.nickname;
            });
            $("#output").html("<h2>"+"Game Id: " + game.id + "</h2>" + "<h3>"+"Created on: " + game.created + "</h2>" + "<h3>"+ "You (" + game.you + ") are playing against " + game.opp + "</h3>");

     });
    }

//ajax call to the api to get the JSON data - if successful it uses data to draw the game view if not it returns an error
  function loadData() {
    var gp = getGamePlayerIdFromURL(); //gets the gamePlayer(gp) id number from the url
    var url = "/api/game_view/"+gp; //inserts the gp id number into the api
    console.log(url);
    $.getJSON(url)
    .done(function(data) {
          gameView(data);
          })
    .fail(function( jqXHR, textStatus ) {
      showOutput( "Failed: " + textStatus );
    });
  }




