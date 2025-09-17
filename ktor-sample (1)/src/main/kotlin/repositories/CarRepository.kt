package com.example.repositories

import com.example.Car
import com.example.Cars
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object CarRepository {

    fun findAll(): List<Car> = transaction {
        Cars.selectAll().map { toCar(it) }
    }

    fun findById(id: Int): Car? = transaction {
        Cars.select { Cars.id eq id }.singleOrNull()?.let { toCar(it) }
    }

    fun findByLicensePlate(licensePlate: String): Car? = transaction {
        Cars.select { Cars.licensePlate eq licensePlate }.singleOrNull()?.let { toCar(it) }
    }

    fun create(car: Car): Car = transaction {
        val id = Cars.insert {
            it[brand] = car.brand
            it[model] = car.model
            it[licensePlate] = car.licensePlate
        } get Cars.id

        findById(id)!!
    }

    fun update(id: Int, car: Car): Car? = transaction {
        val updatedRows = Cars.update({ Cars.id eq id }) {
            it[brand] = car.brand
            it[model] = car.model
            it[licensePlate] = car.licensePlate
        }

        if (updatedRows > 0) findById(id) else null
    }

    fun delete(id: Int): Boolean = transaction {
        Cars.deleteWhere { Cars.id eq id } > 0
    }

    private fun toCar(row: ResultRow): Car {
        return Car(
            id = row[Cars.id],
            brand = row[Cars.brand],
            model = row[Cars.model],
            licensePlate = row[Cars.licensePlate]
        )
    }
}