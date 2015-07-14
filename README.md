# pgPatch

A simple wrapper around the [postgres-async](https://github.com/mauricio/postgresql-async) driver. 

Goal is to avoid wrapping driver objects into more objects while making significant gains in reducing boilerplate code.

Another part of the motivation for this is to keep the access layer as close to how the postgres database works as possible, instead of abstracting it away.

# Examples

You start by specifying the data type (or mapping) for the row, then you can specify the result size of your query.
```scala
val id = 10
val ftName: Future[Option[String]] = prep"SELECT name FROM users WHERE id = $id"
		.map { row => row("name").asInstanceOf[String] }
		.opt
```

There's a few shortcuts to get what you want quick.
```scala
val ftCount: Future[Int] = "SELECT count(*) FROM users".sql.int.scalar
```

You only need to specify the postgres type and the library will take care of the type it needs to cast the driver's output to.
```scala
val ftTime: Future[LocalDateTime] = 
	sql"SELECT stamp FROM users WHERE id = 1"
		.map { row => row(TimeStamp)("date") }
		.scalar
```

You can use implicit functions to get reuse.
```scala

implicit val userMapper: RowData => User = { row =>
	User(row(Text)("name"), row(Integer)("age"))
}

// Then somewhere else in your code...
val name = "foobar"
val ftUser: Future[Option[User]] = prep"SELECT * FROM users WHERE name = $name"
	.as[User]
	.opt
```

# N.B.
The `sql` interpolator does not escape the input, so make sure to sanitize your data when using it.