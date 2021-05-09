/**
 * gameState.kt
 * This class serves as a container of the current game state, which is sent by the Web-UI.
 *
 * Kotlin 1.3.70
 */

package furhatos.app.pentominowithfurhat


class GameState {

    data class Info(
        val left_board: List<PentoPiece>,
        val right_board: List<PentoPiece>,
        val correctly_placed: List<PentoPiece>,
        val game: Game,
        val selected: String
    )

    data class PentoPiece(
        val name: String,
        val type: String,
        val color: String,
        val location: Location
    )

    data class Location(
        val x: Int,
        val y: Int
    )

    data class Game(
        val status: String,
        val startTime: Float,
        val time: Int
    )
}
