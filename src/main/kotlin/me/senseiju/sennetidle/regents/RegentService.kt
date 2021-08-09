package me.senseiju.sennetidle.regents

import me.senseiju.sennetidle.SennetIdle
import me.senseiju.sentils.service.Service
import java.io.File

class RegentService(private val plugin: SennetIdle) : Service() {
    private var regents = HashMap<String, Regent>()

    private fun loadRegents() {
        val newRegents = HashMap<String, Regent>()

        File(plugin.dataFolder, "regents/")
    }
}