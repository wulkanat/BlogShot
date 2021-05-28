package de.wulkanat.model

import de.wulkanat.extensions.hex2Rgb
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed

class JobListingPreview(
    val title: String,
    val department: String,
    val location: String,
    val fullListingUrl: String
) {
    fun toMessageEmbed(): MessageEmbed {
        return EmbedBuilder()
            .setTitle(this.title, this.fullListingUrl)
            .setDescription(this.department)
            .setAuthor(this.location)
            .setColor(hex2Rgb("#337fb0"))
            .build()
    }
}