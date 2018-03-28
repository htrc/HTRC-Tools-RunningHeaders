package org.hathitrust.htrc.textprocessing.runningheaders

trait Page {

  def textLines: Array[String]

  def text(sep: String = "\n"): String = textLines.mkString(sep)

  private[runningheaders] lazy val lines: Array[Line] =
    textLines.zipWithIndex.map { case (text, lineNum) => new Line(text, lineNum, this) }
}
