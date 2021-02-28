package furhatos.app.pentominowithfurhat

/**
 * Diese Klasse dient der Abbildung des momentanen GameStates, der von der Web-UI gesendet wird.
 */
class GameState {

    data class Info(
        val left_board: List<PentoPiece>,
        val right_board: List<PentoPiece>,
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