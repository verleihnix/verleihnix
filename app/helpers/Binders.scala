package helpers
import models.NoticeTyp
import play.api.mvc.PathBindable

object Binders {
  implicit object NoticeTypPathBindable extends PathBindable[models.NoticeTyp] {
    def bind(key: String, value: String) = try {
      Right(NoticeTyp.valueOf(value))
    } catch {
      case e: Exception => Left("Cannot parse parameter '" + key + "' as NoticeTyp")
    }

    def unbind(key: String, value: NoticeTyp): String = value.name()
  }
}