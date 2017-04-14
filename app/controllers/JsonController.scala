package controllers

import com.typesafe.config.ConfigFactory
import controllers.Application.absolutePath
import doodle.backend.Formats.Png
import doodle.core.Image
import doodle.syntax._
import doodle.jvm.Java2DCanvas._
import doodle.backend.StandardInterpreter._
import generation.{Hasher, ImageGenerator}
import play.api.libs.json.{JsBoolean, JsNumber, JsString, Json}
import play.api.mvc.{Action, Controller}

object JsonController extends Controller {

  def generate(lat: Int, lon: Int) = Action {
    val config = ConfigFactory.load()
    val hasher: Hasher = new Hasher(lat, lon)

    var jsonResponse = Json.obj()
    jsonResponse += "hash" -> JsString(hasher.toString)
    jsonResponse += "difficulty" -> JsNumber(hasher.getDifficulty())
    jsonResponse += "lat" -> JsNumber(BigDecimal(lat))
    jsonResponse += "lon" -> JsNumber(BigDecimal(lon))
    jsonResponse += "isCaught" -> JsBoolean(config.getInt("game.difficulty") <= hasher.getDifficulty)

    if (config.getInt("game.difficulty") <= hasher.getDifficulty) {
      val doodle: Image = ImageGenerator.doodle(hasher.getSeed())
      doodle.save[Png](absolutePath(hasher.getSeed() + ".png"))
      Thread.sleep(200)

      jsonResponse += "seed" -> JsString(hasher.getSeed().toString)
      jsonResponse += "time" -> JsNumber(hasher.getTime())
    }
    Ok(jsonResponse).as(JSON)
  }

  def verify(lat: Int, lon: Int, time: Long, hash: String) = Action {
    val hasher: Hasher = new Hasher(lat, lon, Some(time))

    var jsonResponse = Json.obj()

    if(hasher.toString().equals(hash)){
      val doodle: Image = ImageGenerator.doodle(hasher.getSeed())
      doodle.save[Png](absolutePath(hasher.getSeed() + ".png"))
      Thread.sleep(200)

      jsonResponse += "hash" -> JsString(hasher.toString)
      jsonResponse += "difficulty" -> JsNumber(hasher.getDifficulty())
      jsonResponse += "lat" -> JsNumber(BigDecimal(lat))
      jsonResponse += "lon" -> JsNumber(BigDecimal(lon))
      jsonResponse += "verified" -> JsBoolean(true)
      jsonResponse += "seed" -> JsString(hasher.getSeed().toString)
      jsonResponse += "time" -> JsNumber(hasher.getTime())
    } else {
      jsonResponse += "verified" -> JsBoolean(false)
    }

    Ok(jsonResponse).as(JSON)
  }

}
