package org.hathitrust.htrc.textprocessing.runningheaders

trait PageWithStructure[T] {
  def underlying: T

  def headerLines: Array[String]
  def bodyLines: Array[String]
  def footerLines: Array[String]

  def header(sep: String = "\n"): String = headerLines.mkString(sep)
  def body(sep: String = "\n"): String = bodyLines.mkString(sep)
  def footer(sep: String = "\n"): String = footerLines.mkString(sep)

  def hasHeader: Boolean
  def hasFooter: Boolean
}
