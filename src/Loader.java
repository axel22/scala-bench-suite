/*
 * Loader
 * 
 * Version
 * 
 * Created on May 25th 2011
 * 
 * Created by ND P
 */

import java.util.ArrayList;
import java.util.List;


/**
 * Loads the BenchmarkDriver scala class. Needed to generate runnable jar file.
 * 
 * @author ND P
 */
public class Loader {

	public static void main(String[] args) {
		List<String> argList = new ArrayList<String>();
		argList.add("BenchmarkDriver");
		for (String s : args) {
			argList.add(s);
		}
		System.out.println("Running from " + System.getProperty("user.dir"));
		scala.tools.nsc.MainGenericRunner.main(argList.toArray(new String[0]));
	}
}
