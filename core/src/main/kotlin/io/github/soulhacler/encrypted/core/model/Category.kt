package io.github.soulhacler.encrypted.core.model

import kotlinx.serialization.Serializable

/**
 * A puzzle category. Built-in categories live in [Categories]; packs can define their own,
 * which is how future custom or seasonal categories plug in without code changes.
 */
@Serializable
data class Category(
    val id: String,
    val name: String,
    /** A short symbol shown on the category tile. */
    val icon: String = "",
)

object Categories {
    val EVERYDAY = Category("everyday", "Everyday Phrases", "💬")
    val FILMS_TV = Category("films_tv", "Films & TV", "🎬")
    val MUSIC = Category("music", "Music", "🎵")
    val PEOPLE = Category("people", "Famous People", "⭐")
    val FOOD = Category("food", "Food & Drink", "🍔")
    val PLACES = Category("places", "Places", "🌍")
    val SPORTS = Category("sports", "Sports", "🏆")
    val ANIMALS = Category("animals", "Animals", "🐾")

    /** Not a real category: draws from every unlocked puzzle. */
    val RANDOM_MIX = Category("random_mix", "Random Mix", "🎲")

    val builtIn: List<Category> =
        listOf(EVERYDAY, FILMS_TV, MUSIC, PEOPLE, FOOD, PLACES, SPORTS, ANIMALS)
}
