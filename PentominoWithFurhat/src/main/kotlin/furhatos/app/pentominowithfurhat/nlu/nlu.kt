/**
 * nlu.kt
 * Contains entities matching patterns of:
 * + color
 * + shape
 * + position.
 *
 * Wencke Liermann, Lisa Plagemann, Niklas Stepczynski
 * SoSe 21
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
        /**
         * This method maps an exact [color] to its abstract category.
         *
         * @return A natural language expression for a broad color category
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
     *
     * @return `true` if the shape instance is a pronoun,
     *          `false` if it is likely a shape description
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
         *
         * @return Shapes object or null if none found
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


/** To be used for [Positions] as well as [Move] */
class Top : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf(
            "top", "upper", "north", "further towards you",
            "closer towards you", "farther away from me"
        )
    }
}


/** To be used for [Positions] as well as [Move] */
class Bottom : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf(
            "bottom", "button", "lower",
            "south", "further towards me",
            "closer towards me", "farther away from you"
        )
    }
}


/** To be used for [Positions] as well as [Move] */
class Left : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("left", "west", "left-hand")
    }
}


/** To be used for [Positions] as well as [Move] */
class Right : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("right", "white", "east", "right-hand")
    }
}


/** To be used for [Positions] */
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
 * View project documentation for information about our understanding of positions.
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
     * This method checks whether the [loc]ation of a Pentomino piece
     * lies within the given bounds.
     *
     * @return `true` if the Pentomino piece lies in [loc],
     *          `false` if its position is somewhere else
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
     * or also to middle ([detailed]). If called without arguments
     * a [detailed] description is returned.
     *
     * @return String description
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
         *
         * @see Positions.toString for more details.
         * @return String description
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
         *
         * @return Positions object
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


/**
 * Move a piece in a specified [dir]ection. Without any additional
 * input this movement has to be manually halted via the [Stop] intent.
 * Optionally one can specify a fixed [dist]ance.
 */
class Move(
    var dir : String? = null,
    var dist: Int? = null
    ) : GrammarEntity() {
    override fun getGrammar(lang: Language): Grammar {
        return when (lang.main) {
            "en" -> MoveGrammarEn
            else -> throw InterpreterException("Language $lang not supported for ${javaClass.name}")
        }
    }
}


/**
 * This grammar supports very open formulations, as it is used to catch the intent
 * one will probably use most during the placement process. The more often
 * an intent is used the more diverse we expect the user utterances to be.
 *
 * This has the downside of creating more false positive for the related intent.
 */
val MoveGrammarEn =
    grammar {
        /**
         * No fixed movement goal.
         * e.g. Move the piece to the right.
         */
        rule(public = true) {
            choice {
                +("plummet"/"drop"/"down"/"downward"/"downwards")
                entity<Bottom>()
            } tag { Move( dir="down" ) }
            choice {
                +("higher"/"elevate"/"lift"/"raise"/"up"/"upward"/"hoist"/"uplift")
                entity<Top>()
            } tag { Move( dir="up" ) }
            entity<Left>() tag { Move(dir="left") }
            entity<Right>() tag { Move(dir="right") }
            entity<Middle>() tag { Move(dir="middle") }
        }

        /**
         * A fixed movement goal.
         * e.g. Move the piece three blocks to the right.
         */
        rule(public = true) {
            group {
                ruleref("distance")
                -"more"
                -("block" / "blocks" / "bucks" / "field" / "step" / "more" / "farther" / "column" / "row" / "place")
                -"to"
                -"the"
                choice {
                    entity<Top>() tag { Move(dir="up", dist=ref["distance"] as Int) }
                    +("higher"/"elevate"/"lift"/"raise"/"up"/"upward"/"hoist"/"uplift") tag { Move(dir="up", dist=ref["distance"] as Int) }
                    entity<Bottom>() tag { Move(dir="down", dist=ref["distance"] as Int) }
                    +("plummet"/"drop"/"down"/"downward"/"downwards") tag { Move(dir="down", dist=ref["distance"] as Int) }
                    entity<Left>() tag { Move(dir="left", dist=ref["distance"] as Int) }
                    entity<Right>() tag { Move(dir="right", dist=ref["distance"] as Int) }
                }
            }
        }

        /**
         * A fixed movement goal.
         * e.g. go down one block
         */
        rule(public = true) {
            choice {
                entity<Top>() tag { Move(dir="up", dist=ref["distance"] as Int) }
                +("higher"/"elevate"/"lift"/"raise"/"up"/"upward"/"hoist"/"uplift") tag { Move(dir="up", dist=ref["distance"] as Int) }
                entity<Bottom>() tag { Move(dir="down", dist=ref["distance"] as Int) }
                +("plummet"/"drop"/"down"/"downward"/"downwards") tag { Move(dir="down", dist=ref["distance"] as Int) }
                entity<Left>() tag { Move(dir="left", dist=ref["distance"] as Int) }
                entity<Right>() tag { Move(dir="right", dist=ref["distance"] as Int) }
            }
            -("by" / "for" / "to")
            ruleref("distance")
        }


        rule("distance", public = false) {
            +("1" / "one" / "a notch" / "a bit" / "slightly") tag { 1 }
            +("2" / "two") tag { 2 }
            +("3" / "three") tag { 3 }
            +("4" / "four") tag { 4 }
            +("5" / "five") tag { 5 }
        }
    }


