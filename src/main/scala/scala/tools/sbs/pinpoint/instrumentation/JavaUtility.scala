/*
 * PinpointHarnessInJava
 * 
 * Version
 * 
 * Created on November 29th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package pinpoint
package instrumentation

import scala.tools.sbs.pinpoint.strategy.PinpointHarness

/** java instructions to modify {@link PinpointHarness} state during runtime.
 */
object JavaUtility {

  def javaInstruction(instruction: String) = instruction + ";"

  val javaExpressionCurrentTime = "System.currentTimeMillis()"

  def javaSysout(arg: String) = javaInstruction("System.out.println(" + arg + ")")

  def doubleQuote(string: String) = "\"" + string + "\""

  def fromClass(clazz: Class[_], methodCall: String) =
    clazz.getName.replace("$class", "").replace("$", "") + "." + methodCall

  def methodCall(name: String, args: List[String]) = name + "(" + (args mkString ", ") + ")"

  def fromPinpointHarness(methodCall: String) = fromClass(PinpointHarness.getClass, methodCall)

  val callPinpointHarnessStart =
    javaInstruction(fromPinpointHarness(methodCall("start", List(javaExpressionCurrentTime))))

  val callPinpointHarnessEnd =
    javaInstruction(fromPinpointHarness(methodCall("end", List(javaExpressionCurrentTime))))

  def callPinpointHarnessSetStartOrdinal(ordino: Int) =
    javaInstruction(fromPinpointHarness(methodCall("setStartOrdinal", List(ordino.toString))))

  def callPinpointHarnessSetEndOrdinal(ordino: Int) =
    javaInstruction(fromPinpointHarness(methodCall("setEndOrdinal", List(ordino.toString))))

}
