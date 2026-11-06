package br.udesc.radar.util

import br.udesc.radar.data.local.AcademicTask
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object AcademicDates {
    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
    fun local(epochMillis: Long): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault())

    fun epoch(dateTime: LocalDateTime): Long =
        dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun format(epochMillis: Long): String = formatter.format(local(epochMillis))

    fun clock() = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(30_000)
        }
    }

    fun status(task: AcademicTask, now: Long): String = when {
        task.completed -> "Concluída"
        task.deadline < now -> "Atrasada"
        else -> "Pendente"
    }
}
