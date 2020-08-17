package de.wulkanat

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.awt.Color

class OwnerCli : ListenerAdapter() {
    private val prefix = "%!"

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val msg = event.message.contentRaw
        // Only accept admin requests
        if (event.message.member?.hasPermission(Permission.ADMINISTRATOR) != true || !msg.startsWith(prefix)) {
            return
        }

        val command = msg.removePrefix(prefix).split(Regex("\\s+"))
        val channelId = event.message.channel.idLong

        when (command.first()) {
            "add" -> {
                val result = Channels.addChannel(channelId, null)
                if (result == null) {
                    event.message.channel.sendMessage("Already added.").queue()
                } else {
                    event.message.channel.sendMessage("Added.").queue()
                    Admin.info()
                }
            }
            "remove" -> {
                val result = Channels.channels.removeAll { it.id == channelId }
                Channels.saveChannels()
                if (result) {
                    event.message.channel.sendMessage("Removed.").queue()
                } else {
                    event.message.channel.sendMessage("This channel is not registered.").queue()
                }
            }
            "publish" -> {
                val result = Channels.channels.find { it.id == channelId }
                if (result != null) {
                    if (command.size > 1 && listOf("on", "off").contains(command[1])) {
                        result.autoPublish = command[1] == "on"
                        Channels.saveChannels()

                        event.message.channel.sendMessage("Auto publish is now on").queue()
                    } else {
                        event.message.channel.sendMessage("Usage: `${prefix}publish [on|off]`")
                    }
                } else {
                    event.message.channel.sendMessage("Added.").queue()
                }
            }
            "ping" -> {
                val result = Channels.channels.find { it.id == channelId }
                if (result != null) {
                    if (command.size > 1) {
                        val roles = event.message.guild.getRolesByName(command[1], false)
                        result.mentionedRole = when {
                            command[1] == "everyone" -> {
                                event.message.channel.sendMessage("Now pinging everyone.").queue()
                                "everyone"
                            }
                            command[1] == "none" -> {
                                event.message.channel.sendMessage("Now pinging none.").queue()
                                null
                            }
                            roles.firstOrNull() != null -> {
                                event.message.channel.sendMessage("Now pinging ${roles.first().name}").queue()
                                roles.first().id
                            }
                            else -> {
                                event.message.channel.sendMessage("Unknown role.").queue()
                                result.mentionedRole
                            }
                        }
                        Channels.saveChannels()
                    } else {
                        event.message.channel.sendMessage("Usage: `${prefix}ping [everyone|none|roleName]`")
                    }
                } else {
                    event.message.channel.sendMessage("Channel is not registered.").queue()
                }
            }
            "info" -> {
                event.message.channel.sendMessage(EmbedBuilder()
                    .setTitle("Server overview")
                    .setColor(Color.GREEN)
                    .setDescription(Channels.getServerNames(event.message.guild.idLong).joinToString("\n"))
                    .setAuthor(Admin.admin?.name, Admin.admin?.avatarUrl, Admin.admin?.avatarUrl)
                    .build()).queue()
            }
            "help" -> {
                event.message.channel.sendMessage(EmbedBuilder()
                    .setTitle("Help")
                    .setColor(Color.YELLOW)
                    .setAuthor(Admin.admin?.name, Admin.admin?.avatarUrl, Admin.admin?.avatarUrl)
                    .setDescription(
                        """
                            **${prefix}add**
                            Add this channel to the notified list
                            **${prefix}remove**
                            Remove this channel to the notified list
                            **${prefix}publish [on|off]**
                            [Community|Partner|Verified only] Auto publish the message if in an announcement channel
                            **${prefix}ping [none|everyone|roleName]**
                            What role to ping
                            **${prefix}info**
                            Show an overview about all channels registered on this server
                            **${prefix}help**
                            Show this message
                        """.trimIndent())
                    .build()).queue()
            }
        }
    }
}