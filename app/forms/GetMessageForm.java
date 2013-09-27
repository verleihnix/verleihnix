package forms;

import models.Message;
import play.data.validation.Constraints;


public class GetMessageForm {

    @Constraints.Required
    public Message msg;
}
