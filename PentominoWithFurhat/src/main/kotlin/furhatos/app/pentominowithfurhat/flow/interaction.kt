/**
 * interaction.kt
 * Dialog state modelling on top of the abstract parent states in general.kt
 *
 * Wencke Liermann, Lisa Plagemann, Niklas Stepczynski
 * WiSe 20/21
 * Kotlin 1.3.70
 * Windows 10
 */

package furhatos.app.pentominowithfurhat.flow

import furhatos.app.pentominowithfurhat.nlu.*
import furhatos.event.Event
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures.BigSmile
import furhatos.gestures.Gestures.BrowFrown
import furhatos.gestures.Gestures.BrowRaise
import furhatos.gestures.Gestures.Nod
import furhatos.gestures.Gestures.Smile
import furhatos.gestures.Gestures.Surprise
import furhatos.gestures.Gestures.Thoughtful
import furhatos.gestures.Gestures.Wink
import furhatos.nlu.common.*
import furhatos.records.Location
import khttp.async


const val maxPieces = 12  // how many PentoPieces are on the board at game start

/** location of the tablet in meter */
// x: how far to the right(positive) or left(negative) of the robot
// y: difference between eye level of the robot and level of the object
// e.g. given my eye level is 1.50m, if I want to describe the position of something on the ground y would be -1.50m
// z: how far in front of the robot
val leftBoard = Location(-0.1, -0.35, 1.0)
val rightBoard = Location(0.1, -0.35, 1.0)


/**
 * Incoming Transitions from: Idle, Interaction
 * Outgoing Transitions to: Idle, Start
 *
 * Enter while: Attending User
 * Leave while: Attending User or Nobody(-> Idle)
 */
val Greeting : State = state(Interaction) {

    onEntry {
        furhat.gesture(EmpatheticSmile)
        furhat.ask({
            random {
                +"Hi there."
                +"How are you doing?"
                +"Welcome!"
                +"Thanks for visiting me in my laboratory."
                +"So nice for you to come and see me."
            }
        })
    }

    onResponse {
        goto(Start)
    }

    onNoResponse {
        furhat.gesture(questioning(duration = 6.0), async = false)
        furhat.attendNobody()
        goto(Idle)
    }
}


/**
 * Incoming Transitions from: Idle, Greeting
 * Outgoing Transitions to: Idle, Explanation
 *
 * Enter while: Attending User
 * Leave while: Attending User or Nobody(-> Idle)
 */
val Start : State = state(Interaction) {

    onEntry {
        if (users.current.saidNo) {
            furhat.gesture(BigSmile(strength = 0.4, duration = 5.0))
            furhat.ask("Have you changed your mind?")
        }
        furhat.gesture(Smile, async=false)
        furhat.ask{
            +awaitAnswer(duration=5.0)
            +"Do you want to play a game?"}
    }

    onResponse<Yes>{
        send("startGame")
        furhat.gesture(happy(strength = 0.5, duration = 1.5))
        delay(1500)
        users.current.saidNo = false
        if (users.current.played) {
            delay(1000)
            goto(GatherInformation)
        } else {
            goto(Explanation)
        }
    }

    onResponse<No>{
        furhat.gesture(hurt(duration=3.0))
        furhat.sigh()
        furhat.deflated("Too bad.")
        delay(500)
        furhat.gesture(Smile)
        delay(500)
        furhat.gesture(Wink, async=false)
        furhat.say {
            random {
                +"A little busy, it seems."
                +"Maybe next time."
                +"Just come over whenever you have time."
            }
        }
        users.current.saidNo = true
        furhat.attendNobody()
        goto(Idle)
    }
}


/**
 * Incoming Transitions from: Explanation, Start
 * Outgoing Transitions to: Explanation, GatherInformation
 *
 * Enter while: Attending User
 * Leave while: Attending User
 */
val Explanation : State = state(Interaction) {

    onEntry {
        furhat.attend(leftBoard)
        val textFile = javaClass.getClassLoader().getResource("SimpleDescription")
                       .readText(charset = Charsets.UTF_8).split("\n")
        textFile.forEachIndexed { i, line ->
            if (i == 2) {
                furhat.glance(users.current, 2000)
            }
            furhat.say(furhat.voice.prosody(line, rate=0.9))
            delay(500)
        }
        furhat.attend(users.current)
        delay(1000)
        furhat.ask{
            +awaitAnswer(duration=3.0)
            +"Is everything clear?"}
    }

    onResponse<Yes> {
        users.current.played = true
        send("startGame")
        furhat.gesture(Nod(duration = 0.7), async = false)
        goto(GatherInformation)
    }

    onResponse<No> {
        reentry()
    }

    onResponse {
        furhat.ask {
            random {
                +"Sorry, say that again please."
                +"I don't have to repeat, is that correct?"
            }
        }
    }
}


