package dev

import org.apache.jena.atlas.ALib._

object App {
  def main(args : Array[String]) : Unit = {

    val s: Susp[Int] = delay { println("evaluating..."); 3 }

    println("s     = " + s)       // show that s is unevaluated
    println("s()   = " + s())     // evaluate s
    println("s     = " + s)       // show that the value is saved
    println("2 + s = " + (2 + s)) // implicit call to force()

    println 

    val sl = delay { Some(3) }
    val sl1: Susp[Some[Int]] = sl
    val sl2: Susp[Option[Int]] = sl1   // the type is covariant
    
    println("sl2   = " + sl2)
    println("sl2() = " + sl2())
    println("sl2   = " + sl2)
    
    println("2 + sl2 = " + (2 + sl1.get))
  }
}

//import scala.actors.Actor
import scala.actors.Actor._ 

object App1
{
    def main(args : Array[String]) : Unit = {

        val fussyActor = actor {
            loop {
                receive {
                    case s: String => println("I got a String: " + s)
                    case i: Int => println("I got an Int: " + i.toString)
                    case _ => println("I have no idea what I just got.")
                }
            }
        }
        
        fussyActor ! "foo"

    }
}