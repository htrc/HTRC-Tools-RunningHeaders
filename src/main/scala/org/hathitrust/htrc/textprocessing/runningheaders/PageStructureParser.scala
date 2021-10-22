package org.hathitrust.htrc.textprocessing.runningheaders

import org.hathitrust.htrc.textprocessing.runningheaders.utils.Helper
import org.hathitrust.htrc.tools.scala.implicits.CollectionsImplicits._

import scala.collection.compat._
import scala.collection.mutable
import scala.util.matching.Regex

object PageStructureParser {
  type StructuredPage = Page with PageStructure
  private val numberRegex: Regex = """(?<=^|\s)\p{Nd}{1,4}(?=\s|$)""".r

  protected def defaultStructuredPageBuilder(page: Page, headerLinesCount: Int, footerLinesCount: Int): StructuredPage =
    new Page with PageStructure {
      override def numHeaderLines: Int = headerLinesCount
      override def numFooterLines: Int = footerLinesCount
      override def textLines: Lines = page.textLines
    }

  /**
    * Method for parsing a sequence of `Page`s to identify running headers and footers
    *
    * @param pages              The pages to compute structure for
    * @param windowSize         (optional) How far should similarity scores be computed from the current page
    * @param minClusterSize     (optional) The minimum number of pages required in a cluster before the cluster
    *                           is deemed as containing a true running header or footer
    * @param minSimilarityScore (optional) The minimum score required for two candidate headers to be deemed
    *                           as being the same
    * @param maxNumHeaderLines  (optional) The maximum number of lines from the top of the page to consider
    *                           as a candidate header
    * @param maxNumFooterLines  (optional) The maximum number of lines from the bottom of the page to consider
    *                           as a candidate footer
    * @param builder            (optional) A custom builder that returns a new instance of a class deriving
    *                           from PageStructure; the builder is given the page to build from, and two
    *                           integers representing the number of header and footer lines on that
    *                           page
    * @param factory                Implicit builder for collection type `C`
    * @tparam T The type parameter for the Page
    * @tparam U The type parameter of the resulting structured page
    * @tparam C The collection type
    * @return A new collection of pages deriving from `PageStructure` having additional
    *         structure-retrieving methods
    */
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  def parsePageStructure[T <: Page, U <: PageStructure, C[X] <: collection.Seq[X]](pages: C[T],
                                                                                  windowSize: Int = 6,
                                                                                  minSimilarityScore: Double = 0.7d,
                                                                                  minClusterSize: Int = 3,
                                                                                  maxNumHeaderLines: Int = 3,
                                                                                  maxNumFooterLines: Int = 3,
                                                                                  builder: (T, Int, Int) => U = defaultStructuredPageBuilder _)
                                                                                 (implicit factory: Factory[U, C[U]]): C[U] = {
    val candidateHeaderLines = mutable.ListBuffer.empty[IndexedSeq[Line]]
    val candidateFooterLines = mutable.ListBuffer.empty[IndexedSeq[Line]]

    val pagesLines = pages.toSeq.map(page => page.textLines.zipWithIndex.map { case (text, lineNum) => new Line(text, lineNum, page) })

    for (lines <- pagesLines) {
      // Ignore lines that are <4 characters long and/or have no alphabetic characters
      candidateHeaderLines += lines.take(maxNumHeaderLines).filterNot(_.cleanedText.length < 4)
      candidateFooterLines += lines.takeRight(maxNumFooterLines).filterNot(_.cleanedText.length < 4)
    }

    val headersForComparison =
      Helper.pairwiseCombineElementsWithinDistanceOf(windowSize)(candidateHeaderLines.toList)
    val footersForComparison =
      Helper.pairwiseCombineElementsWithinDistanceOf(windowSize)(candidateFooterLines.toList)

    val headerLineSimilarities = headersForComparison.flatMap {
      case (lines1, lines2) =>
        for (l1 <- lines1; l2 <- lines2; sim = l1 ~ l2 if sim >= minSimilarityScore)
          yield l1 -> l2
    }

    val footerLineSimilarities = footersForComparison.flatMap {
      case (lines1, lines2) =>
        for (l1 <- lines1; l2 <- lines2; sim = l1 ~ l2 if sim >= minSimilarityScore)
          yield l1 -> l2
    }

    // Cluster the lines by computing the Levenshtein distance between each pair of lines,
    // keeping together all lines that have a distance < `maxDistance`. Once clustered, keep only
    // clusters that have at least `minClusterSize` elements
    val headerClusters = clusterLines(headerLineSimilarities).filter(_.size >= minClusterSize)
    @SuppressWarnings(Array( "org.wartremover.warts.Var"))
    var footerClusters = clusterLines(footerLineSimilarities).filter(_.size >= minClusterSize)

    if (footerClusters.isEmpty) {
      // if regular clustering didn't turn up any identified footers, try to see if
      // page numbers can be identified and mark those as footers
      val potentialPageNumbers =
        pagesLines
          .view
          .map(_.lastOption)
          .collect {
            case Some(line) => line -> numberRegex.findAllIn(line.text).map(_.toInt).toList
          }
          .collect {
            case (line, x :: Nil) => line -> x
          }

      footerClusters = potentialPageNumbers
        .iterator
        .groupConsecutiveWhen[List] { case ((_, n1), (_, n2)) => n2 - n1 == 1 }
        .map(_.map { case (line, _) => line })
        .withFilter(_.size >= minClusterSize)
        .toSet
    }

    val lastHeaderLineForPage =
      headerClusters
        .flatten
        .groupBy(_.page)
        .view
        .mapValues(lines => lines.maxByOpt(_.lineNumber).map(_.lineNumber))
        .toMap

    val firstFooterLineForPage =
      footerClusters
        .flatten
        .groupBy(_.page)
        .view
        .mapValues(lines => lines.minByOpt(_.lineNumber).map(_.lineNumber))
        .toMap

    pages
      .map { page =>
        val lastHeaderLine = lastHeaderLineForPage.get(page).flatten
        val firstFooterLine = firstFooterLineForPage.get(page).flatten
        val numHeaderLines = lastHeaderLine.map(_ + 1).getOrElse(0)
        val numFooterLines = firstFooterLine.map(page.textLines.length - _).getOrElse(0)
        builder(page, numHeaderLines, numFooterLines)
      }
      .to(factory)
  }

  /**
    * Creates maximal clusters of similar lines
    *
    * @param lines The list of similar line pairs
    * @return The clustered lines
    */
  protected def clusterLines(lines: List[(Line, Line)]): Set[List[Line]] = {
    val clusterMap = mutable.HashMap.empty[Line, mutable.ListBuffer[Line]]

    for ((l1, l2) <- lines) {
      val c1 = clusterMap.get(l1)
      val c2 = clusterMap.get(l2)
      (c1, c2) match {
        case (Some(s1), Some(s2)) if !(s1 eq s2) =>
          val (smaller, larger) = if (s1.size < s2.size) (s1, s2) else (s2, s1)
          larger ++= smaller
          smaller.foreach(clusterMap(_) = larger)

        case (Some(s1), None) =>
          clusterMap(l2) = s1 += l2

        case (None, Some(s2)) =>
          clusterMap(l1) = s2 += l1

        case (None, None) =>
          val c = mutable.ListBuffer(l1, l2)
          clusterMap(l1) = c
          clusterMap(l2) = c

        case _ => // same cluster
      }
    }

    clusterMap.valuesIterator.map(_.toList).toSet
  }

}
