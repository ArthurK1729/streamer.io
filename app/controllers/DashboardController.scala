package controllers

import javax.inject._

import play.api.libs.json._
import play.api.mvc._
import play.api.libs.functional.syntax._

case class CreateComponent(name: String,
                           link: String,
                           sampleJson: String,
                           apiToken: String,
                           mlAlgorithm: String,
                           frequency: String)


@Singleton
class DashboardController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  implicit val placeReads: Reads[CreateComponent] = (
    (JsPath \ "name").read[String]
      and (JsPath \ "link").read[String]
      and (JsPath \ "sampleJson").read[String] // Validate Json in the UI?
      and (JsPath \ "apiToken").read[String]
      and (JsPath \ "mlAlgorithm").read[String]
      and (JsPath \ "frequency").read[String]
    )(CreateComponent.apply _)


  def createComponent = Action(parse.json) { request =>
    val createComponentResult = request.body.validate[CreateComponent]

    createComponentResult.fold(
      errors => {
        BadRequest(Json.obj("message" -> JsError.toJson(errors)))
      },
      componentDetails => {
        // Do component creation here?
        Ok(Json.obj("message" -> "Successfully created new dashboard component."))
      }
    )
  }

}
