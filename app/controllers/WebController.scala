package controllers

import play.api.mvc.{Action, Controller}

object WebController extends Controller {

  def generate() = Action {
    Ok(views.html.generate()).as(HTML)
  }

  def verify() = Action {
    Ok(views.html.verify()).as(HTML)
  }
}
