package aghost7.pgpatch

import com.github.mauricio.async.db.{Connection, RowData, QueryResult}
import scala.concurrent.ExecutionContext

import scala.concurrent.Future

/** All methods in this type will trigger the query. */
trait Collectable[A] {

	/** Returns the driver's underlying class instead of the parsed result */
	def result(implicit con: Connection, exec: ExecutionContext): Future[QueryResult]

	/** Returns a collection of the rows */
	def list(implicit con: Connection, exec: ExecutionContext): Future[List[A]]

	/** Returns the first row only. Will throw an exception if there isn't at
		* least one row return, so you really should just use this for sql queries
		* such a count, etc.
		*/
	def scalar(implicit con: Connection, exec: ExecutionContext): Future[A]

	/** Meh, just there for completeness */
	def seq(implicit con: Connection, exec: ExecutionContext): Future[Seq[A]]

	/** The rows expected are either none, or just one */
	def opt(implicit con: Connection, exec: ExecutionContext): Future[Option[A]]
}

private [pgpatch] abstract class CollectableLike[A](mapper: RowData => A) extends Collectable[A] {

	def list(implicit con: Connection, exec: ExecutionContext): Future[List[A]] = result.map {
		_.rows.get.map(mapper).toList
	}

	def scalar(implicit con: Connection, exec: ExecutionContext): Future[A] =
		result.map { qr => mapper(qr.rows.get.head) }

	def seq(implicit con: Connection, exec: ExecutionContext): Future[Seq[A]] =
		result.map { _.rows.get.map(mapper) }

	def opt(implicit con: Connection, exec: ExecutionContext): Future[Option[A]] =
		result.map { _.rows.get.headOption.map(mapper) }
}

private [pgpatch] abstract class CollectableSimpleLike[A](mapper: Any => A) extends Collectable[A] {

	def list(implicit con: Connection, exec: ExecutionContext): Future[List[A]] = result.map { qr =>
		val rs = qr.rows.get
		val colName = rs.columnNames.head
		rs.map { rowData => mapper(rowData.apply(colName)) }.toList
	}

	def scalar(implicit con: Connection, exec: ExecutionContext): Future[A] = result.map { qr =>
		val rs = qr.rows.get
		val colName = rs.columnNames.head
		mapper(rs.head.apply(colName))
	}

	def seq(implicit con: Connection, exec: ExecutionContext): Future[Seq[A]] = result.map { qr =>
		val rs = qr.rows.get
		val colName = rs.columnNames.head
		rs.map { rowData => mapper(rowData(colName)) }
	}

	def opt(implicit con: Connection, exec: ExecutionContext): Future[Option[A]] = result.map { qr =>
		val rs = qr.rows.get
		rs.headOption.map { rowData =>
			val colName = rs.columnNames.head
			mapper(rowData(colName))
		}
	}
}

private [pgpatch] trait CollectablePrep {
	val query: String
	val args: Seq[Any]

	def result(implicit con: Connection, exec: ExecutionContext): Future[QueryResult] =
		con.sendPreparedStatement(query, args)
}

private [pgpatch] trait CollectableQuery {
	val sql: String

	def result(implicit con: Connection, exec: ExecutionContext): Future[QueryResult] =
		con.sendQuery(sql)
}

private [pgpatch] class CollectablePrepSimple[A](
		val query: String, val args: Seq[Any], val mapper: Any => A
		) extends CollectableSimpleLike[A](mapper) with CollectablePrep

private [pgpatch] class CollectablePrepImpl[A](
		val query: String, val args:Seq[Any], val mapper: RowData => A
		) extends CollectableLike[A](mapper) with CollectablePrep

private [pgpatch] class CollectableImpl[A](
		val sql: String, val mapper: RowData => A
		) extends CollectableLike[A](mapper) with CollectableQuery

private [pgpatch] class CollectableSimpleImpl[A](
		val sql: String, val mapper: Any => A
		) extends CollectableSimpleLike[A](mapper) with CollectableQuery