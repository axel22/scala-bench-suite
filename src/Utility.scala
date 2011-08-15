import scala.io.Source.fromFile

object Utility {

	val ResourcePath = "src/resource/"
	val CellSpliter = "\t"

	val StudentDistTable = "src/resource/distributiontable/student"
	val StudentDistTblClmn090 = 1
	val StudentDistTblClmn095 = 2
	val StudentDistTblClmn099 = 3
	val StudentDistTblMAXROW = 1001

	val FDistTable090 = "src/resource/distributiontable/f090"
	val FDistTable095 = "src/resource/distributiontable/f095"
	val FDistTable099 = "src/resource/distributiontable/f099"
	val FDistTblMAXCOLUMN = 11
	val FDistTblMAXROW = 10001

	def readCell(tableName: String, column: Int, row: Int): Double = {
		try {
			val table = fromFile(tableName).getLines()
			for (i <- 1 to row - 1) {
				table.next
			}
			table.next.split(CellSpliter)(column - 1).toDouble
		} catch {
			// TODO
			case e => throw e
		}
	}
}