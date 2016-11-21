package org.hathitrust.htrc.textprocessing.runningheaders

import java.io.InputStream

import scala.io.{BufferedSource, Codec, Source}

object Page {
  def apply(text: String, pageSeq: String): Page =
    Page(text.split("\n").iterator, pageSeq)

  def apply(source: BufferedSource, pageSeq: String): Page =
    Page(source.getLines(), pageSeq)

  def apply(stream: InputStream, pageSeq: String)(implicit codec: Codec): Page =
    Page(Source.fromInputStream(stream), pageSeq)

  def apply(lines: Iterator[String], pageSeq: String): Page = {
    val pageLines = lines.zipWithIndex.map { case (l, n) => new Line(l, n, pageSeq) }.toList
    new Page(pageLines, pageSeq)
  }
}

/**
  * Object representing a page of text from a volume
  *
  * @param lines   The sequence of lines of text on the page
  * @param pageSeq The page (sequence) identifier, used for distinguishing pages from each other
  */
class Page(val lines: Seq[Line], val pageSeq: String) {

  /**
    * Returns the text on the page
    *
    * @param sep (Optional) The separator to use when joining the lines on the page
    * @return The text on the page
    */
  def getText(sep: String = "\n"): String = getTextLines.mkString(sep)

  /**
    * Returns the lines of text on the page
    *
    * @return The lines of text on the page
    */
  def getTextLines: Seq[String] = lines.map(_.text)

  /**
    * Defines that two Pages are considered identical if their `pageSeq` are the same
    *
    * @param obj The other page
    * @return True if the same page, False otherwise
    */
  override def equals(obj: Any): Boolean = obj match {
    case that: Page => pageSeq == that.pageSeq
    case _ => false
  }

  override def hashCode(): Int = pageSeq.hashCode
}