package controllers

import com.typesafe.config.ConfigFactory
import doodle.core.Image
import doodle.syntax._
import doodle.jvm.Java2DCanvas._
import doodle.backend.StandardInterpreter._
import doodle.backend.Formats._
import generation._
import play.api.mvc._
import play.api.libs.json._

object Application extends Controller {

  private val TEMP: String = ConfigFactory.load().getString("game.temp")

  def absolutePath(path: String = ""): String = TEMP + "/" + path

  def index = Action {
    Ok(views.html.index(null))
  }

  def verify(lat: Option[Int], lon: Option[Int], time: Option[Long], hash: Option[String]) = TODO

  def sendTempFile(name: String) = Action {
    require(!name.equals(""))
    Ok.sendFile(new java.io.File(absolutePath(name.split("/").last)))
  }
}
