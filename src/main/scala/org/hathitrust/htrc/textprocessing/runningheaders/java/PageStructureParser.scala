package org.hathitrust.htrc.textprocessing.runningheaders.java

import org.hathitrust.htrc.textprocessing.runningheaders.utils.TriFunction
import org.hathitrust.htrc.textprocessing.runningheaders.{Page, PageStructure, PageStructureParser => ScalaPageStructureParser}

import scala.jdk.CollectionConverters._

object PageStructureParser {

  val defaultConfig: StructureParserConfig =
    new StructureParserConfig(
      windowSize = 6,
      minSimilarityScore = 0.7d,
      minClusterSize = 3,
      maxNumHeaderLines = 3,
      maxNumFooterLines = 3
    )

  class StructureParserConfig(val windowSize: Int,
                              val minSimilarityScore: Double,
                              val minClusterSize: Int,
                              val maxNumHeaderLines: Int,
                              val maxNumFooterLines: Int)

  trait StructuredPage extends Page with PageStructure

  def parsePageStructure[T <: Page, U <: StructuredPage](pages: java.util.List[T],
                                                         parserConfig: StructureParserConfig,
                                                         fun: TriFunction[T, Integer, Integer, U]): java.util.List[U] =
    ScalaPageStructureParser.parsePageStructure(
      pages = pages.asScala.toList,
      windowSize = parserConfig.windowSize,
      minSimilarityScore = parserConfig.minSimilarityScore,
      minClusterSize = parserConfig.minClusterSize,
      maxNumHeaderLines = parserConfig.maxNumHeaderLines,
      maxNumFooterLines = parserConfig.maxNumFooterLines,
      builder = (page: T, headerLinesCount, footerLinesCount) => fun.apply(page, headerLinesCount, footerLinesCount)
    ).asJava

}
