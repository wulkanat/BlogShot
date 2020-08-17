package de.wulkanat

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import java.awt.Color

object Admin {
    val userId: Long
    val token: String
    val updateMs: Long

    var testModeEnabled: Boolean = false
    set(value) {
        if (field == value)
            return

        field = value

        if (value) {
            jda?.presence?.setPresence(Activity.of(Activity.ActivityType.DEFAULT, "Testing mode, hold on..."), true)
        } else {
            jda?.presence?.setPresence(Activity.watching("for new Blogposts"), false)
        }

        Channels.channels = Channels.refreshFromDisk()
        Admin.info()
    }

    init {
        val admin = Json(JsonConfiguration.Stable).parse(AdminFile.serializer(), ADMIN_FILE.readText())
        userId = admin.adminId
        token = admin.token
        updateMs = admin.updateMs
    }

    var jda: JDA? = null
    set(value) {
        field = value

        admin = value?.retrieveUserById(userId)?.complete()
        if (admin == null) {
            kotlin.io.println("Connection to de.wulkanat.Admin failed!")
        } else {
            kotlin.io.println("Connected to ${admin!!.name}. No further errors will be printed here.")
        }
    }
    var admin: User? = null

    fun println(msg: String) {
        sendDevMessage(
            EmbedBuilder()
                .setTitle(msg)
                .setColor(Color.WHITE)
                .build(),
            msg
        )
    }

    fun printlnBlocking(msg: String) {
        senDevMessageBlocking(
            EmbedBuilder()
                .setTitle(msg)
                .setColor(Color.WHITE)
                .build(),
            msg
        )
    }

    fun error(msg: String, error: String, author: User? = null) {
        sendDevMessage(
            EmbedBuilder()
                .setTitle(msg)
                .setDescription(error)
                .setColor(Color.RED)
                .run {
                    if (author == null) {
                        this
                    } else {
                        this.setAuthor(author.asTag, author.avatarUrl, author.avatarUrl)
                    }
                }
                .build()
            , "$msg\n\n${error}"
        )
    }

    fun errorBlocking(msg: String, error: Exception) {
        senDevMessageBlocking(
            EmbedBuilder()
                .setTitle(msg)
                .setDescription(error.message)
                .setColor(Color.RED)
                .build()
            , "$msg\n\n${error.message}"
        )
    }

    fun warning(msg: String) {
        sendDevMessage(
            EmbedBuilder()
                .setTitle(msg)
                .setColor(Color.YELLOW)
                .build(),
            msg
        )
    }

    fun info() {
        sendDevMessage(
            EmbedBuilder()
                .setTitle("Now watching for new Hytale Blogposts every ${updateMs / 1000}s")
                .setDescription(Channels.getServerNames().joinToString("\n"))
                .setColor(Color.GREEN)
                .build(),
            "Now watching for new Hytale BlogPosts"
        )
    }

    fun silent(msg: String) {
        kotlin.io.println(msg)
    }

    private fun senDevMessageBlocking(messageEmbed: MessageEmbed, fallback: String) {
        admin = jda!!.retrieveUserById(userId).complete()
        val devChannel = admin?.openPrivateChannel() ?: kotlin.run {
            kotlin.io.println(fallback)
            return
        }

        devChannel.complete()
            .sendMessage(messageEmbed).complete()
    }

    private fun sendDevMessage(messageEmbed: MessageEmbed, fallback: String) {
        val devChannel = admin?.openPrivateChannel() ?: kotlin.run {
            kotlin.io.println(fallback)
            return
        }

        devChannel.queue {
            it.sendMessage(messageEmbed).queue()
        }
    }
}