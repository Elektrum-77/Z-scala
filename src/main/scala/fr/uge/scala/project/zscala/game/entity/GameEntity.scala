package fr.uge.scala.project.zscala.game.entity

import indigo.physics.Collider
import indigo.shared.FrameContext
import indigo.shared.collections.Batch
import indigo.shared.datatypes.*
import indigo.shared.scenegraph.{Layer, SceneNode, Text}
import indigo.shared.time.Seconds
import indigo.{Batch, Shape, logger}
import indigo.syntax.second

import fr.uge.scala.project.zscala.{Assets, Fonts, Model}
import fr.uge.scala.project.zscala.game.GameEvent.{DeleteEntity, ShootBullet}
import fr.uge.scala.project.zscala.game.Tags.GameEntityTag
import fr.uge.scala.project.zscala.game.control.Control
import fr.uge.scala.project.zscala.game.prototype.{BulletPrototype, ZombiePrototype}
import fr.uge.scala.project.zscala.game.{GameEvent, GameModel, Tags, Weapon}

import java.lang.Math.max
import java.lang.Math.min

sealed trait GameEntity:
  def present(): SceneNode

  def collider(id: EntityId): Collider[Tags]

  def update(id: EntityId, context: FrameContext[Size], game: GameModel): (GameEntity, Seq[GameEvent])

final case class Health(
  health: Int,
  defence: Int
):
  def takeDamage(amount: Int): Option[Health] =
    val damage = max(0, amount - defence)
    if damage > health then None
    else Some(copy(health = health - damage))

final case class ZombieEntity(
  prototype: ZombiePrototype,
  position: Vector2,
  health: Health,
  targetPosition: GameModel => Vector2,
) extends GameEntity:

  override def collider(id: EntityId): Collider[Tags] =
    Collider.Box(GameEntityTag(id), prototype.hitbox.toBoundingBox)
      .moveTo(position).onCollision(c => {
        c.tag match
          case GameEntityTag(collidedEntityId) => Batch(GameEvent.EntityCollision(id, collidedEntityId))
          case _ => Batch.empty
      })

  override def present(): SceneNode =
    Shape.Box(prototype.hitbox.moveTo(position.toPoint), Fill.Color(RGBA.Red))

  def takeDamage(amount: Int): Option[ZombieEntity] =
    health.takeDamage(amount).map(h => copy(health = h))

  override def update(id: EntityId, context: FrameContext[Size], game: GameModel): (ZombieEntity, Seq[GameEvent]) =
    val positionDelta = targetPosition(game) - position
    val direction = positionDelta.normalise
    val movement = direction.scaleBy(prototype.speed).scaleBy(context.delta.toDouble)
    val nextPosition = position + movement
    (copy(position = nextPosition), Seq.empty)

object ZombieEntity:
  def fromPrototype(prototype: ZombiePrototype, position: Vector2, targetPosition: GameModel=>Vector2): ZombieEntity =
    ZombieEntity(prototype, position, Health(prototype.maxHealth, 0), targetPosition)


final case class PlayerEntity(
  control: Control,
  hitbox: Rectangle,
  position: Vector2,
  speed: Double,
  weapon: Weapon,
  health: Health,
  invincibilityTime: Seconds,
  invincibilityTimer: Seconds,
) extends GameEntity:
  override def collider(id: EntityId): Collider[Tags] =
    Collider.Box(GameEntityTag(id), hitbox.toBoundingBox)
      .moveTo(position)

  override def present(): SceneNode =
    Shape.Box(hitbox, Fill.Color(RGBA.Green))
      .moveTo(position.toPoint)

  def presentHUD(viewportSize: Size): Batch[SceneNode] =
    Batch(Text(s"""
     |Health     : ${health.health} / 100
     |Magazine   : ${weapon.magazine}/${weapon.magazineSize}
     |Ammunition : ${weapon.ammunition}/${weapon.maxAmmunition}""".stripMargin,
      2, 2, 5, Fonts.fontKey, Assets.fontMaterial)
      .alignLeft
      .moveTo(10, 10))

  def takeDamage(amount: Int): Option[PlayerEntity] =
    if invincibilityTimer <= 0.second then
      health.takeDamage(amount)
        .map(h => copy(health = h, invincibilityTimer = invincibilityTimer + invincibilityTime))
    else Some(this)

  private def updateWeapon(id: EntityId, context: FrameContext[Size]): (Weapon, Option[BulletPrototype]) =
    val inputs = context.inputState
    val nextWeapon = weapon.updateTime(context.delta)
    if control.isReloading(inputs) && control.isShooting(inputs) then nextWeapon.tryReloading().tryShooting() else
    if control.isReloading(inputs) then (nextWeapon.tryReloading(), None) else
    if control.isShooting(inputs) then nextWeapon.tryShooting()
    else (nextWeapon, None)

  override def update(id: EntityId, context: FrameContext[Size], game: GameModel): (PlayerEntity, Seq[GameEvent]) =
    val inputState = context.inputState

    val movement = control.movement(inputState).scaleBy(speed).scaleBy(context.delta.toDouble)
    val nextPosition = position + movement

    val (nextWeapon, bulletProtoOpt) = updateWeapon(id, context)
    val shootBulletEvents = bulletProtoOpt
      .map(ShootBullet(id, position, control.aimingDirection(position, inputState), _))
      .toList

    // debug information
    (nextWeapon.reloadingState, weapon.reloadingState) match
      case (true, false) => logger.debug(s"the player $id started to reload")
      case (false, true) => logger.debug(s"the player $id finished to reload")
      case _ => ()
    bulletProtoOpt match
      case Some(_) => logger.debug(s"the player $id fired a bullet")
      case _ => ()

    val nextInvincibilityTimer = if invincibilityTimer > 0.second then invincibilityTimer - context.delta else invincibilityTimer

    val events = shootBulletEvents
    (copy(position = nextPosition, weapon = nextWeapon, invincibilityTimer = nextInvincibilityTimer), events)


final case class BulletEntity(
  position: Vector2,
  direction: Vector2,
  prototype: BulletPrototype.Bullet,
  shooter: EntityId,
  distance: Double,
  piercing: Int,
) extends GameEntity:

  override def present(): SceneNode =
    Shape.Circle(prototype.hitbox.moveTo(position), Fill.Color(RGBA.White))

  override def collider(id: EntityId): Collider[Tags] =
    Collider.Circle(GameEntityTag(id), prototype.hitbox.toBoundingCircle)
      .moveTo(position)

  def hit(zombie: ZombieEntity): (Option[BulletEntity], Option[ZombieEntity]) =
    val bulletOpt = if piercing == 0 then None else Some(copy(piercing = piercing - 1))
    val zombieOpt = zombie.takeDamage(prototype.damage)
    (bulletOpt, zombieOpt)

  override def update(id: EntityId, context: FrameContext[Size], game: GameModel): (BulletEntity, Seq[GameEvent]) =
    val movement = direction.scaleBy(prototype.speed).scaleBy(context.delta.toDouble)
    val nextPosition = position + movement
    val nextDistance = distance + movement.length
    val nextEntity = copy(position = nextPosition, distance = nextDistance)
    val events = if distance >= prototype.distance then Seq(DeleteEntity(id)) else Seq.empty
    (nextEntity, events)
