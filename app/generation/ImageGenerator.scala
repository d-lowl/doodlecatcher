package generation

import com.typesafe.config.ConfigFactory
import doodle.core.{Image, Point, _}
import doodle.core.Image._
import doodle.syntax._
import doodle.jvm.Java2DCanvas._
import doodle.backend.StandardInterpreter._
import doodle.backend.Formats._
import doodle.core.PathElement.{BezierCurveTo, LineTo, MoveTo}

import scala.util.Random
/**
  * Created by dabar347 on 23/03/2017.
  */
object ImageGenerator {
  protected val r: Statistics = new Statistics()

  def main(args: Array[String]): Unit = {
//    println(ConfigFactory.load().getString("hasher.salt"))

//    circle(50).fillColor(Color.red).lineWidth(5.0).draw

    for (_ <- 1 to 10)
      doodle.draw
  }

  val DEBUG_MARKERS: Boolean = true
  private val BEZIER_SHAPE_MAX_ANGLE: Int = 30
  private val BASE_RADIUS: Int = 200
  private val LINE_WIDTH: Int = 10

  def doodle: Image = {
//    torso.lineWidth(5.0).lineColor(Color.darkGoldenrod)
    val baseColor: Color = Color.hsl(r.uniformRangeDouble(0,360).degrees,r.uniformRangeDouble(0.0,1.0).normalized,r.uniformRangeDouble(0.0,0.5).normalized)

    (torso.fillColor(baseColor) under (head.fillColor(baseColor) under face(baseColor) at(0,BASE_RADIUS*1.7))).lineColor(baseColor darken 0.1.normalized).lineWidth(LINE_WIDTH)
//    (head under face).lineWidth(5.0).lineColor(Color.darkRed)
  }

  private val TORSO_MIN_POINTS: Int = 4
  private val TORSO_MAX_POINTS: Int = 10
  private val TORSO_SYMMETRY_PROB: Double = 0.95

  def torso: Image = {
    BezierShape.get(r,r.uniformRangeInt(TORSO_MIN_POINTS,TORSO_MAX_POINTS),BASE_RADIUS,BEZIER_SHAPE_MAX_ANGLE,0.0,2.0,r.booleanBinary(TORSO_SYMMETRY_PROB), x_scale = 0.7, y_scale = 1.2)
  }

  private val HEAD_MIN_POINTS: Int = 4
  private val HEAD_MAX_POINTS: Int = 12
  private val HEAD_SYMMETRY_PROB: Double = 0.7

  def face(baseColor: Color): Image = {

    val _pupil: Image = pupil.fillColor(baseColor desaturateBy 0.1.normalized darkenBy 0.1.normalized)

    (circle(BASE_RADIUS/5).fillColor(Color.white) under _pupil).at(-BASE_RADIUS/3,BASE_RADIUS/4) on
      (circle(BASE_RADIUS/5).fillColor(Color.white) under _pupil).at(BASE_RADIUS/3,BASE_RADIUS/4) on
      mouth(BASE_RADIUS*0.4).at(0,-BASE_RADIUS*0.3)
  }

  def pupil: Image = {
    BezierShape.get(r,4,BASE_RADIUS/10,BEZIER_SHAPE_MAX_ANGLE,0.0,2.0,r.booleanBinary(HEAD_SYMMETRY_PROB))
  }

  private val MOUTH_MAX_ANGLE: Double = 60

  def mouth(radius: Double): Image = {
    val p1: Point = Point(-radius,0)
    val p2: Point = Point(radius,0)
    val cp1: Point = Point(r.uniformRangeDouble(0,radius/2),r.uniformRangeDouble(-MOUTH_MAX_ANGLE,MOUTH_MAX_ANGLE).degrees + 0.turns) + p1.toVec
    val cp2: Point = Point(r.uniformRangeDouble(0,radius/2),r.uniformRangeDouble(-MOUTH_MAX_ANGLE,MOUTH_MAX_ANGLE).degrees + 0.5.turns) + p2.toVec

    OpenPath(List(MoveTo(p1), BezierCurveTo(cp1,cp2,p2)))
  }

  def head: Image = {
    BezierShape.get(r,r.uniformRangeInt(HEAD_MIN_POINTS,HEAD_MAX_POINTS),BASE_RADIUS,BEZIER_SHAPE_MAX_ANGLE,0.0,2.0,r.booleanBinary(HEAD_SYMMETRY_PROB))
  }




}

object BezierShape{

//  private val r: Statistics = ImageGenerator.r

  def optimalCircularDistance(n: Int): Double = {
    4.0/3.0*Math.tan(Math.PI/(2.0*n))
  }

