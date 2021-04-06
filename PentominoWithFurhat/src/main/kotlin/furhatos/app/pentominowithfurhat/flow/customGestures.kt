/**
 * customGestures.kt
 * Contains several custom gestures.
 *
 * Wencke Liermann, Lisa Plagemann, Niklas Stepczynski
 * WiSe 20/21
 * Kotlin 1.3.70
 * Windows 10
 */

package furhatos.app.pentominowithfurhat.flow

import furhatos.gestures.BasicParams
import furhatos.gestures.defineGesture
import java.util.*


/** custom static gestures */

// head and gaze are turned to furhat's right then to his left
val LookAround = defineGesture("lookAround") {
    frame(0.5, 3.0) {
        BasicParams.NECK_PAN to -20.0
        BasicParams.NECK_TILT to -10.0
        BasicParams.GAZE_TILT to -20.0
    }
    frame(3.5, 6.0) {
        BasicParams.NECK_PAN to 20.0
        BasicParams.NECK_TILT to -10.0
        BasicParams.GAZE_TILT to 20.0
    }
    reset(6.5)
}

// bow head to the front and look down
fun LookDown(strength: Double = 1.0, duration: Double = 1.0) =
    defineGesture("lookDown", strength, duration) {
        frame(0.5, 4.5) {
            BasicParams.NECK_TILT to 10.0
            BasicParams.LOOK_DOWN to 10.0
        }
        reset(5.0)
    }

// reset all parameters of the gestures used in Idle
val ReturnToNormal = defineGesture("ReturnToNormal") {
    frame(0.5) {
        BasicParams.NECK_PAN to 0
        BasicParams.NECK_TILT to 0
        BasicParams.GAZE_TILT to 0
        BasicParams.GAZE_PAN to 0
        BasicParams.GAZE_TILT to 0
        BasicParams.LOOK_DOWN to 0
    }
}

// angle head to furhat's right accompanied with a slight smile
val EmpatheticSmile = defineGesture("EmpatheticSmile") {
    frame(0.5, 4.5) {
        BasicParams.NECK_ROLL to 15.0
        BasicParams.SMILE_CLOSED to 15.0
    }
    reset(5.0)
}


/** custom function gestures */

// angle head to furhat's left and lift his right brow -> attentive
// weaker alternative: questioning
fun awaitAnswer(strength: Double = 1.0, duration: Double = 1.0) =
    defineGesture("awaitAnswer", strength, duration) {
        frame(0.1, 0.5) {
            BasicParams.NECK_ROLL to -10.0
            BasicParams.BROW_UP_RIGHT to 1.0
            BasicParams.BROW_DOWN_LEFT to 1.0
            BasicParams.BROW_IN_LEFT to 0.5
            BasicParams.BROW_IN_RIGHT to 0.5
        }
        reset(0.7)
    }

// push lips together and lift furhat's right brow
// stronger alternative: awaitAnswer
fun questioning(strength: Double = 1.0, duration: Double = 1.0) =
    defineGesture("Questioning", strength, duration) {
        frame(0.1, 0.5) {
            BasicParams.BROW_UP_RIGHT to 15.0
            BasicParams.BROW_DOWN_LEFT to 15.0
            BasicParams.BROW_IN_RIGHT to 15.0
            BasicParams.BROW_IN_LEFT to 15.0
            BasicParams.PHONE_B_M_P to 1.0
        }
        reset(0.7)
    }

// wide open-mouthed smile full of excitement
fun happy(strength: Double = 1.0, duration: Double = 1.0) =
    defineGesture("happy", strength, duration) {
        frame(0.4, 0.6) {
            BasicParams.SMILE_OPEN to 0.5
            BasicParams.PHONE_CH_J_SH to 0.6 //cupidsbow
            BasicParams.PHONE_AAH to 0.85
            BasicParams.BROW_UP_LEFT to 0.5
            BasicParams.BROW_UP_RIGHT to 0.5
            BasicParams.EXPR_DISGUST to 0.35
        }
        reset(1.0)
    }

// angle head to furhat's left, press lips together, narrow eyes and avoid eye contact
fun hurt(strength: Double = 1.0, duration: Double = 1.0) =
    defineGesture("hurt", strength, duration) {
        frame(0.1, 0.5) {
            BasicParams.NECK_ROLL to -20.0
            BasicParams.NECK_TILT to 5.0
            BasicParams.EYE_SQUINT_LEFT to 10.0
            BasicParams.EYE_SQUINT_RIGHT to 10.0
            BasicParams.GAZE_TILT to 20.0
            BasicParams.BROW_DOWN_LEFT to 10.0
            BasicParams.BROW_DOWN_RIGHT to 10.0
            BasicParams.PHONE_B_M_P to 20.0
            BasicParams.PHONE_F_V to 20.0
        }
        reset(0.7)
    }
