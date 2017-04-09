package generation

import com.roundeights.hasher.Digest
import com.roundeights.hasher.Implicits._
import com.typesafe.config.ConfigFactory
import generation.Hasher.SALT

/**
  * Created by dabar347 on 27/03/2017.
  */

//SHA1
//500,000 (~8.5 min)
//Vector(468618,    29404,   1858,   115,   4,    1,   0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
//
//SHA512
//15,000,000 (~4.16 h)
//Vector(14061612,  879629,  55087,  3430,  228,  12,  2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
//
//150,000,000 (~41.6 h)
//Vector(140621182, 8792557, 549477, 34467, 2176, 133, 7, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
object Hasher {

  val SALT: String = ConfigFactory.load().getString("application.secret")

  def main(args: Array[String]): Unit = {
//    val lon: Int = 10
//    val lat: Int = 25
//
//    val difficulties: Array[Int] = new Array[Int](41)
//
//    var t = System.currentTimeMillis()
//    val max = Int.MaxValue
//    for(i <- 0 until max) {
//      if(i % 1000000 == 0)
//        println(""+i+"/"+max)
//      val d = getDifficulty(hash(lat,lon,t).bytes)
////      println("Difficulty: "+d)
//      difficulties(d) += 1
//      t += 1
//    }
//
//    println("============")
//    println(difficulties.to)
  }

}

class Hasher(lat: Int, lon: Int) {
  private val (digest,time): (Digest,Long) = hash(lat,lon)

  private def hash(lat: Int, lon: Int, time: Long): (Digest,Long) = {
    require(lat <= 90 && lat >= -90)//1 byte
    require(lon <= 180 && lat >= -180)//2 bytes

    import scala.math.BigInt

    val input: Array[Byte] = new Array[Byte](11)

    input(0) = lat.toByte

    val _lon = BigInt(lon).toByteArray
    for ((v,i) <- _lon.zipWithIndex) input(1+(2-_lon.length)+i) = v

    val _millis: Array[Byte] = BigInt(time).toByteArray
    for ((v,i) <- _millis.zipWithIndex) input(3+(8-_millis.length)+i) = v

    val d = input.salt(SALT).sha512

    (d,time)
  }

  private def hash(lat: Int, lon: Int): (Digest,Long) = {
    hash(lat,lon,System.currentTimeMillis())
  }

  def getSeed(): Long = {
    var seed: Long = 0
    val SIZE = 8

    for(i <- digest.bytes.length - SIZE until digest.bytes.length) {
      seed = (seed << SIZE) + (digest.bytes(i) & 0xFF)
    }

    seed
  }

  def getTime(): Long = time

  def getDifficulty(): Int = {
    for((v,k) <- digest.bytes.zipWithIndex){
      if(v != 0)
        if((v >> 4) == 0)
          return k*2 + 1
        else
          return k*2

    }

    40
  }

  override def toString: String = digest.hex
}
