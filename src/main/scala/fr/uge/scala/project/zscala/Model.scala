package fr.uge.scala.project.zscala

import indigo.{Batch, BoundingBox, BoundingCircle, Shape, Signal, Vector2}
import indigo.physics.{Collider, World}
import indigo.shared.{FrameContext, Outcome}
import indigo.shared.datatypes.{Fill, RGBA, Rectangle, Size}
import indigo.shared.events.{FrameTick, GlobalEvent, MouseEvent}
import indigo.shared.geometry.Vertex
import indigo.shared.materials.Material
import indigo.shared.scenegraph.{Layer, SceneUpdateFragment, Text}

trait Model {
	def update(context: FrameContext[Size]): GlobalEvent => Outcome[Model]

	def present(context: FrameContext[Size]): Outcome[SceneUpdateFragment]
}
