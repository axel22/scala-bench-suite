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
      g add ("c", "m", "s", 0)
      assert(g.length == 1)
    }

    it("has length increases along with number of invocations") {
      val g = new InvocationGraph
      val max = 5
      var i = 0
      while (i < max) {
        g add ("c", "m", "s", 0)
        i += 1
        assert(g.length == i)
      }
    }

    it("has start is the just added invocation") {
      val g = new InvocationGraph
      g add ("c", "m", "s", 0)
      assert(g.first.id == "cms0")
    }

    it("has start stays unchanged when added more invocations") {
      val g = new InvocationGraph
      g add ("c", "m", "s", 0)
      g add ("c", "m2", "s", 0)
      assert(g.first.id == "cms0")
    }

    it("has last is the only added invocation") {
      val g = new InvocationGraph
      g add ("c", "m", "s", 0)
      assert(g.last.id == "cms0")
    }

    it("has last is the latest invocation") {
      val g = new InvocationGraph
      g add ("c", "m", "s", 0)
      g add ("c", "m2", "s", 0)
      assert(g.last.id == "cm2s0")
      g add ("c", "m", "s", 0)
      assert(g.last.id == "cms0")
    }

    it("raises error when splitting with less than 2 invocations") {
      val g = new InvocationGraph
      intercept[Error](g split)
      g add ("c", "m", "s", 0)
      intercept[Error](g split)
    }

    it("splits into two 1-length graph when having 2 invocations") {
      val g = new InvocationGraph
      g add ("c", "1", "s", 0)
      g add ("c", "2", "s", 0)
      val (f, s) = g.split
      assert(f.length == 1)
      assert(f.first.id == "c1s0")
      assert(s.length == 1)
      assert(s.last.id == "c2s0")
    }

    it("splits into two 1-length graph when having 2 invocations of 1 method") {
      val g = new InvocationGraph
      g add ("c", "1", "s", 0)
      g add ("c", "1", "s", 0)
      val (f, s) = g.split
      assert(f.length == 1)
      assert(f.first.id == "c1s0")
      assert(s.length == 1)
      assert(s.last.id == "c1s0")
    }

    it("splits into two new graphs which have equivalent length in respect of time orders") {
      val g = new InvocationGraph
      for (n <- 1 to 10) g add ("c", n.toString, "s", 0)
      val (f, s) = g.split
      assert(f.length == 5)
      f traverse (i => assert(List("c1s0", "c2s0", "c3s0", "c4s0", "c5s0") contains (i id)))
      assert(s.length == 5)
      s traverse (i => assert(List("c6s0", "c7s0", "c8s0", "c9s0", "c10s0") contains (i id)))
    }

    it("should be OK with ad-hoc test on split - 1") {
      val g = new InvocationGraph
      g add ("c", "1", "s", 0)
      g add ("c", "1", "s", 0)
      g add ("c", "1", "s", 0)
      g add ("c", "2", "s", 0)
      val (f, s) = g.split
      f traverse (i => assert(i.id == "c1s0"))
      assert(s.first.id == "c1s0")
      assert(s.last.id == "c2s0")
    }

    it("should be OK with ad-hoc test on split - 2") {
      val g = new InvocationGraph
      g add ("c", "1", "s", 0)
      g add ("c", "2", "s", 0)
      g add ("c", "3", "s", 0)
      g add ("c", "1", "s", 0)
      g add ("c", "4", "s", 0)
      val (f, s) = g.split
      f traverse (i => assert(List("c1s0", "c2s0", "c3s0") contains (i id)))
      val (s1, s2) = s.split
      assert(s1.last.id == "c1s0")
      assert(s2.first.id == "c4s0")
    }

  }

}
