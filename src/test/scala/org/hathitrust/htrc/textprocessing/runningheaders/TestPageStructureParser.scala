package org.hathitrust.htrc.textprocessing.runningheaders

import org.hathitrust.htrc.textprocessing.runningheaders.PageStructureParser.StructuredPage
import org.scalatest.Matchers._
import org.scalatest.{FlatSpec, ParallelTestExecution}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.io.{Codec, Source}
import scala.util.Try

@SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.TryPartial"))
class TestPageStructureParser extends FlatSpec
  with ScalaCheckPropertyChecks with ParallelTestExecution {

  trait SampleVolume {
    protected def loadPages(range: Range, from: String): Try[List[Page]] = Try {
      implicit val codec: Codec = Codec.UTF8

      val pageData = range.view.map(_.toString).map(n => s"$from/$n.txt").map(getClass.getResourceAsStream)
      val pages = pageData.map(data => new Page {
        override val textLines: IndexedSeq[String] = Source.fromInputStream(data).getLines().toIndexedSeq
      })

      pages.to[List]
    }
  }

  trait SampleVol1 extends SampleVolume {
    val pages: List[Page] = loadPages(0 to 9, "/vol1").get
  }

  trait SampleVol2 extends SampleVolume {
    val pages: List[Page] = loadPages(0 to 9, "/vol2").get
  }

  "Running headers" should "be correctly identified" in new SampleVol1 {
    val structuredPages: List[StructuredPage] = PageStructureParser.parsePageStructure(pages)

    structuredPages.map(_.headerLines.mkString("|")) should contain theSameElementsInOrderAs Seq(
      "",
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

  "Running footers" should "be correctly identified" in new SampleVol1 {
    val structuredPages: List[StructuredPage] = PageStructureParser.parsePageStructure(pages)

    structuredPages.map(_.footerLines.mkString("|")) should contain theSameElementsInOrderAs Seq(
      "",
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

  "Page body" should "be correctly identified" in new SampleVol1 {
    val structuredPages: List[StructuredPage] = PageStructureParser.parsePageStructure(pages)

    structuredPages.map(_.bodyLines.length) should contain theSameElementsInOrderAs Seq(
      0, 7, 43, 28, 26, 30, 31, 27, 28, 15
    )
  }

  "Running footers" should "be correctly identified when containing page numbers" in new SampleVol2 {
    val structuredPages: List[StructuredPage] = PageStructureParser.parsePageStructure(pages)

    structuredPages.map(_.footerLines.mkString("|")) should contain theSameElementsInOrderAs Seq(
      "",
      "",
      "2",
      "                                                                                    3",
      "4",
      "                                                                                    5",
      "6",
      "                                                                                    7",
      "8",
      "                                                                                    9"
    )
  }
}
