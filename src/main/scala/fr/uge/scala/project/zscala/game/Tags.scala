package fr.uge.scala.project.zscala.game

import fr.uge.scala.project.zscala.game.entity.{EntityId, GameEntity}

import java.util.UUID

enum Tags:
	case GameEntityTag(id: EntityId) extends Tags
