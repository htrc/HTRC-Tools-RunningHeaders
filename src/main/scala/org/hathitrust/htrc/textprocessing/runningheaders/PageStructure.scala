package org.hathitrust.htrc.textprocessing.runningheaders

/**
  * Trait that adds methods for retrieving the header, body, and footer elements of a page
  */
trait PageStructure { self: Page =>
  /**
    * Returns the number of lines comprising the header
    *
    * @return The number of lines comprising the header
    */
  def numHeaderLines: Int

  /**
    * Returns the number of lines comprising the footer
    *
    * @return The number of lines comprising the footer
    */
  def numFooterLines: Int

  /**
    * Tests whether the page contains an identified header
    *
    * @return True if the page contains an identified header, false otherwise
    */
  def hasHeader: Boolean = numHeaderLines > 0

  /**
    * Tests whether the page contains an identified page body
    *
    * @return True if the page contains an identified page body, false otherwise
    */
  def hasBody: Boolean = textLines.length - numHeaderLines - numFooterLines > 0

  /**
    * Tests whether the page contains an identified footer
    *
    * @return True if the page contains an identified footer, false otherwise
    */
  def hasFooter: Boolean = numFooterLines > 0

  /**
    * Returns the sequence of lines from the page representing the header
    *
    * @return The sequence of lines from the page representing the header
    */
  def headerLines: IndexedSeq[String] = textLines.take(numHeaderLines)

  /**
    * Returns the sequence of lines from the page representing the body
    *
    * @return The sequence of lines from the page representing the body
    */
  def bodyLines: IndexedSeq[String] =
    textLines.slice(numHeaderLines, textLines.length - numFooterLines)

  /**
    * Returns the sequence of lines from the page representing the footer
    *
    * @return The sequence of lines from the page representing the footer
    */
  def footerLines: IndexedSeq[String] = textLines.takeRight(numFooterLines)

  /**
    * Returns the page header text (respecting the OS-specific line separator)
    *
    * @return The page header text (respecting the OS-specific line separator)
    */
  def header: String = headerLines.mkString(System.lineSeparator())

  /**
    * Returns the page body text (respecting the OS-specific line separator)
    *
    * @return The page body text (respecting the OS-specific line separator)
    */
  def body: String = bodyLines.mkString(System.lineSeparator())

  /**
    * Returns the page footer text (respecting the OS-specific line separator)
    *
    * @return The page footer text (respecting the OS-specific line separator)
    */
  def footer: String = footerLines.mkString(System.lineSeparator())
}