package scala.tools.sbs
import scala.tools.sbs.benchmark.BenchmarkMode
import scala.tools.sbs.util.Config
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.util.Log
import scala.tools.sbs.benchmark.BenchmarkMode.BenchmarkMode
import scala.tools.nsc.io.Directory

class LoadStoreManagerFactory(log: Log, config: Config, benchmark: Benchmark, mode: BenchmarkMode) {
  
  def create(location: Directory): LoadStoreManager = {
    new SimpleLoadStoreManager(log, config, benchmark, location, mode)
  }

}