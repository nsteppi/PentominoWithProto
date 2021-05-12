/**
 * interaction.kt
 * All dialog components that do not involve a running game.
 *
 * Wencke Liermann, Lisa Plagemann, Niklas Stepczynski
 * WiSe 20/21
 * Kotlin 1.3.70
 * Windows 10
 */

package furhatos.app.pentominowithfurhat.flow

import furhatos.app.pentominowithfurhat.nlu.Right
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures.BigSmile
import furhatos.gestures.Gestures.Nod
import furhatos.gestures.Gestures.Smile
import furhatos.gestures.Gestures.Wink
import furhatos.nlu.common.*


/** Defines game mode */
var MODE = "Demo"


/**
 * Furhat tries to attract a users attention.
 *
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
 * Furhat invites the user to play a game.
 *
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
        furhat.ask {
            +awaitAnswer(duration=5.0)
            +"Do you want to play a game?"
        }
    }

    onResponse<Yes>{
        furhat.gesture(
            happy(strength = 0.5, duration = 1.5),
            async = false
        )
        call(sendWait(("startGame")))
        // the user is only given an explanation
        // if it's their first time playing
        users.current.saidNo = false
        if (users.current.played) {
            goto(GatherInformation)
        } else {
            goto(Explanation)
        }
    }

    onResponse<No>{
        // gently express disappointment
        furhat.gesture(hurt(duration=3.0))
        furhat.sigh()
        furhat.deflated("Too bad.")
        delay(500)
        furhat.gesture(Smile)
        delay(500)
        furhat.gesture(Wink(duration = 1.5), async=false)
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
 * Furhat explains the game to a new user.
 *
 * Incoming Transitions from: Explanation, Start
 * Outgoing Transitions to: Explanation, GatherInformation
 *
 * Enter while: Attending User
 * Leave while: Attending User
 */
val Explanation : State = state(Interaction) {

    onEntry {
        furhat.attend(LEFT_BOARD)
        // read out description from file
        val textFile = javaClass.getClassLoader()
            .getResource("SimpleDescription")
            .readText(charset = Charsets.UTF_8).split("\n")

        textFile.forEachIndexed { i, line ->
            // attract attention to the template
            if (i == 4) {
                furhat.attend(RIGHT_BOARD)
            }
            // habitually glance to the user
            if (i in listOf(2, 5)) {
                furhat.glance(users.current, 2000)
            }
            furhat.say(furhat.voice.prosody(line, rate=0.9))
            delay(500)
        }
        furhat.attend(users.current)
        delay(1000)
        // answering No here is the only chance for the user
        // to hear the explanation again
        furhat.ask {
            +awaitAnswer(duration=3.0)
            +"Is everything clear?"
        }
    }

    onResponse<Yes> {
        users.current.played = true
        furhat.gesture(Nod(duration = 0.8), async = false)
        // before the real game starts we might wish to let the user perform a simple demo
        if (MODE == "Demo") {
            furhat.say("Ok. But let's go for a test run first.")
            furhat.say("Instead of an elephant we will build a simple square.")
            call(sendWait("startDemo"))
            furhat.say("And remember, you can always ask me for help.")
        } else {
            call(sendWait("startGame"))
        }
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
 * Furhat wraps up a game that ended.
 *
 * Incoming Transitions from: GameRunning
 * Outgoing Transitions to: Idle, GatherInformation
 *
 * Enter while: *unclear*
 * Leave while: Attending User or Nobody(-> Idle)
 */
val GameFinished : State = state(Interaction) {

    onEntry {
        furhat.attend(users.current)
        furhat.glance(RIGHT_BOARD, 2000)
        if (MODE == "Demo") {
            goto(DemoFinished)
        }
        // the game is won
        if (users.current.right_state.isEmpty() && users.current.left_state.isEmpty()) {
            // wait for furhat to stop speaking
            while (furhat.isSpeaking()) {
                delay(100)
            }
            furhat.gesture(
                happy(strength = 0.5, duration = 1.5),
                async = false
            )
            delay(1000)
            furhat.say {
                random {
                    +"Good job!"
                    +"Great game! Thank you!"
                    +"Nice work, well done!"
                    +"And that's how you build an elephant."
                    +"Winner, winner, chicken dinner!"
                    +"Yes! We won!"
                }
            }
        } else {
            // the game is lost
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

    // restart game
    onResponse<Yes> {
        call(sendWait("startGame"))
        furhat.gesture(Wink, async = false)
        furhat.say("You seem to be enjoying the game.")
        goto(GatherInformation)
    }

    onResponse<No> {
        // remember user attitude to later prefer other users
        // that might not have played the game yet
        users.current.saidNo = true
        furhat.gesture(lookDown(duration = 0.3), async = false)
        furhat.say("Ok. Thank you for your time.")
        furhat.say("Bye bye!")
        furhat.attendNobody()
        goto(Idle)
    }

    onNoResponse {
        furhat.attendNobody()
        goto(Idle)
    }
}


/**
 * Furhat wraps up a demo that ended.
 *
 * Incoming Transitions from: GameFinished
 * Outgoing Transitions to: GatherInformation
 *
 * Enter while: Attending User
 * Leave while: Attending User
 */
val DemoFinished : State = state(Interaction) {

    onEntry {
        // the game is won
        if (users.current.right_state.isEmpty() && users.current.left_state.isEmpty()) {
            // necessary so once we win the real game we get the appropriate treatment
            MODE = "Game"

            furhat.say("I see you have understood the game.")
            furhat.gesture(Wink)
            furhat.say("Let's address the elephant in the room.")
            delay(500)
            // the user has to win the demo and then they can move on to the game
            call(sendWait("startGame"))
            goto(GatherInformation)

        } else {
            // the game is lost
            // send the user back into another demo round
            furhat.say("Oh no. We have run out of time.")
            furhat.gesture(hurt(strength = 0.6, duration = 0.7))
            furhat.say("Here some hints that might help you.")
            // read out additional hints
            val textFile = javaClass.getClassLoader()
                .getResource("Hints")
                .readText(charset = Charsets.UTF_8).split("\n")

            for (line in textFile) {
                furhat.say(furhat.voice.prosody(line, rate=0.9))
                delay(500)
            }
            furhat.gesture(Smile(strength = 0.5, duration = 1.2))
            delay(250)
            furhat.gesture(awaitAnswer(duration = 3.0), async = false)
            furhat.say("Let's try this again.")
            call(sendWait("startDemo"))
            goto(GatherInformation)
        }
    }
}
