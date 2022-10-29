package me.senseiju.sennetidle

enum class Message(vararg val strings: String) {
    /**
     * Parameter errors
     */
    INVALID_PLAYER("<red>Invalid player"),
    INVALID_REAGENT("<red>Invalid reagent"),
    INVALID_UPGRADE("<red>Invalid upgrade"),
    INVALID_LEVEL("<red>Invalid level"),
    INVALID_AMOUNT("<red>Invalid amount"),

    /**
     * Reagents
     */
    REAGENT_ADDED("<green>Added <white><amount> <reagent></white> to <white><player_name>"),

    /**
     * Upgrades
     */
    UPGRADE_POINTS_ADDED("<green>Added <white><amount> upgrade points</white> to <white><player_name>"),
    UPGRADE_POINTS_TO_SPEND("<green>You have <white><unspent_points> upgrade points <green>to spend"),
    UPGRADE_EXPLOSIVE_PROCED("<red><b>|| <green>Explosive <red>||"),

    /**
     * Powers
     */
    POWER_COOLDOWN_REMAINING("<white><power> can be used in <time_remaining> seconds"),

    /**
     * Promotions
     */
    PROMOTION_UNAVAILABLE("<red>You do not meet the requirements for promotion")
}