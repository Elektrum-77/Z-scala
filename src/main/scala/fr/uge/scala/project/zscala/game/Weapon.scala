package fr.uge.scala.project.zscala.game

import indigo.shared.time.Seconds
import indigo.syntax.second

import fr.uge.scala.project.zscala.game.prototype.BulletPrototype

final case class Weapon(
	reloadingTime: Seconds,
	fireTime: Seconds,
	spread: Double,
	magazineSize: Int,
	maxAmmunition: Int,

	burstTime: Seconds,
	burstCount: Int,

	bulletPrototype: BulletPrototype,

	reloadingState: Boolean,
	reloadingTimer: Seconds,
	fireTimer: Seconds,
	burst: Int,
	magazine: Int,
	ammunition: Int,
) {

	def cancelReload(): Weapon =
		copy(reloadingState = false, reloadingTimer = reloadingTime)

	def restockAmmunition(): Weapon =
		cancelReload().copy()

	private def missingMagazine(): Int = magazineSize - magazine

	private def reload(): Weapon =
		val newAmmunition = Math.max(ammunition - missingMagazine(), 0)
		copy(magazine = magazineSize, ammunition = newAmmunition, reloadingState = false)

	def updateTime(timeDelta: Seconds): Weapon =
		if reloadingState && reloadingTimer <= 0.second then reload() else
		if reloadingState then copy(reloadingTimer = reloadingTimer - timeDelta) else
		if fireTimer > 0.second then copy(fireTimer = fireTimer - timeDelta) else
		this

	private def rawShot(fireTimer: Seconds, burst: Int): Weapon =
		copy(fireTimer = fireTimer, burst = burst, magazine = magazine - 1)

	private def burstShot(): Weapon = rawShot(burstTime, burst - 1)
	private def finalShot(): Weapon = rawShot(fireTime, burstCount)

	def tryShooting(): (Weapon, Option[BulletPrototype]) =
		if fireTimer > 0.second then (this, None)
		else if magazine == 0 then (this, None)
		else if burst > 0 then (burstShot(), Some(bulletPrototype))
		else (finalShot(), Some(bulletPrototype))

	def tryReloading(): Weapon =
		if magazine >= magazineSize then this
		else if reloadingState then this
		else copy(reloadingState = true, reloadingTimer = reloadingTimer + reloadingTime)
}
