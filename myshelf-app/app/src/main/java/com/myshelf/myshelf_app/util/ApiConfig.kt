package com.myshelf.myshelf_app.util

/**
 * Конфигурация API сервера с поддержкой разных окружений.
 *
 * Использование:
 * - ЭМУЛЯТОР Android Studio: BASE_URL = EMULATOR_API_URL
 * - ФИЗИЧЕСКИЙ смартфон (Wi-Fi): BASE_URL = DEVICE_WIFI_API_URL
 * - СМАРТФОН по USB с port forwarding: BASE_URL = DEVICE_USB_DEBUG_API_URL
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
     * URL для подключения с ФИЗИЧЕСКОГО смартфона в локальной сети (Wi-Fi).
     * 192.168.0.14 - IP адрес ноутбука в локальной сети.
     * Убедитесь, что смартфон подключён к той же сети Wi-Fi!
     */
    const val DEVICE_WIFI_API_URL = "http://192.168.0.14:8080/api/"

    /**
     * URL для подключения со СМАРТФОНА, подключённого по USB в режиме отладки.
     * Предварительно выполните команду: adb forward tcp:8080 tcp:8080
     * Тогда смартфон сможет подключиться к localhost (вашему ноутбуку).
     */
    const val DEVICE_USB_DEBUG_API_URL = "http://localhost:8080/api/"

    /**
     * Активный базовый URL.
     * Измените на нужное значение в зависимости от окружения:
     * - EMULATOR_API_URL для эмулятора
     * - DEVICE_WIFI_API_URL для смартфона в локальной сети
     * - DEVICE_USB_DEBUG_API_URL для смартфона по USB (после выполнения adb forward)
     */
    const val BASE_URL = EMULATOR_API_URL
}
