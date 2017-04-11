package controllers

import java.io.File

import com.typesafe.config.ConfigFactory
import play.api.Logger

//import com.roundeights.hasher.Digest
import doodle.core.Image
//import doodle.core.Image._
import doodle.syntax._
import doodle.jvm.Java2DCanvas._
import doodle.backend.StandardInterpreter._
import doodle.backend.Formats._
import generation._
//import play.api._
import play.api.mvc._
//import play.api.cache.Cache
import play.api.Play.current

import play.api.db._

object Application extends Controller {

  private def absolutePath(path: String = ""): String = new java.io.File(".").getCanonicalPath + "/" + path

  def index = Action {
    Ok(views.html.index(null))
  }

  def hash(lat: Int, lon: Int) = Action {
    val config = ConfigFactory.load()
    val hasher: Hasher = new Hasher(lat,lon)
    println(absolutePath("public/temp/"+hasher.getSeed()+".png"))
    Logger.logger.debug(absolutePath("public/temp/"+hasher.getSeed()+".png"))
    if(config.getInt("game.difficulty") <= hasher.getDifficulty) {
      val doodle: Image = ImageGenerator.doodle(hasher.getSeed())
      doodle.save[Png](absolutePath("public/temp/"+hasher.getSeed()+".png"))
      Thread.sleep(200)
//      import java.nio.file.{Paths, Files}
//      while(!Files.exists(Paths.get("public/temp/"+hasher.getSeed()+".png"))) {
//        Thread.sleep(25)
//      }
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
