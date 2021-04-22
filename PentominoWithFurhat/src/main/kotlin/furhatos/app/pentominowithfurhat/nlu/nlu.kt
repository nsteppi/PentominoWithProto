/**
 * nlu.kt
 * Contains entities matching patterns of color, shape and position.
 *
 * Wencke Liermann, Lisa Plagemann, Niklas Stepczynski
 * WiSe 20/21
 * Kotlin 1.3.70
 * Windows 10
 */

package furhatos.app.pentominowithfurhat.nlu

import furhatos.app.pentominowithfurhat.GameState
import furhatos.nlu.*
import furhatos.nlu.grammar.Grammar
import furhatos.nlu.kotlin.grammar
import furhatos.util.Language


/**
 * Each exact [color] is seen as a member of a [colorSuper] set,
 * where it is grouped with similar looking colors.
 */
class Colors(
    val color : String? = null,
    val colorSuper: String? = null
) : GrammarEntity() {
    override fun getGrammar(lang : Language) : Grammar {
        return when (lang.main) {
            "en" -> ColorGrammarEn
            else -> throw InterpreterException(
                "Language $lang not supported for ${javaClass.name}"
            )
        }
    }

    // for class methods and static methods
    companion object Trans {
        /** This method maps an exact [color] to its abstract category. */
        fun colorSuper(color: String): String? {
            val toSuper: HashMap<String, String> = hashMapOf(
                "light blue" to "blue", "dark blue" to "blue", "turquoise" to "blue",
                "light green" to "green", "dark green" to "green",
                "light red" to "red", "pink" to "red", "purple" to "red",
                "yellow" to "yellow", "orange" to "orange", "beige" to "beige")
            return toSuper.get(color)
        }
    }
}


val ColorGrammarEn =
    grammar {
        rule(public=true) {
            +("lemon"/"amber"/"golden"/"yellow") tag { Colors(color="yellow", colorSuper="yellow")}
            +("carrot"/"orange"/"sunset"/"pumpkin"/"tiger") tag { Colors(color="orange", colorSuper="orange")}
            +("beige"/"brown"/"brownish"/"tan"/"sand"/"latte"/"egg shell"/"hazelnut"/"hazelnuts"/"peanut") tag { Colors(color="beige", colorSuper="beige")}
            +("light red"/"coral"/"rose"/"blush"/"red"/"wet"/"read") tag { Colors(color="light red", colorSuper="red")}
            +("pink"/"magenta"/"fuchsia"/"rouge"/"salmon") tag { Colors(color="pink", colorSuper="red")}
            +("purple"/"lilac"/"violet"/"lavender"/"mauve"/"orchid") tag { Colors(color="purple", colorSuper="red")}
            +("light green"/"light queen"/"lime"/"neon"/"mint"/"jade") tag { Colors(color="light green", colorSuper="green")}
            +("dark green"/"dark queen"/"forest"/"pine"/"olive"/"green"/"queen") tag { Colors(color="dark green", colorSuper="green")}
            +("light blue"/"baby blue"/"sky"/"arctic") tag { Colors(color="light blue", colorSuper="blue")}
            +("dark blue"/"azure"/"sapphire"/"royal"/"navy"/"denim"/"blue") tag { Colors(color="dark blue", colorSuper="blue")}
            +("turquoise"/"aqua"/"cyan") tag { Colors(color="turquoise", colorSuper="blue")}
        }
    }


/** The [shape] of a piece seen as a letter. */
class Shapes(
    val shape : String? = null
) : GrammarEntity() {
    override fun getGrammar(lang : Language) : Grammar {
        return when (lang.main) {
            "en" -> ShapeGrammarEn
            else -> throw InterpreterException(
                "Language $lang not supported for ${javaClass.name}"
            )
        }
    }

    /**
     * An instantiated shape intent and the string [sent] it
     * was extracted from can together be used to check
     * whether the shape was actually a pronoun.
     */
    private fun isPronoun(sent: String): Boolean {
        if (this.text !in setOf("U", "you", "eye", "I")) {
            return false
        }
        val regex = Regex(
            """\b((letter ${this.text})""" +
                    """|(character ${this.text})""" +
                    """|((a|(an)|(the)) (\w* ){0,2}${this.text})""" +
                    """|(capital (case)? ${this.text})""" +
                    """|(uppercase ${this.text})""" +
                    """|(of ${this.text})""" +
                    """|(${this.text} shaped?))\b""" +
                    """|(^${this.text}$)""", RegexOption.IGNORE_CASE
        )
        if (regex.containsMatchIn(sent)) {
            return false
        }
        return true
    }

    // for class methods and static methods
    companion object Trans {
        /**
         * Out of all shapes from [shapeList] that have been identified to not
         * be in fact a pronoun judging from their context in [sent], choose the
         * last mentioned shape. With the exception that if the last
         * shape was a pronoun candidate ("I" or "U") and there is another shape that
         * classifies under no circumstances as a pronoun, choose this one instead.
         */
        fun getShape(shapeList : List<Shapes>, sent: String): Shapes? {
            val shapes = shapeList.filterNot { it.isPronoun(sent) }
            if (shapes.isNotEmpty()) {
                var shape = shapes.last()
                for (s in shapes.reversed()){
                    if (s.shape !in setOf("U", "I")){
                        shape = s
                        break
                    }
                }
                return shape
            }
            return null
        }
    }
}


