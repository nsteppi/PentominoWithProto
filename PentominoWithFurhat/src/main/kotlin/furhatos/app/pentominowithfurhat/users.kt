/**
 * users.kt
 * Holds additional information about the game state.
 *
 * Wencke Liermann, Lisa Plagemann, Niklas Stepczynski
 * SoSe 21
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

// information about the current state of the game
var User.left_state : List<GameState.PentoPiece> by NullSafeUserDataDelegate { listOf<GameState.PentoPiece>()}
var User.right_state : List<GameState.PentoPiece> by NullSafeUserDataDelegate { listOf<GameState.PentoPiece>()}
var User.correctly_placed : List<GameState.PentoPiece> by NullSafeUserDataDelegate { listOf<GameState.PentoPiece>()}

// extracted specification of the target piece and all remaining candidate pieces
var User.candidates : MutableList<GameState.PentoPiece> by NullSafeUserDataDelegate { mutableListOf<GameState.PentoPiece>() }
var User.roundKnowledge : SharedKnowledge? by UserDataDelegate()

// store last movement action performed during piece placement
var User.prevAction : String? by UserDataDelegate()
var User.prevParam : Map<String, Any>? by UserDataDelegate()

// stores previous user attitude
var User.saidNo by NullSafeUserDataDelegate { false }
var User.played by NullSafeUserDataDelegate { false }
