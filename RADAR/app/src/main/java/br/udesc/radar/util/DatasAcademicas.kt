package br.udesc.radar.util

import br.udesc.radar.data.local.TarefaAcademica
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

object DatasAcademicas {
    private val formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
    fun paraDataLocal(instanteMillis: Long): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(instanteMillis), ZoneId.systemDefault())

    fun paraMillis(dataHora: LocalDateTime): Long =
        dataHora.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun formatar(instanteMillis: Long): String = formatador.format(paraDataLocal(instanteMillis))

    fun relogio() = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(30_000)
        }
    }

    fun situacao(tarefa: TarefaAcademica, agora: Long): String = when {
        tarefa.concluida -> "Concluída"
        tarefa.prazo < agora -> "Atrasada"
        else -> "Pendente"
    }
}