/**
 * Catch the users intention to stop movement.
 */
class Stop : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf(
            "hold", "stop", "enough", "perfect",
            "that's it", "wait", "cancel", "nice", "good"
        )
    }
}


/**
 * Catch the users intention to perform the reverse action of a previous action.
 */
class Back : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("back", "return", "too much", "too far")
    }
}


/**
 * Catch the users intention to perform a previous action again.
 */
class Again : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf(
            "again", "repeat", "go on", "once more", "some more",
            "one more time", "a second time", "another time"
        )
    }
}


/**
 * One can rotate a piece by a certain [degree].
 * The sign of the degree, speaking whether we want the rotation to be
 * performed to the left (-1) or right (Default: 1) is saved in the [dir] attribute.
 */
class Rotation(
    var dir : Int = 1,
    var degree: Int = 90
) : GrammarEntity() {
    override fun getGrammar(lang: Language): Grammar {
        return when (lang.main) {
            "en" -> RotationGrammarEn
            else -> throw InterpreterException("Language $lang not supported for ${javaClass.name}")
        }
    }
}


val RotationGrammarEn =
    grammar {
        /** e.g. rotate/ pivot the piece */
        // -> captured in the relevant state due to bug with GrammarEntity

        /** e.g. turn it counterclockwise */
        rule(public = true){
            group {
                ruleref("general")
                ruleref("direction")
            } tag { Rotation(
                dir=ref["direction"] as Int)
            }
        }

        /** e.g. tilt this piece by 180 degrees */
        rule(public = true){
            group {
                ruleref("general")
                ruleref("degree")
            } tag { Rotation(
                degree = ref["degree"] as Int)
            }
        }

        /** e.g. rotate a figure (by) 180 degrees in counterclockwise direction */
        rule(public = true){
            group {
                ruleref("general")
                ruleref("degree")
                ruleref("direction")
            } tag { Rotation(
                        dir=ref["direction"] as Int,
                        degree = ref["degree"] as Int)
                }
            }

        /** e.g. rotate a figure counterclockwise (by) 90 degrees */
        rule(public = true){
            group {
                ruleref("general")
                ruleref("direction")
                ruleref("degree")
            } tag { Rotation(
                dir=ref["direction"] as Int,
                degree = ref["degree"] as Int)
            }
        }

    rule("general", public = false) {
        choice {
            group {
                +("turn" / "rotate" / "spin" / "tilt" / "whirl" / "pivot" / "swing" / "twist" / "perform")
                -("a" / "the" / "this" / "that")
                -("piece" / "peace" / "figure" / "it" / "object" / "rotation" / "at")
            }
            group {
                +("turn" / "rotate" / "spin" / "tilt" / "whirl" / "pivot" / "swing" / "twist" / "perform")
                -("a" / "the" / "this" / "that")
                entity<Shapes>()
            }
        } tag { Rotation() }
    }

    rule("degree", public = false) {
        group {
            -"by"
            -"a"
            choice {
                +("quarter" / "90" / "90°" / "ninety" / "one-fourth" / "quadrant" / "1/4") tag { 90 }
                +("around" / "180" / "180°" / "half" / "upside-down" / "one hundred and eighty" / "1/2") tag { 180 }
                +("270" / "270°" /"two hundred and seventy" / "three quarter" / "3/4") tag { 270 }
            }
            -"degrees"
        }
    }

    rule("direction", public = false) {
        group {
            -("to" / "in")
            -("the" / "a")
            choice {
                +("clockwise" / "dextral" / "right-handed" / "dexter" / "rightward" / "starboard") tag {1}
                entity<Right>() tag {1}
                +("counter-clockwise" / "counterclockwise" / "left-handed" / "contraclockwise" / "anticlockwise" / "leftward" / "sinister" / "sinistral" / "sinistrous" / "larboard") tag {-1}
                entity<Left>() tag {-1}
            }
        }
    }
}


/**
 * One can mirror a piece on the horizontal and vertical [axis].
 * The default is the vertical mirroring.
 *
 * vertical: e.g. |_ -> _|
 * horizontal: e.g. |_ -> |‾
 */
class Mirror(
    var axis : String = "vertical"
    ) : GrammarEntity() {
    override fun getGrammar(lang: Language): Grammar {
        return when (lang.main) {
            "en" -> MirrorGrammarEn
            else -> throw InterpreterException("Language $lang not supported for ${javaClass.name}")
        }
    }
}


val MirrorGrammarEn =
    grammar {
        /** e.g. Mirror the piece. */
        rule(public = true){
            +("mirror" / "reflect" / "flip") tag { Mirror() }
        }

        /** e.g. I want you to flip the piece on the vertical axis. */
        rule(public = true){
            group {
                +("mirror" / "reflect" / "flip")
                -"on"
                -("a" / "the" / "this" / "that" / "a")
                -("piece" / "peace"/ "figure" / "it" / "object" / "rotation")
                choice {
                    +("horizontally" / "horizontal") tag { Mirror(axis="horizontal") }
                    +("vertically" / "vertical") tag { Mirror(axis="vertical") }
                }
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
*/