package fr.uge.scala.project.zscala.game.prototype

import indigo.Shape
import indigo.physics.Collider
import indigo.shared.datatypes.{Circle, Fill, RGBA, Vector2}
import indigo.shared.scenegraph.SceneNode

import fr.uge.scala.project.zscala.game.Tags
import fr.uge.scala.project.zscala.game.Tags.GameEntityTag
import fr.uge.scala.project.zscala.game.entity.{BulletEntity, EntityId}

import java.util.UUID

enum BulletPrototype:
  case Bullet(
    hitbox: Circle,
    distance: Double,
    speed: Double,
    damage: Int,
    piercing: Int,
  ) extends BulletPrototype

object BulletPrototype:
  def toEntities(shooter: EntityId, proto: BulletPrototype, position: Vector2, direction: Vector2): Seq[BulletEntity] =
    proto match
      case bullet @ BulletPrototype.Bullet(_, _, _, _, piercing) =>
        Seq(BulletEntity(position, direction, bullet, shooter, 0, piercing))
