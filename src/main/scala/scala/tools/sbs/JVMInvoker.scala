package scala.tools.sbs

trait JVMInvoker {

  val command = Seq(
	          config.JAVACMD,
	          "-cp",
	          config.SCALALIB,
	          config.JAVAPROP,
	          "scala.tools.nsc.MainGenericRunner",
	          "-classpath",
	          measurer.getClass.getProtectionDomain.getCodeSource.getLocation.getPath +
	            (System.getProperty("path.separator")) +
	            benchmark.bin.path +
	            (System.getProperty("path.separator")) +
	            classOf[org.apache.commons.math.MathException].getProtectionDomain.getCodeSource.getLocation.getPath,
	          measurer.getClass.getName replace ("$", ""))
	
	        for (c <- command) {
	          log.verbose("[Command]  " + c)
	        }
	
	        var subProcessOutput = ArrayBuffer[String]()
	        val processBuilder = Process(command)
	        val processIO = new ProcessIO(
	          _ => (),
	          stdout => scala.io.Source.fromInputStream(stdout).getLines.foreach(subProcessOutput += _),
	          _ => ())
	
	        val process = processBuilder.run(processIO)
	        val success = process.exitValue
	        
}
