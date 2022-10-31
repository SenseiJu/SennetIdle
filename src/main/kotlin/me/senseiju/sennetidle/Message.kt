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
    POWER_COOLDOWN_REMAINING("<light_purple><b><power></b> <red>is not ready <white>(<time_remaining> seconds remaining)"),
    POWER_ACTIVATED("<light_purple><b><power></b> <green>activated"),
    POWER_ACTIVATED_WITH_DURATION("<light_purple><b><power></b> <green>activated for <white><duration> seconds"),

    /**
     * Promotions
     */
    PROMOTION_UNAVAILABLE("<red>You do not meet the requirements for promotion")
}