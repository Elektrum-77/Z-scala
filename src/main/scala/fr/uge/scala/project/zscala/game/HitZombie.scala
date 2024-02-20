package fr.uge.scala.project.zscala.game

import indigo.shared.events.GlobalEvent
import fr.uge.scala.project.zscala.game.entity.ZombieEntity

final case class HitZombie(zombie: ZombieEntity) extends GlobalEvent
