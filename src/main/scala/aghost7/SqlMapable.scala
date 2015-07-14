package aghost7.pgpatch

import com.github.mauricio.async.db.{QueryResult, RowData, Connection}

import scala.concurrent.{ExecutionContext, Future}


trait SqlMapable {

	def as[A](implicit mapper : RowData => A): Collectable[A]

	def map[A](mapper : RowData => A): Collectable[A]

	/** Takes the first column and asks the user to map those values from Any */
	def mapSimple[A](mapper: Any => A) : Collectable[A]

	/** mapSimple preset for ints */
	def int: Collectable[Int]

	def long: Collectable[Long]

	def string: Collectable[String]

	/** Returns the rows affected. Since this is scalar, it doesn't need to
		* map to Collectable.
		*/
	def rows(implicit con: Connection, exec: ExecutionContext): Future[Long]

	/** The driver doesn't seem to provide a simple way of retrieving the ids
		* of the affected rows. This means that you'll need to use the RETURNING
		* statement in your insert for this to actually work properly.
		*
		* This will return the first value in the first column of the query. It does
		* not infer any specific name.
		*/
	def key(implicit con: Connection, exec: ExecutionContext): Future[Long]

	/** If the update has returned multiple keys */
	def keys(implicit con: Connection, exec: ExecutionContext): Future[Seq[Long]]
}

private [pgpatch] abstract class MapableLike extends SqlMapable {
	def int: Collectable[Int] = mapSimple { _.asInstanceOf[Int] }

	def long: Collectable[Long] = mapSimple { _.asInstanceOf[Long] }

	def string: Collectable[String] = mapSimple { _.asInstanceOf[String] }
}

private [pgpatch] class MapablePrepImpl(query: String, args: Seq[Any]) extends MapableLike {

	def as[A](implicit mapper : RowData => A): Collectable[A] =
		new CollectablePrepImpl(query, args, mapper)

	def mapUnderlying[A](mapper: RowData => A) : Collectable[A] =
		new CollectablePrepImpl[A](query, args, mapper)

	def map[A](mapper : RowData => A): Collectable[A] =
		new CollectablePrepImpl(query, args, mapper)

	def mapSimple[A](mapper: Any => A) : Collectable[A] =
		new CollectablePrepSimple[A](query, args, mapper)


	def rows(implicit con: Connection, exec: ExecutionContext): Future[Long] =
		con.sendPreparedStatement(query, args).map { _.rowsAffected }

	def key(implicit con: Connection, exec: ExecutionContext): Future[Long] =
		con.sendPreparedStatement(query, args).map { qr =>
			val rs = qr.rows.get
			rs.head(rs.columnNames.head).asInstanceOf[Long]
		}

	def keys(implicit con: Connection, exec: ExecutionContext): Future[Seq[Long]] =
		con.sendPreparedStatement(query, args).map { qr =>
			val rs = qr.rows.get
			val colName = rs.columnNames.head
			rs.map { _.apply(colName).asInstanceOf[Long] }
		}
}

private [pgpatch] class MapableImpl(query: String) extends MapableLike {
	def as[A](implicit mapper : RowData => A): Collectable[A] =
		new CollectableImpl(query, mapper)

	def mapUnderlying[A](mapper: RowData => A) : Collectable[A] =
		new CollectableImpl[A](query, mapper)

	def map[A](mapper : RowData => A): Collectable[A] =
		new CollectableImpl(query, mapper)

	def mapSimple[A](mapper: Any => A) : Collectable[A] =
		new CollectableSimpleImpl[A](query, mapper)


	def rows(implicit con: Connection, exec: ExecutionContext): Future[Long] =
		con.sendQuery(query).map { _.rowsAffected }

	def key(implicit con: Connection, exec: ExecutionContext): Future[Long] =
		con.sendQuery(query).map { qr =>
			val rs = qr.rows.get
			rs.head(rs.columnNames.head).asInstanceOf[Long]
		}

	def keys(implicit con: Connection, exec: ExecutionContext): Future[Seq[Long]] =
		con.sendQuery(query).map { qr =>
			val rs = qr.rows.get
			val colName = rs.columnNames.head
			rs.map { _.apply(colName).asInstanceOf[Long] }
		}
}
