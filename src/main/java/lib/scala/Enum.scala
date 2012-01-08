package org.apache.jena.atlas

/** Enum: pattern: 
 * 
<pre> 
sealed trait Currency extends Currency.Value
object Currency extends Enum[Currency]
case object EUR extends Currency
case object GBP extends Currency
case object USD extends Currency
</pre>

<pre> 
sealed class Currency(val symbol:String) extends Currency.Value
object Currency extends Enum[Currency]
case object EUR extends Currency("€")
case object GBP extends Currency("£")
case object USD extends Currency("$")
</pre> 

 * and iteration over the defined objects is possible.  
 */

trait Enum[A] {
  trait Value { self: A =>
    _values :+= this
  }
  private var _values = List.empty[A]
  def values = _values
}


/* Alterative:
public object Currency extends Enumeration {
   val GBP = Value("GBP")
   val EUR = Value("EUR") //etc
} 
*/
