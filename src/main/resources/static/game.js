
$(function() {

//Main functions:
   buildGrid();
   loadData();
   addShips();
   makeElementsDraggable();
   rotateShips();
   getPlacedShipDetails();
   getSalvoPositions();
//   loadGameHistoryData();
});

/* Global variable */
var allShipDetails = [];

// ***  On event functions  ***  //

  //** Logout **
    $("#logout_button").click(function(evt) {
    evt.preventDefault(); //used with forms to prevent them getting submitted automatically - used with 'onsubmit="return false"' in html
    var form = evt.target.form; //this is needed later to gets the values from the form
    $.post("/api/logout")
     .done(function() {
        console.log("logged out!"); //to check login has worked
        location.assign('/games.html');
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
            var cell = '<td ' + 'data-grid="'+ gridRef + '"' +
                                'data-gridOpp="'+ gridRef + '"' +
                                ' class="'+ gridRef + '"' + '>' + '</td>';
            }
          colStr += cell;
      }
      rows.push('<tr>' + colStr + '</tr>');
    }
    document.getElementById("ownGrid").innerHTML += rows.join("");
    document.getElementById("opponentGrid").innerHTML += rows.join("");
    removeDataGridAttribute();
    $("#logout_form").show(); //shows logout button

  }

//
function removeDataGridAttribute(){
    $("#opponentGrid > tr > td").removeAttr("data-grid"); //removes data-grid from opponents grid so ships can't be placed there
    $("#ownGrid > tr > td").removeAttr("data-gridOpp"); //removes data-gridOPP from own grid so salvo grid ref is not linked to class

}

  // This gets the gp query value from the url. document.location.search gives the query e.g. "?gp=1"
  // document.location.search.substr(1) returns the parameter & its value without the & e.g. "gp=1"
  function getGamePlayerIdFromURL(){
      var query = document.location.search.substr(1).split('=') //e.g. takes "gp=1" & splits into ["gp", "1"]
      var gp = query[1];
      return gp;
    }

//ajax call to the api to get the JSON data - if successful it uses data to draw the game view if not it returns an error
  function loadData() {
    var gp = getGamePlayerIdFromURL(); //gets the gamePlayer(gp) id number from the url
    var url = "/api/game_view/"+gp; //inserts the gp id number into the api
    $.getJSON(url)
    .done(function(data) {
    // this checks if user is trying to access a gameView for a game they are not a player in
    // if they are not a player then instead of the gameView data there will be an Error object generated by the controller
    // with an error status 403 and returns the body error message as an alert
    if (data.gameView == undefined){
          alert(data.Error.body);
        }
    else{
          gameView(data);
          drawOwnShipLocations(data);
          drawOwnSalvoLocations(data);
          gameHistory(data);
          if (data.opponent != undefined) { //if there's no opponent do not add their ship and salvo data to the grids
                locateOpponentSalvoLocations(data);
                determineHitsOnOpp(data); //TODO: this should check in the same step if a oppship  & own salvo have same location and if so add 'hit'
          }
          determineHitsOnYou();
        }
    })
    .fail(function( jqXHR, textStatus ) {
      showOutput( "Failed: " + textStatus );
    });
  }

  // display text in the output area
  function showOutput(text) {
    $("#output").text(text);
  }

  //get data from JSON and create a new variable which contains the game Id, creation date and players and present this in a string
    function gameView(data) {
          var game = {};
          game.id = data.gameView.gameId;
          game.created = new Date(data.gameView.created);
          game.you = data.you.player.nickname;
          if (data.opponent != undefined) { //if there is an opponent fill in their details
              opponent = data.opponent.player.nickname;
              $("#output").html("<h2>" + "You (<b>" + game.you + "</b>) are playing against <b>" + opponent + "</b></h2>" +
              "<h3><i>"+"Game: " + game.id +", Created on: " + game.created + "</i></h3>");
          }
          else { //if there's no opponent
              $("#output").html("<h2 class='warning'>" + "Game awaiting Opponent</h2>" +
              "<h3><i>"+"Game: " + game.id +", Created on: " + game.created + "</i></h3>");
          }
      }

    //get data from JSON and create a new variable which contains the game Id, creation date and players and present this in a string
    function gameHistory(data) {
    if (data.hits == undefined ){
        $(".gameHistory").hide(); //if no game history available hide gameHistory table
    }
    else {
     var turn = {};
             data.hits.map(function(turnData) {
                turn.number = turnData.turn;
                turn.sunkYou = turnData.hitsOnYou.shipsSunk;
                turn.leftYou = turnData.hitsOnYou.shipsLeft;
                turn.sunkOpp = turnData.hitsOnOpp.shipsSunk;
                turn.leftOpp = turnData.hitsOnOpp.shipsLeft;
                turn.shipsHitYou = returnShipsHit(turnData.hitsOnYou.hitsPerTurn.shipsHit);
                turn.shipsHitOpp = returnShipsHit(turnData.hitsOnOpp.hitsPerTurn.shipsHit);

                $("#gameHistory").append("<tr>" + "<td class='turn'>" + turn.number + "</td>"
                                                + "<td class='youContent'>" + turn.shipsHitYou + "</td>"
                                                + "<td class='youContent'>" + turn.sunkYou + "</td>"
                                                + "<td class='youContent'>" + turn.leftYou + "</td>"
                                                + "<td class='opponentContent'>" + turn.shipsHitOpp + "</td>"
                                                + "<td class='opponentContent'>" + turn.sunkOpp + "</td>"
                                                + "<td class='opponentContent'>" + turn.leftOpp + "</td>"
                                                + "</tr>");
             });
    }
  }


  //determine ships sunk per turn
  function returnShipsHit(data){
  var shipsHit = "";
  var string1 = "";
  var string2 = "";
  var string3 = "";
  var string4 = "";
  var string5 = "";
  if (data.aircraftCarrier != undefined){
       aircraftCarrier = data.aircraftCarrier;
       var string1 = "Aircraft Carrier: " + data.aircraftCarrier + " ";
       };
  if (data.battleship != undefined){
        battleship = data.battleship;
        var string2 = "Battleship: " + data.battleship + " ";
        };
  if (data.destroyer != undefined){
         destroyer = data.destroyer;
         var string3 = "Destroyer: " + data.destroyer + " ";
         };
  if (data.submarine != undefined){
         submarine = data.submarine;
         var string4 = "Submarine: " + data.submarine + " ";
          };
  if (data.patrolBoat != undefined){
         patrolBoat = data.patrolBoat;
         var string5 = "Patrol Boat: " + data.patrolBoat + " ";
          };
  return shipsHit.concat(string1, string2, string3, string4, string5);
  }


