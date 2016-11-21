package org.hathitrust.htrc.textprocessing.runningheaders

/**
  * Trait that adds methods for retrieving the header, body, and footer elements of a page
  */
trait PageWithStructure extends Page {

  protected[runningheaders] val MaxNumHeaderLines: Int
  protected[runningheaders] val MaxNumFooterLines: Int

  /**
    * Returns the sequence of lines from the page representing the header
    *
    * @return The sequence of lines from the page representing the header
    */
  def getHeader: Seq[Line] = lines.takeWhile(_.isHeader)

  /**
    * Returns the page header text
    *
    * @param sep (Optional) The separator to use for joining the header lines
    * @return The page header text
    */
  def getHeaderText(sep: String = "\n"): String = getHeader.map(_.text).mkString(sep)

  /**
    * Returns the sequence of lines from the page representing the body
    *
    * @return The sequence of lines from the page representing the body
    */
  def getBody: Seq[Line] = lines.dropWhile(_.isHeader).takeWhile(!_.isFooter)

  /**
    * Returns the page body text
    *
    * @param sep (Optional) The separator to use for joining the body lines
    * @return The page body text
    */
  def getBodyText(sep: String = "\n"): String = getBody.map(_.text).mkString(sep)

  /**
    * Returns the sequence of lines from the page representing the footer
    *
    * @return The sequence of lines from the page representing the footer
    */
  def getFooter: Seq[Line] = lines.takeRight(MaxNumFooterLines).filter(_.isFooter)

  /**
    * Returns the page footer text
    *
    * @param sep (Optional) The separator to use for joining the footer lines
    * @return The page footer text
    */
  def getFooterText(sep: String = "\n"): String = getFooter.map(_.text).mkString(sep)
}
