package com.example.Services

import java.util.regex.Pattern

object CarValidationService {
    // Регулярное выражение для российских госномеров
    private val LICENSE_PLATE_PATTERN = Pattern.compile(
        "^[АВЕКМНОРСТУХ]\\d{3}(?<!000)[АВЕКМНОРСТУХ]{2}\\d{2,3}\$",
        Pattern.UNICODE_CASE
    )

    fun validateLicensePlate(licensePlate: String): Boolean {
        return LICENSE_PLATE_PATTERN.matcher(licensePlate).matches()
    }

    fun validateCar(brand: String, model: String, licensePlate: String): Pair<Boolean, String?> {
        if (brand.isBlank()) {
            return false to "Марка не может быть пустой"
        }
        if (model.isBlank()) {
            return false to "Модель не может быть пустой"
        }
        if (!validateLicensePlate(licensePlate)) {
            return false to "Неверный формат госномера. Пример: А123УЕ77"
        }
        return true to null
    }
}