val ShapeGrammarEn =
    grammar {
        rule("shape", public = true) {
            +("column" / "eye" / "I" / "I-shaped" / "line" / "long" / "pipe" / "ruler" / "stick" / "tube") tag { "I" }
            +("bird" / "F" / "F-shaped" / "flower") tag { "F" }
            +("hook" / "L" / "L-shaped") tag { "L" }
            +("chair" / "coiling tube" / "duck"/ "M" / "M-shaped" / "N" / "N-shaped" / "torch" / "winding pipe") tag { "N" }
            +("block" / "open box" / "opened box" / "P" / "P-shaped" / "pea" / "pee" / "square") tag { "P" }
            +("candle" / "hammer" / "T" / "T-shaped" / "tea" / "tee" / "tower" / "tree") tag { "T" }
            +("bowl" / "box" / "bridge" / "C" / "C-shaped" / "cup" / "gate" / "U" / "U-shaped" / "you") tag { "U" }
            +("angle" / "right angle" / "roof" / "tick" / "V" / "V-shaped") tag { "V" }
            +("stairs" / "steps" / "W" / "W-shaped") tag { "W" }
            +("cross" / "plus" / "star"/ "X" / "X-shaped") tag { "X" }
            +("hydrant" / "lego" / "pump" / "tap" / "water tap" / "why" / "Y" / "Y-shaped") tag { "Y" }
            +("duck" / "mirrored S" / "reflected S" / "S" / "S-shaped" / "swan" / "Z" / "Z-shaped") tag { "Z" }
        }
    }

/** To be used for [Positions] as well as [Directions] */
class Right : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("right", "white", "east", "right-hand")
    }
}

/** To be used for [Positions] as well as [Directions] */
class Left : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("left", "west", "left-hand")
    }
}

/** To be used for [Positions] as well as [Directions] */
class Top : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("top", "upper", "north", "further towards you",
            "closer towards you", "farther away from me")
    }
}

/** To be used for [Positions] as well as [Directions] */
class Bottom : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf(
            "bottom", "button", "lower",
            "south", "further towards me",
            "closer towards me", "farther away from you"
        )
    }
}

class Middle : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("mid", "middle", "centre", "center", "central")
    }
}


/**
 * top/bottom border is for yAxis values
 * left/right border is for xAxis values
 *
 * [topBorder]/[leftBorder]: accept values as within limits that are bigger
 * [bottomBorder]/[rightBorder]: accept values as within limits that are smaller
 *
 * coordinate system has the orientation:
 *   0 -   200  - 400
 *   |      |
 *   200  -   - ...
 *   |      |
 *   400  -   - ...
 */
