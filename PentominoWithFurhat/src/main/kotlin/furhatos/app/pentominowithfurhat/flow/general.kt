package furhatos.app.pentominowithfurhat.flow

import furhatos.flow.kotlin.*
import furhatos.skills.HostedGUI
import furhatos.util.*

//Hier wird unsere Pentomino Web-UI definiert
val GUI = HostedGUI("Pentomino", "assets/pentomino", 3000)

val Idle: State = state {

    init {
        furhat.setVoice(Language.ENGLISH_US, Gender.MALE)
        if (users.count > 0) {
            furhat.attend(users.random)
            goto(Start)
        }
    }

    onEntry {
        furhat.attendNobody()
    }

    onUserEnter {
        furhat.attend(it)
        goto(Start)
    }
}

val Interaction: State = state {

    onUserLeave(instant = true) {
        if (users.count > 0) {
            if (it == users.current) {
                furhat.attend(users.other)
                goto(Start)
            } else {
                furhat.glance(it)
            }
        } else {
            goto(Start)
        }
    }

    onUserEnter(instant = true) {
        furhat.glance(it)
    }



}