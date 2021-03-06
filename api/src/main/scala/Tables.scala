package com.banno
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.driver.PostgresDriver
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.driver.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Tweets.schema ++ Users.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table Tweets
   *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
   *  @param content Database column content SqlType(text), Default(None)
   *  @param createdAt Database column created_at SqlType(timestamp), Default(None)
   *  @param latitude Database column latitude SqlType(float8), Default(None)
   *  @param longitude Database column longitude SqlType(float8), Default(None)
   *  @param userId Database column user_id SqlType(int8) */
  case class TweetsRow(id: Long, content: Option[String] = None, createdAt: Option[java.sql.Timestamp] = None, latitude: Option[Double] = None, longitude: Option[Double] = None, userId: Long)
  /** GetResult implicit for fetching TweetsRow objects using plain SQL queries */
  implicit def GetResultTweetsRow(implicit e0: GR[Long], e1: GR[Option[String]], e2: GR[Option[java.sql.Timestamp]], e3: GR[Option[Double]]): GR[TweetsRow] = GR{
    prs => import prs._
    TweetsRow.tupled((<<[Long], <<?[String], <<?[java.sql.Timestamp], <<?[Double], <<?[Double], <<[Long]))
  }
  /** Table description of table tweets. Objects of this class serve as prototypes for rows in queries. */
  class Tweets(_tableTag: Tag) extends Table[TweetsRow](_tableTag, "tweets") {
    def * = (id, content, createdAt, latitude, longitude, userId) <> (TweetsRow.tupled, TweetsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), content, createdAt, latitude, longitude, Rep.Some(userId)).shaped.<>({r=>import r._; _1.map(_=> TweetsRow.tupled((_1.get, _2, _3, _4, _5, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column content SqlType(text), Default(None) */
    val content: Rep[Option[String]] = column[Option[String]]("content", O.Default(None))
    /** Database column created_at SqlType(timestamp), Default(None) */
    val createdAt: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("created_at", O.Default(None))
    /** Database column latitude SqlType(float8), Default(None) */
    val latitude: Rep[Option[Double]] = column[Option[Double]]("latitude", O.Default(None))
    /** Database column longitude SqlType(float8), Default(None) */
    val longitude: Rep[Option[Double]] = column[Option[Double]]("longitude", O.Default(None))
    /** Database column user_id SqlType(int8) */
    val userId: Rep[Long] = column[Long]("user_id")
  }
  /** Collection-like TableQuery object for table Tweets */
  lazy val Tweets = new TableQuery(tag => new Tweets(tag))

  /** Entity class storing rows of table Users
   *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
   *  @param username Database column username SqlType(varchar), Length(50,true)
   *  @param name Database column name SqlType(text), Default(None)
   *  @param description Database column description SqlType(text), Default(None)
   *  @param imageUrl Database column image_url SqlType(text), Default(None) */
  case class UsersRow(id: Long, username: String, name: Option[String] = None, description: Option[String] = None, imageUrl: Option[String] = None)
  /** GetResult implicit for fetching UsersRow objects using plain SQL queries */
  implicit def GetResultUsersRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Option[String]]): GR[UsersRow] = GR{
    prs => import prs._
    UsersRow.tupled((<<[Long], <<[String], <<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table users. Objects of this class serve as prototypes for rows in queries. */
  class Users(_tableTag: Tag) extends Table[UsersRow](_tableTag, "users") {
    def * = (id, username, name, description, imageUrl) <> (UsersRow.tupled, UsersRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(username), name, description, imageUrl).shaped.<>({r=>import r._; _1.map(_=> UsersRow.tupled((_1.get, _2.get, _3, _4, _5)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column username SqlType(varchar), Length(50,true) */
    val username: Rep[String] = column[String]("username", O.Length(50,varying=true))
    /** Database column name SqlType(text), Default(None) */
    val name: Rep[Option[String]] = column[Option[String]]("name", O.Default(None))
    /** Database column description SqlType(text), Default(None) */
    val description: Rep[Option[String]] = column[Option[String]]("description", O.Default(None))
    /** Database column image_url SqlType(text), Default(None) */
    val imageUrl: Rep[Option[String]] = column[Option[String]]("image_url", O.Default(None))

    /** Uniqueness Index over (username) (database name users_username_key) */
    val index1 = index("users_username_key", username, unique=true)
  }
  /** Collection-like TableQuery object for table Users */
  lazy val Users = new TableQuery(tag => new Users(tag))
}
