package com.myshelf.myshelf_app.util

/**
 * Конфигурация API сервера с поддержкой разных окружений.
 *
 * Использование:
 * - ЭМУЛЯТОР Android Studio: BASE_URL = EMULATOR_API_URL
 * - ФИЗИЧЕСКИЙ смартфон в локальной сети: BASE_URL = DEVICE_API_URL
 *
 * Текущее значение: [BASE_URL]
 */
object ApiConfig {

    /**
     * URL для подключения с ЭМУЛЯТОРА Android Studio.
     * 10.0.2.2 - специальный IP адрес, который указывает на localhost хост-машины в Android эмуляторе.
     */
    const val EMULATOR_API_URL = "http://10.0.2.2:8080/api/"

    /**
     * URL для подключения с ФИЗИЧЕСКОГО смартфона в локальной сети.
     * 192.168.0.14 - IP адрес ноутбука в локальной сети.
     * Убедитесь, что смартфон подключён к той же сети Wi-Fi!
     */
    const val DEVICE_API_URL = "http://192.168.0.14:8080/api/"

    /**
     * Активный базовый URL. Измените на DEVICE_API_URL для тестирования на физическом смартфоне.
     */
    const val BASE_URL = EMULATOR_API_URL  // ← измените здесь при необходимости
}
