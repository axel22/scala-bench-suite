/*
 * BottleneckDiggingFinder
 * 
 * Version
 * 
 * Created on November 6th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package pinpoint
package bottleneck

import scala.tools.nsc.io.Directory
import scala.tools.sbs.io.Log
import scala.tools.sbs.pinpoint.instrumentation.CodeInstrumentor.MethodCallExpression
import scala.tools.sbs.pinpoint.instrumentation.CodeInstrumentor
import scala.tools.sbs.pinpoint.strategy.InstrumentationRunner
import scala.tools.sbs.pinpoint.strategy.PreviousVersionExploiter

class BottleneckDiggingFinder(val config: Config,
                              val log: Log,
                              benchmark: PinpointBenchmark,
                              entryClass: String,
                              entryMethod: String,
                              val instrumentedOut: Directory,
                              val backupPlace: Directory)
  extends BottleneckFinder
  with Configured
  with InstrumentationRunner
  with PreviousVersionExploiter {

  def find(): BottleneckFound = find(entryClass, entryMethod, List(entryClass + entryMethod))

  // TODO: issue with jar files:
  // - extracts
  // - backup

  def find(declaringClass: String, diggingMethod: String, dug: List[String]): BottleneckFound = {

    val instrumentor = CodeInstrumentor(config, log, benchmark.pinpointExclude)

    val currentMethod = instrumentor.getMethod(
      diggingMethod,
      declaringClass,
      config.classpathURLs ++ benchmark.classpathURLs)

    log.info("Finding bottleneck in: " + currentMethod.getLongName)
    log.info("")

    val previousMethod = exploit(
      benchmark.pinpointPrevious,
      benchmark.context,
      instrumentor.getMethod(
        diggingMethod,
        declaringClass,
        benchmark.pinpointPrevious.toURL :: config.classpathURLs ++ benchmark.classpathURLs))

    val currentCallingList = instrumentor callListOf currentMethod

    if (currentCallingList == Nil) {
      log.info("  No detectable method call found")
      log.info("")
      throw new BottleneckUndetectableException(benchmark, Nil)
    }

    log.debug("Not empty calling list from: " + currentMethod.getLongName)

    val previousCallingList = instrumentor callListOf previousMethod
    if ((currentCallingList map (call => (call.getClassName, call.getMethodName, call.getSignature))) !=
      (previousCallingList map (call => (call.getClassName, call.getMethodName, call.getSignature)))) {
      log.error("Mismatch expression lists, skip further detection")
      throw new MismatchExpressionList(benchmark, currentCallingList, previousCallingList)
    }

    var callIndexList = List[Int]()
    currentCallingList foreach (_ => callIndexList :+= callIndexList.length)

    log.debug("Binary finding")
    val currentLevelBottleneck = BottleneckFinderFactory(
      config,
      log,
      benchmark,
      declaringClass,
      diggingMethod,
      callIndexList,
      currentCallingList,
      instrumentedOut,
      backupPlace) find ()

    currentLevelBottleneck.toReport foreach (line => {
      log.info(line)
    })
    log.info("")

    currentLevelBottleneck match {
      case Bottleneck(_, position, _, _, _) if ((position.length == 1) &&
        (shouldProceed(position.head, dug)) &&
        !(benchmark.pinpointExclude exists (declaringClass matches _))) =>
        try {
          log.verbose("  Digging into: " +
            position.head.getClassName() + "." + position.head.getMethodName + position.head.getSignature)

          val lowerLevelBottleneckFound =
            find(
              position.head.getClassName,
              position.head.getMethodName,
              dug :+ (position.head.getClassName + position.head.getMethodName))

          lowerLevelBottleneckFound match {
            case _: NoBottleneck => currentLevelBottleneck
            case _               => lowerLevelBottleneckFound
          }
        }
        catch {
          case e => {
            log.debug("Digging failed: " + e)
            currentLevelBottleneck
          }
        }

      case _ => currentLevelBottleneck
    }

  }

  def shouldProceed(call: MethodCallExpression, dug: List[String]) =
    (benchmark.pinpointDepth == -1) ||
      (dug.length < benchmark.pinpointDepth && !(dug contains (call.getClassName + call.getMethodName)))

}
