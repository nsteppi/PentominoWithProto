# Pentomino-Spiel

Um das Pentomino-Spiel zu starten, müssen wir es zunächst bauen.

## Voraussetzungen

Ihr müsst npm & node installiert haben. 

## Bauen des Pentomino-Spiels

 1. Terminal öffnen 
 2. cd in den Folder `cd PentominoWithFurhat/assets/pentomino`
 3. `npm install` eingeben um die libraries zu installieren 
    (Achtung: Das müsst ihr nur einmal machen - auch wenn ihr Änderungen in der Web-UI vornehmt muss dieser Schritt nicht wiederholt werden)
 4. `npm run build` um das Pentomino-Spiel zu bauen

Nachdem ihr alle vier Schritte ausgeführt habt solltet ihr in `PentominoWithFurhat/assets/pentomino` einen Ordner `node_modules` und einen Ordner `dist` sehen.


# Skill starten

Um den Skill auszuführen, startet wie gehabt die main-Funktion in `main.kt`. 
Wenn ihr in das Webinterface wechselt solltet ihr oben neben dem Stopp-Button für den Skill auch einen Button `Pentomino` sehen, mit dem ihr das Pentomino-Spiel in einem neuen Tab startet.

# Interaktion zwischen Roboter und GUI

## Roboter -> GUI 
Der Roboter kann mit der Web-UI interagieren, in dem ihr die `send()` Funktion aufruft. UI-seitig sind folgende Funktionen implementiert:

1. `startGame`: Dieses Event hat den gleichen Effekt als wenn ihr im Web-Interface auf den "Start New Game" Button klickt.
2. `selectPiece`: Als Paramter müsst ihr hier der send-Funktion `{piece: "nameOfPiece"}` mitgeben. Ein Beispiel ist in `interaction.kt` in der GameStart -> onEntry bereits implementiert. Der Effekt dieses Events ist das gleiche als wenn ihr im Web-Interface einen der Button unter dem Spielfeld anklickt.
3. `deselectPiece`: Der momentan ausgewählte Pentomino-Stein wird wieder "losgelassen". Hat den gleichen Effekt wie das Drücken des Buttons "Deselect Piece"
4. `startPlacing`: Plaziert den momentan ausgewählten Pentomino-Stein auf dem rechten Board. Hat den gleichen Effekt wie das Drücken des "Place Selected" Buttons.

## GUI -> Roboter
Das Webinterface sendet dem Roboter einmal die Sekunde ein Event, in dem der gesamte Spielstand abgebildet ist. Die Daten haben die folgende Struktur:

`{"left_board":[{"name":String?PieceName,"type":String?PieceType,"color":String?PieceColor,"location":{"x":Int?,"y":Int?}},...],
"right_board":[{"name":String?PieceName,"type":String?PieceType,"color":String?PieceColor,"location":{"x":Int?,"y":Int?}},...],
"game":{"status":String?ongoing/won/lost,"startTime":Int?,"time":Int?TimeLeft},"
selected":String?None/PieceName}`

Ihr habt also eine List von Pentomino-Steinen, die auf dem linken und rechten Board liegen, mit Koordinaten relativ zum Ursprung ihres Boards, außerdem allgemeine Informationen über den Zustand des Games und welches Pentomino-Teil aktuell ausgewählt ist.

In `GameStarted -> onEvent("GameStateUpdate")` seht ihr, wie ihr den Datensatz in eine Kotlin-Datenstruktur umwandeln könnt, die ihr dann einfacher benutzen könnt.

## Anpassungen in der Web-UI vornehmen

Solltet ihr in der Web-UI Änderungen vornehmen wollen (auch wenn das für das Projekt eigentlich nicht erforderlich sein sollte), dann ist `App.js` die Datei, in die ihr schauen wollt.
Denkt dran, dass ihr nach jeder Änderung die Web-UI neu bauen müsst!