/**
 * Incoming Transitions from: Explanation, GameFinished, PieceSelected, VerifyInformation
 * Outgoing Transitions to: PieceSelected, SelectPiece
 *
 * Enter while: Attending User
 * Leave while: Attending User
 */
val GatherInformation : State = state(GameRunning) {

    init {
        furhat.gesture(Smile, async=false)
        furhat.say {
            random {
                +"Great. Let's get started."
                +"Ok, here we go."
                +"Glad you found the time."
            }
        }
        delay(1000)
    }

    onEntry {
        furhat.glance(leftBoard)
        users.current.roundKnowledge = KnowledgeBase()
        if (users.current.state.size == maxPieces || users.current.state.size <= 0) {
            furhat.ask("Which piece do you want to start with?", timeout = 20000)
        } else {
            if (users.current.state.size == 1) {
                furhat.say("Only one piece to go lilo.")
                send("selectPiece", mapOf("piece" to users.current.state[0].name))
                furhat.attend(leftBoard)
                goto(PieceSelected) //TODO: keine abfrage
            } else {
                furhat.ask({
                    random {
                        +"Which piece do you want to select?"
                        +"Please describe a piece to me!"
                        +"We already made it to piece number ${(maxPieces - users.current.state.size) + 1}. What's next?"
                        +"Please describe another piece."
                        +"New description, please!"
                        +"Elaborate on another piece, please."
                    }
                }, timeout = 20000)
            }
        }
    }
/**
    onResponse<Last> {
        if (users.current.state.size == 1) {
            send("selectPiece", mapOf("piece" to users.current.state[0].name))
            furhat.attend(leftBoard)
            goto(PieceSelected)
        }
        propagate()  // enter the onResponse block below
    }*/

    onResponse {
        users.current.candidates = users.current.state
        users.current.roundKnowledge.color = it.findFirst(Colors())
        users.current.roundKnowledge.shape = Shapes.getShape(it.findAll(Shapes()), it.text)
        users.current.roundKnowledge.position = Positions.toCompPosition(it.findAll(Positions()))
        if (users.current.roundKnowledge == KnowledgeBase()) {
            furhat.gesture(Thoughtful, async=false)
            furhat.ask{
                random{
                    +"I didn't get it. Which piece did you talk about?"
                    +"Sorry. I didn't understand. What piece?"
                    +"Could you rephrase that?"
                    +"I am having trouble understanding. Please try again!"
                }
            }
        }
        goto(SelectPiece)
    }

    onNoResponse {
        furhat.gesture(questioning(duration = 2.0), async = false)
        furhat.say {
            random {
                +"No need to hesitate. Here, let me help you."
                +"Don't take the game too serious. Let me show you how to do it."
                +"Wow, that was an awkward pause."
                +"You seem to be stuck. I will come to rescue."
            }
        }
        furhat.attend(leftBoard)
        send("selectPiece", mapOf("piece" to users.current.rand_piece_name.toString()))
        if (users.current.rand_piece_loc.y >= 0) {
            furhat.say("This is the ${users.current.rand_piece_color} " +
                      "${users.current.rand_piece_type} piece " +
                      "${Positions.toString(users.current.rand_piece_loc)} of the field.")
            }
        goto(PieceSelected)
    }
}


/**
 * Incoming Transitions from: GatherInformation, GetInformation
 * Outgoing Transitions to: PieceSelected, VerifyInformation
 *
 * Enter while: Attending User
 * Leave while: Attending Location
 */
