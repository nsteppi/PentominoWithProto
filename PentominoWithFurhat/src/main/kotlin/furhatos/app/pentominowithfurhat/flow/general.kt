/**
 * general.kt
 * Contains general states as a foundation to be extended in interaction.kt
 *
 * Wencke Liermann, Lisa Plagemann, Niklas Stepczynski
 * WiSe 20/21
 * Kotlin 1.3.70
 * Windows 10
 */

package furhatos.app.pentominowithfurhat.flow

import com.google.gson.Gson
import furhatos.app.pentominowithfurhat.GameState
import furhatos.app.pentominowithfurhat.nlu.Colors
import furhatos.app.pentominowithfurhat.nlu.Positions
import furhatos.app.pentominowithfurhat.nlu.Shapes
import furhatos.flow.kotlin.*
import furhatos.flow.kotlin.voice.PollyNeuralVoice
import furhatos.gestures.Gestures
import furhatos.gestures.Gestures.GazeAway
import furhatos.nlu.common.No
import furhatos.nlu.common.Yes
import furhatos.skills.HostedGUI
import furhatos.util.*

// Defines pentomino Web-UI
val GUI = HostedGUI("Pentomino", "assets/pentomino", 3000)
var upToDate = true

class KnowledgeBase(
    var color: Colors? = null,
    var shape: Shapes? = null,
    var position: Positions = Positions()
) {
    override fun toString(): String {
        return "${this.color?.color?: run {""}} "+
                "${this.shape?.shape?: run {""}} piece " +
                "${this.position?.toString(detailed = true)}"
    }

    override fun equals(other: Any?): Boolean {
        when (other) {
            is KnowledgeBase -> {
                return this.color == other.color
                        && this.shape == other.shape
                        && this.position == other.position
            }
            else -> return false
        }
    }
}


// wait after send until furhat info was updated
fun sendWait(name: String) = state(GameRunning){
    onEntry {
        upToDate = false
        send(name)
        while (!upToDate){
            delay(50)
        }
        terminate()
    }
}


/**
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
        furhat.setTexture("Proto")

        if (users.count > 0) {
            furhat.attend(users.random)
            goto(Greeting)
        }

    }

    onTime(repeat=10000..15000) {
        furhat.gesture(listOf(LookDown(), GazeAway, LookAround).shuffled().take(1)[0], async = false)
    }

    // First look for a users that has never declined furhat's request before
    // if you can't find one, try to just choose any user
    onTime(repeat=12000) {
        val newUsers = users.list.filterNot { it.saidNo }
        if (newUsers.isNotEmpty()) {
            furhat.gesture(ReturnToNormal, priority = 2)
            furhat.attend(newUsers.shuffled().take(1)[0])
            goto(Greeting)
        }
        if (users.count > 0) {
            furhat.gesture(ReturnToNormal, priority = 2)
            val randomUser = users.random
            furhat.attend(randomUser)
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
        furhat.say(PollyNeuralVoice.Matthew().whisper("Please don't interrupt us."))
        furhat.attend(oldUser)
        reentry()
    }
}


/**
 * Parent of: GatherInformation, GetInformation, PieceSelected, SelectPiece, VerifyInformation
 */
val GameRunning : State = state(Interaction) {

    onEvent("GameStateUpdate", instant = true) {
        // get information from Web-UI and output them on the console for debugging purposes
        // transform json-structure received from the Web-UI to intern Kotlin-structure
        val gson = Gson()
        val latestGameData:  GameState.Info = gson.fromJson(it.get("data").toString(), GameState.Info::class.java)

        users.current.state = latestGameData.left_board
        // check game status to signal end of game if the case
        if (latestGameData.game.status in listOf("won","lost")) {
            goto(GameFinished)
        }
        // select and remember new random element
        if (users.current.state.isNotEmpty()) {
            val randomPiece = users.current.state.shuffled().take(1)[0]
            users.current.rand_piece_name = randomPiece.name
            users.current.rand_piece_loc = randomPiece.location
            users.current.rand_piece_type = randomPiece.type
            users.current.rand_piece_color = randomPiece.color
        }
        upToDate = true
    }
}
