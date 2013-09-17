package main

import scala.xml.NodeSeq

/**
 * Created with IntelliJ IDEA.
 * User: vijay
 * Date: 17/09/13
 * Time: 3:34 PM
 * To change this template use File | Settings | File Templates.
 */
object Helper {
  case class ParseOp[Long](op: String => Long)
  def getElements(xml: scala.xml.Elem)(element: String): NodeSeq = {
    xml \\ element
  }

  def getElements(xml: scala.xml.NodeSeq)(element: String): NodeSeq = {
    xml \ element
  }
  def parse[T: ParseOp](s: String) = try {
    if (s.trim.length < 1) None
    else Some(implicitly[ParseOp[T]].op(s))
  }
  catch {
    case _: Throwable => None
  }
  object SortType extends Enumeration{
    val MESSAGES= Value("messages")
    val FOLLOWERS= Value("followers")
    val NONE= Value("none")
  }
}
