/**
 * sharedKnowledge.kt
 * This class serves as a container for the shared knowledge
 * about the current target piece.
 *
 * Wencke Liermann, Lisa Plagemann, Niklas Stepczynski
 * SoSe21
 * Kotlin 1.3.70
 * Windows 10
 */

package furhatos.app.pentominowithfurhat

import furhatos.app.pentominowithfurhat.nlu.Colors
import furhatos.app.pentominowithfurhat.nlu.Positions
import furhatos.app.pentominowithfurhat.nlu.Shapes
import furhatos.nlu.Response


/**
 * If a turn in the dialog is reached that suggests that the user
 * would include an initial description of a target piece in
 * his response, this class can be used to extract any information
 * present on [color], [shape] or [position] of the piece.
 * This initial state of shared knowledge can later be updated.
 *
 * @param[response] Object created by an onResponse block
 * @constructor Structures and filters information from a piece description.
 */
class SharedKnowledge(response: Response<*>) {
    var color: Colors? = null
    var shape: Shapes? = null
    var position: Positions = Positions()

    init {
        // this is not beautiful but a better place couldn't be found
        val text = response.text.replace(
                            "right angle", "angle")
        this.color = response.findFirst(Colors())
        this.shape = Shapes.getShape(response.findAll(Shapes()), text)
        this.position = Positions.toCompPosition(response.findAll(Positions()))
    }

    /**
     * Leaves only the candidates that match the description provided
     * by the user. This function has side effects in the form of
     * altering the passed [candidates] list.
     *
     * @return `false` if any filter has been ignored,
     *         `true` if all could be applied
     *                without eliminating all candidates
     */
    fun findCandidates(candidates: MutableList<GameState.PentoPiece>): Boolean {
        var ignoredInformation = false
        // create a list of criteria the candidates will be filtered by
        val filters = mutableListOf<(GameState.PentoPiece) -> Boolean>()
        if (this.color != null) {
            // retain pieces matching the extracted vague color category
            filters.add({ this.color!!.colorSuper == Colors.colorSuper(it.color) })
        }
        if (this.position != Positions()) {
            // retain pieces the position of which falls within extracted borders
            filters.add({ this.position.includedIn(it.location) })
        }
        if (this.shape != null) {
            // retain pieces the shape of which exactly matches the extracted one
            filters.add({ this.shape!!.shape == it.type })
        }
        for (filter in filters) {
            // if a filter is too strict (no remaining candidates) it is ignored
            if (candidates.any(filter)) {
                candidates.retainAll(filter)
            } else {
                ignoredInformation = true
            }
        }
        // special filter that is ok to be ignored
        if (this.color != null) {
            // retain pieces matching the extracted specific color
            // (use of synonyms allowed)
            if (candidates.any({ this.color!!.color == it.color })) {
                candidates.retainAll({ this.color!!.color == it.color })
            }
        }
        this.debug()
        return ignoredInformation
    }

    /**
     * Takes a list of [candidates] Pentomino pieces and looks for one
     * property the value of which is unique among all candidates.
     * If this could be a more detailed description of a property
     * the user already gave information on, it is not considered.
     *
     * @return Natural language specifier for this property
     */
    fun getDisambiguatingProperty(candidates: MutableList<GameState.PentoPiece>): String {
        return when {
            // check number of unique values per property present in the candidates
            // if no value was present more than once it can unambiguously specify a piece
            (this.color == null
                    && candidates.map { it.color }.distinct()
                        .count() == candidates.size) -> {
                "color"
            }
            (this.position == Positions()
                    && candidates.map { Positions.toString(it.location) }.distinct()
                        .count() == candidates.size) -> {
                "position"
            }
            // assumption: shape is always unique, so we can use it if nothing else is
            else -> {
                "shape"
            }
        }
    }

    /**
     * Check whether no useful information could be extracted from the response.
     *
     * @return `false` if the data base stored any valuable info,
     *          `true` if the data base is the same as it was on initialization
     */
    fun isEmpty(): Boolean {
        return this.color == null
                && this.shape == null
                && this.position == Positions()
    }

    /**
     * @return Natural language description of the content of the data base
     */
    override fun toString(): String {
        return "${this.color?.color ?: ""} " +
                "${this.shape?.shape ?: "" } piece " +
                "${this.position}"
    }

    fun debug() {
        println("")
        println("Extracted Intents:")
        println("Exact Color: ${this.color?.color}")
        println("Abstract Color: ${this.color?.colorSuper}")
        println("Shape: ${this.shape}")
        println("Position: ${this.position}")
    }
}
