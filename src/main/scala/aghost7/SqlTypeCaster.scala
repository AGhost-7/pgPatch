package aghost7.pgpatch

import org.joda.time._

/** No need to remember what datatype the postgresql driver maps the db types to.
	* Just specify the DB type and then the method will have all the info it needs :)
	*/
sealed trait SqlTypeCaster[A] {
	def cast(a: Any): A = a.asInstanceOf[A]
}

// reference: https://github.com/mauricio/postgresql-async/tree/master/postgresql-async

object Bool extends SqlTypeCaster[Boolean]

object SmallInt extends SqlTypeCaster[Short]

object Integer extends SqlTypeCaster[Int]
object Serial extends SqlTypeCaster[Int]

object BigInt extends SqlTypeCaster[Long]
object BigSerial extends SqlTypeCaster[Long]

object Numeric extends SqlTypeCaster[BigDecimal]
object Real extends SqlTypeCaster[Float]
/** The Double Postgres datatype */
object Dbl extends SqlTypeCaster[Double]

object Text extends SqlTypeCaster[String]
object VarChar extends SqlTypeCaster[String]
object BPChar extends SqlTypeCaster[String]


object TimeStamp extends SqlTypeCaster[LocalDateTime]
object TimeStampWTimeZone extends SqlTypeCaster[DateTime]
object Date extends SqlTypeCaster[LocalDate]
object Time extends SqlTypeCaster[LocalTime]

object ByteA extends SqlTypeCaster[Array[Byte]]

object Other extends SqlTypeCaster[String]



