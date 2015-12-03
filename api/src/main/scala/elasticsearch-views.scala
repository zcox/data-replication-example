package com.banno

import spray.json._
import java.util.concurrent.{TimeUnit, Executors}
import scala.sys.addShutdownHook
import scala.concurrent.duration._
import akka.actor.{Actor, Props}

trait ReplicateIntoElasticsearch extends KafkaConsumer with JsonSupport {

  def startElasticsearchReplication(): Unit = {
    val streams = getStreams("api-elasticsearch")

    val usersStream = streams(usersTopic)(0)
    val usersHandler = new ReplicateDatabaseRowsIntoElasticsearch[Long, User]("twitter", "users")
    val usersConsumer = new DatabaseChangeConsumer(usersTopic, usersStream, usersHandler)(UserKafkaAvroSerde)
    new Thread(usersConsumer).start()

    val tweetsStream = streams(tweetsTopic)(0)
    val tweetsHandler = new ReplicateDatabaseRowsIntoElasticsearch[Long, Tweet]("twitter", "tweets")
    val tweetsConsumer = new DatabaseChangeConsumer(tweetsTopic, tweetsStream, tweetsHandler)(TweetKafkaAvroSerde)
    new Thread(tweetsConsumer).start()
  }
}

class ReplicateDatabaseRowsIntoElasticsearch[K, V : JsonFormat](
    indexName: String, 
    typeName: String) 
  extends DatabaseChangeHandler[K, V] 
  with ElasticsearchUtils 
  with JsonSupport 
  with ActorModule
  with Logging 
  with Config {

  import ElasticsearchBulkActor._
  import actorSystem.dispatcher

  val actor = actorSystem.actorOf(ElasticsearchBulkActor.props, s"$indexName-$typeName")
  val bulkWritePeriod = Duration(config.getString("api.elasticsearch.bulk-write-period")).asInstanceOf[FiniteDuration]
  actorSystem.scheduler.schedule(bulkWritePeriod, bulkWritePeriod, actor, SendBulkRequest)

  override def rowChanged(key: K, value: V): Unit = {
    actor ! IndexDocument(indexName, typeName, key.toString, value.toJson.compactPrint)
  }

  override def rowDeleted(key: K): Unit = {
    actor ! DeleteDocument(indexName, typeName, key.toString)
  }
}

object ElasticsearchBulkActor {
  case class IndexDocument(indexName: String, typeName: String, documentId: String, document: String)
  case class DeleteDocument(indexName: String, typeName: String, documentId: String)
  case object SendBulkRequest

  def props: Props = Props[ElasticsearchBulkActor]
}

class ElasticsearchBulkActor extends Actor with ElasticsearchUtils {
  import ElasticsearchBulkActor._

  private[this] lazy val client = elasticsearchClient
  private[this] var bulkRequest = client.prepareBulk()
  private[this] val log = akka.event.Logging(context.system, this)

  def receive = {
    case IndexDocument(indexName, typeName, documentId, document) => 
      bulkRequest.add(client.prepareIndex(indexName, typeName, documentId).setSource(document))

    case DeleteDocument(indexName, typeName, documentId) => 
      bulkRequest.add(client.prepareDelete(indexName, typeName, documentId))

    case SendBulkRequest => 
      if (bulkRequest.numberOfActions > 0) {
        val bulkResponse = bulkRequest.get()
        log.debug(s"Bulk write with ${bulkResponse.getItems.size} documents took ${bulkResponse.getTookInMillis} msec")
        if (bulkResponse.hasFailures) log.error(s"Error in bulk indexing: ${bulkResponse.buildFailureMessage}")
        bulkRequest = client.prepareBulk()
      }
  }
}
