package org.hathitrust.htrc.textprocessing.runningheaders

import org.hathitrust.htrc.textprocessing.runningheaders
import org.scalatest.{FlatSpec, ParallelTestExecution}
import org.scalatest.Matchers._
import org.scalatest.prop.PropertyChecks

import scala.io.{Codec, Source}
import scala.util.Try

@SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.TryPartial"))
class TestPageStructureParser extends FlatSpec
  with PropertyChecks with ParallelTestExecution {

  trait SampleVolume {

    private def loadPages(range: Range, from: String): Try[List[Page]] = Try {
      implicit val codec: Codec = Codec.UTF8

      val pageData = range.view.map(_.toString).map(n => s"$from/$n.txt").map(getClass.getResourceAsStream).to[List]
      val pages = pageData.map(data => new Page {
        override val textLines: IndexedSeq[String] = Source.fromInputStream(data).getLines().toIndexedSeq
      })

      pages
    }

    val pages: List[Page] = loadPages(1 to 9, "/vol1").get
  }

  "Running headers" should "be correctly identified" in new SampleVolume {
    val structuredPages: List[PageWithStructure[Page]] = PageStructureParser.parsePageStructure(pages)

    structuredPages.map(_.header("|")) should contain theSameElementsInOrderAs Seq(
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
    val structuredPages: List[PageWithStructure[Page]] = runningheaders.PageStructureParser.parsePageStructure(pages)

    structuredPages.map(_.footer("|")) should contain theSameElementsInOrderAs Seq(
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

  "Page body" should "be correctly identified" in new SampleVolume {
    val structuredPages: List[PageWithStructure[Page]] = runningheaders.PageStructureParser.parsePageStructure(pages)

    structuredPages.map(_.bodyLines.length) should contain theSameElementsInOrderAs Seq(
      7, 11, 7, 7, 7, 7, 7, 7, 5
    )
  }
}
