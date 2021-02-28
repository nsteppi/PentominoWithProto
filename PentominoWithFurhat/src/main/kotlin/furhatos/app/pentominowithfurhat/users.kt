package furhatos.app.pentominowithfurhat.flow

import furhatos.app.pentominowithfurhat.GameState
import furhatos.flow.kotlin.UserDataDelegate
import furhatos.records.User

//Hier sind einige Beispiele dafür, wie wir uns Teile des GameStates für den aktuellen User speichern können.
var User.selected : String? by UserDataDelegate()
var User.prev_selected : String? by UserDataDelegate()
var User.rand_piece : String? by UserDataDelegate()