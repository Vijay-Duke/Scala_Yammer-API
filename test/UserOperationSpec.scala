import main.{UserProfile, MessageOperations, Message, UserOperations}
import org.scalatest.FlatSpec
import scala.collection.immutable.Seq

/**
 * Created with IntelliJ IDEA.
 * User: vijay
 * Date: 17/09/13
 * Time: 3:44 PM
 * To change this template use File | Settings | File Templates.
 */
class UserOperationSpec extends FlatSpec{
  "users Method of UsersOperations" should "Not be empty" in{
    val value: Seq[UserProfile] = UserOperations().users()
    for (user <- value)
    {
      println("-----------------------------")
      println(user.admin)
      println(user.contact)
      println(user.dataType)
      println(user.expertise)
      println(user.firstName)
      println(user.id)
      println(user.mugshotUrl)
      println(user.summary)
      println("-----------------------------")
    }
  }
}
