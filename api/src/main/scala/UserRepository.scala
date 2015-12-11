package com.banno

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global //TODO DB operations should be performed on their own ExecutionContext
import org.apache.avro.generic.GenericData
import slick.driver.PostgresDriver.api._
import org.postgresql.util.PSQLException

trait UserRepository extends Logging {
  import SlickDatabase._
  import Tables._

  def getUser(userId: Long): Future[Option[UserResponse]] = 
    // getUserFromDatabase(userId)
    getUserFromRocksDb(userId)

  def getUserFromDatabase(userId: Long): Future[Option[UserResponse]] = 
    db.run(Users.filter(_.id === userId).result.headOption).map(_.map(rowToUser).map(u => UserResponse(u, Nil)))

  def createUser(user: NewUser): Future[User] = 
    db.run((Users returning Users.map(_.id) into ((user,id) => user.copy(id=id))) += userToRow(user)) map rowToUser

  def createUser(user: User): Future[User] = 
    db.run(Users.forceInsert(userToRow(user))) map { _ => user }

  def updateUser(user: User): Future[User] = {
    val q = for (u <- Users if u.id === user.id) yield u
    db.run(q.update(userToRow(user.updatedNow))) map { _ => user }
  }

  def createOrUpdateUser(user: User): Future[User] = 
    createUser(user) recoverWith { //Slick's insertOrUpdate doesn't use the specified primary key on insert https://groups.google.com/d/msg/scalaquery/3geiy_lH3SM/ntAbDp8cCAAJ
      case e: PSQLException if e.getMessage contains "duplicate key value violates unique constraint" => 
        // log.debug(s"Insert for user failed, updating instead: $user")
        updateUser(user)
    }

  //////////////////////////////////////
  // RocksDB
  lazy val usersDb = UsersRocksDb.rocksDb
  lazy val recentTweetsDb = RecentTweets.rocksDb

  def getUserFromRocksDb(userId: Long): Future[Option[UserResponse]] = Future {
    usersDb.get(userId) map { user => 
      val recentTweets = recentTweetsDb.get(userId).getOrElse(Nil)
      UserResponse(user, recentTweets)
    }
  }
}
