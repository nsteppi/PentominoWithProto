/**
 * general.kt
 * Contains general states as a foundation to be extended upon.
 *
 * Wencke Liermann, Lisa Plagemann, Niklas Stepczynski
 * WiSe 20/21
 * Kotlin 1.3.70
 * Windows 10
 */

package furhatos.app.pentominowithfurhat.flow

import com.google.gson.Gson
import furhatos.app.pentominowithfurhat.GameState
import furhatos.flow.kotlin.*
import furhatos.flow.kotlin.voice.PollyNeuralVoice
import furhatos.gestures.Gestures.GazeAway
import furhatos.skills.HostedGUI
import furhatos.util.*


/** Defines Pentomino Web-UI */
val GUI = HostedGUI("Pentomino", "assets/pentomino", 3000)
/** Defines whether Furhat is in sync with the Web-UI */
var UPTODATE = true


/**
 * Wait after send operation with objective [name]
 * until furhat received an update on the game state.
 */
fun sendWait(name: String) = state(GameRunning){
    onEntry {
        UPTODATE = false
        var time_out = 0
        send(name)
        while (!UPTODATE){
            delay(100)
            time_out += 100
            if (time_out > 20000) {
                println("Updating Information from web interface failed")
                goto(Idle)
            }
        }
        terminate()
    }
}


/**
 * Furhat waits for a user.
 *
 * Incoming Transitions from: GameFinished, Greeting, Start
 * Outgoing Transitions to: Greeting, Start
 *
 * Enter while: Attending Nobody
 * Leave while: Attending User
 */
val Idle: State = state {

    init {
        // voice setting
        if (PollyNeuralVoice.Matthew().isAvailable) {
            val yourFriendMatthew = PollyNeuralVoice.Matthew()
            yourFriendMatthew.style = PollyNeuralVoice.Style.Conversational
            furhat.setVoice(yourFriendMatthew)
        } else {
            furhat.setVoice(Language.ENGLISH_US, Gender.MALE)
        }
        // texture setting
        // this does not raise an error even if 'proto' is missing
        furhat.setTexture("Proto")

        // speech recognition phrases
        furhat.setSpeechRecPhrases(listOf(
            "furhat", "turn", "rotate", "spin", "tilt",
            "whirl", "pivot", "swing", "twist",
            "mirror", "reflect", "piece"
        ))

        if (users.count > 0) {
            furhat.attend(users.random)
            goto(Greeting)
        }
    }

    onTime(repeat=10000..15000) {
        furhat.gesture(
            listOf(lookDown(), GazeAway, LookAround).shuffled().take(1)[0],
            async = false
        )
    }

     // Start by looking for a user that has never before declined
     // furhat's invitation to play a game. If you can't find one,
     // try to just choose any user.
    onTime(repeat=12000) {
        val newUsers = users.list.filterNot { it.saidNo }
        if (newUsers.isNotEmpty()) {
            furhat.gesture(ReturnToNormal, priority = 2)
            furhat.attend(newUsers.shuffled().take(1)[0])
            goto(Greeting)
        }
        if (users.count > 0) {
            furhat.gesture(ReturnToNormal, priority = 2)
            furhat.attend(users.random)
            goto(Start)
        }
    }

    onUserEnter {
        furhat.gesture(ReturnToNormal, priority = 2)
        furhat.attend(it)
        goto(Greeting)
    }

    onUserLeave {
        it.saidNo = false
    }
}


/**
 * Furhat manages user arrival and departure.
 *
 * Parent of: Explanation, GameFinished, GameRunning, Greeting, Start
 */
val Interaction: State = state {

    onUserLeave(instant = true) {
        it.saidNo = false
        it.played = false
        if (users.count > 0) {
            if (it == users.current) {
                furhat.attend(users.other)
                goto(Greeting)
            } else {
                furhat.glance(it)
            }
        } else {
            furhat.attendNobody()
            goto(Idle)
        }
    }

    onUserEnter {
        val oldUser = users.current
        furhat.attend(it)
        furhat.hush()
        furhat.say(
            PollyNeuralVoice.Matthew().whisper(
                "We are playing a game. Please don't interrupt us."
            )
        )
        furhat.attend(oldUser)
        reentry()
    }
}


/**
 * Furhat stays in contact with the Web-UI.
 *
 * Parent of: GatherInformation, GetInformation, PieceSelected, SelectPiece, VerifyInformation
 */
val GameRunning : State = state(Interaction) {

    onEvent("GameStateUpdate", instant = true) {
        // get information from Web-UI
        // transform json-structure received from the Web-UI to intern Kotlin-structure
        val gson = Gson()
        val latestGameData:  GameState.Info = gson
            .fromJson(it.get("data").toString(), GameState.Info::class.java)

        users.current.left_state = latestGameData.left_board
        users.current.right_state = latestGameData.right_board
        users.current.correctly_placed = latestGameData.correctly_placed

        // check game status to signal end of game if the case
        if (latestGameData.game.status in listOf("won","lost")) {
            send("placeSelected")
            goto(GameFinished)
        }
        // select and remember new random element
        if (users.current.left_state.isNotEmpty()) {
            val randomPiece = users.current.left_state.shuffled().take(1)[0]
            users.current.rand_piece_name = randomPiece.name
            users.current.rand_piece_loc = randomPiece.location
            users.current.rand_piece_type = randomPiece.type
            users.current.rand_piece_color = randomPiece.color
        }
        UPTODATE = true
    }
}
