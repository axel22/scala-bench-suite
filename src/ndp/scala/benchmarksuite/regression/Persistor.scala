package ndp.scala.benchmarksuite.regression

import scala.collection.mutable.ArrayBuffer

class Persistor {

  /**
   * The list of previous result series loaded from container.
   */
  var results: ArrayBuffer[ArrayBuffer[Long]] = null

}