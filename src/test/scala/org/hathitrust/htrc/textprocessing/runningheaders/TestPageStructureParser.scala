package org.hathitrust.htrc.textprocessing.runningheaders

import _root_.java.util.Scanner

import org.hathitrust.htrc.textprocessing.runningheaders.PageStructureParser.StructuredPage
import org.hathitrust.htrc.tools.scala.io.IOUtils.readLinesWithDelimiters
import org.scalatest.ParallelTestExecution
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

import scala.io.Codec
import scala.util.Try

@SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.TryPartial"))
class TestPageStructureParser extends AnyFlatSpec
  with ParallelTestExecution {

  trait SampleVolume {
    protected def loadPages(range: Range, from: String): Try[List[Page]] = Try {
      implicit val codec: Codec = Codec.UTF8

      val pageData = range.view.map(_.toString).map(n => s"$from/$n.txt").map(getClass.getResourceAsStream)
      val pages = pageData.map(data => new Page {
        override val textLines: IndexedSeq[String] = {
          val scanner = new Scanner(data, "UTF-8")
          readLinesWithDelimiters(scanner).toIndexedSeq
        }
      })

      pages.toList
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

    structuredPages.map(_.header) should contain theSameElementsInOrderAs Seq(
      "",
      "",
      "CHAPTER 1\nINTRODUCTION TO RUNNING HEADERS\nLorem Ipsum style\n",
      "1 INTRODUCTION TO RUNNING HEADERS\nLorem Ipsum style\n",
      "INTRODUCTION TO RUNNING HEADERS 1\nLorem Ipsum style\n",
      "1 INTRODUCTION TO RUNNING HEADERS\nLorem Ipsum style\n",
      "CHAPTER 2\nEVERYTHING IS RELATIVE\n",
      "2 EVERYTHING IS RELATIVE\n",
      "EVERYTHING IS RELATIVE 2\n",
      "2 EVERYTHING IS RELATIVE\n"
    )
  }

  "Running footers" should "be correctly identified" in new SampleVol1 {
    val structuredPages: List[StructuredPage] = PageStructureParser.parsePageStructure(pages)

    structuredPages.map(_.footer) should contain theSameElementsInOrderAs Seq(
      "",
      "",
      "Page 2\n",
      "Page 3\n",
      "Page 4\n",
      "Page 5\n",
      "Page 6\n",
      "Page 7\n",
      "Page 8\n",
      "Page 9\n"
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

    structuredPages.map(_.footer) should contain theSameElementsInOrderAs Seq(
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