  def get(r: Statistics, n: Int, d: Int, maxAngle: Int, minMult: Double, maxMult: Double, isSymmetric: Boolean, x_scale: Double = 1.0, y_scale: Double = 1.0): Image = {
    val rate: Double = optimalCircularDistance(n)
    val alpha: Angle = (360.0/n).degrees
    var image: Image = empty

    var c_gammas: List[Int] = null
    var c_muls: List[Double] = null
    if(isSymmetric){
      val h_g::l_g = (for (_ <- 0 until n/2+1) yield r.uniformRangeInt(-maxAngle,maxAngle)).toList
      if(n%2 == 0)
        c_gammas = 0 :: l_g.dropRight(1) ::: List(0) ::: l_g.reverse.tail.map(x => -x)
      else
        c_gammas = 0 :: l_g ::: l_g.reverse.map(x => -x)

      c_muls = (for (_ <- 0 until n) yield rate * r.uniformRangeDouble(0.0,3.0)).toList
      c_muls = c_muls ::: c_muls.reverse
    }
    else {
      c_gammas = (for (_ <- 0 until n) yield r.uniformRangeInt(-45,45)).toList
      c_muls = (for (_ <- 0 until 2*n) yield rate * r.uniformRangeDouble(0.0,3.0)).toList
    }

    var list: List[PathElement] = List(MoveTo(scalePoint(Point(d,0.degrees),y_scale,x_scale)))

    for(i <- 0 until n) {
      list = list ::: List(bezierCurve(alpha, (alpha.toDegrees*i).degrees, d, c_muls((2*i)%(n*2)), c_muls((2*i+1)%(n*2)), c_gammas(i%n).degrees, c_gammas((i+1)%n).degrees, y_scale, x_scale))
//      image = bezierCurve(alpha, (alpha.toDegrees*i).degrees, d, c_muls((2*i)%(n*2)), c_muls((2*i+1)%(n*2)), c_gammas(i%n).degrees, c_gammas((i+1)%n).degrees, y_scale, x_scale) on image
    }
    ClosedPath(list).rotate(90.degrees)
//    image.rotate(90.degrees)
  }

  def scalePoint(p: Point, x_scale: Double, y_scale: Double): Point = {
    Point(p.x * x_scale, p.y * y_scale)
  }

  def bezierCurve(alpha: Angle, omega: Angle, d: Int, c_mul1: Double, c_mul2: Double, c_gamma1: Angle, c_gamma2: Angle, x_scale: Double = 1.0, y_scale: Double = 1.0): PathElement.BezierCurveTo = {
    require(c_mul1 > 0 &&
      c_mul2 > 0 &&
      c_gamma1.toDegrees >= -90 &&
      c_gamma1.toDegrees <= 90 &&
      c_gamma2.toDegrees >= -90 &&
      c_gamma2.toDegrees <= 90)

    val p1 = scalePoint(Point(d,omega),x_scale,y_scale)
    val p2 = scalePoint(Point(d,alpha+omega),x_scale,y_scale)

    val gamma1: Double = omega.toDegrees + 90.0
    val gamma2: Double = omega.toDegrees + alpha.toDegrees - 90

    val c_p1 = scalePoint(Point(d*c_mul1,gamma1.degrees+c_gamma1),x_scale,y_scale)+p1.toVec
    val c_p2 = scalePoint(Point(d*c_mul2,gamma2.degrees+c_gamma2),x_scale,y_scale)+p2.toVec

    BezierCurveTo(c_p1,c_p2,p2)
    //    val image = OpenPath(List(
//      MoveTo(p1),
//      BezierCurveTo(c_p1,c_p2,p2)
//    ))
//
//    if(ImageGenerator.DEBUG_MARKERS)
//      image on (circle(d.toDouble/20) lineColor Color.red at c_p1.toVec) on (circle(d.toDouble/20) lineColor Color.green at c_p2.toVec) on (circle(d.toDouble/20) lineColor Color.blueViolet at p1.toVec)
//    else
//      image
  }

}

class Statistics extends Random{

  def booleanBinary(p: Double): Boolean = {
    p > this.nextDouble()
  }

  def uniformRangeInt(a: Int, b: Int): Int = {
    this.nextInt(b-a) + a
  }

  def uniformRangeDouble(a: Double, b: Double): Double = {
    a + (b - a) * this.nextDouble()
  }

  def normalDistribution(mean: Double, sd: Double): Double = {
    this.nextGaussian()*sd + mean
  }
}

class ImageGenerator {

}
