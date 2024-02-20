package fr.uge.scala.project.zscala

import fr.uge.scala.project.zscala.start.StartModel
import fr.uge.scala.project.zscala.{Assets, Fonts, Model}
import indigo.*
import indigo.physics.*
import indigo.scenes.*
import indigo.shared.events.{GlobalEventError, IndigoSystemEvent}
import indigoextras.jobs.JobMarketEvent
import indigoextras.subsystems.{AssetBundleLoaderEvent, FPSCounter}
import indigoextras.ui.InputFieldChange
import fr.uge.scala.project.zscala.game.entity.ZombieEntity

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object Application extends IndigoSandbox[Size, Model]:

	val config: GameConfig =
		GameConfig.default
			.withViewport(960, 640)

	val assets: Set[AssetType] = Assets.assets
	val fonts: Set[FontInfo] = Set(Fonts.fontInfo)
	val animations: Set[Animation] = Set()
	val shaders: Set[Shader] = Set()

	def setup(
		assetCollection: AssetCollection,
		dice: Dice
	): Outcome[Startup[Size]] =
		Outcome(Startup.Success(config.viewport.size))

	def initialModel(viewportSize: Size): Outcome[Model] =
		Outcome(StartModel())

	def updateModel(
		context: FrameContext[Size],
		model: Model
	): GlobalEvent => Outcome[Model] = model.update(context)

	def present(
		context: FrameContext[Size],
		model: Model
	): Outcome[SceneUpdateFragment] = model.present(context)
