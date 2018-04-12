package org.hathitrust.htrc.textprocessing.runningheaders

import org.hathitrust.htrc.textprocessing.runningheaders.utils.StringMetrics.levenshteinDistance

import scala.math.max

private[runningheaders] class Line(val text: String, val lineNumber: Int, val page: Page) {
  lazy val cleanedText: String =
    text.replaceAll("""[^\p{L}\s]+""", "").replaceAll("""\s{2,}""", " ").trim.toLowerCase

  /**
    * Calculates the (Levenshtein) similarity metric between two lines
    *
    * @param other The other line
    * @return The similarity metric value (between 0 and 1, higher means more similar)
    */
  def ~(other: Line): Double = {
    1 - levenshteinDistance(cleanedText, other.cleanedText).toDouble /
      max(cleanedText.length, other.cleanedText.length)
  }

  override def equals(obj: scala.Any): Boolean = obj match {
    case that: Line => page.eq(that.page) && lineNumber == that.lineNumber
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(lineNumber.asInstanceOf[Object], page.asInstanceOf[Object])
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

}
