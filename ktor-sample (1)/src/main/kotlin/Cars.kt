package com.example

import org.jetbrains.exposed.sql.Table


object Cars : Table("cars") {
    val id = integer("id").autoIncrement()
    val brand = varchar("brand", 50)
    val model = varchar("model", 50)
    val licensePlate = varchar("license_plate", 15).uniqueIndex()

    override val primaryKey = PrimaryKey(id)
}
