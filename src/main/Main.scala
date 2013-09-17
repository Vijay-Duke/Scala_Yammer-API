package main

import dispatch.{Http, host}
import dispatch._, Defaults._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.xml.{Elem, NodeSeq}
import scala.Predef._
import com.ning.http.client.{FilePart, Part, Response, Request}
import java.io.File

/**
 * Created with IntelliJ IDEA.
 * User: vijay
 * Date: 13/09/13
 * Time: 4:56 AM
 * To change this template use File | Settings | File Templates.
 */

//CODE -  ZXEsdpyqoH6brjDWrUbGw
// TOKEN - Ka9Gp3IHbYsmVxFEPePIWA
object Main extends App{
 /* def myPost = url("https://www.yammer.com/api/v1/messages.xml?access_token=Ka9Gp3IHbYsmVxFEPePIWA")
  val f = new  java.io.File("/home/vijay/my_feeds.xml")
  val p:Part = new FilePart("attachment1",f,"","")
  val myPostWithParams: Req = ((myPost << Map("body" -> "TESTING POST METHOD REST API")).addBodyPart(p) ).POST
  println(myPostWithParams)


  val s: dispatch.Future[Response] = Http(myPostWithParams)

  val ss: Response =Await.result(s,20 second)
       println(ss.getResponseBody)*/

  /*val str: dispatch.Future[Elem] = Http(url("https://www.yammer.com/api/v1/messages/algo.xml?access_token=Ka9Gp3IHbYsmVxFEPePIWA")
      OK as.xml.Elem)
   for(c <- str)
   {
     println(c)
   }*/

  val f1 = new  java.io.File("/home/vijay/my_feeds.xml")
  val f2 = new  java.io.File("/home/vijay/my_feeds1.xml")
  val f3 = new  java.io.File("/home/vijay/my_feeds2.xml")
  val f4 = new  java.io.File("/home/vijay/my_feeds3.xml")


  val b = List(f1)
  val x= new UserOperations().users()
  println(x.length)
  for (u <-x)
  {
   println(u.id)

  }
  val ux:UserProfile = new UserOperations().user("Mail@VijayIyengar.com")
  println(ux.id)
  println(ux.firstName)
  println(ux.fullName)
  // println("PHONE NUMBER -"+x.users()(1).contact.phoneNumbers)
 // println(x.messageDataList)
//  val x = MessageOperation().delete(MessageOperation().getMessages().messageDataList(0).id)
 /*

  val id: Long =x.messageDataList(0).id
   println(id)
Thread.sleep(3000)
  val res: Req = url(s"https://www.yammer.com/api/v1/messages/${325635533}?access_token=Ka9Gp3IHbYsmVxFEPePIWA").DELETE
  val c: dispatch.Future[Response] =   Http(res)
  val ss: Response =Await.result(c,20 second)
  println(ss.getResponseBody)*/
  println("DONE")
}
