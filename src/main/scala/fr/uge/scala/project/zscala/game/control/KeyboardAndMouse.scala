package fr.uge.scala.project.zscala.game.control

import indigo.Key
import indigo.shared.datatypes.Vector2
import indigo.shared.events.{Combo, InputMapping, InputState};

final case class KeyboardAndMouse() extends Control {

  private def isDown(state: InputState, key: Key): Boolean = state.keyboard.keysAreDown(key)

  private def leftKeyDown(state: InputState): Double =
    if isDown(state, Key.LEFT_ARROW) || isDown(state, Key.KEY_Q) then -1 else 0

  private def rightKeyDown(state: InputState): Double =
    if isDown(state, Key.RIGHT_ARROW) || isDown(state, Key.KEY_D) then 1 else 0

  private def upKeyDown(state: InputState): Double =
    if isDown(state, Key.UP_ARROW) || isDown(state, Key.KEY_Z) then -1 else 0

  private def downKeyDown(state: InputState): Double =
    if isDown(state, Key.DOWN_ARROW) || isDown(state, Key.KEY_S) then 1 else 0

  private def xMovement(state: InputState): Double =
    leftKeyDown(state) + rightKeyDown(state)

  private def yMovement(state: InputState): Double =
    upKeyDown(state) + downKeyDown(state)

  def movement(state: InputState): Vector2 =
    Vector2(xMovement(state), yMovement(state)).normalise

  override def isShooting(state: InputState): Boolean =
    state.mouse.leftMouseIsDown

  override def isReloading(state: InputState): Boolean =
    state.keyboard.keysAreDown(Key.KEY_R)

  def aimingDirection(playerPosition: Vector2, inputState: InputState): Vector2 =
    (inputState.mouse.position.toVector - playerPosition).normalise


}
