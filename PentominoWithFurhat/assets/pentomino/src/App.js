/**
 * App.js
 * Maike Paetzel-Prüsmann
 *
 * Modifikationen durch:
 * - Wencke Liermann, Lisa Plagemann, Niklas Stepczynski
 * - SoSe 21
 * - Kotlin 1.3.70
 * - Windows 10
 */

import './css/normalize.css';
import './css/skeleton.css';
import './css/style.css';
import './css/App.css';
import React, { useEffect, useReducer, useState, useRef } from 'react'
import { PentoBoard } from "./pento-objects/PentoBoard";
import { PentoConfig } from "./config";
import { configPerShape, createNewPentoPieceInShape, generateShape } from "./pento-objects/HelperDrawComplexShapes";
import { grid_cell_to_coordinates, coordinates_to_grid_cell } from "./pento-objects/HelperDrawingBoard";
import Furhat from 'furhat-gui'


const App = () => {

  const pento_config = new PentoConfig()

  // Ist die Web-UI mit dem Roboter verbunden
  const [initialized, setInitialized] = useState(false)

  // Wie lange dauert das Spiel im Webinterface (in Sekunden)
  const [gameTime, setGameTime] = useState(pento_config.game_time)

  // Welche Form bauen wir gerade
  const [finalShape, setFinalShape] = useState('elephant')

  // Dient dazu, den Intervall-Handler für die Spielzeit nach Ablauf der Spielzeit wieder zu zerstören
  // (ansonsten würde die Spielzeit negativ werden)
  const gameTimeHandler = useRef();

  // Die Pentomino-Steine auf dem linken Board
  const [initialShapes, setInitialShapes] = useState([])

  // Die Pentomino-Steine auf dem rechten Board
  const [placedShapes, setPlacedShapes] = useState([]);

  // Die momentan ausgewählten Pento-Steine
  const [activeShape, setActiveShape] = useState([]);

  //Speichert, ob wir gerade unser Popup für Spiel gewonnen anzeigen
  const [isPopupOpen, setIsPopupOpen] = useState(false);

  const togglePopup = () => {
    setIsPopupOpen(!isPopupOpen);
  };

  // Der initiale GameState
  const initialState = {
    "left_board": [],
    "right_board": [],
    "correctly_placed": [],
    "game": {
      "status": "initial",
      "startTime": undefined,
      "time": gameTime,
    },
    "selected": "None",
    "selected_coords": []
  }

  // Hält den momentanen GameState und sorgt dafür, dass Änderungen korrekt umgesetzt werden
  const [gameState, dispatch] = useReducer((state, action) => {
    switch (action.type) {
      case 'gameStart':
        if (isPopupOpen) {
          togglePopup()
        }
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
          selected: action.piece.name,
        };
      }
      case 'deselectPiece': {
        return {
          ...state,
          selected: "None"
        };
      }
      case 'updateCoords': {
        // Pixelkoordinaten in Gitterkoordinaten umrechnen
        // falls kein Stein aktiv, leeres Array speichern
        return {
          ...state,
          selected_coords: (action.x < 0 || action.y < 0) ? [] : coordinates_to_grid_cell(action.x, action.y, pento_config.block_size)
        }
      }
      case 'addToRightBoard':
        return {
          ...state,
          right_board: [...state.right_board, action.piece],
        };
      case 'addToLeftBoard':
        return {
          ...state,
          left_board: [...state.left_board, action.piece]
        };
      case 'removeFromLeftBoard':
        let new_left_board = state.left_board.filter(item => item.name !== action.piece.name)
        return {
          ...state,
          left_board: new_left_board
        };
      case 'pieceAtGoal':
        let new_right_board = state.right_board.filter(item => item.name !== action.piece.name);
        return {
          ...state,
          right_board: new_right_board,
          correctly_placed: [...state.correctly_placed, action.piece]
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
      case 'resetGame':
        return {
          left_board: [],
          right_board: [],
          correctly_placed: [],
          game: {
            status: "initial",
            startTime: undefined,
            time: gameTime,
          },
          selected: "None",
          selected_coords: []
        }
      case 'refreshTime':
        const currentTime = new Date().getTime();
        const newDiff = gameTime - Math.floor((currentTime - state.game.startTime) / 1000.0);
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


  const getHint = () => {
    if (activeShape.length > 0) {
      let active_template = placedShapes.filter(
          s => pento_config.get_color_name(s.color) === pento_config.templ_col
                && s.type === activeShape[0].type
      )
      selectPentoPiece(active_template[0].name)
    }
  };

  const removeHint = () => {
    if (activeShape.length > 0) {
      let active = placedShapes.filter(
          s => pento_config.get_color_name(s.color) !== pento_config.templ_col
              && s.type === activeShape[0].type
      )
      selectPentoPiece(active[0].name)
    }
  };


  /**
   * Diese Methode wird aufgerufen, wenn ein Pentomino-Stein ausgewählt wird (entweder durch Button-klick oder durch
   * ein Event vom Roboter)
   *
   * @param pento_name Der Name des Pentomino-Teils als String
   */
  const selectPentoPiece = (pento_name) => {
    if (activeShape.length > 0 && activeShape[0].name == pento_name) {
      deselect();
    } else {
      // Evtl. laufende Bewegung des alten aktiven Steins stoppen.
      stopMove();
      setActiveShape(initialShapes.concat(placedShapes).filter(item => item.name == pento_name.toString()));
    }
  };

  /**
   * Diese Methode sorgt dafür, dass alle momentan ausgewählten Spielsteine nicht mehr ausgewählt sind
   * Wird entweder durch Button-Klick oder Event vom Roboter aufgerufen.
   */
  const deselect = () => {
    stopMove();
    setActiveShape([]);
  };

  /**
   * Hilfsmethode zum Testen, ob der ausgewählte ('aktive') Stein auf dem linken Brett ist.
   */
  const activeOnLeftBoard = () => {
    return (activeShape[0] && initialShapes.find(shape => shape.name == activeShape[0].name));
  }

  /**
   * Hilfsmethode zum Testen, ob der ausgewählte ('aktive') Stein auf dem rechten Brett ist.
   */
  const activeOnRightBoard = () => {
    return (activeShape[0] && placedShapes.find(shape => shape.name == activeShape[0].name));
  }

  // Parameter und Variablen zur Bewegung des aktiven Spielsteins auf dem rechten Spielbrett
  const [MOVESPEED, setMOVESPEED] = useState(5);
  const [MOVEFREQ, setMOVEFREQ]   = useState(200);
  const moveHandler               = useRef(null);

  /**
   * Den aktiven Stein um (dx,dy) verschieben.
   * @param {Entfernung horizontal} dx
   * @param {Entfernung vertikal} dy
   */
  const moveActive = (dx, dy) => {
    let active = activeShape[0];
    // Sicherstellen, dass der Stein das Spielfeld nicht verlässt
    let new_x = Math.max(2*pento_config.block_size, active.x+dx);
    new_x   = Math.min(new_x, pento_config.board_size - 2*pento_config.block_size);
    let new_y = Math.max(2*pento_config.block_size, active.y+dy);
    new_y   = Math.min(new_y, pento_config.board_size - 2*pento_config.block_size);
    // den Gamestate mit den neuen Koordinaten updaten
    dispatch({type: 'updateCoords', x:new_x, y:new_y});
    active.moveTo(new_x, new_y);
  };

  /**
   * Eine aktuelle Bewegung des aktiven Spielsteins stoppen und eine neue Bewegung starten.
   * @param {eine Richtung aus ['up', 'down', 'left', 'right']} dir
   * @param {Häufigkeit der Bewegung, default:200} interval
   * @param {mit jedem Schritt zurückgelegte Pixel, default: 5} step
   */
  const startMove = (dir, interval=MOVEFREQ, step=MOVESPEED) => {
    if (activeOnRightBoard()) {
      // evtl. stattfindende Bewegung anhalten, aber für eine flüssige Richtungsänderung
      // Stein nicht ein
      stopMove(false);
      // setInterval wird genutzt, um die Funktion moveActive in regelmäßigen Abständen auszuführen
      switch (dir) {
        case 'up':
          moveHandler.current = setInterval(moveActive, interval, 0, -step);
          break;
        case 'down':
          moveHandler.current = setInterval(moveActive, interval, 0, step);
          break;
        case 'left':
          moveHandler.current = setInterval(moveActive, interval, -step, 0);
          break;
        case 'right':
          moveHandler.current = setInterval(moveActive, interval, step, 0);
          break;
        case 'middle':
          let active = activeShape[0];
          let dist_x = pento_config.board_size/2 - active.x;
          let dist_y = pento_config.board_size/2 - active.y;
          dist_y = step*Math.abs(dist_y/dist_x)*Math.sign(dist_y)
          dist_x = step*Math.sign(dist_x)
          moveHandler.current = setInterval(moveActive, interval, dist_x, dist_y);
          break;
        default:
          console.log(`Unknown direction: ${dir} at startMove`);
      }
    } else {
      console.log('No active shape');
    }
  }

  /**
   * Bewegung des aktiven Spielstein anhalten und den Stein auf einem Quadrat der Matrix 'einrasten' lassen
   */
  const stopMove = (lock_and_fix_correct=true) =>  {
    clearInterval(moveHandler.current);
    if (lock_and_fix_correct && activeOnRightBoard()) {
      lockActiveOnGrid();
      fixCorrectlyPlaced(activeShape[0]); // falls Stein korrekt liegt: fixieren
    }
  }

  /**
   * Den aktiven Spielstein auf dem rechten Brett rotieren, um delta_angle Grad
   */
  const rotateActive = (delta_angle) => {
    if (activeShape.length > 0) {
      let active = activeShape[0];
      // Aktive Shape rotieren; testen, ob Shape in Zielkonfiguration ist und evtl. dort fixieren
      active.rotate(delta_angle);
      fixCorrectlyPlaced(active);
    } else {
      console.log('No active shape');
    }
  }

  /**
   * Spiegelt einen aktiven Spielstein auf dem rechten Brett
   */
  const flipActive = (axis) => {
    if (activeShape.length > 0) {
      let active = activeShape[0];
      // Aktive Shape spiegeln; testen, ob Shape in Zielkonfiguration ist und evtl. dort fixieren.
      active.flip(axis);
      fixCorrectlyPlaced(active);
    } else {
      console.log('No active shape');
    }
  }

  /**
   * Falls die Shape korrekt platziert und ausgerichtet ist, wird dies im Gamestate vermerkt
   * und der zugehörige Button versteckt, um weitere Änderungen zu verhindern.
   */
  const fixCorrectlyPlaced = (shape_to_check) => {
    if (placedShapes.find(s => s.name == shape_to_check.name) && // Stein muss rechts liegen ...
        isCorrectlyPlaced(shape_to_check)) { // ... und an der richtigen Stelle auf dem Board sein
      if (gameState.correctly_placed.filter(shape => shape.name === shape_to_check.name).length === 0){
        placementSuccessful()
        dispatch({type: 'pieceAtGoal', piece: shape_to_check});
      }
      setActiveShape([]);
    }
  }

  /**
   * Testet, ob ein Spielstein an der richtigen Position in der richtigen Ausrichtung liegt.
   * {zu testendes PentoShape object} shape
   */
  const isCorrectlyPlaced = (shape) => {
    let goalCoords = configPerShape(finalShape, pento_config.n_blocks);

    goalCoords = grid_cell_to_coordinates(goalCoords['x'] + goalCoords['coords'][shape.type]['x'],
        goalCoords['y'] + goalCoords['coords'][shape.type]['y'],
        pento_config.block_size);
    if (shape.x != goalCoords[0] || shape.y != goalCoords[1]) {return false; };
    // Die Zielrotation ist 0. Das 'rotation'-Attribut eines Steins kann jedoch nicht einfach
    // verwendet werden, da der Wert bei Verwendung einer Spiegelung ('flip') verfälscht
    // werden kann. Hier wird eine etwas 'hacky' Lösung verwendet: Es wird eine neue Shape
    // desselben Typs erstellt und die Blockanordnung abgeglichen.
    let dummy_shape = createNewPentoPieceInShape(finalShape, pento_config, shape.type, "black", -1);
    // Überprüfe Spiegelung
    if (shape.is_mirrored !== dummy_shape.is_mirrored) { return false; };
    // Überprüfe Rotation
    let shape_grid = shape.get_internal_grid();
    let dummy_grid = dummy_shape.get_internal_grid();
    for (let row = 0; row < shape.get_grid_height(); row++) {
      for (let col = 0; col < shape.get_grid_width(); col++) {
        if (dummy_grid[row] == undefined ||
            dummy_grid[row][col] == undefined || // sollte nicht vorkommen: Größen der internen Matrizen ('grid') stimmen nicht überein
            dummy_grid[row][col] != shape_grid[row][col]) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Einen auf dem rechten Spielbrett aktiven Spielstein auf einem Quadrat 'einrasten' lassen, sodass
   * der Stein auf dem Feld bleibt und am Hintergrundgitter ausgerichtet ist.
   */
  const lockActiveOnGrid = () => {
    if (activeOnRightBoard()) {
      let active = activeShape[0];
      // Stein auf einem Quadrat einrasten lassen
      let new_x = Math.floor(((active.x - pento_config.x) / pento_config.block_size) + 0.5) * pento_config.block_size;
      let new_y = Math.floor(((active.y - pento_config.y) / pento_config.block_size) + 0.5) * pento_config.block_size;
      active.moveTo(new_x, new_y);
      dispatch({type: 'updateCoords', x:new_x, y:new_y});
    } else {
      console.log('No active shape');
    }
  }

  const startGame = () => {
    setFinalShape('elephant')
    console.log("from start game")
    console.log(finalShape)
    setGameTime(pento_config.game_time)
    start('elephant')
  }

  const startDemo = () => {
    setFinalShape('block')
    setGameTime(pento_config.demo_time)
    start('block')
  }

  /**
   * Diese Methode wird aufgerufen, um das Spiel zu starten (entweder durch Button-klick oder durch Event vom Roboter)
   */
  const start = (shape) => {
    dispatch({type: "resetGame"});
    // Alle aktuellen Spielsteine auf dem rechten Board löschen
    setPlacedShapes([]);
    // ALle aktuell ausgewählten Spielsteine löschen
    setActiveShape([]);

    let [origin, goal] = generateShape(shape, pento_config);
    setInitialShapes(origin);

    // Registriere Template Teile als korrekt platziert
    setPlacedShapes(goal);
    for (let temp_piece of goal) {
      dispatch({type: 'pieceAtGoal', piece: temp_piece})
    }

    dispatch({type: 'gameStart'})
    if (gameTimeHandler.current){
      clearInterval(gameTimeHandler.current)
    }
    gameTimeHandler.current = setInterval(() => {
      dispatch({type: 'refreshTime'});
    }, 500)
  };

  /**
   * Diese Methode plaziert einen Spielstein an der richtigen Position auf dem rechten Spielbrett.
   * Wird entweder durch Button-Klick oder Event vom Roboter aufgerufen.
   */
  const placeSelected = () => {
    console.log("Trying to place selected piece")
    if (activeShape.length > 0) {
      let selected_shape = activeShape[0].name;
      let to_replace = null;
      initialShapes.forEach(el => {
        if (el.name == selected_shape) {
          to_replace = el
        }
      });

      // Falls der Spielstein auf dem linken Brett nicht gefunden wurde, gibt es nichts zu tun
      if (!to_replace) { return; }

      // Kopie des aktiven Stein erstellen, mit anderer Position, aber gleicher Rotation und Spiegelung
      let new_shape = createNewPentoPieceInShape("upper_left_corner", pento_config, to_replace.type, to_replace.color, to_replace.id);

      // _is_mirrored speichert, ob geflipped wurde, is_mirrored gibt für symmetrische Shapes immer false zurück
      if (to_replace._is_mirrored) { new_shape.flip('vertical'); }
      // wie beim Generieren: erst rotieren, dann spiegeln
      if (to_replace.rotation !=  0) { new_shape.rotate(to_replace.rotation); }

      const newPiece = pentoPieceToObj(new_shape.name, new_shape.type, new_shape.color, new_shape.x, new_shape.y);
      dispatch({type: 'addToRightBoard', piece: newPiece});
      dispatch({type: 'removeFromLeftBoard', piece: to_replace});

      setPlacedShapes(placedShapes.concat(new_shape));
      setInitialShapes(initialShapes.filter(item => item.name !== to_replace.name));

      // Spielstein der rechts gesetzt wurde wird aktiv
      setActiveShape([new_shape]);
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
    }

    // Setzt den Alert für ein gewonnenes / verlorenes Spiel
    if (['lost', 'won'].includes(gameState.game.status)){
      sendDataToFurhat()
      togglePopup();
      if (gameTimeHandler.current){
        clearInterval(gameTimeHandler.current)
      }
    }
  }, [gameState.game.status]);

  /**
   * Dies wird getriggert, wenn es eine Änderung im Spielstatus oder der Liste mit Steinen auf dem linken Board gibt
   */
  useEffect(() => {
    let shape_config = configPerShape(finalShape, pento_config.n_blocks)
    // Wenn auf beiden Boards keine Steine mehr verfügbar sind (alle korrekt plaziert sind)
    // und das Spiel noch läuft, haben wir gewonnen
    if (gameState.game.status === 'ongoing'
        && gameState.correctly_placed?.length === 2*Object.keys(shape_config["coords"]).length) {
      dispatch({type: 'gameWon'});
    }

    if (gameState.game.status === 'ongoing' && window.furhat) {
      // Wir subscriben zu dem Event, das vom Roboter gesendet werden kann, um einen Spielstein auszuwählen
      window.furhat.subscribe('selectPiece', function (params) {
        selectPentoPiece(params.piece)
      })
    }

  }, [gameState.correctly_placed, gameState.game.status, activeShape]); //placedShapes

  /**
   * Hier werden Änderungen im aktuell ausgewählten Spielstein an die richtigen Stellen kommuniziert.
   */
  useEffect(() => {
    if (activeShape && activeShape.length > 0) {
      dispatch({type: 'selectPiece', piece: activeShape[0]});
      dispatch({type: 'updateCoords', x:activeShape[0].x, y:activeShape[0].y});
    }
    else {
      dispatch({type: 'deselectPiece'});
      dispatch({type: 'updateCoords', x:-1, y:-1});
    }

    if (gameState.game.status === 'ongoing' && window.furhat) {

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

      // Wir subscriben zu dem Event, das vom Roboter gesendet werden kann, um den aktuell ausgewählten Spielstein
      // auf dem rechten Board zu bewegen
      window.furhat.subscribe('moveSelected', function (params) {
        startMove(params.dir)
        if (params.dist !== undefined) {
            setTimeout(stopMove, params.dist*(pento_config.block_size/MOVESPEED)*MOVEFREQ);
        }
      })

      // Wir subscriben zu dem Event, das vom Roboter gesendet werden kann, um den aktuell ausgewählten Spielstein
      // auf dem rechten Board um den angegebenen Winkel zu drehen
      window.furhat.subscribe('rotateSelected', function (params) {
        rotateActive(params.angle)
      })

      // Wir subscriben zu dem Event, das vom Roboter gesendet werden kann, um den aktuell ausgewählten Spielstein
      // auf dem rechten Board an der momentanen Stelle abzulegen
      window.furhat.subscribe('placeSelected', function () {
        stopMove()
      })

      // Wir subscriben zu dem Event, das vom Roboter gesendet werden kann, um den aktuell ausgewählten Spielstein
      // auf dem rechten Board horizontal oder vertikal zu spiegeln
      window.furhat.subscribe('flipSelected', function (params) {
        flipActive(params.axis)
      })

      window.furhat.subscribe('getHint', function () {
        getHint()
      })

      window.furhat.subscribe('removeHint', function() {
        removeHint()
      })
    }
  }, [activeShape]);

  /** Einmal die Sekunde beim Update der Spielzeit wird der aktuelle Spielstand an Furhat gesendet */
  useEffect(() => {
    sendDataToFurhat()
  }, [gameState.game.time, gameState.left_board]);

  /** Wenn die Web-UI initialisiert wird, verbinden wir uns mit dem Roboter. */
  useEffect(() => {
    Furhat(function (furhat) {
      window.furhat = furhat
      // We subscribe to the event to start the game
      furhat.subscribe('startGame', function () {
        startGame()
      });
      // We subscribe to the event to start a short demo game
      furhat.subscribe('startDemo', function () {
        startDemo()
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
   * ** EVENTS FÜR FURHAT **
   */

  const sendDataToFurhat = () => {
    if(window.furhat) {
      window.furhat.send({
        event_name: "GameStateUpdate",
        data: JSON.stringify(gameState)
      })
    }
  }

  const placementSuccessful = () => {
    if(window.furhat) {
      window.furhat.send({
        event_name: "placementSuccessful"
      })
    }
  }

  /**
   * ** WEBSITE LAYOUT KOMPONENTEN **
   */

  /** Hier erzeugen wir unseren Popup für das Ende des Spieles */
  const Popup = props => {
    return (
        <div className="popup-box">
          <div className="box">
            <b>{props.title}</b>
            <p>{props.message}</p>
            <button onClick={props.handleClose}>Okay</button>
          </div>
        </div>
    );
  };

  /**
   * Rendert die Buttons unter dem Game-Board, mit denen zu Testzwecken einzelne Teile ausgewählt werden können.
   * Nicht gerendert werden Buttons für Teile, die als Template dienen.
   */
  const renderButtons = () => {
    return initialShapes.concat(placedShapes.filter(
        s => pento_config.get_color_name(s.color) !== pento_config.templ_col)
    ).sort().map(element => {
      return <button
              id={"pento_" + element.type}
              style={{ visibility: gameState.correctly_placed.find(shape => shape.name == element.name) ? 'hidden':'hidden'}}
              onClick={() => {selectPentoPiece(element.name);}}>
                {pento_config.get_color_name(element.color)} {element.type}
            </button>
    })
  };


  /** Hier werden die einzelnen Komponenten in der Web-UI angezeigt */
  return (
      <div className="App">
        <div className="twelve columns">
          <h5>Pentomino Game</h5>
        </div>
        {isPopupOpen && gameState.game.status === 'won' && <Popup
            handleClose={togglePopup}
            title="Congratulations!"
            message="You won this round of Pentomino."
        />}
        {isPopupOpen && gameState.game.status === 'lost' && <Popup
            handleClose={togglePopup}
            title="The time is up!"
            message="But there is always a next time."
        />}
        <div className="row">
          <div className="six columns">
          </div>
          <div className="six columns">
            <div style={{ color: "#555", fontSize: "16px" }}>Game State: {gameState.game.status}</div>
            <div style={{ color: "#555", fontSize: "16px" }}>Remaining Game Time: {gameState.game.time}</div>
          </div>
        </div>
        <hr />
        <div className="row">
          <div className="five columns">
            <PentoBoard shapes={initialShapes}
                        activeShape={activeOnLeftBoard() ? activeShape[0]:null}
                        text={"Initial"}
                        config={pento_config}
            />
          </div>
          <div className="two columns">
            <button id="startBtn" onClick={() => startDemo()}>Start New Demo</button>
            <button id="startBtn" onClick={() => startGame()}>Start new game</button>
            <button id="placeBtn" onClick={() => placeSelected()}>Place selected</button>
            <button id="placeBtn" onClick={() => deselect()}>Deselect Piece</button>
            <hr/>
            <button id="leftBtn" onClick={() => rotateActive(-90)} style={{ fontSize: "15px" }}>{'\u21b6'}</button>
            <button onClick={() => startMove('up')} style={{margin: "5px"}}>{'\u25b2'}</button>
            <button onClick={() => rotateActive(90)} style={{ fontSize: "15px" }}>{'\u21b7'}</button>
            <br/>
            <button onClick={() => startMove('left')}>{'\u25c0'}</button>
            <button onClick={() => stopMove()} style={{ fontSize: "20px", margin: "5px" }}>{'\u2613'}</button>
            <button onClick={() => startMove('right')}>{'\u25b6'}</button>
            <br />
            <button onClick={() => flipActive('horizontal')} style={{ fontSize: "14px" }}>{'\u21c5'}</button>
            <button onClick={() => startMove('down')} style={{margin: "5px"}}>{'\u25bc'}</button>
            <button onClick={() => flipActive('vertical')} style={{ fontSize: "14px" }}>{'\u21c6'}</button>
          </div>
          <div className="five columns">
            <PentoBoard shapes={placedShapes}
                        activeShape={activeOnRightBoard() ? activeShape[0] : null}
                        text={"Elephant"}
                        config={pento_config}
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
