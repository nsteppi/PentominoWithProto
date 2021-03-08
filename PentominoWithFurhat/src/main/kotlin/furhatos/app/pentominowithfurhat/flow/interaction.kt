package furhatos.app.pentominowithfurhat.flow

import com.google.gson.Gson
import furhatos.app.pentominowithfurhat.GameState
import furhatos.nlu.common.*
import furhatos.flow.kotlin.*


val Start : State = state(Interaction) {

    onEntry {
        furhat.ask("Hi there. Do you want to play a game?")
    }

    onResponse<Yes>{
        send("startGame")
        goto(GameStarted)
    }

    onResponse<No>{
        furhat.say("Too bad. Maybe next time")
    }
}

val GameStarted : State = state(Interaction) {

    onEntry {
        furhat.say("Do you want to select a piece?")
        delay(2000) //Hier ist ein Beispiel dafür, wie wir den aktuellen Flow für eine kurze Wartezeit unterbrechen können
        furhat.say("Sending piece")
        println("We'll send " + users.current.rand_piece) //Hier ist ein Beispiel, wie ihr euch in Kotlin Debug auf der Konsole ausgeben könnt.

        //Hier senden wir ein zufälliges Pentomino-Teil, das wir uns im aktuellen User gespeichert haben, an die Web-UI
        send("selectPiece", mapOf("piece" to users.current.rand_piece.toString()))

        delay(2000) //Hier ist ein Beispiel dafür, wie wir den aktuellen Flow für eine kurze Wartezeit unterbrechen können
        furhat.say("Placing selected piece")
        send("startPlacing")

    }

    /**
     * Hier ist eine simple Methode, die auf das GameStateUpdate-Event aus der Web-UI reagiert und die
     * eingehenden Daten in eine Kotlin-interne Struktur umwandelt.
     */
    onEvent("GameStateUpdate", instant = true) {
        // Hier bekommen wir die Daten aus der Web-UI und schreiben sie zu Debug-Zwecken in die Konsole
        println(it.get("data").toString())

        // Hier benutzen Gson, um die JSON-Struktur aus der Web-UI in unsre interne Kotlin-Struktur zu parsen.
        val gson = Gson()
        val latest_game_data:  GameState.Info = gson.fromJson(it.get("data").toString(), GameState.Info::class.java)

        // Hier ist eine Dummy-Implementation, in der wir uns das aktuelle und das letzte ausgewählte Pento-Piece
        // in der Web-UI speichern und etwas sagen, wenn ein neues Teil ausgewählt wurde.
        val selected_element = latest_game_data.selected;
        users.current.prev_selected = users.current.selected
        users.current.selected = selected_element
        if (users.current.selected != null &&  users.current.prev_selected != null) {
            println(selected_element)
            println(users.current.prev_selected)
            if (selected_element != users.current.prev_selected) {
                furhat.say("The selected element has changed!")
            }
        }

        // Hier speichern wir uns zu Test-Zwecken ein random Element vom linken Board ab, das wir dann
        // mit send() später in der Interaktion auswählen können
        val listPieces = latest_game_data.left_board
        if (listPieces.size > 0) {
            users.current.rand_piece = listPieces.shuffled().take(1)[0].name
        }

    }



}
