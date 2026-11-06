package br.udesc.radar

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.udesc.radar.data.local.Disciplina
import br.udesc.radar.data.local.RadarDatabase
import br.udesc.radar.data.local.TarefaAcademica
import br.udesc.radar.data.repositorio.RepositorioAcademico
import br.udesc.radar.ui.tarefas.FormularioTarefaViewModel
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositorioAcademicoTest {
    @Test
    fun mantemDadosAoReabrirERemoveFotosVinculadas() = runBlocking {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val nome = "test-${UUID.randomUUID()}.db"
        val arquivos = File(contexto.cacheDir, nome)
        try {
            val idDisciplina = comBanco(contexto, nome) { banco ->
                val repositorio = RepositorioAcademico(banco, arquivos)
                repositorio.salvarDisciplina(Disciplina(nome = "Matemática", chaveNome = "matemática", professor = "", cor = 0))
                val disciplina = repositorio.disciplinas().first().single()
                repositorio.salvarDisciplina(disciplina.copy(professor = "Ana"))
                try {
                    repositorio.salvarDisciplina(disciplina.copy(id = 0L, nome = "MATEMÁTICA"))
                    fail("Nome duplicado deve ser rejeitado pelo banco")
                } catch (_: SQLiteConstraintException) {
                    assertEquals(1, repositorio.disciplinas().first().size)
                }
                assertTrue(repositorio.pastaFotos.mkdirs())
                repositorio.arquivoFoto("original.jpg").writeText("anexo de teste")
                repositorio.salvarTarefa(
                    TarefaAcademica(
                        idDisciplina = disciplina.id,
                        titulo = "Lista",
                        descricao = "",
                        tipo = "Tarefa",
                        prioridade = "Alta",
                        prazo = 1_800_000_000_000L,
                        foto = "original.jpg",
                    ),
                )
                disciplina.id
            }

            comBanco(contexto, nome) { banco ->
                val repositorio = RepositorioAcademico(banco, arquivos)
                val disciplina = repositorio.disciplina(idDisciplina).first()!!
                assertEquals("Ana", disciplina.professor)
                val tarefa = repositorio.tarefas().first().single().tarefa
                assertEquals("Lista", tarefa.titulo)
                assertTrue(repositorio.arquivoFoto("original.jpg").isFile)
                verificarRascunhoEdicao(repositorio, tarefa)

                repositorio.definirConclusao(tarefa.id, true)
                repositorio.arquivoFoto("nova.jpg").writeText("novo anexo de teste")
                repositorio.salvarTarefa(tarefa.copy(titulo = "Lista revisada", foto = "nova.jpg"))
                assertFalse(repositorio.arquivoFoto("original.jpg").exists())
                assertEquals("Lista revisada", repositorio.tarefa(tarefa.id)?.titulo)
                assertEquals(true, repositorio.tarefa(tarefa.id)?.concluida)
                repositorio.definirConclusao(tarefa.id, false)
                assertEquals(false, repositorio.tarefa(tarefa.id)?.concluida)

                repositorio.excluirTarefa(tarefa.id)
                assertNull(repositorio.tarefa(tarefa.id))
                assertFalse(repositorio.arquivoFoto("nova.jpg").exists())

                repositorio.arquivoFoto("vinculada.jpg").writeText("anexo vinculado de teste")
                repositorio.salvarTarefa(tarefa.copy(id = 0, foto = "vinculada.jpg"))
                repositorio.excluirDisciplina(disciplina)
                assertTrue(repositorio.disciplinas().first().isEmpty())
                assertTrue(repositorio.tarefas().first().isEmpty())
                assertFalse(repositorio.arquivoFoto("vinculada.jpg").exists())
            }
        } finally {
            contexto.deleteDatabase(nome)
            arquivos.deleteRecursively()
        }
    }

    private suspend fun verificarRascunhoEdicao(repositorio: RepositorioAcademico, tarefa: TarefaAcademica) {
        withContext(Dispatchers.Main) {
            val salvo = SavedStateHandle(mapOf("id" to tarefa.id))
            val editor = FormularioTarefaViewModel(repositorio, salvo)
            withTimeout(5_000) { editor.formulario.first { !it.carregando } }
            assertEquals(tarefa.foto, editor.foto.value)
            assertEquals(tarefa.prazo, editor.prazo.value)

            editor.removerFoto()
            val restaurado = FormularioTarefaViewModel(repositorio, salvo)
            withTimeout(5_000) { restaurado.formulario.first { !it.carregando } }
            assertNull(restaurado.foto.value)
            assertEquals(tarefa.prazo, restaurado.prazo.value)
            assertTrue(repositorio.arquivoFoto(checkNotNull(tarefa.foto)).isFile)
        }
    }

    private suspend fun <T> comBanco(
        contexto: Context,
        nome: String,
        acao: suspend (RadarDatabase) -> T,
    ): T {
        val banco = Room.databaseBuilder(contexto, RadarDatabase::class.java, nome).build()
        try {
            return acao(banco)
        } finally {
            banco.close()
        }
    }
}
