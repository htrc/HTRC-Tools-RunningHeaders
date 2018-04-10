package org.hathitrust.htrc.textprocessing.runningheaders

import org.hathitrust.htrc.textprocessing.runningheaders.utils.Helper

import scala.collection.generic.CanBuildFrom
import scala.collection.{SeqLike, mutable}
import scala.language.higherKinds

object PageStructureParser {

  /**
    * Method for parsing a sequence of `Page`s to identify running headers and footers
    *
    * @param pages              The pages to compute structure for
    * @param windowSize         How far should similarity scores be computed from the current page
    * @param minClusterSize     The minimum number of pages required in a cluster before the cluster
    *                           is deemed as containing a true running header or footer
    * @param minSimilarityScore The minimum score required for two candidate headers to be deemed
    *                           as being the same
    * @param maxNumHeaderLines  The maximum number of lines from the top of the page to consider
    *                           as a candidate header
    * @param maxNumFooterLines  The maximum number of lines from the bottom of the page to consider
    *                           as a candidate footer
    * @param cbf The builder
    * @tparam T The type parameter for the Page
    * @tparam C The collection type
    * @return A new collection of Pages with additional structure-retrieving methods
    */
  def parsePageStructure[T <: Page, C[X] <: SeqLike[X, C[X]]](pages: C[T],
                                                              windowSize: Int = 6,
                                                              minSimilarityScore: Double = 0.7d,
                                                              minClusterSize: Int = 3,
                                                              maxNumHeaderLines: Int = 3,
                                                              maxNumFooterLines: Int = 3)
                                                             (implicit cbf: CanBuildFrom[C[T], PageWithStructure[T], C[PageWithStructure[T]]]): C[PageWithStructure[T]] = {
    val candidateHeaderLines = mutable.ListBuffer.empty[IndexedSeq[Line]]
    val candidateFooterLines = mutable.ListBuffer.empty[IndexedSeq[Line]]

    for (page <- pages) {
      val lines = page.textLines.zipWithIndex.map { case (text, lineNum) => new Line(text, lineNum, page) }

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
    val footerClusters = clusterLines(footerLineSimilarities).filter(_.size >= minClusterSize)

    val lastHeaderLineForPage =
      headerClusters
        .flatten
        .groupBy(_.page)
        .mapValues(lines => lines.maxBy(_.lineNumber).lineNumber)

    val firstFooterLineForPage =
      footerClusters
        .flatten
        .groupBy(_.page)
        .mapValues(lines => lines.minBy(_.lineNumber).lineNumber)

    pages
      .map { page =>
        val lastHeaderLine = lastHeaderLineForPage.get(page)
        val firstFooterLine = firstFooterLineForPage.get(page)
        new PageWithStructure[T] {
          override val underlying: T = page
          override val numHeaderLines: Int = lastHeaderLine.map(_ + 1).getOrElse(0)
          override val numFooterLines: Int = firstFooterLine.map(underlying.textLines.length - _).getOrElse(0)
        }
      }
  }

  /**
    * Creates maximal clusters of similar lines
    *
    * @param lines The list of similar line pairs
    * @return The clustered lines
    */
  protected def clusterLines(lines: List[(Line, Line)]): Set[mutable.ListBuffer[Line]] = {
    import org.hathitrust.htrc.tools.scala.implicits.AnyRefImplicits._

    val clusterMap = mutable.HashMap.empty[Line, mutable.ListBuffer[Line]]

    for ((l1, l2) <- lines) {
      val c1 = clusterMap.get(l1)
      val c2 = clusterMap.get(l2)
      (c1, c2) match {
        case (Some(s1), Some(s2)) if s1 neq s2 =>
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

    clusterMap.values.toSet
  }

}
