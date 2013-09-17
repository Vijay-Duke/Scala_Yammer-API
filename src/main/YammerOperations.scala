package main {

import java.net.URI
import dispatch._, Defaults._
import java.util.Date
import scala.xml.{XML, NodeSeq, Elem, Node}
import scala.collection.JavaConverters._
import scala.collection.immutable
import scala.concurrent.Await
import org.scalatest.time.Second
import scala.concurrent.duration._
import main.Body
import scala.Some
import main.File
import main.LikedBy
import main.Name
import main.MessageData
import main.Attachment
import com.ning.http.client.{FilePart, Response, Part}
import javax.xml.ws.http.HTTPException
import scala.util.{Success,Failure}

class YammerException(message:String) extends Exception(message){

}

package object YammerHttp {
  def GET(endPoint: String): Elem = {
    val response = Http(url(endPoint) OK as.xml.Elem)
    Await.result(response, 20 second)
  }
  def GETasString(endPoint: String): String = {
    val response = Http(url(endPoint) OK as.String)
    Await.result(response, 20 second)
  }

  def POST(endPoint:Req):String ={
    val response = Http(endPoint.POST)
   val result = Await.result(response, 20 second)
    if (result.getStatusCode !=201)
      throw new YammerException(s"HTTP RESPONSE CODE -${result.getStatusCode} \n HTTP RESPONSE BODY -${result.getResponseBody}")
   result.getResponseBody
  }

  def DELETE(endPoint:Req): Unit ={

    val response: dispatch.Future[Response] = Http(endPoint.DELETE)
     for(res <- response)
     {
       res.getStatusCode match {
       case 200 => println("Success")
       case _ => throw new YammerException(s"HTTP RESPONSE CODE -${res.getStatusCode} \n HTTP RESPONSE BODY -${res.getResponseBody}")
     }
     }

  }
}

trait YammerOperations {
  def BASE_URL = "https://www.yammer.com/api/v1/"
}

case class Message(messageDataList: Seq[MessageData], messageMetaData: MessageMetaData, messageReferenceList: Seq[MessageReference]) extends YammerOperations
case class MessageMetaData(currentUserId: String, feedName: String, feedDescription: String)
case class MessageReference(refType: String, name: String, id: String, url: String, webUrl: String)
case class MessageData(
                        id: Long, body: Body, url: String, networkId: Long,
                        privacy: String, threadId: Long, senderType: String,
                        attachments: Seq[Attachment], repliedToId: Option[Long],
                        senderId: Long, webUrl: String, clientUrl: String,
                        systemMessage: Boolean, messageType: String, createdAt: Date,
                        directMessage: Boolean, clientType: String, likedBy: LikedBy,
                        groupId: Option[Long], sharedMessageId: Option[Long]
                        )

case class Body(val plain: String, val parsed: String, rich: String, urls: List[String] = List.empty)

case class LikedBy(val count: Int, names: List[Name])

case class Name(val link: String, val fullName: String, val userId: Option[Long])

case class Attachment(val overlayUrl: String, val dataType: String, val fullName: String, val privacy: String, val official: String, val uuid: String, val description: String, val ownerId: Option[Long], val name: String, val realType: String, val contentType: String, val path: String, val yammerId: Option[Long], val size: String, val thumbnailUrl: String, val webUrl: String, val id: Option[Long], val file: Seq[File])

case class File(val size: Option[Long], val url: String)

object MessageOperations {
  //def apply() = new MessageOperation()
}

case class MessageOperations() extends YammerOperations with Implicits {

  import YammerHttp._
  import Helper._


  override def BASE_URL = super.BASE_URL + "messages"

  private def buildGetParameter(olderThan: Long, newerThan: Long, threaded: String, limit: Int): Map[String, String] = {
    val map: scala.collection.mutable.Map[String, String] = scala.collection.mutable.Map[String, String]()
    if (olderThan > 0) map += "older_than" -> String.valueOf(olderThan)
    if (newerThan > 0) map += "newer_than" -> String.valueOf(newerThan)
    if (threaded.length > 0) map += "threaded" -> String.valueOf(threaded);
    if (limit > 0) map += "limit" -> String.valueOf(limit);
    map.toMap

  }




  private def getMessageReference(res: scala.xml.Elem): Seq[MessageReference] = {
    //  val messageReferenceElements = getElements(res)(_)
    val messageMetaElements = getElements(res)("reference")
    messageMetaElements map {
      ref =>
        implicit val x = ref
        MessageReference(refType = ("type").text, name = ("name").text, id = ("id").text, url = ("url").text,
          webUrl = ("web-url").text)
    }
  }

  private def getMessageMetaData(res: scala.xml.Elem): MessageMetaData = {
    implicit val messageMetaElements = getElements(res)("meta")
    MessageMetaData(currentUserId = (("current-user-id"): NodeSeq).text, feedName = ("feed-name": NodeSeq).text, feedDescription = ("feed-desc": NodeSeq).text)
  }

  private def getMessageData(res: scala.xml.Elem): Seq[MessageData] = {
    val messageElements = getElements(res)(_)
    val m: Seq[MessageData] = messageElements("message") map {
      message =>
        val messageElement = getElements(message)(_)
        val urls: Seq[String] = messageElement("body").map(url => (url \ " url").text)
        val attachmentElement: Seq[Attachment] = message \\ "attachment" map {
          attachment =>
            implicit val x = attachment
            val files: Seq[File] = ("file": NodeSeq).map(file => File(size = parse[Long]((file \ "size").text), (file \ "url").text))
            Attachment(
              contentType = ("content-type").text,
              dataType = ("type").text,
              webUrl = ("web-url").text,
              uuid = ("uuid").text,
              size = ("size").text,
              privacy = ("privacy").text,
              path = ("path").text,
              overlayUrl = ("overlay-url").text,
              official = ("official").text,
              name = ("name").text,
              description = ("description").text,
              file = files, yammerId = parse[Long](("description").text),
              ownerId = parse[Long](("owner-id").text),
              realType = ("real-type").text,
              thumbnailUrl = ("thumbnail-url").text,
              fullName = ("full-name").text,
              id = parse[Long](("id").text)
            )
        }
        val names: Seq[Name] = (messageElement("liked-by") \\ "name") map {
          name => Name(link = (name \ "link").text, fullName = (name \ "full-name").text, parse[Long]((name \ "user-id").text))
        }
        MessageData(body = Body(parsed = (messageElement("body") \ "parsed").text, plain = (messageElement("body") \ "plain").text, rich = (messageElement("body") \ "rich").text, urls = urls.toList),
          systemMessage = messageElement("system-message").text.toBoolean, webUrl = messageElement("web-url").text, id = messageElement("id").text.toLong,
          privacy = messageElement("privacy").text, messageType = messageElement("message-type").text,
          clientType = messageElement("client-type").text, url = messageElement("url").text,
          senderId = messageElement("sender-id").text.toLong, networkId = messageElement("network-id").text.toLong,
          clientUrl = messageElement("client-url").text,
          threadId = messageElement("thread-id").text.toLong,
          senderType = messageElement("sender-type").text,
          repliedToId = parse[Long](messageElement("replied-to-id").text),
          likedBy = new LikedBy((message \ "liked-by" \ "count").text.toInt, names.toList),
          groupId = parse[Long](messageElement("group-id").text),
          directMessage = messageElement("direct-message").text.toBoolean, createdAt = new Date, attachments = attachmentElement,
          sharedMessageId = parse[Long](messageElement("shared-message-id").text))
    }
    m
  }

  private def getMessages(res:Elem):Message ={
    Message(messageDataList = getMessageData(res), messageReferenceList = getMessageReference(res), messageMetaData = getMessageMetaData(res))
  }
  def getMessages(olderThan: Long = 0, newerThan: Long = 0, threaded: String = "", limit: Int = 0): Message = {
    val messagesUrl = BASE_URL + ".xml"
    val res = YammerHttp.GET(s"$messagesUrl?access_token=Ka9Gp3IHbYsmVxFEPePIWA")
    Message(messageDataList = getMessageData(res), messageReferenceList = getMessageReference(res), messageMetaData = getMessageMetaData(res))
  }

  def getMessagesFollowing(olderThan: Long = 0, newerThan: Long = 0, threaded: String = "", limit: Int = 0): Message = {
    val myFeedsUrl = BASE_URL + "/my_feed.xml"
    val res = YammerHttp.GET(s"$myFeedsUrl?access_token=Ka9Gp3IHbYsmVxFEPePIWA")
    Message(messageDataList = getMessageData(res), messageReferenceList = getMessageReference(res), messageMetaData = getMessageMetaData(res))
  }

  def getMessagesSent(olderThan: Long = 0, newerThan: Long = 0, threaded: String = "", limit: Int = 0): Message = {
    val sentUrl = BASE_URL + "/sent.xml"
    val res = YammerHttp.GET(s"$sentUrl?access_token=Ka9Gp3IHbYsmVxFEPePIWA")
    Message(messageDataList = getMessageData(res), messageReferenceList = getMessageReference(res), messageMetaData = getMessageMetaData(res))
  }

  def getMessagesPrivate(olderThan: Long = 0, newerThan: Long = 0, threaded: String = "", limit: Int = 0): Message = {
    val privateUrl = BASE_URL + "/private.xml"
    val res = YammerHttp.GET(s"$privateUrl?access_token=Ka9Gp3IHbYsmVxFEPePIWA")
    Message(messageDataList = getMessageData(res), messageReferenceList = getMessageReference(res), messageMetaData = getMessageMetaData(res))
  }

  def getMessagesReceived(olderThan: Long = 0, newerThan: Long = 0, threaded: String = "", limit: Int = 0): Message = {
    val receivedUrl = BASE_URL + "/received.xml"
    val res = YammerHttp.GET(s"$receivedUrl?access_token=Ka9Gp3IHbYsmVxFEPePIWA")
    Message(messageDataList = getMessageData(res), messageReferenceList = getMessageReference(res), messageMetaData = getMessageMetaData(res))
  }

  def getMessagesAboutTopic(topicId: Long, olderThan: Long = 0, newerThan: Long = 0, threaded: String = "", limit: Int = 0): Message = {
    val aboutTopicUrl = BASE_URL + s"/about_topic/$topicId.xml"
    val res = YammerHttp.GET(s"$aboutTopicUrl?access_token=Ka9Gp3IHbYsmVxFEPePIWA")
    Message(messageDataList = getMessageData(res), messageReferenceList = getMessageReference(res), messageMetaData = getMessageMetaData(res))
  }

  def getMessagesInGroup(groupId: Long, olderThan: Long = 0, newerThan: Long = 0, threaded: String = "", limit: Int = 0): Message = {
    val inGroupUrl = BASE_URL + s"/in_group/$groupId.xml"
    val res = YammerHttp.GET(s"$inGroupUrl?access_token=Ka9Gp3IHbYsmVxFEPePIWA")
    Message(messageDataList = getMessageData(res), messageReferenceList = getMessageReference(res), messageMetaData = getMessageMetaData(res))
  }

  def getMessagesInThread(threadId: Long, olderThan: Long = 0, newerThan: Long = 0, threaded: String = "", limit: Int = 0): Message = {
    val inThreadUrl = BASE_URL + s"/in_thread/$threadId.xml"
    val res = YammerHttp.GET(s"$inThreadUrl?access_token=Ka9Gp3IHbYsmVxFEPePIWA")
    Message(messageDataList = getMessageData(res), messageReferenceList = getMessageReference(res), messageMetaData = getMessageMetaData(res))
  }

  def getMessagesFromUser(userId: Long, olderThan: Long = 0, newerThan: Long = 0, threaded: String = "", limit: Int = 0): Message = {
    val inThreadUrl = BASE_URL + s"/from_user/$userId.xml"
    val res = YammerHttp.GET(s"$inThreadUrl?access_token=Ka9Gp3IHbYsmVxFEPePIWA")
    Message(messageDataList = getMessageData(res), messageReferenceList = getMessageReference(res), messageMetaData = getMessageMetaData(res))
  }

  def getMessagesLikedByUser(userId: Long, olderThan: Long = 0, newerThan: Long = 0, threaded: String = "", limit: Int = 0): Message = {
    val inThreadUrl = BASE_URL + s"/liked_by/$userId.xml"
    val res = YammerHttp.GET(s"$inThreadUrl?access_token=Ka9Gp3IHbYsmVxFEPePIWA")
    Message(messageDataList = getMessageData(res), messageReferenceList = getMessageReference(res), messageMetaData = getMessageMetaData(res))
  }

  def postUpdate(message: String, attachments: List[java.io.File] = List.empty[java.io.File],
                 groupId: Int = 0, repliedToId: Int = 0, directToId: Int = 0, commaSeparatedTopics: String = "",
                 broadcast: Boolean = false):Message = {
    //pending_attachmentn and graph objects not implemented for Now
    val map: scala.collection.mutable.Map[String, String] = scala.collection.mutable.Map[String, String]()
    map += ("body" -> message)
    if (groupId > 0) map += ("group_id" -> groupId.toString)
    if (repliedToId > 0) map += ("replied_to_id" -> repliedToId.toString)
    if (directToId > 0) map += ("direct_to_id" -> directToId.toString)
    val myPost = url("https://www.yammer.com/api/v1/messages.xml?access_token=Ka9Gp3IHbYsmVxFEPePIWA")
    var myPostWithParams: Req = (myPost << Map("body" -> s"$message"))
    for (attachment <- attachments) {
      println(s"${attachments.indexOf(attachment) + 1}")
      myPostWithParams = myPostWithParams.addBodyPart(new FilePart(s"attachment${attachments.indexOf(attachment) + 1}", attachment.getAbsoluteFile, "", ""))
    }
    getMessages(XML.loadString(POST(myPostWithParams)))

  }
  def delete(messageId:Long){
    val deletedUrl = BASE_URL + s"/$messageId"
    DELETE(url(s"${deletedUrl}?access_token=Ka9Gp3IHbYsmVxFEPePIWA"))
  }

}

}