val SelectPiece : State = state(GameRunning) {

    // apply filters to candidates, if a filter is too strict (no remaining candidates) it is ignored
    onEntry {
        var ignoredInformation = false
        furhat.attend(leftBoard)
        users.current.candidates  = users.current.state
        // collect pieces matching the extracted vague color category
        if (users.current.roundKnowledge.color != null) {
            val newCandidates = users.current.candidates.filter {
                    candi -> Colors.colorSuper(candi.color) == users.current.roundKnowledge.color!!.colorSuper
            }
            if (newCandidates.isNotEmpty()) {
                users.current.candidates = newCandidates
            }
            else {
                ignoredInformation = true
            }
        }
        // additionally remove all pieces the position of which does not match the extracted one
        if (users.current.roundKnowledge.position != Positions()) {
            val newCandidates = users.current.candidates.filter {
                    candi -> users.current.roundKnowledge.position!!.includedIn(candi.location)
            }
            if (newCandidates.isNotEmpty()) {
                users.current.candidates = newCandidates
            }
            else {
                ignoredInformation = true
            }
        }
        // additionally remove all pieces the shape of which does not match the extracted one
        if (users.current.roundKnowledge.shape != null) {
            val newCandidates = users.current.candidates.filter {
                    candi -> candi.type == users.current.roundKnowledge.shape!!.shape
            }
            if (newCandidates.isNotEmpty()) {
                users.current.candidates = newCandidates
            }
            else {
                ignoredInformation = true
            }
        }
        // finally remove all pieces the exact color of which does not match the extracted one
        if (users.current.roundKnowledge.color != null) {
            val newCandidates = users.current.candidates.filter {
                    candi -> candi.color == users.current.roundKnowledge.color!!.color
            }
            if (newCandidates.isNotEmpty()) {
                users.current.candidates = newCandidates
            }
        }

        if (users.current.candidates.size == 1) {
            send("selectPiece", mapOf("piece" to users.current.candidates[0].name))
            println(ignoredInformation.toString())
            if (ignoredInformation) {
                furhat.say(
                    "I could not find the ${users.current.roundKnowledge}."
                )
                furhat.say(
                    "But I found the ${users.current.candidates[0].color} " +
                            "${users.current.candidates[0].type} piece " +
                            "${Positions.toString(users.current.candidates[0].location)}."
                )
            }
            goto(PieceSelected)
        } else {
            if (!ignoredInformation) {
                goto(VerifyInformation)
            }
            else {
                furhat.attend(users.current)
                furhat.gesture(BrowFrown, async = false)
                furhat.say(
                        "I am sorry, but a ${users.current.roundKnowledge} does not exist.")
                goto(GatherInformation)
            }
        }
    }

    // no functionality except aid in debugging
    onExit {
        println("")
        println("Extracted Intents:")
        println("Exact Color: ${users.current.roundKnowledge.color?.color}")
        println("Abstract Color: ${users.current.roundKnowledge.color?.colorSuper}")
        println("Shape: ${users.current.roundKnowledge.shape}")
        println("Position: ${users.current.roundKnowledge.position?.toString(detailed = true)}")
    }
}


/**
 * Incoming Transitions from: SelectPiece
 * Outgoing Transitions to: GatherInformation, GetInformation
 *
 * Enter while: Attending Location
 * Leave while: Attending User
 */
val VerifyInformation : State = state(GameRunning) {

    onEntry {
        furhat.glance(users.current)
        furhat.say("Ok. I seem to have missed out on some information.")
        furhat.say("Here is, what I have:")
        furhat.glance(users.current)
        furhat.say(
            "We are looking for a ${users.current.roundKnowledge}.")
        furhat.attend(users.current)
        furhat.ask("Any wrong information?")
    }

    onResponse<Yes> {
        furhat.gesture(BrowFrown)
        furhat.glance(leftBoard)
        furhat.say {
            random {
                +"Oh I am sorry!"
                +"Sorry, I got confused."
                +"Oh look! A butterfly!"
            }
        }
        goto(GatherInformation)
    }

    onResponse<No> {
        goto(GetInformation)
    }
}


/**
 * Incoming Transitions from: VerifyInformation
 * Outgoing Transitions to: SelectPiece
 *
 * Enter while: Attending User
 * Leave while: Attending User
 */
