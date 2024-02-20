package fr.uge.scala.project.zscala.game

import indigo.shared.datatypes.Vector2
import indigo.shared.events.GlobalEvent

import fr.uge.scala.project.zscala.game.entity.EntityId
import fr.uge.scala.project.zscala.game.prototype.{BulletPrototype, ZombiePrototype}

final case class ZombieSpawn(
	prototype: ZombiePrototype,
	position: Vector2,
)

enum GameEvent extends GlobalEvent:
	case DeleteEntity(id: EntityId) extends GameEvent
	case SpawnZombies(zombies: Seq[ZombieSpawn]) extends GameEvent
	case ShootBullet(from: EntityId, position: Vector2, direction: Vector2, bulletProto: BulletPrototype) extends GameEvent
	case EntityCollision(from: EntityId, to: EntityId) extends GameEvent