//method to draw players own ships on their grid
  function drawOwnShipLocations(data){
        showShipPlacementOptions(data);  //if ships are not already added to grid show ships placement div and add ships button
        for (var i = 0; i < data.yourShips.length; i++){
            for (var j = 0; j < data.yourShips[i].locations.length; j++){
                var location = data.yourShips[i].locations[j];
                $("#ownGrid > tr > td."+location).addClass("ship"); //only adds ships to own grid
//              $("#ownGrid").children().children("."+location).addClass("ship"); //Alternative for >1 grids
                }
            }
  }
//method to draw opponents ships on their grid
  function determineHitsOnOpp(data){
        if ( data.opponentShips.length > 0 ) {
            for (var i = 0; i < data.opponentShips.length; i++){
                        for (var j = 0; j < data.opponentShips[i].locations.length; j++){
                            var location = data.opponentShips[i].locations[j];
                            if ( $("#opponentGrid > tr > td."+location).hasClass("mySalvoes") ){
                                $("#opponentGrid > tr > td."+location).removeClass("mySalvoes").addClass("hit");
                                //if salvoes have same location as ships show hit
                            }
                        }
            }
        }
        else {
            if (data.yourShips.length != 0) {
            $("#gameStatus").html("<h2 class='gameStatus'>" + "Waiting for Opponent to place ships</h2>");
            }
        }
  }

//method to draw players own salvos on opponent grid
  function drawOwnSalvoLocations(data){
      if ( data.yourSalvoes.length > 0 ) {
        for (var i = 0; i < data.yourSalvoes.length; i++){
            for (var j = 0; j < data.yourSalvoes[i].locations.length; j++){
                var location = data.yourSalvoes[i].locations[j];
                var turn = data.yourSalvoes[i].turn;
                $("#opponentGrid > tr > td."+location).addClass("mySalvoes").html(turn); //only adds ships to own grid
//              $("#ownGrid").children().children("."+location).addClass("ship"); //Alternative for >1 grids
                }
            }
      } else {
          if ( data.yourShips.length > 0  && data.opponentShips.length > 0 ) {
             $("#gameStatus").html("<h2 class='gameStatus'>" + "Waiting for you to fire Salvoes</h2>");
          }
      }
  }

