/**
 * users.kt
 * Holds additional information about the game state.
 *
 * Wencke Liermann, Lisa Plagemann, Niklas Stepczynski
 * WiSe 20/21
 * Kotlin 1.3.70
 * Windows 10
 */

package furhatos.app.pentominowithfurhat.flow

import furhatos.app.pentominowithfurhat.GameState
import furhatos.app.pentominowithfurhat.SharedKnowledge
import furhatos.flow.kotlin.NullSafeUserDataDelegate
import furhatos.flow.kotlin.UserDataDelegate
import furhatos.records.User


// information about a preselected random piece to be accessed when needed
var User.rand_piece_name : String? by UserDataDelegate()
var User.rand_piece_color : String? by UserDataDelegate()
var User.rand_piece_type : String? by UserDataDelegate()
var User.rand_piece_loc by NullSafeUserDataDelegate { GameState.Location(-1,-1) }

// knowledge extracted from user input and its consequences for to the state
// renewed every round
var User.left_state : List<GameState.PentoPiece> by NullSafeUserDataDelegate { listOf<GameState.PentoPiece>()}
var User.right_state : List<GameState.PentoPiece> by NullSafeUserDataDelegate { listOf<GameState.PentoPiece>()}
var User.selected : String by NullSafeUserDataDelegate { "" }
var User.candidates : MutableList<GameState.PentoPiece> by NullSafeUserDataDelegate { mutableListOf<GameState.PentoPiece>() }
var User.roundKnowledge : SharedKnowledge? by UserDataDelegate()

// stores previous user attitude
var User.saidNo by NullSafeUserDataDelegate { false }
var User.played by NullSafeUserDataDelegate { false }
