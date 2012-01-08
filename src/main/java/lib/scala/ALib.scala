package org.apache.jena.atlas

object ALib {
    
  /* From http://www.scala-lang.org/node/49
   * Works by putting a delayed arg inside an object (SuspImpl) as a mutable Option.
   * When the value needed, via () or force,   
   */
  /** Delay the evaluation of an expression until it is needed. */
  def delay[A](value: => A): Susp[A] = new SuspImpl[A](value)

  /** Get the value of a delayed expression. */
  implicit def force[A](s: Susp[A]): A = s()

  /** 
   * Data type of suspended computations. (The name comes from ML.) 
   */
  abstract class Susp[+A] extends Function0[A]

  /** 
   * Implementation of suspended computations, separated from the 
   * abstract class so that the type parameter can be invariant. 
   */
  private class SuspImpl[A](lazyValue: => A) extends Susp[A] {
    private var maybeValue: Option[A] = None

    override def apply() = maybeValue match {
      case None =>
        val value = lazyValue
        maybeValue = Some(value)
        value
      case Some(value) =>
        value
    }

    override def toString() = maybeValue match {
      case None => "Susp(?)"
      case Some(value) => "Susp(" + value + ")"
    }
  }
}