//method to draw opponents salvos on own grid
  function locateOpponentSalvoLocations(data){
    if ( data.opponentSalvoes.length > 0 ) {
        for (var i = 0; i < data.opponentSalvoes.length; i++){
            for (var j = 0; j < data.opponentSalvoes[i].locations.length; j++){
                var location = data.opponentSalvoes[i].locations[j];
                var turn = data.opponentSalvoes[i].turn;
                $("#ownGrid > tr > td."+location).addClass("oppSalvoes").html(turn); //only adds ships to own grid
                }
        }
    } else {
         if ( data.yourSalvoes.length > 0  && data.yourShips.length > 0  && data.opponentShips.length > 0 ) {
            $("#gameStatus").html("<h2 class='gameStatus'>" + "Waiting for opponent to fire Salvoes</h2>");
         }
    }
  }

//method to determine if salvos have hit any ships
function determineHitsOnYou(){
    $(".ship.oppSalvoes").removeClass("ship oppSalvoes").addClass("hit");
}

//Get the ships types and locations from front-end and pass them to the back-end then reload page
function addShips(){
    $("#add_ships").click(function(evt){
        var gpId = getGamePlayerIdFromURL(); //gets the gamePlayer(gp) id number from the url
        var overlapped = checkShipsNotOverlapped(allShipDetails);
        console.log(overlapped);
        if (overlapped == true){
            alert( "Ships overlapping, please move them!");
            return;
        }
        $.post({
          url: "/api/games/players/" + gpId + "/ships",
          data: JSON.stringify( allShipDetails ),
          dataType: "text",
          contentType: "application/json"
        })
        .done(function(data) {
          alert( "Ships added!");
          location.reload();
          console.log(data);
        })
        .fail(function(data) {
        console.log(data);
          alert(data.responseText); //TODO: why does it appear like this - because data type is text not JSON?? test to find out
          //{ "error" : "You have already placed ships for this game" } (data.responseText.error gives undefined)
        })
    });
}

// ASK DRAG: These are the key functions, 1st makes the element with 'draggable' class draggable - can customise type and location of cursor
function makeElementsDraggable() {
  $('.draggable').draggable({ cursor: "crosshair", cursorAt: { top: 14, left: 14 } });
}

////ASK DRAG: this makes the draggable object return to its previous location
//function revertToPreviousLocation() {
//  $('.draggable').draggable({ revert: true});
//}
//
////ASK DRAG: this cancels return object to its previous location so it can be dragged and more importantly dropped again
//function cancelRevert() {
//  $('.draggable').draggable({ revert: false});
//}



function getStartingPosition(type, x, y){
        $("#" + type).hide(); //hide just the placed ship momentarily to obtain grid data below it
        var coords = document.elementFromPoint(x, y); //shows whats at the grid point
        var startingPosition = $(coords).attr("data-grid"); //This selects the starting position
        $("#" + type).show(); // show ship again
        return startingPosition;
}


function getPlacedShipDetails(ship, type, shipLength, rotation, startingPosition){
    $(".draggable").mouseup (function(){
//        cancelRevert(); //ASK  DRAG: 2. this needs to be reset each time the dragged element is released otherwise it keeps returning to previous location
        var ship = $(this);
        var type = $(this).attr("id");  //this get ship type of placed ship - needed for posting ship
        var shipLength = $(this).attr("data-length");  //this gets the length of placed ship - to generate locations
        var rotation = $(this).attr("data-rotation");
        var x = event.clientX; //gets the location of the cursor i.e. first grid square where ships starts
        var y = event.clientY;
        var startingPosition = getStartingPosition(type, x, y);

        if (startingPosition == undefined){ //ASK DRAG: if the cursor of the mouse is not over you own grid, i.e. it can't obtain a data-grid attribute from the location where ship was dropped
            $(ship).attr("style", "position: relative; top: 0px; left: 0px;"); //resets ship to original location in select ship div
//            revertToPreviousLocation(); // ASK DRAG: it tells the element to go back to its original location
//            return alert("ship must be placed correctly on your own grid")
        }
        var locations = calculateShipLocations(rotation, shipLength, startingPosition);
        var ship = {
            type: type,
            locations: locations
        }
        var shipDetailsForJSON = '{"type": "'+ type + '", "locations":[' + locations + ']}';
        allShipDetails = buildJsonToAddShips(ship, type); //pass ship details to Json builder & update it

      });
}


