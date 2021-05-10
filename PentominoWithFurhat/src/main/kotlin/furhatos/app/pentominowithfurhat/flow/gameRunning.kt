/**
 * gameRunning.kt
 * All dialog components that involve a running game.
 *
 * Wencke Liermann, Lisa Plagemann, Niklas Stepczynski
 * SoSe21
 * Kotlin 1.3.70
 * Windows 10
 */

package furhatos.app.pentominowithfurhat.flow

import furhatos.app.pentominowithfurhat.SharedKnowledge
import furhatos.app.pentominowithfurhat.nlu.*
import furhatos.event.Event
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures
import furhatos.gestures.Gestures.Blink
import furhatos.gestures.Gestures.BrowFrown
import furhatos.gestures.Gestures.BrowRaise
import furhatos.gestures.Gestures.Smile
import furhatos.gestures.Gestures.Thoughtful
import furhatos.nlu.common.DontKnow
import furhatos.nlu.common.No
import furhatos.nlu.common.Yes
import furhatos.records.Location


/** location of the tablet in meter */
// x: how far to the right(positive) or left(negative) of the robot
// y: difference between eye level of the robot and level of the object
// e.g. given my eye level is 1.50m,
// if I want to describe the position of something on the ground y would be -1.50m
// z: how far in front of the robot
val LEFT_BOARD = Location(-0.14, -0.36, 0.45)
val RIGHT_BOARD = Location(0.14, -0.36, 0.45)


/**
 * Furhat collects and processes all information necessary
 * to identify a target piece.
 *
 * Incoming Transitions from: PieceSelected, PlaceSelected, GatherInformation, VerifyInformation
 * Outgoing Transitions to: PieceSelected, GatherInformation, VerifyInformation
 *
 * Enter while: Attend User
 * Leave while: Attend LeftBoard
 */
val GatherInformation : State = state(GameRunning) {
    // only executed once per session
    init {
        furhat.gesture(Smile, async = false)
        furhat.say {
            random {
                +"Let's get started."
                +"Here we go."
                +"Glad you found the time."
            }
        }
        delay(1000)
    }

    // ask for an initial target description
    onEntry {
        furhat.glance(LEFT_BOARD)
        // each Pentomino piece has a corresponding template shape template
        // pieces always occupy the right board, in case we have as many
        // pieces on the right as left, we are in the first game round
        if (users.current.correctly_placed.size == users.current.left_state.size) {
            furhat.ask(
                "Which piece do you want to start with?",
                endSil = 1250, timeout = 20000
            )
        } else {
            // the last piece
            if (users.current.left_state.size == 1) {
                furhat.say("Only one piece to go.")
                send(
                    "selectPiece",
                    mapOf("piece" to users.current.left_state[0].name)
                )
                furhat.attend(LEFT_BOARD)
                goto(PieceSelected)
            } else {
                // half of all pieces are template pieces that are always
                // correctly placed subtract that from all correctly placed
                // pieces to obtain the number of successfully placed real pieces
                val pieceN = (users.current.correctly_placed.size
                            - (users.current.correctly_placed.size
                            + users.current.left_state.size)/ 2) + 1
                furhat.ask({
                    random {
                        +"Which piece do you want to select?"
                        +"Please describe a piece to me!"
                        +"We already made it to piece number $pieceN. What's next?"
                        +"Please describe another piece."
                        +"New description, please!"
                        +"Elaborate on another piece, please."
                    }
                }, endSil = 1250, timeout = 20000)
            }
        }
    }

    // reduce the initial set of candidates, if necessary by
    // inquiring more information from the user
    onResponse {
        // extract any detail on the piece
        users.current.roundKnowledge = SharedKnowledge(it)
        // in case no useful information could be extracted
        if (users.current.roundKnowledge!!.isEmpty()) {
            furhat.gesture(
                listOf(Thoughtful, BrowFrown, awaitAnswer(duration=5.0))
                    .shuffled().take(1)[0], async = false
            )
            furhat.ask({
                random{
                    +"I didn't get it. Which piece did you talk about?"
                    +"Sorry. I didn't understand. What piece?"
                    +"Could you rephrase that?"
                    +"I am having trouble understanding. Please try again!"
                }
            }, endSil = 1250, timeout = 20000)
        }
        furhat.attend(LEFT_BOARD)
        // reset the list of candidates
        users.current.candidates = users.current.left_state.toMutableList()
        // a first filtering based on the initial description
        var ignoredInformation = users.current.roundKnowledge!!
            .findCandidates(users.current.candidates)
        // gather additional information until sufficient or lacking integrity
        while (users.current.candidates.size > 1 && !ignoredInformation) {
            call(VerifyInformation)
            ignoredInformation = users.current.roundKnowledge!!
                .findCandidates(users.current.candidates)
        }
        // with or without ignoring Information we got one candidate left
        if (users.current.candidates.size == 1) {
            send(
                "selectPiece",
                mapOf("piece" to users.current.candidates[0].name)
            )
            // the single candidate does not match at least one detail of the description
            // we select it anyway but inform the user about this deviation
            if (ignoredInformation) {
                furhat.say("I could not find the ${users.current.roundKnowledge}.")
                furhat.say(
                    "But I found the ${users.current.candidates[0].color} " +
                    "${users.current.candidates[0].type} piece " +
                    "${Positions.toString(users.current.candidates[0].location, detailed = true)}."
                )
            }
            goto(PieceSelected)
        } else {
            // several candidates are left but our information do not add up
            furhat.attend(users.current)
            furhat.gesture(BrowFrown, async = false)
            furhat.say(
                "I am sorry, but a ${users.current.roundKnowledge} does not exist.")
            delay(1000)
            reentry()
        }
    }

    // suggest a piece to the user accompanied by a full description
    // which can guide and prime the user
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
        furhat.attend(LEFT_BOARD)
        send(
            "selectPiece",
            mapOf("piece" to users.current.rand_piece_name.toString())
        )
        furhat.say("This is the ${users.current.rand_piece_color} " +
            "${users.current.rand_piece_type} piece " +
            "${Positions.toString(users.current.rand_piece_loc)} of the field.")
        goto(PieceSelected)
    }
}


