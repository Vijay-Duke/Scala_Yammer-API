package main

import scala.xml.{NodeSeq, Elem}
import main.Helper._
import main.EmailAddress
import main.InstantMessenger
import main.Contact
import main.Implicits
import scala.collection.immutable.Seq

/**
 * Created with IntelliJ IDEA.
 * User: vijay
 * Date: 17/09/13
 * Time: 2:43 PM
 * To change this template use File | Settings | File Templates.
 */

case class UserOperations() extends YammerOperations with Implicits{
  import YammerHttp._

  override def BASE_URL = super.BASE_URL + "users"
  private def buildGetParameter(page:Int=0,sortBy:SortType.Value=SortType.NONE,reverse:Boolean=false,letter:Char='\0',delete:Boolean=false): Map[String, String] = {
    val map: scala.collection.mutable.Map[String, String] = scala.collection.mutable.Map[String, String]()
    if (page > 0) map += "page" -> String.valueOf(page)
    if (sortBy != SortType.NONE) map += "sort_by" -> String.valueOf(sortBy)
    if (reverse) map += "reverse" -> String.valueOf(reverse).toUpperCase;
    if (!letter.equals('\0')) map += ("letter" -> letter.toString);
    if (delete) map += ("delete" -> "TRUE");
    map.toMap

  }
 //
  def usersInGroup(groupId:Long)
 {
   val res: Elem =GET(s"$BASE_URL/in_group/$groupId.xml?access_token=Ka9Gp3IHbYsmVxFEPePIWA")
   users(res)
 }
  def user(email:String)={
   println(s"$BASE_URL/by_email.xml?email=$email")
   val res: Elem =GET(s"$BASE_URL/by_email.xml?email=$email&access_token=Ka9Gp3IHbYsmVxFEPePIWA")
  users(res)(1)

 }
  def user(id:Long):UserProfile=
 {
   val res: Elem =GET(s"$BASE_URL/$id.xml?access_token=Ka9Gp3IHbYsmVxFEPePIWA")
   users(res)(0)
 }
  def users(page:Int=0,sortBy:SortType.Value=SortType.NONE,reverse:Boolean=false,letter:Char='\0',delete:Boolean=false): Seq[UserProfile]=
 {
   val res: Elem =GET(s"$BASE_URL.xml?access_token=Ka9Gp3IHbYsmVxFEPePIWA")
   users(res)
 }
  def getUsers():Elem={
    GET(s"$BASE_URL.xml?access_token=Ka9Gp3IHbYsmVxFEPePIWA")
  }
  def users(res:Elem): Seq[UserProfile] ={

   val userElements = getElements(res)(_)
   val m: Seq[UserProfile] = userElements("response") map{
     userProfile =>
     implicit val x = userProfile
     UserProfile(url ="url".text,summary = "summary".text,name = "name".text,
     mugshotUrl = "mugshot-url".text,lastName = "last-name".text,jobTitle = "job-title".text,fullName = "full-name".text,
     firstName = "first-name".text,expertise = "expertise".text,dataType = "type".text,networkId =parse[Long]("network-id".text),
     contact=  Contact(hasFakeEmail = "has-fake-email".text,im =InstantMessenger(provider=("contact" \ "im" \ "provider").text,
     userName =("contact" \ "im" \ "username").text),emailAddresses =
       (userProfile \ "contact" \\ "email-address") map{
        email =>
         EmailAddress(emailType = (email \ "type").text,address = (email \ "address").text)
     },phoneNumbers =
       (userProfile \ "contact" \\ "phone-number") map{
        phoneNumber =>
         PhoneNumber(phoneType = (phoneNumber \ "type").text,number = (phoneNumber \ "number").text)
       }
     ),
    stats=Stats(updates = parse[Long]((userProfile \ "stats" \ "updates").text),following =parse[Long]((userProfile \ "stats" \ "updates").text),followers = parse[Long]((userProfile \ "stats" \ "updates").text) ),
     admin =false,externalUrls =List.empty[String],timezone ="timezone".text,id = parse[Long]("id".text))
   }
   m
  }

}
case class Stats(following:Option[Long],followers:Option[Long],updates:Option[Long])
case class Contact(hasFakeEmail:String,im:InstantMessenger,emailAddresses:Seq[EmailAddress],phoneNumbers:Seq[PhoneNumber])
case class InstantMessenger(userName:String,provider:String)
case class PhoneNumber(phoneType:String,number:String)
case class EmailAddress(emailType:String,address:String)
case class UserProfile(id:Option[Long],mugshotUrl:String,dataType:String,url:String,fullName:String,name:String,admin:Boolean,
                       expertise:String,summary:String,jobTitle:String,contact:Contact,
                       externalUrls:List[String],stats:Stats,lastName:String,firstName:String,networkId:Option[Long],timezone:String)