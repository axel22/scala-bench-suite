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

    val invocationCollector = new InvocationCollector(
      config,
      log,
      benchmark,
      declaringClass,
      diggingMethod,
      instrumentedOut,
      backupPlace)

    val currentMethod = instrumentor.getMethod(
      diggingMethod,
      declaringClass,
      config.classpathURLs ++ benchmark.classpathURLs)

    log.info("Finding bottleneck in: " + currentMethod.getLongName)
    log.info("")

    if (invocationCollector.graph.length == 0) {
      log.info("  No detectable method call found")
      log.info("")
      throw new BottleneckUndetectableException(declaringClass, diggingMethod, invocationCollector.graph)
    }

    log.debug("Not empty calling list from: " + currentMethod.getLongName)

    if (!invocationCollector.isMatchOK) {
      log.error("Mismatch expression lists, skip further detection")
      throw new MismatchExpressionList(declaringClass, diggingMethod, invocationCollector)
    }

    log.debug("Binary finding")
    val currentLevelBottleneck = BottleneckFinderFactory(
      config,
      log,
      benchmark,
      declaringClass,
      diggingMethod,
      invocationCollector.graph,
      instrumentedOut,
      backupPlace) find ()

    currentLevelBottleneck.toReport foreach (line => {
      log.info(line)
    })
    log.info("")

    currentLevelBottleneck match {
      case Bottleneck(_, position, _, _, _) if ((position.length == 1) &&
        (shouldProceed(position.first.prototype, dug)) &&
        !(benchmark.pinpointExclude exists (position.first.declaringClass matches _))) =>
        try {
          log.verbose("  Digging into: " + position.first.prototype)

          val lowerLevelBottleneckFound =
            find(
              position.first.declaringClass,
              position.first.methodName,
              dug :+ position.first.prototype)

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

  def shouldProceed(prototype: String, dug: List[String]) =
    (benchmark.pinpointDepth == -1) || (dug.length < benchmark.pinpointDepth && !(dug contains prototype))

}