class Positions(
    var topBorder : Int = -1,
    var bottomBorder : Int = 401,
    var leftBorder : Int = -1,
    var rightBorder : Int = 401
) : GrammarEntity() {
    override fun getGrammar(lang : Language) : Grammar {
        return when (lang.main) {
            "en" -> PositionsGrammarEn
            else -> throw InterpreterException(
                "Language $lang not supported for ${javaClass.name}"
            )
        }
    }

    /**
     * This method checks whether the [loc]ation of a pento piece
     * lies within the given bounds.
     */
    fun includedIn(loc: GameState.Location) : Boolean {
        return loc.x < this.rightBorder
                && loc.x >= this.leftBorder
                && loc.y >= this.topBorder
                && loc.y < this.bottomBorder
    }

    override fun toString(): String {
        return this.toString(detailed = true)
    }

    /**
     * This method translates given bounds to a natural language description.
     * It may either only refer to top/bottom/left/right (not [detailed])
     * or also to middle ([detailed]).
     */
    fun toString(detailed: Boolean = true) : String {
        val yAxis =  when {
            this.topBorder >= 200 -> "bottom"
            this.bottomBorder <= 200 -> "top"
            else -> ""
        }
        val xAxis = when {
            this.leftBorder >= 200 -> "right"
            this.rightBorder <= 200 -> "left"
            else -> ""
        }

        return when {
            (detailed
             && this.bottomBorder <= 300 && this.topBorder >= 100
             && this.leftBorder >= 100 && this.rightBorder <= 300) ->
                "in the middle $yAxis $xAxis"
            (yAxis != "" || xAxis != "") -> "at the $yAxis $xAxis"
            else -> ""
        }
    }

    // for class methods and static methods
    companion object Trans {
        /**
         * This method translates given [loc]ation to a natural language description.
         */
        fun toString(loc: GameState.Location, detailed : Boolean = false): String {
            return Positions(
                topBorder = loc.y, bottomBorder = loc.y,
                leftBorder = loc.x, rightBorder = loc.x
            ).toString(detailed = detailed)
        }

        /**
         * This method takes the atomic position bounds (e.g. left, top)
         * in [posList] and combines them to a complex position (e.g. top left).
         * Atomic positions stated more recently are given priority over those at
         * the beginning of the utterance if conflicts exist.
         */
        fun toCompPosition(posList : List<Positions>): Positions {
            val pos = Positions()
            for (p in posList.reversed()) {
                // first cond: check whether new info exists
                // second cond: ensure that there is room between the two opposite borders
                // third cond: ensure that the stronger restriction is kept
                if (p.topBorder != -1
                        && pos.bottomBorder > p.topBorder
                        && p.topBorder > pos.topBorder) {
                    pos.topBorder = p.topBorder
                }
                if (p.bottomBorder != 401
                        && pos.topBorder < p.bottomBorder
                        && p.bottomBorder < pos.bottomBorder) {
                    pos.bottomBorder = p.bottomBorder
                }
                if (p.leftBorder != -1
                        && pos.rightBorder > p.leftBorder
                        && p.leftBorder > pos.leftBorder) {
                    pos.leftBorder = p.leftBorder
                }
                if (p.rightBorder != 401
                        && pos.leftBorder < p.rightBorder
                        && p.rightBorder < pos.rightBorder) {
                    pos.rightBorder = p.rightBorder
                }
            }
            return pos
        }
    }
}


/**
 * View project ReadMe.md for information about our understanding of positions.
 */
val PositionsGrammarEn =
    grammar {
        rule(public = true) {
            choice {
                entity<Right>() tag { Positions(leftBorder = 200) }
                entity<Left>() tag { Positions(rightBorder=200) }
                entity<Top>() tag { Positions(bottomBorder=200 ) }
                entity<Bottom>() tag { Positions(topBorder=200) }
                entity<Middle>() tag {
                    Positions(
                        bottomBorder = 300,
                        topBorder = 100,
                        leftBorder = 100,
                        rightBorder = 300
                    )
                }
            }
        }
    }


/// only crazy stuff below here


class Directions(
    var dir : String? = null
    ) : GrammarEntity() {
    override fun getGrammar(lang: Language): Grammar {
        return when (lang.main) {
            "en" -> DirectionGrammarEn
            else -> throw InterpreterException("Language $lang not supported for ${javaClass.name}")
        }
    }
}


val DirectionGrammarEn =
    grammar {
        rule(public = true) {
            choice {
                +("plummet"/"drop"/"down") tag { Directions( dir="down" ) }
                entity<Bottom>() tag { Directions( dir="down" ) }
                +("higher"/"elevate"/"lift"/"raise"/"up"/"upward") tag { Directions(dir="up")}
                entity<Top>() tag { Directions(dir="up")}
                entity<Left>() tag { Directions(dir="left") }
                entity<Right>() tag { Directions(dir="right") }
                entity<Middle>() tag { Directions(dir="middle") }
            }
        }
    }


/**
Context: One person has to complete a 16-piece puzzle while blindfolded.
Another person describes where to get and put pieces.
Source: https://www.youtube.com/watch?v=a5WCgpKO4cQ 3:47
No, no, no, no. Top. Top right.
Around, around, around, around...
And you go all the way straight, straight all the way up.
Right in front of you. All the way front, all the way front. No no keep going. Keep going.
Turn, turn, turn. Place it down.
Go up. Go up. One more. Right there.
Take that one. One more down. One more that way. One more. Right there. Perfect.
Flip. Flip. Flip. Flip. Flip. Right.

Context: People have to walk through a field blindfolded and throw balls at objects.
Source: https://www.youtube.com/watch?v=a5WCgpKO4cQ 6:15
...
You have to go a little more to the right.
...
Ok, go. Right, right.
...
Still, too much to the right.
Go, go. Straight.




turn it/ that piece clockwise
rotate a figure (by) 90 degrees in clockwise direction
rotate a figure clockwise (by) 90 degrees
rotate a figure (by) 90 degrees counterclockwise

clockwise -> to the right...
perform a 90-degree counterclockwise rotation


rotate, turn, spin, tilt
clockwise, counterclockwise, by .. degree, to the right



mirror, reflect,  flipped (horizontally, vertically or diagonally)


*/