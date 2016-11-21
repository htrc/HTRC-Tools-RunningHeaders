package org.hathitrust.htrc.textprocessing.runningheaders

/**
  * Object defining a line of text on a page
  *
  * @param text       The text
  * @param lineNumber The line number (used primarily for distinguishing lines from each other)
  * @param pageSeq    The page (sequence) identifier (used for distinguishing lines
  *                   on different pages)
  */
class Line(val text: String, val lineNumber: Int, val pageSeq: String) {

  import edu.illinois.i3.scala.utils.implicits.StringsImplicits._

  import scala.math.max

  protected[runningheaders] var isHeader = false
  protected[runningheaders] var isFooter = false

  // trim string, lowercase, replace multiple whitespaces with single whitespace, and
  // remove punctuation and numbers
  protected[runningheaders] lazy val cleanedText =
    text.replaceAll("""[^\p{L}\s]+""", "").replaceAll("""\s{2,}""", " ").trim.toLowerCase

  /**
    * Calculates the (Levenshtein) similarity metric between two lines
    *
    * @param other The other line
    * @return The similarity metric value (between 0 and 1, higher means more similar)
    */
  def ~(other: Line): Double = {
    1 - cleanedText.editDistance(other.cleanedText).toDouble /
      max(cleanedText.length, other.cleanedText.length)
  }

  /**
    * Defines that two lines are considered identical if the `lineNumber` and `pageSeq` match
    *
    * @param obj The other line
    * @return True if the same line, False otherwise
    */
  override def equals(obj: Any): Boolean = obj match {
    case that: Line =>
      lineNumber == that.lineNumber &&
        pageSeq == that.pageSeq

    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(lineNumber, pageSeq)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString: String = s"Line(P$pageSeq,L$lineNumber: $text)"
}