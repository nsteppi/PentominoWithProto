import './css/normalize.css';
import './css/skeleton.css';
import './css/style.css';
import './css/App.css';
import React, { useEffect, useReducer, useState, useRef } from 'react'
import { pento_I } from "./pento-objects/HelperPentoShapes";
import { PentoBoard } from "./pento-objects/PentoBoard";
import { PentoConfig } from "./config";
import { createNewPentoPieceInShape, generateElephantShape } from "./pento-objects/HelperDrawComplexShapes";
import Furhat from 'furhat-gui'


const App = () => {

  const pento_config = new PentoConfig()

  const n_blocks = pento_config.n_blocks;
  const board_size = pento_config.board_size;
  const block_size = pento_config.block_size;
  const grid_x = 0;
  const grid_y = 0;

  // Diese Variable setzt, wie lang das Game im Webinterface dauert (in Sekunden)
  const game_time = 10;

  const grid_config = {
    "n_blocks": n_blocks,
    "board_size": board_size,
    "block_size": block_size,
    "x": grid_x,
    "y": grid_y
  }

  // Der initiale GameState
  const initialState = {
    "left_board": [],
    "right_board": [],
    "game": {
      "status": "initial",
      "startTime": undefined,
      "time": game_time,
    },
    "selected": "None"
  }


  // Dient dazu, den Intervall-Handler für die Spielzeit nach Ablauf der Spielzeit wieder zu zerstören
  // (ansonsten würde die Spielzeit negativ werden)
  const gameTimeHandler = useRef();

  // Die Pentomino-Steine auf dem linken Board
  const [initialShapes, setInitialShapes] = useState([])

  // Die Pentomino-Steine auf dem rechten Board
  const [placedShapes, setPlacedShapes] = useState([]);

  // Die momentan ausgewählten Pento-Steine
  const [activeShape, setActiveShape] = useState([]);

  // Speichert, ob die Web-UI mit dem Roboter verbunden ist
  const [initialized, setInitialized] = useState(false)

  //Speichert, ob wir gerade unser Popup für Spiel gewonnen anzeigen
  const [isPopupWonOpen, setIsPopupWonOpen] = useState(false);

  //Speichert, ob wir gerade unser Popup für Spiel gewonnen anzeigen
  const [isPopupLostOpen, setIsPopupLostOpen] = useState(false);

  const togglePopupWon = () => {
    setIsPopupWonOpen(!isPopupWonOpen);
  };

  const togglePopupLost = () => {
    setIsPopupLostOpen(!isPopupLostOpen);
  };

  //Hält den momentanen GameState und sorgt dafür, dass Änderungen korrekt umgesetzt werden
  const [gameState, dispatch] = useReducer((state, action) => {
    switch (action.type) {
      case 'gameStart':
        return {
          ...state,
          game: {
            ...state.game,
            status: 'ongoing',
            startTime: new Date().getTime()
          }
        };
      case 'selectPiece': {
        return {
          ...state,
          selected: action.piece.name
        };
      }
      case 'deselectPiece': {
        return {
          ...state,
          selected: "None"
        };
      }
      case 'addToRightBoard':
        return {
          ...state,
          right_board: [...state.right_board, action.piece]
        };
      case 'addToLeftBoard':
        return {
          ...state,
          left_board: [...state.left_board, action.piece]
        };
      case 'removeFromLeftBoard':
        let filtered_list = state.left_board.filter(item => item.name !== action.piece.name)
        return {
          ...state,
          left_board: filtered_list
        };
      case 'gameWon':
        return {
          ...state,
          game: {
            ...state.game,
            status: 'won',
            startTime: new Date().getTime()
          }
        };
      case 'refreshTime':

        const currentTime = new Date().getTime();
        const newDiff = game_time - Math.floor((currentTime - state.game.startTime) / 1000.0);
        let newStatus = state.game.status;
        if (newDiff <= 0){
          newStatus = 'lost'
        }

        return {
          ...state,
          game: {
            ...state.game,
            status: newStatus,
            time: newDiff
          }
        };

      default:
        return state

    }
  }, initialState);


  /**
   * Hier erzeugen wir unseren Popup für den Fall, dass wir das Spiel gewonnen haben
   * @param props
   * @returns {*}
   * @constructor
   */
  const PopupWon = props => {
    return (
        <div className="popup-box">
          <div className="box">
            <b>Congratulations!</b>
            <p>You won this round of Pentomino.</p>
            <button onClick={props.handleClose}>Okay</button>
          </div>
        </div>
    );
  };

  /**
   * Hier erzeugen wir unseren Popup für den Fall, dass wir das Spiel verloren haben
   * @param props
   * @returns {*}
   * @constructor
   */
  const PopupLost = props => {
    return (
        <div className="popup-box">
          <div className="box">
            <b>Awwwwww!</b>
            <p>Sorry, but the time is up - you lost this round of Pentomino.</p>
            <button onClick={props.handleClose}>Okay</button>
          </div>
        </div>
    );
  };


  /**
   * Rendert die Buttons unter dem Game-Board, mit denen zu Testzwecken einzelne Teile ausgewählt werden können.
   */
  const renderButtons = () => {
    return initialShapes.map(element => {
      return <button id={"pento_" + element.type} onClick={() => {
        selectPentoPiece(element.name)
      }}> {pento_config.get_color_name(element.color)} {element.type} </button>
    })
  };

  /**
   * Diese Methode wird aufgerufen, wenn ein Pentomino-Stein ausgewählt wird (entweder durch Button-klick oder durch
   * ein Event vom Roboter.
   *
   * @param pento_name Der Name des Pentomino-Teils als String
   */
  const selectPentoPiece = (pento_name) => {
    if (activeShape.length > 0 && activeShape[0].name == pento_name) {
      setActiveShape([])
    } else {
      setActiveShape(initialShapes.filter(item => item.name == pento_name.toString()));
    }
  };

  /**
   * Diese Methode sorgt dafür, dass alle momentan ausgewählten Spielsteine nicht mehr ausgewählt sind
   * Wird entweder durch Button-Klick oder Event vom Roboter aufgerufen.
   */
  const deselect = () => {
    setActiveShape([])
  };

  /**
   * Diese Methode wird aufgerufen, um das Spiel zu starten (entweder durch Button-klick oder durch Event vom Roboter)
   */
  const startGame = () => {
    // Alle aktuellen Spielsteine auf dem rechten Board löschen
    setPlacedShapes([]);

    // ALle aktuell ausgewählten Spielsteine löschen
    setActiveShape([]);
    setInitialShapes(generateElephantShape("elephant", pento_config, grid_config));

    dispatch({type: 'gameStart'})
  };

  /**
   * Diese Methode plaziert einen Spielstein an der richtigen Position auf dem rechten Spielbrett.
   * Wird entweder durch Button-Klick oder Event vom Roboter aufgerufen.
   */
  const placeSelected = () => {
    console.log("Active Shapes: " + activeShape)
    if (activeShape.length > 0) {
      let selected_shape = activeShape[0].name;
      let to_replace = null;
      initialShapes.forEach(el => {
        if (el.name == selected_shape) {
          to_replace = el
        }
      });

      let new_shape = createNewPentoPieceInShape("elephant", pento_config, grid_config, to_replace.type, to_replace.color, to_replace.id);

      const newPiece = pentoPieceToObj(new_shape.name, new_shape.type, new_shape.color, new_shape.x, new_shape.y);
      dispatch({type: 'addToRightBoard', piece: newPiece});
      dispatch({type: 'removeFromLeftBoard', piece: to_replace});

      setPlacedShapes(placedShapes.concat(new_shape));
      setInitialShapes(initialShapes.filter(item => item.name !== to_replace.name));

      //Kein Spielstein ist ausgewählt nachdem gerade ein Spielstein plaziert wurde
      setActiveShape([])
    }
  };

  /**
   * Hilfsmethode, um einen Pentomino-Stein als JSON zu speichern
   * @param name Name des Pentimon-Steins
   * @param type Typ des Pentomino-Steins
   * @param color_code Farbe des Steins (als Hex-code)
   * @param x x-Koordinate auf dem Spielbrett
   * @param y z-Koordinate auf dem Spielbrett
   * @returns {{color, name, location: {x, y}, type}}
   */
  const pentoPieceToObj = (name, type, color_code, x, y) => {
    let color = pento_config.get_color_name(color_code)
    return {name, type, color, location: { x, y}}
  };

  /**
   * Dies hier wird getriggert, wenn es eine Änderung im Spielstatus gab
   */
  useEffect(() => {

    // Sorgt für den richtigen Status wenn das Spiel beginnt
    if (gameState.game.status === 'ongoing') {
      console.log('Game status changed to ongoing');
      initialShapes.forEach(el => {
        const newPiece = pentoPieceToObj(el.name, el.type, el.color, el.x, el.y);
        dispatch({type: 'addToLeftBoard', piece: newPiece});
      });
      gameTimeHandler.current = setInterval(() => {
        dispatch({type: 'refreshTime'});
      }, 500)
    }

    // Setzt den Alert für ein gewonnenes / verlorenes Spiel
    if (['lost', 'won'].includes(gameState.game.status)){
      if ('lost' === gameState.game.status){
        togglePopupLost();
      }
      if ('won' === gameState.game.status){
        togglePopupWon();
      }
      if (gameTimeHandler.current){
        clearInterval(gameTimeHandler.current)
      }
    }
  }, [gameState.game.status]);

  /**
   * Dies wird getriggert, wenn es eine Änderung im Spielstatus oder der Liste mit Steinen auf dem linken Board gibt
   */
  useEffect(() => {

    // Wenn es keine Steine mehr auf dem linken Board gibt und das Spiel noch läuft, haben wir gewonnen
    if (gameState.game.status === 'ongoing' && initialShapes?.length === 0) {
      dispatch({type: 'gameWon'})
    }

    if (gameState.game.status === 'ongoing') {
        // Wir subscriben zu dem Event, das vom Roboter gesendet werden kann, um einen Spielstein auszuwählen
        window.furhat.subscribe('selectPiece', function (params) {
          selectPentoPiece(params.piece)
        });

        // Wir subscriben zu dem Event, das vom Roboter gesendet werden kann, um die aktuelle Auswahl an Spielsteinen
        // zu löschen
        window.furhat.subscribe('deselectPiece', function () {
          deselect()
        });

        // Wir subscriben zu dem Event, das vom Roboter gesendet werden kann, um den aktuell ausgewählten Spielstein
        // auf dem rechten Board zu plazieren
        window.furhat.subscribe('startPlacing', function () {
          placeSelected()
        })
    }

  }, [initialShapes, gameState.game.status]);

  /**
   * Hier werden Änderungen im aktuell ausgewählten Spielstein an die richtigen Stellen kommuniziert.
   */
  useEffect(() => {
    if (activeShape && activeShape.length > 0) {
      dispatch({type: 'selectPiece', piece: activeShape[0]});
    }
    else {
      dispatch({type: 'deselectPiece'})
    }
    if(window.furhat) {
        window.furhat.subscribe('startPlacing', function () {
          placeSelected()
        })
    }

  }, [activeShape]);

  /**
   * Bei jedem Update in der Spielzeit (also einmal die Sekunde) wird der aktuelle Spielstand and Furhat gesendet
   */
  useEffect(() => {
    sendDataToFurhat()
  }, [gameState.game.time]);


  /**
   * Wenn die Web-UI initialisiert wird, verbinden wir uns mit dem Roboter.
   * Initial hören wir nur auf "startGame" events.
   */
  useEffect(() => {
    Furhat(function (furhat) {

      window.furhat = furhat
      // We subscribe to the event to start the game
      furhat.subscribe('startGame', function () {
        startGame()
      });
    })

  }, [])

  /**
   * Hilfs-methode zum Initialisieren der Verbindung mit Furhat
   */
  const initializationMonitor = () => {

    if(!window.furhat) {
      setTimeout(initializationMonitor, 1000)
    }
    else {
      setInitialized(true)
      console.log("Initialization successful")
    }

  }

  /**
   * Hilfs-methode zum Initialisieren der Verbindung mit Furhat
   */
  useEffect(() => {
    initializationMonitor()
  }, [])

  /**
   * Hilfsmethode, die Daten an den Roboter sendet
   */
  const sendDataToFurhat = () => {
    if(window.furhat) {
      window.furhat.send({
        event_name: "GameStateUpdate",
        data: JSON.stringify(gameState)
      })
    }
  }


  /**
   * Hier werden die einzelnen Komponenten in der Web-UI angezeigt
   */
  return (
    <div className="App">
      <div className="twelve columns">
        <h5>Pentomino Game</h5>
      </div>
      {isPopupWonOpen && <PopupWon
          handleClose={togglePopupWon}
      />}
      {isPopupLostOpen && <PopupLost
          handleClose={togglePopupLost}
      />}
      <div className="row">
        <div className="six columns">
          <button id="startBtn" style={{ marginRight: 50 }} onClick={() => startGame()}>Start new game</button>
          <button id="placeBtn" style={{ marginRight: 50 }} onClick={() => placeSelected()}>Place selected</button>
          <button id="placeBtn" onClick={() => deselect()}>Deselected Piece</button>
        </div>
        <div className="six columns">
          <div style={{ color: "#555", fontSize: "16px" }}>Game State: {gameState.game.status}</div>
          <div style={{ color: "#555", fontSize: "16px" }}>Remaining Game Time: {gameState.game.time}</div>
        </div>
      </div>
      <hr />
      <div className="row">
        <div className="six columns">
          <PentoBoard shapes={initialShapes}
                      activeShape={activeShape[0]}
                      grid_properties={{
                        "title": "Initial",
                        "with_grid": true,
                        "with_tray": true,
                        "x": grid_x,
                        "y": grid_y
                      }}
                      config={{ "n_blocks": n_blocks, "board_size": board_size, "block_size": block_size }}
          />
        </div>
        <div className="six columns">
          <PentoBoard shapes={placedShapes}
                      grid_properties={{
                        "title": "Elephant",
                        "with_grid": true,
                        "with_tray": true,
                        "x": grid_x,
                        "y": grid_y
                      }}
                      config={{ "n_blocks": n_blocks, "board_size": board_size, "block_size": block_size }}
          />
        </div>
      </div>
      <div>
        {renderButtons()}
      </div>
    </div>
  );
};

export default App;