function calculateShipLocations(rotation, shipLength, startingPosition){
    var characters = ["A","B","C","D","E","F","G","H","I","J"]; //array for vertical axis
    var locations = [];
    locations.push(startingPosition); // initialize array with starting position
    var letter = startingPosition.slice(0,1); // this isolates the letter
    var number = parseInt(startingPosition.slice(1)); // this isolates the number & converts it from string to number
    var shipLengthInt = parseInt(shipLength);
    var currentLetterPos = characters.indexOf(letter);
    //this calculates ship locations array for horizontally placed ships
    if (rotation == "horizontal"){ //makes sure ship fits on grid - using current position & character array length (which is 9)
        if (currentLetterPos + shipLengthInt-1 > 9){
                        alert("Ship out of grid range, please move it to be completely within the grid.")
                        return;}
        for (var i = 0; i < shipLengthInt-1; i++) { //this calculated for vertically placed ships
            currentLetterPos+= 1;
            letter = characters[currentLetterPos];
            var location = letter + number;
            locations.push(location);
        }
        return locations;
    }
    //this calculates ship locations array for vertically placed ships
    if (rotation == "vertical"){
        for (var i = 0; i < shipLengthInt-1; i++) { //this calculated for vertically placed ships
                number+= 1;
                if (number > 10){ //Exit method if ship location is out of range
                    alert("Ship out of grid range, please move it to be completely within the grid.")
                    return;
                }
                var location = letter + number;
                locations.push(location);
            }
        return locations;
    }
}

function rotateShips(){
    $(":radio").click(function(){
        var type = $(this).attr("data-id");
        var rotation = $(this).attr("value");
        $('#' + type).removeAttr("data-rotation");
        $('#' + type).attr("data-rotation", rotation); //this sets the correct rotation in terms of the selected radio button
        $(this).siblings("div").children().attr("style", "position: relative; top: 0px; left: 0px;"); //resets ship to original location in select ship div
        $("#add_ships").attr("disabled", true); //deactivate addShips button to avoid already placed ships changing rotation and posting old location
    });
}

function buildJsonToAddShips(ship, type){
          //loop through the array 'allShipDetails' to check if there is already an object with that type and if so delete the whole object
          for (var i = 0; i < allShipDetails.length; i++){
          console.log(allShipDetails);
                if (allShipDetails[i].type  == type){
                allShipDetails.splice(i,1); //this should remove one item at position 'i' eg. where the duplicate is
                //TODO: if change rotation on a placed ship it should also re-change locations - but where to implement this??
                }
          }
        allShipDetails.push(ship);
        if (allShipDetails.length == 5) { // if all 5 ships have been correctly placed then active 'add_ships' button
            $("#add_ships").attr("disabled", false);
            }
        return allShipDetails;
}

//This checks that no ships are overlapping before posting to back-end)
function checkShipsNotOverlapped(allShipDetails){
    var arr1 = allShipDetails[0].locations;
    var arr2 = allShipDetails[1].locations;
    var arr3 = allShipDetails[2].locations;
    var arr4 = allShipDetails[3].locations;
    var arr5 = allShipDetails[4].locations;
    var allLocations = arr1.concat(arr2, arr3, arr4, arr5);
    console.log(arr1, arr2, arr3, arr4, arr5);
    console.log("allLocations: ");
    for (var i = 0; i < allLocations.length; i++){
        var result = jQuery.inArray(allLocations[i], allLocations, i+1);
        if (result == -1){
            return false; //This means there are no duplicate locations so overlapping is false
        }
        else{
             return true; //This means there are no duplicate locations so overlapping is false
        }
    }

}

function showShipPlacementOptions(data){
    if (data.yourShips.length == 0) {
        $(".shipPlacementDiv").show();
        $("#gameStatus").html("<h2 class='gameStatus'>" + "Waiting for you to place ships</h2>");
    }
}

//Get the ships types and locations from front-end and pass them to the back-end then reload page
function fireSalvoes(salvoLocations){
    $("#fire_salvoes").click(function(evt){
        console.log(salvoLocations);
        var gpId = getGamePlayerIdFromURL(); //gets the gamePlayer(gp) id number from the url
        $.post({
          url: "/api/games/players/" + gpId + "/salvoes",
          data: JSON.stringify({locations: salvoLocations}),
          dataType: "text",
          contentType: "application/json"
        })
        .done(function(data) {
          alert( "Salvoes added!");
          location.reload();
          console.log(data);
        })
        .fail(function(data) {
        console.log(data);
          alert(data.responseText); //TODO: why does it appear like this - because data type is text not JSON?? test to find out
          //{ "error" : "You have already placed ships for this game" } (data.responseText.error gives undefined)
        })
    });
}

