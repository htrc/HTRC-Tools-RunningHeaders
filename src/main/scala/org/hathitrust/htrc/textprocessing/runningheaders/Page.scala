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
  def textLines: Lines

  /**
    * Returns the text on the page (respecting the OS-specific line separator)
    *
    * @return The text on the page (respecting the OS-specific line separator)
    */
  def text: String = textLines.mkString("", System.lineSeparator(), System.lineSeparator())
}
