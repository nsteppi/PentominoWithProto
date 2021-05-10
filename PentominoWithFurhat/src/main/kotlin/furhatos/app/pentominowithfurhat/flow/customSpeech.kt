/**
 * customSpeech.kt
 * Contains several custom voice transformations to accompany the experience.
 *
 * Wencke Liermann, Lisa Plagemann, Niklas Stepczynski
 * SoSe21
 * Kotlin 1.3.70
 * Windows 10
*/

package furhatos.app.pentominowithfurhat.flow

import furhatos.flow.kotlin.Furhat
import furhatos.flow.kotlin.voice.PollyNeuralVoice
import furhatos.flow.kotlin.voice.PollyVoice


/** exasperated breathing sound, more breathy than sigh */
fun Furhat.breathing(){
    val oldVoice = voice
    voice = PollyVoice.Matthew()
    say(PollyVoice.Matthew().breath(
        duration = PollyVoice.BreathDuration.LONG,
        volume=PollyVoice.BreathVolume.XLOUD
    ))
    say(PollyVoice.Matthew().breath(
        duration = PollyVoice.BreathDuration.MEDIUM,
        volume=PollyVoice.BreathVolume.XLOUD
    ))
    voice = oldVoice
}


/** a mixture of disappointed and sad but mainly spiritless */
fun Furhat.deflated(text: String) {
    say(voice.prosody(
        PollyNeuralVoice.Matthew().whisper(text),
        volume = "+12dB",
        rate = 0.7
        )
    )
}


/** the sound you make when you want someone to be quiet */
fun Furhat.hush() {
    say(voice.prosody(
            PollyNeuralVoice.Matthew().whisper("tsche"),
            rate = 0.4
        )
    )
}


/** audible breathing sound */
fun Furhat.sigh() {
    say(voice.prosody(
        PollyNeuralVoice.Matthew().whisper("hou..."),
        rate = 0.4
        )
    )
}
