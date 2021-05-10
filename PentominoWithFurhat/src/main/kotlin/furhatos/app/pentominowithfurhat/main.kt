/**
 * main.kt
 * Implements a user-robot dialog interaction in a Pentomino game setting.
 *
 * Kotlin 1.3.70
 */

package furhatos.app.pentominowithfurhat

import furhatos.app.pentominowithfurhat.flow.*
import furhatos.flow.kotlin.*
import furhatos.skills.Skill


class PentominowithfurhatSkill : Skill() {
    override fun start() {
        Flow().run(Idle)
    }
}

fun main(args: Array<String>) {
    Skill.main(args)
}