/**
 * We arrive at this state if additional information is necessary
 * for an unambiguous piece selection. Before asking for it
 * furhat verifies the information integrity.
 *
 * Incoming Transitions from: GatherInformation
 * Outgoing Transitions to: GatherInformation
 *
 * Enter while: Attending LeftBoard
 * Leave while: Attending User
 */
val VerifyInformation : State = state(GameRunning) {

    onEntry {
        furhat.glance(users.current)
        furhat.say("Ok. I seem to be missing some information.")
        furhat.say("Here is, what I have:")
        furhat.glance(users.current)
        furhat.say("We are looking for a ${users.current.roundKnowledge}.")
        furhat.attend(users.current)
        // obtain answer to yes/no question within this single line
        val infoIncorrect = furhat.askYN("Any wrong information?")!!
        if (infoIncorrect) {
            raise("WrongInfo")
        } else {
            raise("CorrectInfo")
        }
    }

    /** Responses */

    onResponse<Colors> {
        users.current.roundKnowledge!!.color = it.intent
        terminate()
    }

    onResponse<Positions> {
        users.current.roundKnowledge!!.position = Positions
            .toCompPosition(it.findAll(Positions()))
        terminate()
    }

    onResponse<Shapes> {
        users.current.roundKnowledge!!.shape = Shapes
            .getShape(it.findAll(Shapes()), it.text)
        if (users.current.roundKnowledge!!.shape == null) {
            // if what was thought to be a shape was only a pronoun
            // go down to the onResponse block
            propagate()
        }
        terminate()
    }

    onResponse {
        furhat.say("Pardon?")
        raise("CorrectInfo")
    }

    /** Events */

    // discard any information that was collected so far and start anew
    onEvent("WrongInfo") {
        furhat.gesture(BrowFrown)
        furhat.glance(LEFT_BOARD)
        furhat.say {
            random {
                +"Oh I am sorry!"
                +"Sorry, I got confused."
                +"Oh look! A butterfly!"
            }
        }
        goto(GatherInformation)
    }

    // ask the user to elaborate on the current target piece
    onEvent("CorrectInfo") {
        furhat.gesture(EmpatheticSmile)
        // determine the attribute that can certainly help disambiguating
        val dprop = users.current.roundKnowledge!!
            .getDisambiguatingProperty(users.current.candidates)
        furhat.ask({
            random {
                +"Please, tell me about the $dprop of the piece!"
                +"Try to explain the $dprop of the piece."
                +"Additional information on the $dprop could help me."
            }
        }, endSil = 1250, timeout = 10000)
    }
}


