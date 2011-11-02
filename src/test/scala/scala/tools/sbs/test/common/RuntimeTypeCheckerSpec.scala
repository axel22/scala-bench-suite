package scala.tools.sbs.test
package common

import scala.tools.sbs.common.RuntimeTypeChecker

import org.scalatest.Spec

class RuntimeTypeCheckerSpec extends Spec {

  trait Mark

  abstract class Init extends Mark

  abstract class Snippet extends Mark

  trait Perf extends Mark

  class PerfInit extends Init with Perf

  class PerfSnippet extends Snippet with Perf

  trait Prof extends Mark

  class ProfInit extends Init with Prof

  class ProfSnippet extends Snippet with Prof

  trait Pinp extends Perf

  class PinpInit extends PerfInit with Pinp

  class PinpSnippet extends PerfSnippet with Pinp

  val perfInitName = "PerfInit"
  val perfSnippetName = "PerfSnippet"
  val profInitName = "ProfInit"
  val profSnippetName = "ProfSnippet"
  val pinpInitName = "PinpInit"
  val pinpSnippetName = "PinpSnippet"

  def factory(kind: String): Mark = kind match {
    case _ if (kind == pinpInitName)    => new PinpInit
    case _ if (kind == pinpSnippetName) => new PinpSnippet
    case _ if (kind == perfInitName)    => new PerfInit
    case _ if (kind == perfSnippetName) => new PerfSnippet
    case _ if (kind == profInitName)    => new ProfInit
    case _ if (kind == profSnippetName) => new ProfSnippet
    case _                              => throw new Exception("Mark not supported: " + kind)
  }

  trait Runner extends RuntimeTypeChecker {

    def run(mark: Mark) = check(mark.getClass)

  }

  object PerfRunner extends Runner {

    val upperBound = manifest[Perf]

  }

  object ProfRunner extends Runner {

    val upperBound = manifest[Prof]

  }

  object PinpRunner extends Runner {

    val upperBound = manifest[Pinp]

  }

  describe("A RuntimeTypeChecker") {

    it("should yield true with a runner runs a suitable Init") {
      assert(PerfRunner run factory(perfInitName))
    }

    it("should yield true with a runner runs a suitable Snippet") {
      assert(PerfRunner run factory(perfSnippetName))
    }

    it("should yield true with a runner runs a suitable sub-Init") {
      assert(PerfRunner run factory(pinpInitName))
    }

    it("should yield true with a runner runs a suitable sub-Snippet") {
      assert(PerfRunner run factory(pinpSnippetName))
    }

    it("should yield false with a runner runs a unsuitable Init") {
      assert(!(PerfRunner run factory(profInitName)))
    }

    it("should yield false with a runner runs a unsuitable Snippet") {
      assert(!(PerfRunner run factory(profSnippetName)))
    }

    it("should yield false with a runner runs a super-Init") {
      assert(!(PinpRunner run factory(perfInitName)))
    }

    it("should yield false with a runner runs a super-Snippet") {
      assert(!(PinpRunner run factory(perfSnippetName)))
    }

  }

}
