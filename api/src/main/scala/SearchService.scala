package com.banno

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConversions._
import org.elasticsearch.search.SearchHit
import org.elasticsearch.index.query.QueryBuilders
import org.joda.time.format.ISODateTimeFormat

trait SearchService extends ElasticsearchUtils {
  val format = ISODateTimeFormat.dateTime

  def search(query: String): Future[SearchResults] = Future {
    val searchResponse = elasticsearchClient.prepareSearch("twitter").setQuery(QueryBuilders.matchQuery("_all", query)).get()
    val groupedHits = searchResponse.getHits.hits.toSeq.groupBy(hit => hit.getType)
    SearchResults(query, groupedHits.getOrElse("users", Nil).map(toUser), groupedHits.getOrElse("tweets", Nil).map(toTweet))
  }

  def toUser(hit: SearchHit): User = {
    val source = hit.getSource.toMap
    User(
      id = source("id").asInstanceOf[Long],
      username = source("username").toString,
      name = source.get("name").map(_.toString).getOrElse(""),
      description = source.get("description").map(_.toString).getOrElse(""),
      imageUrl = source.get("imageUrl").map(_.toString).getOrElse(""))
  }

  def toTweet(hit: SearchHit): Tweet = {
    val source = hit.getSource.toMap
    Tweet(
      id = source("id").asInstanceOf[Long],
      text = source.get("text").map(_.toString).getOrElse(""),
      userId = (source.get("userId").map(_.asInstanceOf[Number].longValue).getOrElse(0)),
      createdAt = source.get("createdAt").map(s => format.parseDateTime(s.toString)).getOrElse(null),
      latitude = source.get("latitude").map(_.asInstanceOf[Double]),
      longitude = source.get("longitude").map(_.asInstanceOf[Double]))
  }
}
