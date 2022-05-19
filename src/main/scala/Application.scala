import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.execution.datasources.v2.*
import org.apache.spark.sql.execution.datasources.v2.jdbc.*

import java.sql.{Connection, DriverManager}

case class DB(name: String, url: String, driver: String) {
  def buildSparkConf(): SparkConf = new SparkConf()
    .set(s"spark.sql.catalog.$name", classOf[JDBCTableCatalog].getName)
    .set(s"spark.sql.catalog.$name.url", url)
    .set(s"spark.sql.catalog.$name.driver", driver)
    .set(s"spark.sql.catalog.$name.pushDownAggregate", "true")
    .set(s"spark.sql.catalog.$name.pushDownLimit", "true")
}

@main
def main(): Unit = {
  System.setProperty("spark.testing", "true")

  val h2PostgresWithInit = (name: String, file: String) =>
    s"jdbc:h2:mem:$name;DB_CLOSE_DELAY=-1;" +
      s"MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;" +
      s"INIT=RUNSCRIPT FROM 'classpath:sql/$file'"

  val db1 = DB(
    name = "db1",
    url = h2PostgresWithInit("db1", "create-person-table.sql"),
    driver = "org.h2.Driver"
  )

  val db2 = DB(
    name = "db2",
    url = h2PostgresWithInit("db2", "create-todos-table.sql"),
    driver = "org.h2.Driver"
  )

  // Force RUNSCRIPT to be executed on each DB
  val connDb1 = DriverManager.getConnection(db1.url)
  val connDb2 = DriverManager.getConnection(db2.url)

  // Setup fake Hadoop environment
  val tmpdir = System.getProperty("java.io.tmpdir")
  System.setProperty("hadoop.home.dir", tmpdir)

  // Create spark session
  val spark = SparkSession
    .builder()
    .appName("JDBC Federated Example")
    .master("local[*]")
    .config(db1.buildSparkConf())
    .config(db2.buildSparkConf())
    .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    .config("spark.kryo.unsafe", "true")
    // .config("spark.sql.codegen.comments", "true")
    .config("spark.sql.adaptive.enabled", "true")
    .config("spark.sql.cbo.enabled", "true")
    .config("spark.sql.cbo.joinReorder.dp.star.filter", "true")
    .config("spark.sql.cbo.joinReorder.enabled", "true")
    .config("spark.sql.cbo.planStats.enabled", "true")
    .config("spark.sql.cbo.starSchemaDetection", "true")
    .config("spark.sql.ui.explainMode", "extended")
    .getOrCreate()

  spark.sql("SHOW TABLES IN db1.public").show()
  spark.sql("SHOW TABLES IN db2.public").show()

  val query1 =
    """
      | SELECT p.id, p.name, t.id, t.title
      | FROM db1.public.person p
      | JOIN db2.public.todos t
      | ON p.id = t.person_id
      |""".stripMargin

  // Query person joined with todos
  val df1 = spark.sql(query1)
  spark.time(df1.show())

  val query2 =
    """
      | SELECT p.id, p.name, t.id, t.title
      | FROM db1.public.person p
      | JOIN db2.public.todos t
      | ON p.id = t.person_id
      | WHERE p.id = 1
      |""".stripMargin

  val df2 = spark.sql(query2)
  spark.time(df2.show())

  val query3 =
    """
      | SELECT p.id, p.name, t.id, t.title
      | FROM db1.public.person p
      | JOIN db2.public.todos t
      | ON p.id = t.person_id
      | WHERE p.id = 2
      | LIMIT 1
      |""".stripMargin

  val df3 = spark.sql(query3)
  spark.time(df3.show())

  val query4 =
    """
      | SELECT p.id, p.name, t.id, t.title
      | FROM db1.public.person p
      | JOIN db2.public.todos t
      | ON p.id = t.person_id
      | WHERE p.id = 2
      |""".stripMargin

  val df4 = spark.sql(query4)
  spark.time(df4.show())

  println("Press any button to stop...")
  scala.io.StdIn.readLine()
}