/**
 * Furhat verifies that the correct piece is selected
 * and upon confirmation moves it to the right board.
 *
 * Incoming Transitions from: GatherInformation
 * Outgoing Transitions to: GatherInformation, PlaceSelected
 *
 * Enter while: Attending LeftBoard
 * Leave while: Attending User (-> GatherInformation), RightBoard(-> PlaceSelected)
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

    // move a correctly selected piece to the left top corner of the right board
    onResponse<Yes> {
        furhat.attend(RIGHT_BOARD)
        call(sendWait("startPlacing"))
        furhat.gesture(
            listOf(BrowRaise, Smile).shuffled().take(1)[0]
        )
        delay(500)
        goto(PlaceSelected)
    }

    // deselect a incorrectly selected piece and go back to the selection process
    onResponse<No> {
        furhat.attend(users.current)
        send("deselectPiece")
        furhat.gesture(
            Gestures.Surprise(strength = 0.5, duration = 1.5),
            async = false
        )
        delay(500)
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
        furhat.glance(users.current, 1500)
        furhat.ask("A simple 'Yes' or 'No' is enough.")
    }
}


/**
 * Same signature as send() but allows to add additional logic.
 */
class Action(val text : String, val param : Map<String, Any> = mapOf()) : Event()


/**
 * After the piece has been positioned in the left top corner,
 * furhat can be instructed to move, rotate and mirror the piece,
 * as well as give hints and reverse and repeat previous actions.
 *
 * Incoming Transitions from: PieceSelected, PlaceSelected
 * Outgoing Transitions to: GatherInformation
 *
 * Enter while: Attending RightBoard
 * Leave while: Attending User
 */
