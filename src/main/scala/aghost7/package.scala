package aghost7.pgpatch


import com.github.mauricio.async.db.RowData

package object `package` {

	implicit class SQLInterpolation (val st: StringContext) extends AnyVal {

		def prep(args: Any*): SqlMapable =
			new MapablePrepImpl(st.parts.mkString("?"), args)


		def sql(args: Any*): SqlMapable = {
			val query = args.zip(st.parts).foldLeft("") { case (accu, (arg, str)) =>
				accu + str + arg
			}
			new MapableImpl(query)
		}
	}

	implicit class SQLStringExtensions(val str: String) extends AnyVal {

		def prep(args: Any*): SqlMapable = new MapablePrepImpl(str, args)

		def sql: SqlMapable = new MapableImpl(str)
	}


	implicit class rowDataExtension(val row: RowData) extends AnyVal {
		// Works meh, might remove it.
		def ~> [B](sqlType: SqlTypeCaster[B])(rowName: String): B =
			sqlType.cast(row(rowName))

		def apply[B](sqlType: SqlTypeCaster[B])(rowName: String): B =
			sqlType.cast(row(rowName))

		@inline
		def cast[A](rowName: String): A = row(rowName).asInstanceOf[A]
	}

}


