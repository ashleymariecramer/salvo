$(function() {

//Main functions:
   buildGrid();
   loadData();
   addShips();
   makeElementsDraggable();
   getPlacedShipDetails();
   rotateShips();

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
        //window.location.replace("/games.html"); This replaces the games.html page so its not in history
//        $(location).attr('href', '/games.html'); //Takes logged out user back to games page
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
            var cell = '<td ' + 'data-grid="'+ gridRef + '"' + ' class="'+ gridRef + '"' + '>' + '</td>';
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
          if (data.opponent != undefined) { //if there's no opponent do not add their ship and salvo data to the grids
                locateOpponentShipLocations(data);
                locateOpponentSalvoLocations(data);
          }
          determineHits();
        }
    })
    .fail(function( jqXHR, textStatus ) {
      showOutput( "Failed: " + textStatus );
    });
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

//Get the ships types and locations from front-end and pass them to the back-end then reload page
function addShips(){
    $("#add_ships").click(function(evt){
        var gpId = getGamePlayerIdFromURL(); //gets the gamePlayer(gp) id number from the url
 //       var url = "api/games/players/" + gpId + "/ships"; //inserts the gp id number into the api
        //TODO: check locations not overlapping:
        var overlapped = checkShipsNotOverlapped(allShipDetails);
        console.log(overlapped);
        if (overlapped == true){
            alert( "Ships overlapping, please move them!");
            return;
        }
        $.post({
          url: "/api/games/players/" + gpId + "/ships",
          data: JSON.stringify( allShipDetails ),
//          data: JSON.stringify([ {"type": "destroyer", "locations":["A1", "B1", "C1"]},
//                                 {"type": "patrol boat", "locations":["H5", "H6"]},
//                                 {"type": "aircraft carrier", "locations":["J6", "J7", "J8", "J9", "J10", "J10"]},
//                                 {"type": "submarine", "locations":["D3", "E3", "F4"]},
//                                 {"type": "battleship", "locations":["G4", "H4", "I4", "J4"] }]
//                                 ),
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
          alert(data.responseText); //TODO: why does it appear like this:
          //{ "error" : "You have already placed ships for this game" } (data.responseText.error gives undefined)
        })
    });
}


function makeElementsDraggable() {
  $('.draggable').draggable({ cursor: "crosshair", cursorAt: { top: 14, left: 14 } });
}

function revertToOriginalLocation() {
  $('.draggable').draggable({ revert: true});
}

function cancelRevert() {
  $('.draggable').draggable({ revert: false});
}


function getPlacedShipDetails(){
    $(".draggable").mouseup(function(){
        cancelRevert(); //need to reactive this each time to cancel revert for ships not placed correctly
        var type = $(this).children().attr("id");  //this get ship type of placed ship - needed for posting ship
        var shipLength = $(this).children().attr("data-length");  //this gets the length of placed ship - to generate locations
        var rotation = $(this).children().attr("data-rotation");  //gets if ship horizontally or vertically placed - to generate locations
        var x = event.clientX; //gets the location of the cursor i.e. first grid square where ships starts
        var y = event.clientY;
        $("#" + type).hide(); //hide just the placed ship momentarily to obtain grid data below it
        var coords = document.elementFromPoint(x, y);
        var startingPosition = $(coords).attr("data-grid"); //This selects the starting position
        if (startingPosition == undefined){
            $(this).children().removeAttr( "style" )
            revertToOriginalLocation();
//            return alert("ship must be placed correctly on your own grid")
        }
        $("#" + type).show(); // show ship again
        var locations = calculateShipLocations(rotation, shipLength, startingPosition);
        var ship = {
            type: type,
            locations: locations
        }
        var shipDetailsForJSON = '{"type": "'+ type + '", "locations":[' + locations + ']}';
        console.log("before build: ");
        console.log(allShipDetails);
        allShipDetails = buildJsonToAddShips(ship, type); //pass ship details to Json builder & update it
        console.log("after build: ");
        console.log(allShipDetails);
  })

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
    if (rotation == "horizontal"){
        //and makes sure ship fits on grid - using current position & length of the character array (which is 9)
        if (currentLetterPos + shipLengthInt-1 > 9){
                        alert("Ship out of grid range, please move it to be completely within the grid.")
                        return;}
        for (var i = 0; i < shipLengthInt-1; i++) { //this calculated for vertically placed ships
            currentLetterPos+= 1;
            letter = characters[currentLetterPos];
            //need to loop through array 'characters' to determine next letter
            var location = letter + number;
            locations.push(location);
        }
        return locations;
    }
    //this calculates ship locations array for vertically placed ships
    if (rotation == "vertical"){
        for (var i = 0; i < shipLengthInt-1; i++) { //this calculated for vertically placed ships
                number+= 1;
                //Exit method if ship location is out of range
                if (number > 10){
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
        $('#' + type).attr("data-rotation", rotation); //this sets the correct rotation in terms of the selcted radio button
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

//TODO: Avoid ships being overlapped
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
    }
}