val PlaceSelected : State = state(GameRunning) {

    onEntry {
        delay(1000)
        furhat.attend(users.current)
        furhat.ask ({
            random {
                +"Where do you want me to move the piece?"
                +"Where to?"
                +"Where should I move it?"
                +"Where does this piece go?"
                +"Where does the piece belong?"
                +"How should I move it?"
                +"Can you tell me where to move the piece?"
            }
        }, timeout = 20000)
    }

    /** Responses */

    // InterimResponses allow us to react faster to the user by parsing
    // input as soon as it was given and potentially before it is complete
    // But by applying them we risk that we react before all relevant information
    // has been passed, obviously we could just use the Back intent to reverse a
    // previous malformed action but this could confuse the user therefore we
    // perform actions rotation and mirror that are fallible to this scenario
    // only after the full response has been collected
    onInterimResponse {
        val interimIntent = it.classifyIntent()
        if (interimIntent != null) {
            if (interimIntent.intent is Move
                || interimIntent.intent is Stop
                || interimIntent.intent is Back) {
                raise(interimIntent)
            }
        }
    }

    onResponse<Rotation> {
        raise(Action("rotateSelected", mapOf("angle" to it.intent.dir*it.intent.degree)))
        furhat.listen(timeout = 20000)
    }

    onResponse<Mirror> {
        raise(Action("flipSelected", mapOf("axis" to it.intent.axis)))
        furhat.listen(timeout = 20000)
    }

    onResponse<Move> {
        if (it.intent.dist != null) {
            raise(Action("moveSelected", mapOf("dir" to it.intent.dir as String,
                                                   "dist" to it.intent.dist as Int)))
        } else {
            raise(Action("moveSelected", mapOf("dir" to it.intent.dir as String)))
        }
        furhat.listen(timeout = 20000)
    }

    // stop any movement
    onResponse<Stop> {
        raise(Action("placeSelected"))
        furhat.listen(timeout = 20000)
    }

    // repeat the previous action
    onResponse<Again> {
        if (users.current.prevAction != null) {
            raise(Action(users.current.prevAction as String,
                         users.current.prevParam as Map<String, Any>))
        }
        furhat.listen(timeout = 20000)
    }

    // if a Stop intent was registered too late, the user might wish
    // to perform the opposite of the previous action
    onResponse<Back> {
        if (users.current.prevAction != null) {
            val prevAction = users.current.prevAction!!
            val prevParam = users.current.prevParam!!
            furhat.gesture(Thoughtful, async = false)
            when (prevAction) {
                // get the opposite direction but keep the distance if it exists
                "moveSelected" -> {
                    val counterDir = mapOf("up" to "down", "down" to "up",
                        "left" to "right", "right" to "left", "middle" to "middle")
                    val newValue = counterDir.getValue(prevParam.getValue("dir") as String)
                    // middle has no unambiguous opposite action
                    if (newValue != "middle") {
                        if ("dist" in prevParam) {
                            raise(Action(prevAction,
                                         mapOf("dir" to newValue,
                                               "dist" to prevParam["dist"]!!)))
                        } else {
                            raise(Action(prevAction, mapOf("dir" to newValue)))
                        }
                    } else {
                        furhat.say("There is no going back.")
                    }
                }
                // a flip can be reversed by just performing it again
                "flipSelected" -> {
                    raise(Action(prevAction, prevParam))
                }
                // take the opposite direction but keep the angle
                "rotateSelected" -> {
                    val newValue = -1*prevParam.getValue("angle") as Int
                    raise(Action(prevAction, mapOf("angle" to newValue)))
                }
                // shouldn't be triggered for now
                else -> {
                    furhat.say("There is no going back.")
                }
            }
        }
        furhat.listen(timeout = 20000)
    }

    // If the user plays with a template this block may
    // only help to find a goal position slightly faster.
    // For the game without template a hint is essential, the
    // better a user can remember the hinted at position the
    // less hints they will need and the faster they can finish the game
    onResponse<DontKnow> {
        furhat.attend(users.current)
        furhat.gesture(Blink, async = false)
        furhat.gesture(Blink, async = false)
        delay(500)
        send("getHint")
        furhat.attend(RIGHT_BOARD)
        furhat.say {
            random {
                +"You seem to be in desperate need of some hint. Here."
                +"Let me help you. Here."
                +"That's where the piece goes."
            }
        }
        delay(1500)
        send("removeHint")
        reentry()
    }


    onResponse {
        // One day we might not need the below block but until grammar
        // entities can decide on whether they are greedy or not, we need it
        // (would we add this rule to the grammar, more
        // specific rules wouldn't be applied any longer)
        val regex = Regex(
            """\b((turn)|(rotate)""" +
                    """|(spin)|(tilt)""" +
                    """|(whirl)|(pivot)""" +
                    """|(swing)|(swing)""" +
                    """|(twist)|(perform))\b""", RegexOption.IGNORE_CASE
        )
        if (regex.containsMatchIn(it.speech.text)) {
            raise(Action("rotateSelected", mapOf("angle" to 90)))
        } else {
            furhat.gesture(
                listOf(
                    awaitAnswer(duration = 5.0, strength = 0.8),
                    questioning(duration = 2.0),
                    BrowFrown
                ).shuffled().take(1)[0]
            )
            furhat.say {
                random {
                    +"Again, please."
                    +"Wait, what?"
                    +"Pardon?"
                    +"Sorry?"
                }
            }
        }
        furhat.listen(timeout = 20000)
    }

    // hesitation is considered as a wish for guidance
    onNoResponse {
        raise(DontKnow())
    }

    /** Events */

    // the most recent movement event is remembered
    onEvent<Action>(instant = true) {
        furhat.attend(RIGHT_BOARD)
        if (it.param.isEmpty()) {
            send(it.text)
            furhat.glance(users.current)
        } else {
            send(it.text, it.param)
            users.current.prevAction = it.text
            users.current.prevParam = it.param
        }
    }

    // this event is emitted once a shape could be anchored in its correct place
    // the user is forced to return to the selection process
    onEvent("placementSuccessful") {
        if (Math.random() < 0.3) {
            furhat.gesture(slightSmile(), async = false)
            delay(500)
        }
        furhat.attend(users.current)
        furhat.stopSpeaking()
        furhat.say {
            random {
                +"Great."
                +"We are making progress."
                +"This game is so fun."
                +"Good work!"
                +"Yes. That's it."
                +"Well done!"
                +"Good job!"
                +"Things are taking shape."
            }
        }
        delay(500)
        goto(GatherInformation)
    }
}
