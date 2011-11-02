package scala.tools.sbs.test
package common

import scala.tools.nsc.io.Path
import scala.tools.nsc.util.ClassPath
import scala.tools.nsc.Global
import scala.tools.nsc.Settings
import scala.tools.sbs.common.Reflector
import scala.tools.sbs.util.FileUtil

import org.scalatest.Spec

class ReflectorSpec extends Spec {

  val A = "A"

  val classA = "class " + A

  val traitA = "trait " + A

  val objectA = "object " + A

  val extendsTemplate = "extends " + classOf[DummyTemplate].getName

  val reflectDir = testDir / "ReflectorSpec" createDirectory ()

  val reflectFile = reflectDir / "A.scala" toFile

  val reflectSub = reflectDir / "sub" toDirectory

  def clean = FileUtil clean reflectDir

  def mkDir(path: Path) = FileUtil mkDir path

  def define(content: String) {
    FileUtil createFile reflectFile.path
    FileUtil.write(reflectFile.path, content)
  }

  def compile(out: Path): Boolean = {
    val settings = new Settings(Console.println)
    val (ok, _) = settings.processArguments(
      List("-cp", ClassPath.fromURLs(
        classOf[DummyTemplate].getProtectionDomain.getCodeSource.getLocation :: testConfig.classpathURLs: _*)),
      false)

    if (ok) {
      mkDir(out)
      settings.outdir.value = out.path
      val compiler = new Global(settings)
      new compiler.Run compile List(reflectFile.path)
      !compiler.reporter.hasErrors
    }
    else {
      false
    }
  }

  def init(content: String, out: Path): Boolean = {
    clean
    define(content)
    compile(out)
  }

  val reflector = Reflector(testConfig)

  describe("A Reflector") {

    it("should load the right class from given classpath") {
      val out = reflectDir
      init(classA, out)
      val clazz = reflector.getClass(A, List(out.toURL))
      assert(clazz.getCanonicalName == A)
    }

    it("should load the right trait from given classpath") {
      val out = reflectDir
      init(traitA, out)
      val clazz = reflector.getClass(A, List(out.toURL))
      assert(clazz.getCanonicalName == A)
      assert(clazz.isInterface)
    }

    it("should load the right object from given classpath") {
      val out = reflectDir
      init(objectA, out)
      val clazz = reflector.getClass(A, List(out.toURL))
      assert(clazz.getCanonicalName == A)
    }

    it("should load the right class from given classpath: parent rather then child dir") {
      val parent = reflectDir
      init(classA, parent)
      val child = reflectSub
      reflectFile.deleteIfExists
      define(traitA)
      compile(child)

      val clazz = reflector.getClass(A, List(parent.toURL))
      assert(clazz.getCanonicalName == A)
      assert(!clazz.isInterface)
    }

    it("should load the right class from given classpath: child rather then parent dir") {
      val parent = reflectDir
      init(classA, parent)
      val child = reflectSub
      reflectFile.deleteIfExists
      define(traitA)
      compile(child)

      val clazz = reflector.getClass(A, List(child.toURL))
      assert(clazz.getCanonicalName == A)
      assert(clazz.isInterface)
    }

    it("should load the right class from given classpath: from classpath comes first") {
      val parent = reflectDir
      init(classA, parent)
      val child = reflectSub
      reflectFile.deleteIfExists
      define(traitA)
      compile(child)

      val clazz = reflector.getClass(A, List(child.toURL, parent.toURL))
      assert(clazz.getCanonicalName == A)
      assert(clazz.isInterface)
    }

    it("should rase ClassNotFoundException when the class cannot be found in the given classpath") {
      clean
      intercept[ClassNotFoundException] {
        reflector.getClass(A, Nil)
      }
    }

    it("should create the right object from given classpath: get from class") {
      val out = reflectDir
      init(classA + " " + extendsTemplate, out)
      reflector.getObject[DummyTemplate](A, List(out.toURL))
    }

    it("should create the right object from given classpath: load object") {
      val out = reflectDir
      init(objectA + " " + extendsTemplate, out)
      reflector.getObject[DummyTemplate](A, List(out.toURL))
    }

    it("should rase ClassNotFoundException when the class with given name is a trait") {
      val out = reflectDir
      init(traitA + " " + extendsTemplate, out)
      intercept[ClassNotFoundException] {
        reflector.getObject[DummyTemplate](A, List(out.toURL))
      }
    }

    it("should rase ClassNotFoundException when the class with given name is a abstract class") {
      val out = reflectDir
      init("abstract " + classA + " " + extendsTemplate, out)
      intercept[ClassNotFoundException] {
        reflector.getObject[DummyTemplate](A, List(out.toURL))
      }
    }

    it("should rase ClassCastException incase the class defined with given name does not implement the given trait") {
      val out = reflectDir
      init(classA, out)
      intercept[ClassCastException] {
        reflector.getObject[DummyTemplate](A, List(out.toURL))
      }
    }

    it("should return the location where a class loaded") {
      val out = reflectDir
      init(classA, out)
      val clazz = reflector.getClass(A, List(out.toURL))
      expect(Some(out.toURL))(reflector.locationOf(A, clazz.getClassLoader))
    }
    
    it("should return the location where a class loaded: parent rather than child dir") {
      val parent = reflectDir
      init(classA, parent)
      val child = reflectSub
      reflectFile.deleteIfExists
      define(traitA)
      compile(child)

      val clazz = reflector.getClass(A, List(parent.toURL, child.toURL))
      expect(Some(parent.toURL))(reflector.locationOf(A, clazz.getClassLoader))
    }
    
    it("should return the location where a class loaded: child rather than parent dir") {
      val parent = reflectDir
      init(classA, parent)
      val child = reflectSub
      reflectFile.deleteIfExists
      define(traitA)
      compile(child)

      val clazz = reflector.getClass(A, List(child.toURL, parent.toURL))
      expect(Some(child.toURL))(reflector.locationOf(A, clazz.getClassLoader))
    }

  }

}

trait DummyTemplate
