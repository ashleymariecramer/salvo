$(function() {

//Main functions:
   buildGrid();
   loadData();

});


//  ***  On event functions  ***  //

  //** Logout **
    $("#logout_button").click(function(evt) {
    evt.preventDefault(); //ASK: Is this necessary - doesn't seem to do anything????
    var form = evt.target.form; //this is needed later to gets the values from the form
    $.post("/api/logout")
     .done(function() {
        console.log("logged out!"); //to check login has worked
        //window.location.replace("/games.html");
        $(location).attr('href', '/games.html'); //Takes logged out user back to games page
        })
     .fail(function() {
          alert('Booh! Something went wrong with the logout. Please try again!');
      })
  });


//  ***   Auxiliary functions   ***  //

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
            var cell = '<td ' + 'class="'+ gridRef + ' key"' + '>' + gridRef + '</td>';
          } else {
            var cell = '<td ' + 'class="'+ gridRef + '"' + '>' + '</td>';
            }
          colStr += cell;
      }
      rows.push('<tr>' + colStr + '</tr>');
    }
    document.getElementById("ownGrid").innerHTML += rows.join("");
    document.getElementById("opponentGrid").innerHTML += rows.join("");
    $("#logout_form").show(); //shows logout button

  }


  // This gets the gp query value from the url. document.location.search gives the query e.g. "?gp=1"
  // document.location.search.substr(1) returns the parameter & its value without the & e.g. "gp=1"
  //TODO: need to changes this to get game player from game number and username the same as logged in
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
//         data.map(function(gameData) {
            var game = {};
            game.id = data.gameView.gameId;
            game.created = new Date(data.gameView.created);
            game.you = data.you.player.nickname;
            game.opp = data.opponent.player.nickname;
         $("#output").html("<h2>" + "You (<b>" + game.you + "</b>) are playing against <b>" + game.opp + "</b></h2>" +
         "<h3><i>"+"Game: " + game.id +", Created on: " + game.created + "</i></h3>");
//     });
    }

//ajax call to the api to get the JSON data - if successful it uses data to draw the game view if not it returns an error
  function loadData() {
    var gp = getGamePlayerIdFromURL(); //gets the gamePlayer(gp) id number from the url
    var url = "/api/game_view/"+gp; //inserts the gp id number into the api
    $.getJSON(url)
    .done(function(data) {
          gameView(data);
          drawOwnShipLocations(data);
          locateOpponentShipLocations(data);
          drawOwnSalvoLocations(data);
          locateOpponentSalvoLocations(data);
          determineHits();
          })
    .fail(function( jqXHR, textStatus ) {
      showOutput( "Failed: " + textStatus );
    });
  }

//method to draw players own ships on their grid
  function drawOwnShipLocations(data){
        for (var i = 0; i < data.yourShips.length; i++){
            for (var j = 0; j < data.yourShips[i].locations.length; j++){
                var location = data.yourShips[i].locations[j];
                $("#ownGrid > tr > td."+location).addClass("ship"); //only adds ships to own grid
//              $("#ownGrid").children().children("."+location).addClass("ship"); //Alternative for >1 grids
                }
            }
  }
//method to draw opponents ships on their grid
  function locateOpponentShipLocations(data){
        for (var i = 0; i < data.opponentShips.length; i++){
            for (var j = 0; j < data.opponentShips[i].locations.length; j++){
                var location = data.opponentShips[i].locations[j];
                $("#opponentGrid > tr > td."+location).addClass("shipHidden"); //only adds ships to own grid
                }
            }
  }

//method to draw players own salvos on opponent grid
  function drawOwnSalvoLocations(data){
        for (var i = 0; i < data.yourSalvoes.length; i++){
            for (var j = 0; j < data.yourSalvoes[i].locations.length; j++){
                var location = data.yourSalvoes[i].locations[j];
                var turn = data.yourSalvoes[i].turn;
                $("#opponentGrid > tr > td."+location).addClass("mySalvoes").html(turn); //only adds ships to own grid
//              $("#ownGrid").children().children("."+location).addClass("ship"); //Alternative for >1 grids
                }
            }
  }
//method to draw opponents salvos on own grid
  function locateOpponentSalvoLocations(data){
        for (var i = 0; i < data.opponentSalvoes.length; i++){
            for (var j = 0; j < data.opponentSalvoes[i].locations.length; j++){
                var location = data.opponentSalvoes[i].locations[j];
                var turn = data.yourSalvoes[i].turn;
                $("#ownGrid > tr > td."+location).addClass("oppSalvoes").html(turn); //only adds ships to own grid
                }
            }
  }

//method to determine if salvos have hit any ships
function determineHits(){
    $(".shipHidden.mySalvoes").removeClass("shipHidden mySalvoes").addClass("hit");
    $(".ship.oppSalvoes").removeClass("ship oppSalvoes").addClass("hit");
}

