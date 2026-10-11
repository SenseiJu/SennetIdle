package me.senseiju.sennetidle.idlemob

import me.senseiju.sennetidle.reagents.Reagent
import me.senseiju.sennetidle.upgrades.Upgrade
import me.senseiju.sennetidle.upgrades.upgradeData.Explosive
import me.senseiju.sennetidle.user.User
import org.bukkit.Location
import org.bukkit.entity.EntityType

class IdleMob(
    user: User,
    location: Location,
    entityType: EntityType,
    maxHealth: Long
) : AbstractIdleMob(user, location, entityType, maxHealth) {

    init {
        user.idleMob = this

        runnables.addRepeatingRunnable(1, 1) {
            val explosive = Upgrade.EXPLOSIVE.dataAs<Explosive>()
            if (explosive.shouldProc(user)) {
                currentHealth = 0
                entity.health = 0.0

                explosive.handleProc(user, location)
            }
        }
        runnables.addRepeatingRunnable(20) {
            currentHealth -= user.dps

            if (currentHealth <= 0) {
                entity.health = 0.0
            }

            updateEntityHealthVisuals()
        }
    }

    override fun onHit() {
        user.addMoney(15.0)
    }

    override fun onKill(success: Boolean) {
        if (success) {
            user.addMoney(100.0)

            Reagent.droppable.forEach {
                val data = it.asDroppable()
                if (!user.hasEnoughPromotions(it) || user.currentWave < data.waveUnlock) {
                    return
                }
                if (data.shouldDrop()) {
                    user.addReagent(
                        it,
                        data.randomAmount() * (user.currentWave.div(BOSS_WAVE_INTERVAL)).coerceAtLeast(1)
                    )
                }
            }
        }
    }
}