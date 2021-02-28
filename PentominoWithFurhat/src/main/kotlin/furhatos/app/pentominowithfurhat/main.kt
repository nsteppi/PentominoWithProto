package furhatos.app.pentominowithfurhat

import furhatos.app.pentominowithfurhat.flow.*
import furhatos.skills.Skill
import furhatos.flow.kotlin.*

class PentominowithfurhatSkill : Skill() {
    override fun start() {
        Flow().run(Idle)
    }
}

fun main(args: Array<String>) {
    Skill.main(args)
}
