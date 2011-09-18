/*
 * StatisticFactory
 * 
 * Version
 * 
 * Created on September 18th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package regression

import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log

class StatisticFactory(log: Log, config: Config) {

  def create(alpha: Double = 0): Statistic = {
    new SimpleStatistic(log, config, alpha)
  }

}