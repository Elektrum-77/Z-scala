package fr.uge.scala.project.zscala.game

import indigo.*
import indigo.physics.World
import indigo.shared.datatypes.{Rectangle, Size}
import indigo.shared.events.{FrameTick, GlobalEvent}
import indigo.shared.scenegraph.{Layer, SceneUpdateFragment}
import indigo.shared.{FrameContext, Outcome}
import indigo.syntax.second

import fr.uge.scala.project.zscala.*
import fr.uge.scala.project.zscala.game.GameModel.nextId
import fr.uge.scala.project.zscala.game.control.KeyboardAndMouse
import fr.uge.scala.project.zscala.game.entity.*
import fr.uge.scala.project.zscala.game.prototype.BulletPrototype.{Bullet, toEntities}
import fr.uge.scala.project.zscala.game.prototype.{BulletPrototype, ZombiePrototype}
import fr.uge.scala.project.zscala.start.LostModel

import scala.util.Random

final case class GameModel(
  entities: Map[EntityId, GameEntity],
  playerId: EntityId,
  zombieSpawner: ZombieSpawner,
  world: World[Tags],
) extends Model {

  override def present(context: FrameContext[Size]): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Layer(Batch.toBatch(entities.values.map(_.present()).toArray)),
        Layer(player.presentHUD(context.startUpData) ++ zombieSpawner.presentHUD(context.startUpData))
      )
    )

  // AAAAAAH !!!!!
  private def player: PlayerEntity = entities(playerId).asInstanceOf[PlayerEntity]

  private def updateEntity(id: EntityId, opt: Option[GameEntity]): GameModel =
    opt match
      case Some(e) => copy(entities = entities updated (id, e))
      case _ =>
        logger.debug(s"remove entity $id")
        copy(entities = entities removed id)

  private def updateEntities(entities: Map[EntityId, GameEntity]): GameModel =
    copy(entities = this.entities concat entities)

  private def addEntities(entities: Seq[GameEntity]): GameModel =
    updateEntities(Map.from(entities.map((nextId(), _))))

  private def zombieHitPlayer(
    player: PlayerEntity,
    playerId: EntityId,
    zombie: ZombieEntity,
    zombieId: EntityId): Model =
    val playerOpt = player.takeDamage(zombie.prototype.damage)
    playerOpt match
      case Some(_) =>
        logger.debug(s"player $playerId updated")
        updateEntity(playerId, playerOpt)
      case None =>
        logger.debug(s"player $playerId died")
        LostModel()


  private def bulletHitZombie(
    bullet: BulletEntity,
    bulletId: EntityId,
    zombie: ZombieEntity,
    zombieId: EntityId): GameModel =
    logger.debug(s"zombie $zombieId was damaged by a bullet fired by player ${bullet.shooter}")
    val (newBulletOpt, newZombieOpt) = bullet.hit(zombie)
    // debug information
    newZombieOpt match
      case Some(z) => logger.debug(s"zombie $zombieId health reduced to${z.health.health}")
      case None =>logger.debug(s"zombie $zombieId killed")

    updateEntity(zombieId, newZombieOpt).updateEntity(bulletId, newBulletOpt)

  private def processCollision(fromId: EntityId, toId: EntityId): Option[Model] =
    (entities.get(fromId), entities.get(toId)) match
      case (Some(_), None) =>
        logger.error(s"collision between entity $fromId with unknown entity $toId");
        None
      case (
        Some(player: PlayerEntity),
        Some(zombie: ZombieEntity)
        ) => Some(zombieHitPlayer(player, fromId, zombie, toId))
      case (
        Some(zombie: ZombieEntity),
        Some(bullet: BulletEntity)
        ) => Some(bulletHitZombie(bullet, toId, zombie, fromId))
      case _ => None

  override def update(context: FrameContext[Size]): GlobalEvent => Outcome[Model] =
    case FrameTick =>

      val nextEntities = entities.map((id, entity) => (id, entity.update(id, context, this)))
      val (nextSpawner, spawnEventOpt) = zombieSpawner.update(context)
      val events = nextEntities.flatMap((id, u) => u._2) ++ spawnEventOpt

      val colliders = nextEntities.map((id, u) => u._1.collider(id))
      World.empty
        .addColliders(Batch.fromIterator(colliders.iterator))
        .update(0.second)
        .map(updatedWorld => copy(
          world = updatedWorld,
          entities = nextEntities.map((id, u) => (id, u._1)),
          zombieSpawner = nextSpawner,
        ))
        .addGlobalEvents(Batch.fromIterator(events.iterator))

    case GameEvent.EntityCollision(fromId: EntityId, toId: EntityId) =>
      val from = entities.get(fromId)
      val to = entities.get(toId)
      Outcome(processCollision(fromId, toId).orElse(processCollision(toId, fromId)).getOrElse(this))

    case GameEvent.ShootBullet(fromId: EntityId, position: Vector2, direction: Vector2, bulletProto: BulletPrototype) =>
      // val from = entities.get(fromId)
      // TODO reward player here maybe
      // TODO what if not a player ???
      val bulletEntities = BulletPrototype.toEntities(fromId, bulletProto, position, direction)
      Outcome(updateEntities(Map.from(bulletEntities.map((nextId(), _)))))

    case GameEvent.SpawnZombies(spawns: Seq[ZombieSpawn]) =>
      logger.debug(s"spawning ${spawns.size} zombies")
      Outcome(addEntities(spawns.map(spawn => ZombieEntity.fromPrototype(
        prototype = spawn.prototype,
        position = spawn.position,
        targetPosition = game => game.player.position))))

    case GameEvent.DeleteEntity(id) =>
      logger.debug(s"removing entity $id")
      Outcome(updateEntity(id, None))

    case _ => Outcome(this)
}

