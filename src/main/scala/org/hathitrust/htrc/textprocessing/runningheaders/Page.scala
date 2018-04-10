package org.hathitrust.htrc.textprocessing.runningheaders

/**
  * Trait defining the notion of a page, as a collection of lines of text
  */
trait Page {

  /**
    * Returns the lines of text on the page
    *
    * @return The lines of text on the page
    */
  def textLines: IndexedSeq[String]

  /**
    * Returns the text on the page
    *
    * @param sep (Optional) The separator to use when joining the lines on the page
    * @return The text on the page
    */
  def text(sep: String = "\n"): String = textLines.mkString(sep)

}
