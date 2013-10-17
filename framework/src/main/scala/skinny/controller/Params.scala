package skinny.controller

import scala.language.dynamics

/**
 * Scalatra's params wrapper.
 *
 * @param underlying Scalatra's params
 */
case class Params(underlying: Map[String, Any]) extends Dynamic {

  /**
   * Enables accessing key using type-dynamic. Both of the following code is same.
   *
   * {{{
   *   params.get("userId")
   *   params.userId
   * }}}
   *
   * @param key key
   * @return value if exists
   */
  def selectDynamic(key: String): Option[Any] = underlying.get(key).map { v =>
    v match {
      case Some(v) => v
      case None => null
      case v => v
    }
  }

}