object GameModel:
  private val playerHitbox: Rectangle = Rectangle(8,24)

  private val zombiePrototype: ZombiePrototype = ZombiePrototype(
    hitbox = playerHitbox,
    maxHealth = 100,
    damage = 10,
    speed = 80,
  )

  private val initialWeaponBulletPrototype: BulletPrototype = Bullet(
    hitbox = Circle(0, 0, 10),
    distance = 300,
    speed = 500,
    damage = 20,
    piercing = 0,
  )

  private val initialWeapon: Weapon = Weapon(
    reloadingTime = 1.second,
    fireTime = 0.5.second,
    spread = 0.05,
    magazineSize = 10,
    maxAmmunition = 100,
    burstTime = 0.second,
    burstCount = 0,
    bulletPrototype = initialWeaponBulletPrototype,
    reloadingState = false,
    reloadingTimer = 0.second,
    fireTimer = 0.second,
    burst = 0,
    magazine = 10,
    ammunition = 100,
  )

  private def middleOfScreen(viewportSize: Size): Vector2 = viewportSize.toVector.scaleBy(.5)

  def initial(viewportSize: Size): GameModel =
    val start = (viewportSize / 2).toVertex
    val playerId = nextId()
    val player = PlayerEntity(
      control = KeyboardAndMouse(),
      hitbox = playerHitbox,
      position = middleOfScreen(viewportSize),
      speed = 100,
      weapon = initialWeapon,
      health = Health(100, 0),
      invincibilityTime = 1.second,
      invincibilityTimer = 0.second,
    )

    GameModel(
      entities = Map((playerId, player)),
      playerId = playerId,
      zombieSpawner = ZombieSpawner(
        zombiePrototype = zombiePrototype,
        waveSize = 10,
        waveTimer = 10.second,
        waveTime = 10.second,
        spawnRadiusMin = 200,
        spawnRadiusMax = 250,
        spawnCenter = middleOfScreen(viewportSize),
        random = new Random()),
      world = World.empty[Tags],
    )

  var id: EntityId = EntityId(0)

  /**
   * Not Functional Programming
   * <p>
   * Returns an unused entity id and increment internally for the next id
   */
  def nextId(): EntityId =
    val result = id
    id = EntityId(id.value + 1)
    result
