package scala.tools.sbs
package test
package measurement

import scala.collection.mutable.ArrayBuffer
import org.scalatest.Spec
import scala.tools.sbs.measurement.Series

class SeriesSpec extends Spec {

  private var s: Series = _

  describe("ArrayBufferSeries") {

    it("should have 1 more element at the end after SO had invoked +=()") {
      init(ArrayBuffer[Long](1, 2))
      val oldlength = s.length
      s += 3
      assert(s.last == 3.asInstanceOf[Long])
      assert(s.length == oldlength + 1)
    }

    it("should have length 0 after SO had invoked clear()") {
      init(ArrayBuffer[Long](1, 2))
      s.clear()
      assert(s.length == 0)
      s.clear()
      assert(s.length == 0)
    }

    it("should rase Exception after cleared and SO called accessing methods)") {
      init(ArrayBuffer[Long](1, 2))
      s.clear()
      intercept[IndexOutOfBoundsException] {
        s.apply(0)
      }
      intercept[NoSuchElementException] {
        s.head
      }
      intercept[NoSuchElementException] {
        s.last
      }
      intercept[UnsupportedOperationException] {
        s.tail
      }
    }

    it("should apply a binary operator to a start value and all elements, going from left with method foldLeft()") {
      init(ArrayBuffer[Long](1, 2, 3))
      val str = s.foldLeft("")((s, l) => s + l)
      assert(str equals "123")
    }

    it("should apply a binary operator to a start value and all elements, going from right with method foldRight()") {
      init(ArrayBuffer[Long](1, 2, 3))
      val str = s.foldRight("")((l, s) => s + l)
      assert(str equals "321")
    }

    it("should apply a function to all elements with method foreach()") {
      init(ArrayBuffer[Long](1, 2, 3))
      var str = ""
      s foreach (str += _)
      assert(str equals "123")
      s.clear
      s foreach (str += _)
      assert(str equals "123")
    }

    it("should give the first element with method head() and the last element with method last()") {
      init(ArrayBuffer[Long](1, 2, 3))
      assert(s.head == 1)
      assert(s.last == 3)
      init(ArrayBuffer[Long](1))
      assert(s.head == s.last)
    }

    it("should give all elements in order, except the first one with method tail()") {
      init(ArrayBuffer[Long](1, 2, 3))
      assert(s.tail(0) == 2)
      assert(s.tail(1) == 3)
    }

    it("should have element at index i removed after method remove(i) invoked") {
      init(ArrayBuffer[Long](1, 2, 3))
      val removed = s remove 0
      assert(removed == 1)
      assert(s.length == 2)
      assert(s(0) == 2)
      assert(s(1) == 3)
    }

    it("should be reliable if all elements statiscally equivalent") {
      init(ArrayBuffer[Long](1054, 1044, 1043, 1045, 1045, 1046, 1066, 1048, 1051, 1050, 1050))
      assert(s.isReliable)
    }

    it("should have confidence level that at that, it is reliable") {
      init(ArrayBuffer[Long](1054, 1044, 1043, 1045, 1045, 1046, 1066, 1048, 1051, 1050, 1050))
      s.isReliable
      assert(s.confidenceLevel == 99)
    }

    it("should not be reliable if all elements are not statiscally equivalent") {
      init(ArrayBuffer[Long](1054, 1144, 1043, 1245, 1045, 1146, 1266, 1048, 1151, 1250, 1050))
      assert(!s.isReliable)
    }

  }

  def init(arr: ArrayBuffer[Long]) {
    s = new Series(testLog, arr, 100)
  }

}
