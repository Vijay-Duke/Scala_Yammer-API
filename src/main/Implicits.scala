package main

import scala.xml.NodeSeq
import main.Helper.ParseOp

/**
 * Created with IntelliJ IDEA.
 * User: vijay
 * Date: 17/09/13
 * Time: 3:33 PM
 * To change this template use File | Settings | File Templates.
 */
trait Implicits {

  implicit val populateLong = ParseOp[Long](_.toLong)
  //implicit val populateInt = ParseOp[Int](_.toInt)

  implicit val populateString = ParseOp[String](_.toString)

  implicit def parseXml(value: String)(implicit res: NodeSeq): NodeSeq = {
      Helper.getElements(res)(value)
  }
}
