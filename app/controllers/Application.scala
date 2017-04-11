package controllers

import java.io.File

import com.typesafe.config.ConfigFactory
import play.api.Logger

import doodle.core.Image
import doodle.syntax._
import doodle.jvm.Java2DCanvas._
import doodle.backend.StandardInterpreter._
import doodle.backend.Formats._
import generation._
import play.api.mvc._
import play.api.Play.current

import play.api.db._

object Application extends Controller {

  private val TEMP: String = if(System.getenv("DOODLE_TEMP") != null) System.getenv("DOODLE_TEMP") else ConfigFactory.load().getString("game.temp")

  private def absolutePath(path: String = ""): String = TEMP + "/" + path

  def index = Action {
    Ok(views.html.index(null))
  }

  def hash(lat: Int, lon: Int) = Action {
    val config = ConfigFactory.load()
    val hasher: Hasher = new Hasher(lat,lon)
    if(config.getInt("game.difficulty") <= hasher.getDifficulty) {
      val doodle: Image = ImageGenerator.doodle(hasher.getSeed())
      doodle.save[Png](absolutePath(hasher.getSeed()+".png"))
      Thread.sleep(200)
      Ok(views.html.hash(hasher.toString,hasher.getDifficulty(),true,hasher.getSeed(),hasher.getTime()))
    }
    else {
      Ok(views.html.hash(hasher.toString,hasher.getDifficulty(),false))
    }


  }
//
//  def getImage(path: String) = Action {
//    Ok(new File(path))
//  }

  def sendTempFile(name: String) = Action {
    require(!name.equals(""))
    Ok.sendFile(new java.io.File(absolutePath(name.split("/").last)))
  }

  def db = Action {
    var out = ""
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement

      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)")
      stmt.executeUpdate("INSERT INTO ticks VALUES (now())")

      val rs = stmt.executeQuery("SELECT tick FROM ticks")

      while (rs.next) {
        out += "Read from DB: " + rs.getTimestamp("tick") + "\n"
      }
    } finally {
      conn.close()
    }
    Ok(out)
  }
}
