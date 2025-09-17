package com.example.routing


import com.example.Car
import com.example.CarResponse
import com.example.repositories.CarRepository
import com.example.Services.CarValidationService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.HttpStatusCode

fun Route.carRoutes() {
    route("/cars") {
        authenticate("auth-jwt") {
            get {
                try {
                    val cars = CarRepository.findAll()
                    call.respond(CarResponse(success = true, cars = cars))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        CarResponse(success = false, message = "Ошибка при получении списка машин")
                    )
                }
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        CarResponse(success = false, message = "Неверный ID")
                    )
                    return@get
                }

                try {
                    val car = CarRepository.findById(id)
                    if (car != null) {
                        call.respond(CarResponse(success = true, car = car))
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            CarResponse(success = false, message = "Машина не найдена")
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        CarResponse(success = false, message = "Ошибка при поиске машины")
                    )
                }
            }

            post {
                try {
                    val car = call.receive<Car>()

                    // Валидация данных
                    val (isValid, errorMessage) = CarValidationService.validateCar(
                        car.brand, car.model, car.licensePlate
                    )

                    if (!isValid) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            CarResponse(success = false, message = errorMessage)
                        )
                        return@post
                    }

                    // Проверка уникальности госномера
                    if (CarRepository.findByLicensePlate(car.licensePlate) != null) {
                        call.respond(
                            HttpStatusCode.Conflict,
                            CarResponse(success = false, message = "Машина с таким госномером уже существует")
                        )
                        return@post
                    }

                    val createdCar = CarRepository.create(car)
                    call.respond(
                        HttpStatusCode.Created,
                        CarResponse(success = true, car = createdCar)
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        CarResponse(success = false, message = "Ошибка при создании машины")
                    )
                }
            }

            put("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        CarResponse(success = false, message = "Неверный ID")
                    )
                    return@put
                }

                try {
                    val car = call.receive<Car>()

                    // Валидация данных
                    val (isValid, errorMessage) = CarValidationService.validateCar(
                        car.brand, car.model, car.licensePlate
                    )

                    if (!isValid) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            CarResponse(success = false, message = errorMessage)
                        )
                        return@put
                    }

                    // Проверка уникальности госномера (исключая текущую запись)
                    val existingCar = CarRepository.findByLicensePlate(car.licensePlate)
                    if (existingCar != null && existingCar.id != id) {
                        call.respond(
                            HttpStatusCode.Conflict,
                            CarResponse(success = false, message = "Машина с таким госномером уже существует")
                        )
                        return@put
                    }

                    val updatedCar = CarRepository.update(id, car)
                    if (updatedCar != null) {
                        call.respond(CarResponse(success = true, car = updatedCar))
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            CarResponse(success = false, message = "Машина не найдена")
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        CarResponse(success = false, message = "Ошибка при обновлении машины")
                    )
                }
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        CarResponse(success = false, message = "Неверный ID")
                    )
                    return@delete
                }

                try {
                    if (CarRepository.delete(id)) {
                        call.respond(CarResponse(success = true, message = "Машина удалена успешно"))
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            CarResponse(success = false, message = "Машина не найдена")
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        CarResponse(success = false, message = "Ошибка при удалении машины")
                    )
                }
            }
        }
    }
}