package fr.uge.scala.project.zscala.game.prototype

import indigo.shared.datatypes.Rectangle

import fr.uge.scala.project.zscala.game.entity.Health

final case class ZombiePrototype(
  hitbox: Rectangle,
  maxHealth: Int,
  damage: Int,
  speed: Double
)