val GetInformation : State = state(GameRunning) {

    onEntry {
        furhat.gesture(EmpatheticSmile)
        // create list of unique colors present for the candidates
        val remainingColors = users.current.candidates.foldRight(listOf<String>()) {
                it, acc -> if (it.color !in acc) (acc + it.color) else acc
        }
        // check whether there was no color present more than once
        // if yes the attribute color can uniquely identify a piece
        if (remainingColors.size == users.current.candidates.size
            && (users.current.roundKnowledge.color == null) //TODO: discuss with group
        ) {
            furhat.ask("Please, tell me about the exact color of the piece!")
        }
        // create list of unique positions present for the candidates
        val remainingPositions = users.current.candidates.foldRight(listOf<String>()) {
                it, acc -> if (Positions.toString(it.location) !in acc)
                                (acc + Positions.toString(it.location)) else acc
        }
        if (remainingPositions.size == users.current.candidates.size
            && (users.current.roundKnowledge.position == Positions()) //TODO: discuss with group
        ) {
            furhat.ask("Tell me about the exact position of the piece!")
        }
        // assumption: shape is always unique, so we can use it if nothing else is
        furhat.ask("Try to explain the shape of the piece with a letter.")
    }

    onResponse<Colors> {
        users.current.roundKnowledge.color = it.intent
        goto(SelectPiece)
    }

    onResponse<Positions> {
        users.current.roundKnowledge.position = Positions.toCompPosition(it.findAll(Positions()))
        goto(SelectPiece)
    }

    onResponse<Shapes> {
        users.current.roundKnowledge.shape = it.intent
        goto(SelectPiece)
    }
}


/**
 * Incoming Transitions from: GatherInformation, PieceSelected, SelectPiece
 * Outgoing Transitions to: GetInformation, PieceSelected
 *
 * Enter while: Attending Location
 * Leave while: Attending User
 */
val PieceSelected : State = state(GameRunning)  {

    onEntry {
        furhat.glance(users.current, 1500)
        furhat.ask {
            random {
                +"Do you want to place the selected piece?"
                +"Should we play this piece?"
                +"Is this a good choice?"
                +"I will place this piece, okay?"
                +"Let's go with this one, okay?"
                +"Could this be a fitting piece?"
                +"This one?"
            }
        }
    }

    onResponse<Yes> {
        furhat.attend(rightBoard)
        call(sendWait("startPlacing"))
        furhat.say {
            + "Ok."
            + BrowRaise
            random {
                +"Great choice."
                +"What a lovely choice."
                +"I have placed the piece for you."
                +"We are making progress."
                +"This game is so fun."
            }
        }
        furhat.attend(users.current)
        goto(GatherInformation)
    }

    onResponse<No> {
        furhat.attend(users.current)
        send("deselectPiece")
        furhat.gesture(Surprise(strength = 0.5, duration = 1.5))
        delay(1500)
        furhat.say {
            random {
                +"Oh sorry, I misunderstood!"
                +"Shame!"
                +"No? Ok, give me another chance, please!"
                +"Hm!"
                +"We all make mistakes."
            }
        }
        goto(GatherInformation)
    }

    onResponse {
        furhat.glance(users.current)
        furhat.ask("A simple 'Yes' or 'No' is enough.")
    }
}


/**
 * Incoming Transitions from: GameRunning
 * Outgoing Transitions to: Idle, GatherInformation
 *
 * Enter while: *unclear*
 * Leave while: Attending User or Nobody(-> Idle)
 */
val GameFinished : State = state(GameRunning) {

    onEntry {
        furhat.attend(users.current)
        furhat.glance(rightBoard, 2000)
        if (users.current.state.isEmpty()) {
            // wait for furhat to stop speaking
            while (furhat.isSpeaking()) {
                delay(100)
            }
            furhat.gesture(happy(strength = 0.5, duration = 1.5), async = false)
            delay(1000)
            furhat.say {
                random {
                    +"Good job!"
                    +"Great game! Thank you!"
                    +"Nice work, well done!"
                    +"Winner, winner, chicken dinner!"
                    +"Yes! We won!"
                }
            }
        } else {
            furhat.stopSpeaking()
            furhat.gesture(hurt(duration=3.0))
            furhat.sigh()
            delay(1000)
            furhat.say {
                random {
                    +"What a shame!"
                    +"Oh no, we lost! But thanks for playing!"
                    +"The poor elephant!"
                    +"Oh, so close!"
                    +"Even the best of us fail sometimes!"
                    }
            }
        }
        furhat.ask("Want to play another round?")
    }

    onResponse<Yes> {
        send("startGame")
        furhat.say("You seem to be enjoying the game.")
        goto(GatherInformation)
    }

    onResponse<No> {
        users.current.saidNo = true
        furhat.gesture(LookDown(duration = 0.3), async = false)
        furhat.say("Ok. Thank you for your time.")
        furhat.say("Bye bye!")
        furhat.attendNobody()
        goto(Idle)
    }
}
