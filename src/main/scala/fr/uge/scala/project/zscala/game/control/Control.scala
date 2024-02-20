package fr.uge.scala.project.zscala.game.control

import indigo.shared.datatypes.Vector2
import indigo.shared.events.{InputMapping, InputState}

trait Control {

  /**
   * @return a normalized vector 2d
   */
  def movement(inputState: InputState): Vector2

  def isShooting(inputState: InputState): Boolean

  def isReloading(inputState: InputState): Boolean

  /**
   * @return a normalized vector 2d
   */
  def aimingDirection(playerPosition: Vector2, inputState: InputState): Vector2

}
