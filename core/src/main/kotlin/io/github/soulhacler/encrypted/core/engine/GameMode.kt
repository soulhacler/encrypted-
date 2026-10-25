package io.github.soulhacler.encrypted.core.engine

/** The ways to play. Limits here are enforced by [GameConfig] and shown by the setup screen. */
enum class GameMode(
    val title: String,
    val tagline: String,
    /** False for Party mode, which has no clock. */
    val timed: Boolean,
    /** False for Party mode, where keeping score is optional. */
    val competitive: Boolean,
    val minParticipants: Int,
    val maxParticipants: Int,
    val defaultRounds: Int,
    val maxRounds: Int,
    /** What a participant is called on screen ("team", "player"). */
    val participantNoun: String,
) {
    CLASSIC(
        title = "Classic",
        tagline = "Solo against the clock. Crack as many as you can.",
        timed = true,
        competitive = true,
        minParticipants = 1,
        maxParticipants = 1,
        defaultRounds = 1,
        maxRounds = 1,
        participantNoun = "player",
    ),
    TEAM_BATTLE(
        title = "Team Battle",
        tagline = "One reads aloud, teammates decode. Highest score wins.",
        timed = true,
        competitive = true,
        minParticipants = 2,
        maxParticipants = 6,
        defaultRounds = 3,
        maxRounds = 10,
        participantNoun = "team",
    ),
    PASS_AND_PLAY(
        title = "Pass & Play",
        tagline = "Everyone takes a timed turn. Pass the phone, beat your mates.",
        timed = true,
        competitive = true,
        minParticipants = 2,
        maxParticipants = 12,
        defaultRounds = 1,
        maxRounds = 5,
        participantNoun = "player",
    ),
    PARTY(
        title = "Party",
        tagline = "No clock, no pressure. Solve them together, as long as you like.",
        timed = false,
        competitive = false,
        minParticipants = 1,
        maxParticipants = 1,
        defaultRounds = 1,
        maxRounds = 1,
        participantNoun = "group",
    ),
}
