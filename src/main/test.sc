implicit def intToString(x: Int)(implicit y:Int): String = {
  (x+y).toString
}
implicit val z =1
