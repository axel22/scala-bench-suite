/*
 * Utility
 * 
 * Version 
 * 
 * Created on August 15th 2011
 *
 * Created by ND P
 */

import scala.io.Source.fromFile

/**
 * Utilities for benchmarking.
 */
object Utility {

	/**
	 * The relative path of the resource folder of the benchmark suite.
	 */
	val RESOURCE_PATH = "src/resource/"
	/**
	 * The string spliter for cells in one row of a file storing a distribution table.
	 */
	val CELL_SPLITER = "\t"

	/**
	 * The relative path of the file storing the Student distribution table.
	 */
	val STUDENT_DISTRIBUTION_TABLE = "src/resource/distributiontable/student"
	/**
	 * The column index of the confidence level 90% in the file storing the Student distribution table.
	 */
	val STUDENT_DISTRIBUTION_TABLE_090 = 1
	/**
	 * The column index of the confidence level 95% in the file storing the Student distribution table.
	 */
	val STUDENT_DISTRIBUTION_TABLE_095 = 2
	/**
	 * The column index of the confidence level 99% in the file storing the Student distribution table.
	 */
	val STUDENT_DISTRIBUTION_TABLE_099 = 3
	/**
	 * The maximum row of the Student distribution table.
	 */
	val STUDENT_DISTRIBUTION_TABLE_ROW_MAX = 1001

	/**
	 * The relative path of the file storing the Fisher F distribution table with confidence level 90%.
	 */
	val F_DISTRIBUTION_TABLE_090 = "src/resource/distributiontable/f090"
	/**
	 * The relative path of the file storing the Fisher F distribution table with confidence level 95%.
	 */
	val F_DISTRIBUTION_TABLE_095 = "src/resource/distributiontable/f095"
	/**
	 * The relative path of the file storing the Fisher F distribution table with confidence level 99%.
	 */
	val F_DISTRIBUTION_TABLE_099 = "src/resource/distributiontable/f099"
	/**
	 * The maximum column of the Fisher F distribution table.
	 */
	val F_DISTRIBUTION_TABLE_COLUMN_MAX = 11
	/**
	 * The maximum row of the Fisher F distribution table.
	 */
	val F_DISTRIBUTION_TABLE_ROW_MAX = 11001
	
	/**
	 * Reads the value in a pre-computed distribution table stored in file with the given row and column.
	 * 
	 * @param tableFile	The path to the file stores the desired distribution table.
	 * @param column	The column index of the desired value.
	 * @param row	The row index of the desired value.
	 * @return	the read value.
	 */
	def readCell(tableFile: String, column: Int, row: Int): Double = {
		try {
			val table = fromFile(tableFile).getLines()
			for (i <- 1 to row - 1) {
				table.next
			}
			table.next.split(CELL_SPLITER)(column - 1).toDouble
		} catch {
			// TODO
			case e => throw e
		}
	}
}