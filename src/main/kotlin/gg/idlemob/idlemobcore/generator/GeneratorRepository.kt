package gg.idlemob.idlemobcore.generator

import gg.idlemob.idlemobcore.framework.database.Database
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import java.sql.SQLIntegrityConstraintViolationException
import java.util.*

class DuplicateBlueprintException : RuntimeException("Blueprint is already in a generator")

class GeneratorRepository(private val database: Database) {

    suspend fun insert(user: UUID, state: GeneratorState) {
        try {
            database.query {
                UserGeneratorsTable.insert {
                    it[userUuid] = user.toString()
                    it[slot] = state.slot
                    it[blueprint] = state.blueprint
                    it[blueprintUuid] = state.blueprintUuid.toString()
                    it[uses] = state.uses
                    it[progressMs] = state.progressMs
                }
            }
        } catch (ex: Exception) {
            if (generateSequence<Throwable>(ex) { it.cause }.any { it is SQLIntegrityConstraintViolationException }) {
                throw DuplicateBlueprintException()
            }
            throw ex
        }
    }

    suspend fun delete(user: UUID, state: GeneratorState): Boolean = database.query {
        UserGeneratorsTable.deleteWhere {
            (userUuid eq user.toString()) and (slot eq state.slot) and (blueprintUuid eq state.blueprintUuid.toString())
        } > 0
    }
}
