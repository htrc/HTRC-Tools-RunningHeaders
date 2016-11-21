package org.hathitrust.htrc.textprocessing.runningheaders.utils

object Helper {

  /**
    * For purposes of detecting running headers by comparing the similarity of candidate header
    * lines from each page it is sufficient to consider candidate headers from only a few pages
    * around the page in question since running headers are, by definition, supposed to occur
    * sequentially (give or take a page). This method generates a pairing of elements that are not
    * farther than `n` apart. These pairs of elements will then be used to assess candidate
    * header similarity.
    *
    * @param n        Max distance to consider
    * @param elements The elements
    * @return A list of pairs of elements that are no farther than `n` apart
    */
  def pairwiseCombineElementsWithinDistanceOf[T](n: Int)(elements: Seq[T]): List[(T, T)] = {
    var x = elements.head
    var xs = elements.tail
    var result = List.empty[(T, T)]

    while (xs.nonEmpty) {
      result ++= xs.take(n - 1).map(x -> _)
      x = xs.head
      xs = xs.tail
    }

    result
  }

}
