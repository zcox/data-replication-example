package com.banno

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global //TODO DB operations should be performed on their own ExecutionContext

trait UserRepository extends Database {
  def getUser(userId: Long): Future[User] = getUserFromDatabase(userId)

  def getUserFromDatabase(userId: Long): Future[User] = Future {
    query(s"SELECT * FROM users WHERE id=$userId") { results => 
      if (!results.next()) throw new IllegalArgumentException(s"User $userId does not exist") //TODO better fail here so GET /api/users/123 returns 404
      User(results.getLong("id"), results.getString("username"), results.getString("name"), results.getString("description"), results.getString("image_url"))
    }
  }

  def userExists(userId: Long): Boolean = 
    query(s"SELECT COUNT(*) FROM users WHERE id=$userId") { results => 
      if (results.next()) results.getInt(1) > 0 else false
    }

  def createUser(user: NewUser): Future[User] = Future {
    insertAndGetGeneratedId(s"INSERT INTO users (username, name, description, image_url) VALUES ('${user.username}', '${user.name}', '${user.description}', '${user.imageUrl}')")
  } flatMap getUserFromDatabase

  def rawCreateUser(userId: Long, user: NewUser): Unit = 
    update(s"INSERT INTO users (id, username, name, description, image_url) VALUES (${userId}, '${user.username}', '${user.name}', '${user.description}', '${user.imageUrl}')")

  def createUser(userId: Long, user: NewUser): Future[User] = Future {
    rawCreateUser(userId, user)
  } flatMap { _ => getUserFromDatabase(userId) }

  def updateUser(user: User): Future[User] = Future {
    if (!userExists(user.id)) rawCreateUser(user.id, user.toNewUser)
    else update(s"UPDATE users SET username='${user.username}', name='${user.name}', description='${user.description}', image_url='${user.imageUrl}' WHERE id=${user.id}")
    user
  }
}
