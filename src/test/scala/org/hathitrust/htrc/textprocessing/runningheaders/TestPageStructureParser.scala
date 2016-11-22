package org.hathitrust.htrc.textprocessing.runningheaders

import org.scalatest.{FlatSpec, ParallelTestExecution}
import org.scalatest.Matchers._
import org.scalatest.prop.PropertyChecks

import scala.io.Codec
import scala.util.Try

class TestPageStructureParser extends FlatSpec
  with PropertyChecks with ParallelTestExecution {

  trait SampleVolume {

    private def loadPages(range: Range, from: String): Try[Seq[Page]] = Try {
      implicit val codec = Codec.UTF8

      // load the sample test volume from the resources folder
      val pageSeq = range.map(_.toString)
      val pageData = pageSeq.map(n => s"$from/$n.txt").map(getClass.getResourceAsStream)
      val pages = pageSeq.zip(pageData).map { case (seq, data) => Page(data, seq) }

      pages
    }

    val pages = loadPages(1 to 9, "/vol1").get
  }

  "Running headers" should "be correctly identified" in new SampleVolume {
    val structuredPages = PageStructureParser.parsePageStructure(pages)

    structuredPages.map(_.getHeaderText("|")) should contain theSameElementsInOrderAs Seq(
      "",
      "CHAPTER 1|INTRODUCTION TO RUNNING HEADERS|Lorem Ipsum style",
      "1 INTRODUCTION TO RUNNING HEADERS|Lorem Ipsum style",
      "INTRODUCTION TO RUNNING HEADERS 1|Lorem Ipsum style",
      "1 INTRODUCTION TO RUNNING HEADERS|Lorem Ipsum style",
      "CHAPTER 2|EVERYTHING IS RELATIVE",
      "2 EVERYTHING IS RELATIVE",
      "EVERYTHING IS RELATIVE 2",
      "2 EVERYTHING IS RELATIVE"
    )
  }

  "Running footers" should "be correctly identified" in new SampleVolume {
    val structuredPages = PageStructureParser.parsePageStructure(pages)

    structuredPages.map(_.getFooterText("|")) should contain theSameElementsInOrderAs Seq(
      "",
      "Page 2",
      "Page 3",
      "Page 4",
      "Page 5",
      "Page 6",
      "Page 7",
      "Page 8",
      "Page 9"
    )
  }
}