function getSalvoPositions(){
var salvoLocations = [];
    $("#opponentGrid").on("click", "td", function() {
        var location = $(this).attr("data-gridOpp"); //this gets the grid ref from data-gridOpp attribute

        if ($(this).hasClass("mySalvoes") || $(this).hasClass("key")|| $(this).hasClass("hit")){
            return;
        } //if the grid belongs to the key, already has a salvo placed or has been hit then exit out of method

        if ($(this).hasClass("toFire")){ //if already selected - remove class and removed from array
            $(this).removeClass("toFire");
            var grid = $(this).attr("data-gridOpp");
            var pos = salvoLocations.indexOf(grid);
            salvoLocations.splice(pos,1);
            console.log(salvoLocations);
        } else if (salvoLocations.length < 5) {
             salvoLocations.push(location);
             $(this).addClass("toFire");
             console.log(salvoLocations);
        } else if (salvoLocations.length == 5){
        alert("You can only fire up to 5 salvos per turn");
        }
        enableFireSalvosButton(salvoLocations);
    });

    fireSalvoes(salvoLocations);
}


function enableFireSalvosButton(salvoLocations){
    if (salvoLocations.length > 0) { //activate fire_salvoes button
        $("#fire_salvoes").attr("disabled", false);
    } else {
        $("#fire_salvoes").attr("disabled", true);
    }
}
//
////TODO: can this be added into getSalvo Positions or best kept separate
//function buildJsonToAddSalvos(salvoLocations){
//          var allSalvoDetails;
//          var salvoLocations = getSalvoPositions();//TODO: don´t think this is the best way to call this
//          var salvo = {
//                      locations: salvoLocations
//                  }
//                  console.log(all)
//        return allSalvoDetails;
//}

// TODO: if this is too complicated could implement a simple option that when rotation is changed it returns the ship to original location

//        var salvos = []; //empty array for pushing salvo locations
//
//        $("#" + type).hide(); //hide just the placed ship momentarily to obtain grid data below it
//        var coords = document.elementFromPoint(x, y); //shows whats at the grid point
//        var startingPosition = $(coords).attr("data-grid"); //This selects the starting position
//        $("#" + type).show(); // show ship again
//        return startingPosition;


//TODO At present the ship location data is not updated once ship has been placed and rotation is changed - fix when have time
//TODO the div around the ship was draggable - i've changed this to be just the div with the ship itself to be draggable so
//TODO maybe this will helo with ship locations getting moved on save????
//function dropShip(){
//      $(".draggable").mouseup (function(){
////      cancelRevert();
//            var ship = $(this);
//            var type = $(this).attr("id");  //this get ship type of placed ship - needed for posting ship
//            var shipLength = $(this).attr("data-length");  //this gets the length of placed ship - to generate locations
//            var rotation = $(this).attr("data-rotation");
//            var x = event.clientX; //gets the location of the cursor i.e. first grid square where ships starts
//            var y = event.clientY;
//            var startingPosition = getStartingPosition(type, x, y);
//            getPlacedShipDetails(ship, type, shipLength, rotation, startingPosition);
//      });
//}
//
//function changeRotationOfPlacedShip(){
//     $(":radio").click (function(){
////     cancelRevert();
//         var ship = $(this).siblings($(".draggable"));
//         var type = $(ship).children("div").attr("id");  //this get ship type of placed ship - needed for posting ship
//         var shipLength = $(ship).children("div").attr("data-length");  //this gets the length of placed ship - to generate locations
//         var rotation = $(ship).children("div").attr("data-rotation");
//         var offset = $("#" + type).offset();
//         var x =  offset.top;//
//         var y = offset.left;
//         var startingPosition = getStartingPosition(type, x, y);
//         getPlacedShipDetails(ship, type, shipLength, rotation, startingPosition);
//     });
//}

//ajax call to the api to get the JSON data - if successful it uses data to draw the game view if not it returns an error
  function loadGameHistoryData() {
    var gp = getGamePlayerIdFromURL(); //gets the gamePlayer(gp) id number from the url
    var url = "/api/games/players/"+gp+"/gameHistory"; //inserts the gp id number into the api
    $.getJSON(url)
    .done(function(data) {
    // this checks if user is trying to access a gameView for a game they are not a player in
    // if they are not a player then instead of the gameView data there will be an Error object generated by the controller
    // with an error status 403 and returns the body error message as an alert
    if (data.gameView == undefined){
          alert(data.Error.body);
        }
    else{
          gameHistory(data);
        }
    })
    .fail(function( jqXHR, textStatus ) {
      showOutput( "Failed: " + textStatus );
    });
  }