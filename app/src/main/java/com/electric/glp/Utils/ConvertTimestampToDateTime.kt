package com.electric.glp.Utils

import java.text.SimpleDateFormat
import java.util.*

class ConvertTimestampToDateTime {
     fun convert(timestamp: Long): String {
        // El timestamp recibido está en segundos, necesitamos convertirlo a milisegundos
        val date = Date(timestamp * 1000L)

        // Definimos el formato de salida: día/mes/año y hora:minuto
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        // Convertimos el Date a la cadena con el formato deseado
        return format.format(date)
    }
}