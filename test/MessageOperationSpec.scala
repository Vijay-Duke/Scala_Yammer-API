import org.scalatest.FlatSpec
import main.{Message, YammerOperations}
/**
 * Created with IntelliJ IDEA.
 * User: vijay
 * Date: 17/09/13
 * Time: 3:37 PM
 * To change this template use File | Settings | File Templates.
 */
class MessageOperationSpec extends FlatSpec{

  "GetMessages of Message Operations " should "Not be empty" in{

     val x: Message = main.MessageOperations().getMessages()
     assert(x.messageDataList.size>0)

  }

}
