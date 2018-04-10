package org.hathitrust.htrc.textprocessing.runningheaders

/**
  * Trait that adds methods for retrieving the header, body, and footer elements of a page
  */
trait PageWithStructure[T] {
  /**
    * Returns the underlying page
    *
    * @return The underlying page
    */
  def underlying: T

  /**
    * Tests whether the page contains an identified header
    *
    * @return True if the page contains an identified header, false otherwise
    */
  def hasHeader: Boolean

  /**
    * Tests whether the page contains an identified footer
    *
    * @return True if the page contains an identified footer, false otherwise
    */
  def hasFooter: Boolean

  /**
    * Returns the sequence of lines from the page representing the header
    *
    * @return The sequence of lines from the page representing the header
    */
  def headerLines: IndexedSeq[String]

  /**
    * Returns the sequence of lines from the page representing the body
    *
    * @return The sequence of lines from the page representing the body
    */
  def bodyLines: IndexedSeq[String]

  /**
    * Returns the sequence of lines from the page representing the footer
    *
    * @return The sequence of lines from the page representing the footer
    */
  def footerLines: IndexedSeq[String]

  /**
    * Returns the page header text
    *
    * @param sep (Optional) The separator to use for joining the header lines
    * @return The page header text
    */
  def header(sep: String = "\n"): String = headerLines.mkString(sep)

  /**
    * Returns the page body text
    *
    * @param sep (Optional) The separator to use for joining the body lines
    * @return The page body text
    */
  def body(sep: String = "\n"): String = bodyLines.mkString(sep)

  /**
    * Returns the page footer text
    *
    * @param sep (Optional) The separator to use for joining the footer lines
    * @return The page footer text
    */
  def footer(sep: String = "\n"): String = footerLines.mkString(sep)
}