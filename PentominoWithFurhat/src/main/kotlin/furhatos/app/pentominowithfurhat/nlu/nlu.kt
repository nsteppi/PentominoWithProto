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


class Last : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("last", "remaining", "closing", "finishing",
                      "end", "ultimate", "endmost", "final")
    }
}


class Colors(
    val color : String? = null,
    val colorSuper: String? = null
) : GrammarEntity() {
    override fun getGrammar(lang : Language) : Grammar {
        return when (lang.main) {
            "en" -> ColorGrammarEn
            else -> throw InterpreterException("Language $lang not supported for ${javaClass.name}")
        }
    }

    // for class methods and static methods
    companion object Trans {
        /**
         * This method maps an exact color to its abstract category.
         */
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


class Shapes(
    val shape : String? = null
) : GrammarEntity() {
    override fun getGrammar(lang : Language) : Grammar {
        return when (lang.main) {
            "en" -> ShapeGrammarEn
            else -> throw InterpreterException("Language $lang not supported for ${javaClass.name}")
        }
    }

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
        fun getShape(shapeList : List<Shapes>, sent: String): Shapes? {
            println(shapeList)
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
            +("chair" / "coiling tube" / "duck" / "N" / "N-shaped" / "torch" / "winding pipe") tag { "N" }
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


/**
 * top/bottom border is for yAxis values
 * left/right border is for xAxis values
 *
 * topBorder/leftBorder: accept values that are bigger
 * bottomBorder/rightBorder: accept values that are smaller
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
            else -> throw InterpreterException("Language $lang not supported for ${javaClass.name}")
        }
    }

    /**
     * This method checks whether the location of a pento piece lies within the given bounds.
     */
    fun includedIn(loc: GameState.Location) : Boolean {
        return loc.x < this.rightBorder
                && loc.x >= this.leftBorder
                && loc.y >= this.topBorder
                && loc.y < this.bottomBorder
    }

    override fun toString(): String {
        return this.toString(detailed = false)
    }

    /**
     * This method translates given bounds to a natural language description.
     */
    fun toString(detailed: Boolean = false) : String {
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
             && this.leftBorder >= 100 && this.rightBorder <= 300) -> "in the middle $yAxis $xAxis"
            (yAxis != "" || xAxis != "") -> "at the $yAxis $xAxis"
            else -> ""
        }
    }

    // for class methods and static methods
    companion object Trans {
        /**
         * This method translates given location to a natural language description.
         */
        fun toString(loc: GameState.Location): String {
            return Positions(
                topBorder = loc.y, bottomBorder = loc.y,
                leftBorder = loc.x, rightBorder = loc.x
            ).toString(detailed = false)
        }

        /**
         * This method takes atomic position bounds (e.g. left, top) and combines
         * them to a complex position (e.g. top left).
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
            +("bottom"/"button"/"lower"/"south") tag {
                Positions(
                    topBorder=200
                )
            }
            +("top"/"upper"/"north") tag {
                Positions(
                    bottomBorder=200
                )
            }
            +("left"/"west") tag {
                Positions(
                    rightBorder=200
                )
            }
            +("right"/"white"/"east") tag {
                Positions(
                    leftBorder=200
                )
            }
            +("mid"/"middle"/"centre"/"center"/"central") tag {
                Positions(
                    bottomBorder = 300,
                    topBorder = 100,
                    leftBorder = 100,
                    rightBorder = 300
                )
            }
        }
    }
