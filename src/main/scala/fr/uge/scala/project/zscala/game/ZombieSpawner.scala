package fr.uge.scala.project.zscala.game

import indigo.{Layer, Text}
import indigo.shared.FrameContext
import indigo.shared.collections.Batch
import indigo.shared.datatypes.{Size, Vector2}
import indigo.shared.scenegraph.SceneNode
import indigo.shared.time.Seconds
import indigo.syntax.second

import fr.uge.scala.project.zscala.{Assets, Fonts}
import fr.uge.scala.project.zscala.game.entity.ZombieEntity
import fr.uge.scala.project.zscala.game.prototype.ZombiePrototype

import scala.util.Random

final case class ZombieSpawner(
  zombiePrototype: ZombiePrototype,
  waveSize: Int,
  waveTimer: Seconds,
  waveTime: Seconds,
  spawnRadiusMin: Double,
  spawnRadiusMax: Double,
  spawnCenter: Vector2,
  random: Random
):
  private def spawn(): ZombieSpawn =
    val angle = random.between(0, Math.TAU)
    val distance = random.between(spawnRadiusMin, spawnRadiusMax)
    val position = spawnCenter + Vector2(Math.cos(angle), -Math.sin(angle)).scaleBy(distance)
    ZombieSpawn(zombiePrototype, position)

  private def spawnWave(): GameEvent.SpawnZombies = GameEvent.SpawnZombies(Seq.fill(waveSize)(spawn()))

  private def advanceTimer(delta: Seconds): ZombieSpawner = copy(waveTimer = waveTimer - delta)

  private def resetTimer(): ZombieSpawner  = copy(waveTimer = waveTimer + waveTime)

  def update(context: FrameContext[Size]): (ZombieSpawner, Option[GameEvent.SpawnZombies]) =
    if waveTimer < 0.second then (resetTimer(), Some(spawnWave())) else
    (advanceTimer(context.delta), None)

  def presentHUD(viewportSize: Size): Batch[SceneNode] =
    Batch(Text(s"Next wave in ${waveTimer.toInt}",
      2, 2, 5, Fonts.fontKey, Assets.fontMaterial)
      .alignLeft.scaleBy(Vector2.one.scaleBy(0.5))
      .moveTo(10, viewportSize.height - 30))
