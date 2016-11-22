package org.hathitrust.htrc.textprocessing.runningheaders

import org.hathitrust.htrc.textprocessing.runningheaders.utils.Helper

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

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
    * @return A new sequence of Pages with additional structure-retrieving methods
    */
  def parsePageStructure(pages: Seq[Page],
                         windowSize: Int = 6,
                         minSimilarityScore: Double = 0.7d,
                         minClusterSize: Int = 3,
                         maxNumHeaderLines: Int = 3,
                         maxNumFooterLines: Int = 3): Seq[Page with PageWithStructure] = {

    // Ignore lines that are <4 characters long and/or have no alphabetic characters
    val candidateHeaderLines =
      pages.map(_.lines.take(maxNumHeaderLines).filterNot(_.cleanedText.length < 4))
    val candidateFooterLines =
      pages.map(_.lines.takeRight(maxNumFooterLines).filterNot(_.cleanedText.length < 4))

    val headersForComparison =
      Helper.pairwiseCombineElementsWithinDistanceOf(windowSize)(candidateHeaderLines)
    val footersForComparison =
      Helper.pairwiseCombineElementsWithinDistanceOf(windowSize)(candidateFooterLines)

    val headerLineSimiarities = headersForComparison.flatMap {
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
    val headerClusters = clusterLines(headerLineSimiarities).filter(_.size >= minClusterSize)
    val footerClusters = clusterLines(footerLineSimilarities).filter(_.size >= minClusterSize)

    // Mark each line in the clusters as being part of the header or footer
    headerClusters.flatten.foreach(_.isHeader = true)
    footerClusters.flatten.foreach(_.isFooter = true)

    // Loop through all pages to mark all the lines above (below) the last (first) line marked as
    // a header (footer) as being also part of the header (footer)
    for (page <- pages) {
      page.lines.take(maxNumHeaderLines).reverse.dropWhile(l => !l.isHeader).foreach(_.isHeader = true)
      page.lines.takeRight(maxNumFooterLines).dropWhile(l => !l.isFooter).foreach(_.isFooter = true)
    }

    // Return a new `Page` object with the additional structure access methods
    pages.map(p => new Page(p.lines, p.pageSeq) with PageWithStructure {
      override protected[runningheaders] val MaxNumHeaderLines: Int = maxNumHeaderLines
      override protected[runningheaders] val MaxNumFooterLines: Int = maxNumFooterLines
    })
  }

  /**
    * Creates maximal clusters of similar lines
    *
    * @param lines The list of similar line pairs
    * @return The clustered lines
    */
  protected def clusterLines(lines: List[(Line, Line)]): Set[ListBuffer[Line]] = {
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
