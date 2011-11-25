package scala.tools.sbs.test
package pinpoint
package bottleneck

import scala.tools.sbs.pinpoint.bottleneck.InvocationGraph

import org.scalatest.Spec

class InvocationGraphSpec extends Spec {

  describe("An invocation graph") {

    it("has length == 0 while holding no invocation") {
      val g = new InvocationGraph
      assert(g.length == 0)
    }

    it("has start == null while holding no invocation") {
      val g = new InvocationGraph
      assert(g.first == null)
    }

    it("has last == null while holding no invocation") {
      val g = new InvocationGraph
      assert(g.last == null)
    }

    it("has length == 1 while added 1 invocation") {
      val g = new InvocationGraph
      g add "one"
      assert(g.length == 1)
    }

    it("has length increases along with number of invocations") {
      val g = new InvocationGraph
      val max = 5
      var i = 0
      while (i < max) {
        g add ""
        i += 1
        assert(g.length == i)
      }
    }

    it("has start is the just added invocation") {
      val g = new InvocationGraph
      g add "one"
      assert(g.first.prototype == "one")
    }

    it("has start stays unchanged when added more invocations") {
      val g = new InvocationGraph
      g add "one"
      g add "two"
      assert(g.first.prototype == "one")
    }

    it("has last is the only added invocation") {
      val g = new InvocationGraph
      g add "one"
      assert(g.last.prototype == "one")
    }

    it("has last is the latest invocation") {
      val g = new InvocationGraph
      g add "one"
      g add "two"
      assert(g.last.prototype == "two")
      g add "one"
      assert(g.last.prototype == "one")
    }

    it("raises error when splitting with less than 2 invocations") {
      val g = new InvocationGraph
      intercept[Error](g split)
      g add ""
      intercept[Error](g split)
    }

    it("splits into two 1-length graph when having 2 invocations") {
      val g = new InvocationGraph
      g add "one"
      g add "two"
      val (f, s) = g.split
      assert(f.length == 1)
      assert(f.first.prototype == "one")
      assert(s.length == 1)
      assert(s.last.prototype == "two")
    }

    it("splits into two 1-length graph when having 2 invocations of 1 method") {
      val g = new InvocationGraph
      g add "one"
      g add "one"
      val (f, s) = g.split
      assert(f.length == 1)
      assert(f.first.prototype == "one")
      assert(s.length == 1)
      assert(s.last.prototype == "one")
    }

    it("splits into two new graphs which have equivalent length in respect of time orders") {
      val g = new InvocationGraph
      for (n <- 1 to 10) g add n.toString
      val (f, s) = g.split
      assert(f.length == 5)
      f traverse (i => assert(List("1", "2", "3", "4", "5") contains (i prototype)))
      assert(s.length == 5)
      s traverse (i => assert(List("6", "7", "8", "9", "10") contains (i prototype)))
    }

    it("should be OK with ad-hoc test on split - 1") {
      val g = new InvocationGraph
      g add "A"
      g add "A"
      g add "B"
      g add "A"
      val (f, s) = g.split
      f traverse (i => assert(i.prototype == "A"))
      assert(s.first.prototype == "B")
      assert(s.last.prototype == "A")
    }

    it("should be OK with ad-hoc test on split - 2") {
      val g = new InvocationGraph
      g add "A"
      g add "B"
      g add "C"
      g add "A"
      g add "D"
      val (f, s) = g.split
      f traverse (i => assert(List("A", "B", "C") contains (i prototype)))
      val (s1, s2) = s.split
      assert(s1.last.prototype == "A")
      assert(s2.first.prototype == "D")
    }

  }

}
