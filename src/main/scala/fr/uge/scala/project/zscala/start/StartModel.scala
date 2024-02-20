package fr.uge.scala.project.zscala.start

import fr.uge.scala.project.zscala.game.GameModel
import fr.uge.scala.project.zscala.{Assets, Fonts, Model}
import indigo.Signal
import indigo.shared.datatypes.{Size, Vector2}
import indigo.shared.events.{GlobalEvent, KeyboardEvent, MouseEvent}
import indigo.shared.scenegraph.{SceneUpdateFragment, Text}
import indigo.shared.{FrameContext, Outcome}
import indigo.syntax.second

final case class StartModel() extends Model {
	private def titleText(context: FrameContext[Size]) = Text("Infinite Zombies!", 2, 2, 5, Fonts.fontKey, Assets.fontMaterial)
		.alignCenter
		.moveTo(context.startUpData.width / 2, 30)

	private def howToPlayText(context: FrameContext[Size]) = Text("""
    Move your mouse to aim.
		Press left click to shoot.
		Press r to reload.
		Press ZQSD or arrows to move.
		""", 2, 2, 5, Fonts.fontKey, Assets.fontMaterial)
			.alignCenter
			.moveTo(context.startUpData.width / 2, 90)

	private def pressToStartText(context: FrameContext[Size]) = Text("click to start!", 2, 2, 5, Fonts.fontKey, Assets.fontMaterial
		.withAlpha(Signal.Pulse(1.second).map(p => if p then 1.0 else 0.0).at(context.running)))
		.alignCenter
		.moveTo(context.startUpData.width / 2, context.startUpData.height - 60)

	override def present(context: FrameContext[Size]): Outcome[SceneUpdateFragment] =
		Outcome(SceneUpdateFragment(titleText(context), howToPlayText(context), pressToStartText(context)))

	override def update(context: FrameContext[Size]): GlobalEvent => Outcome[Model] =
		case KeyboardEvent.KeyDown(_) => Outcome(GameModel.initial(context.startUpData))
		case MouseEvent.Click(_) => Outcome(GameModel.initial(context.startUpData))
		case _ => Outcome(this)
}
