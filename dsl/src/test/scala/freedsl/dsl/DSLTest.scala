/**
  * Created by Romain Reuillon on 01/11/16.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
package freedsl.dsl

import cats._
import cats.data._
import cats.free.Free
import freek._
import cats.implicits._
import scala.language.experimental.macros

object PureFreek extends App {

  sealed trait Instruction[T]
  final case class Get() extends Instruction[Int]
  final case class GetSet() extends Instruction[Set[Int]]
  final case class GetMap() extends Instruction[Map[Int, Int]]

  type DSL = Instruction :|: NilDSL
  type O = Option :&: Bulb

  val prog =
    for {
      i <- Get().freek[DSL].onionT[O]
      j <- GetSet().freek[DSL].onion[O]
      k<- GetMap().freek[DSL].onion[O]
    } yield (i, j, k)


  def interpreter = new (Instruction ~> Option) {
    def apply[A](a: Instruction[A]) = a match {
      case Get() => Some(1)
      case GetSet() => Some(Set(1))
      case GetMap() => Some(Map(1 -> 1))
    }
  }

  println(prog.value.interpret(interpreter))
}


object PureFreek2 extends App {

  sealed trait Instruction[T]
  final case class Get() extends Instruction[Option[Int]]
  final case class GetSet() extends Instruction[Option[Set[Int]]]
  final case class GetMap() extends Instruction[Option[Map[Int, Int]]]
  final case class GetMap2() extends Instruction[Ior[String, Map[Int, Int]]]


  type DSL = Instruction :|: NilDSL
  type O = Ior[String, ?] :&: Option :&: Bulb

  val prog =
    for {
      i <- Get().freek[DSL].onionX1[O]
      j <- GetSet().freek[DSL].onionX1[O]
      k <- GetMap().freek[DSL].onionX1[O]
      l <- GetMap2().freek[DSL].onionX1[O]
    } yield (i, j, k, l)

  def interpreter = new (Instruction ~> Id) {
    def apply[A](a: Instruction[A]) = a match {
      case Get() => Some(1)
      case GetSet() => Some(Set(1))
      case GetMap() => Some(Map(1 -> 1))
      case GetMap2() => Ior.right(Map(8 -> 9))
    }
  }

  println(prog.value.interpret(interpreter))
}


object DSLTest extends App {

  object DSLTestM {
    def interpreter = new Interpreter[Id] {
      def interpret[_] = {
        case get() => Right(1)
        case getSet() => Right(Set(1))
         case option() => Right(Some("cool"))
      }
    }

    case class FileNotFound(s: String) extends Error
    case class ItFailed(s: String) extends Error
  }

  @dsl trait DSLTestM[M[_]] {
    def get: M[Int]
    def getSet: M[Set[Int]]
    def option: M[Option[String]]
  }


  def prg[M[_]: Monad](implicit dSLTestM: DSLTestM[M]) =
    for {
      i <- dSLTestM.get
      j <- dSLTestM.getSet
      k <- dSLTestM.get
      o <- dSLTestM.option
    } yield (i, j, k, o)


  type I = DSLTestM.I :|: freek.NilDSL
  type O = DSLTestM.O :&: Bulb

  val DSLInstance = freek.DSL.Make[I]
  type Context[T] = freek.OnionT[cats.free.Free, DSLInstance.Cop, O, T]

  implicit def dslTestImplicit = dslImpl[DSLTestM, I, O]

  println(prg[Context].value.interpret(DSLTestM.interpreter))
}

