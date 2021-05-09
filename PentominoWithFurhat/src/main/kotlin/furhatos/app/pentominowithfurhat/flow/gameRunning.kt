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
import furhatos.gestures.Gestures.BrowFrown
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
 * Furhat collects information on the target piece the
 * user wants to select.
 *
 * Incoming Transitions from: Start, Explanation, GameFinished,
 *                            PieceSelected, VerifyInformation
 * Outgoing Transitions to: VerifyInformation, PieceSelected,
 *                          GatherInformation
 *
 * Enter while: Attending User
 * Leave while: Attending User
 */
val GatherInformation : State = state(GameRunning) {

    init {
        furhat.gesture(Gestures.Smile, async = false)
        furhat.say {
            random {
                +"Let's get started."
                +"Here we go."
                +"Glad you found the time."
            }
        }
        delay(1000)
    }

    onEntry {
        furhat.glance(LEFT_BOARD)
        if (users.current.correctly_placed.size == users.current.left_state.size) {
            furhat.ask(
                "Which piece do you want to start with?",
                endSil = 1250, timeout = 20000
            )
        } else {
            if (users.current.left_state.size == 1) {
                furhat.say("Only one piece to go.")
                send(
                    "selectPiece",
                    mapOf("piece" to users.current.left_state[0].name)
                )
                furhat.attend(LEFT_BOARD)
                goto(PieceSelected)
            } else {
                val pieceN = users.current.correctly_placed.size + 1
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

    onResponse {
        users.current.roundKnowledge = SharedKnowledge(it)
        if (users.current.roundKnowledge!!.isEmpty()) {
            furhat.gesture(
                listOf(Gestures.Thoughtful, Gestures.BrowFrown, awaitAnswer(duration=5.0))
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
        // a first filtering based on the description
        var ignoredInformation = users.current.roundKnowledge!!
            .findCandidates(users.current.candidates)
        // gather additional information until sufficient or lacking integrity
        while (users.current.candidates.size > 1 && !ignoredInformation) {
            call(VerifyInformation)
            ignoredInformation = users.current.roundKnowledge!!
                .findCandidates(users.current.candidates)
        }
        if (users.current.candidates.size == 1) {
            send(
                "selectPiece",
                mapOf("piece" to users.current.candidates[0].name)
            )
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
            furhat.attend(users.current)
            furhat.gesture(Gestures.BrowFrown, async = false)
            furhat.say(
                "I am sorry, but a ${users.current.roundKnowledge} does not exist.")
            delay(1000)
            reentry()
        }
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
 * Furhat checks for information integrity and ask for
 * additional information if necessary.
 *
 * Incoming Transitions from: SelectPiece
 * Outgoing Transitions to: GatherInformation, GetInformation
 *
 * Enter while: Attending Location
 * Leave while: Attending User
 */
val VerifyInformation : State = state(GameRunning) {

    onEntry {
        furhat.glance(users.current)
        furhat.say("Ok. I seem to be missing some information.")
        furhat.say("Here is, what I have:")
        furhat.glance(users.current)
        furhat.say(
            "We are looking for a ${users.current.roundKnowledge}."
        )
        furhat.attend(users.current)
        val infoIncorrect = furhat.askYN("Any wrong information?")!!
        println(infoIncorrect) //TODO: remove
        if (infoIncorrect) {
            raise("WrongInfo")
        } else {
            raise("CorrectInfo")
        }
    }

    onEvent("WrongInfo") {
        furhat.gesture(Gestures.BrowFrown)
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

    onEvent("CorrectInfo") {
        furhat.gesture(EmpatheticSmile)
        val dp = users.current.roundKnowledge!!
            .getDisambiguatingProperty(users.current.candidates)
        furhat.ask({
            random {
                +"Please, tell me about the $dp of the piece!"
                +"Try to explain the $dp of the piece."
            }
        }, endSil = 1250, timeout = 10000)
    }

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
            propagate()
        }
        terminate()
    }

    onResponse {
        furhat.say("Pardon?")
        raise("CorrectInfo")
    }
}


/**
 * Furhat lets the user move the piece to the right board.
 *
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
        furhat.attend(RIGHT_BOARD)
        call(sendWait("startPlacing"))
        furhat.say {
            + "Okay."
            + Gestures.BrowRaise
            + Gestures.Smile
        }
        delay(500)
        goto(PlaceSelected)
    }

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

class Action(val text : String, val param : Map<String, Any> = mapOf()) : Event()

// Enter while: Attending Right Board
val PlaceSelected : State = state(GameRunning) {
    onEntry {
        furhat.attend(users.current)
        furhat.ask ({
            random {
                +"Fine. Where do you want me to move the piece?"
                +"Where to?"
                +"Where should I move it?"
                +"Where does this piece go?"
                +"Where does the piece belong?"
                +"How should I move it?"
                +"Can you tell me where to move the piece?"
            }
        }, timeout = 20000)
    }

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
    }

    onResponse<Mirror> {
        raise(Action("flipSelected", mapOf("axis" to it.intent.axis)))
    }

    onResponse<Move> {
        println(it.speech.text)
        if (it.intent.dist != null) {
            raise(Action("moveSelected", mapOf("dir" to it.intent.dir as String, "dist" to it.intent.dist as Int)))
        } else {
            raise(Action("moveSelected", mapOf("dir" to it.intent.dir as String)))
        }
    }

    onResponse<Stop> {
        raise(Action("placeSelected"))
    }

    onResponse<Again> {
        if (users.current.prevAction != null) {
            raise(Action(users.current.prevAction as String, users.current.prevParam as Map<String, Any>))
        }
        furhat.listen(timeout = 20000)
    }

    onResponse<Back> {
        if (users.current.prevAction != null) {
            val prevAction = users.current.prevAction!!
            val prevParam = users.current.prevParam!!
            when (prevAction) {
                "moveSelected" -> {
                    val counterDir = mapOf("up" to "down", "down" to "up",
                        "left" to "right", "right" to "left", "middle" to "middle")
                    val newValue = counterDir.getValue(prevParam.getValue("dir") as String)
                    if (newValue != "middle") {
                        raise(Action(prevAction, mapOf("dir" to newValue)))
                    }
                }
                "flipSelected" -> {
                    raise(Action(prevAction, prevParam))
                }
                "rotateSelected" -> {
                    val newValue = -1*prevParam.getValue("angle") as Int
                    raise(Action(prevAction, mapOf("angle" to newValue)))
                }
            }
        }
        furhat.say("There is no going back.")
        furhat.listen(timeout = 20000)
    }

    onResponse<DontKnow> {
        furhat.attend(users.current)
        furhat.gesture(awaitAnswer(duration = 4.0), async = false)
        send("getHint")
        furhat.say {
            random {
                +"You seem to be in desperate need of some hint. Here."
                +"Let me help you. Here."
                +"That's where the piece goes."
            }
        }
        furhat.attend(RIGHT_BOARD)
        delay(1500)
        send("removeHint")
        reentry()
    }


    onResponse {
        // One day we might not need the below block
        // but until grammar entities can decide on
        // whether they are greedy or not, we need it
        val regex = Regex(
            """\b((turn)|(rotate)""" +
                    """|(spin)|(tilt)""" +
                    """|(whirl)|(pivot)""" +
                    """|(swing)|(swing)""" +
                    """|(twist)|(perform))\b""", RegexOption.IGNORE_CASE
        )
        if (regex.containsMatchIn(it.speech.text)) {
            raise(Action("rotateSelected", mapOf("angle" to 90)))
        }
        furhat.say {
            random {
                +"Again, please."
                +"Wait, what?"
                +"Pardon?"
                +"Sorry?"
            }
        }
        furhat.gesture(
            listOf(
                awaitAnswer(duration = 5.0),
                questioning(duration = 2.0),
                BrowFrown
            ).shuffled().take(1)[0]
        )
        furhat.listen(timeout = 20000)
    }

    onNoResponse {
        raise(DontKnow())
    }

    onEvent<Action>(instant = true) {
        furhat.attend(RIGHT_BOARD)
        if (it.param.isEmpty()) {
            send(it.text)
        } else {
            send(it.text, it.param)
            users.current.prevAction = it.text
            users.current.prevParam = it.param
        }
        furhat.listen(timeout = 20000)
    }

    onEvent("placementSuccessful") {
        furhat.attend(users.current)
        furhat.stopSpeaking()
        furhat.say {
            random {
                +"And that's how you build an elephant."
                +"Great."
                +"We are making progress."
                +"This game is so fun."
                +"Good work!"
                +"Yes. That's it."
                +"Well done!"
                +"Good job!"
            }
        }
        goto(GatherInformation)
    }
}


//TODO: select one out of three candidates approaches to the placement
// - "right right right right down down down ... stop
// - "right some more go on more more (signal of continuation) stop
// - "right ... ... ... stop
