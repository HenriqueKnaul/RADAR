package br.udesc.radar.ui.tarefas

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.udesc.radar.data.local.TarefaAcademica
import br.udesc.radar.data.repositorio.RepositorioAcademico
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EstadoFormularioTarefa(
    val carregando: Boolean = true,
    val indisponivel: Boolean = false,
    val tarefa: TarefaAcademica? = null,
    val salvando: Boolean = false,
    val salvo: Boolean = false,
    val erro: String? = null,
)

class FormularioTarefaViewModel(private val repositorioAcademico: RepositorioAcademico, private val estado: SavedStateHandle) : ViewModel() {
    val id = estado.get<Long>("id") ?: 0L
    val idDisciplina = estado.get<Long>("disciplineId") ?: 0L
    val disciplinas = repositorioAcademico.disciplinas()
    val tipos = listOf("Tarefa", "Trabalho", "Prova")
    val prioridades = listOf("Baixa", "Média", "Alta")

    private val formularioMutavel = MutableStateFlow(EstadoFormularioTarefa())
    val formulario = formularioMutavel.asStateFlow()
    val foto = estado.getStateFlow<String?>("photo", null)
    val prazo = estado.getStateFlow<Long?>("deadline", null)

    init {
        viewModelScope.launch {
            try {
                val tarefa = if (id == 0L) null else repositorioAcademico.tarefa(id)
                if (estado.get<Boolean>("draftInitialized") != true) {
                    estado["photo"] = tarefa?.foto
                    estado["deadline"] = tarefa?.prazo
                    estado["draftInitialized"] = true
                }
                formularioMutavel.value = EstadoFormularioTarefa(carregando = false, indisponivel = id != 0L && tarefa == null, tarefa = tarefa)
            } catch (erro: CancellationException) {
                throw erro
            } catch (_: Exception) {
                formularioMutavel.value = EstadoFormularioTarefa(carregando = false, indisponivel = true)
            }
        }
    }

    fun definirPrazo(novoPrazo: Long) {
        estado["deadline"] = novoPrazo
    }

    fun arquivoFoto(nome: String) = repositorioAcademico.arquivoFoto(nome)

    fun prepararCaptura(): File {
        check(estado.get<String>("pendingCapture") == null)
        val pasta = repositorioAcademico.pastaFotos
        check(pasta.isDirectory || pasta.mkdirs())
        return File.createTempFile("radar-", ".jpg", pasta).also { estado["pendingCapture"] = it.name }
    }

    fun finalizarCaptura(sucesso: Boolean) {
        val nome = estado.remove<String>("pendingCapture") ?: return
        if (sucesso && arquivoFoto(nome).length() > 0L) {
            estado.remove<String>("newPhoto")?.let(repositorioAcademico::excluirFoto)
            estado["newPhoto"] = nome
            estado["photo"] = nome
        } else {
            repositorioAcademico.excluirFoto(nome)
            if (sucesso) informarFalhaNaCaptura()
        }
    }

    fun removerFoto() {
        estado.remove<String>("newPhoto")?.let(repositorioAcademico::excluirFoto)
        estado["photo"] = null
    }

    fun informarFalhaNaCaptura() {
        formularioMutavel.value = formularioMutavel.value.copy(erro = "Não foi possível usar a câmera. Confira se há um app de câmera disponível.")
    }

    fun salvar(titulo: String, descricao: String, idDisciplina: Long, tipo: String, prioridade: String) {
        val atual = formularioMutavel.value
        if (atual.carregando || atual.indisponivel || atual.salvando || atual.salvo) return
        val vencimento = prazo.value
        val erro = when {
            titulo.isBlank() -> "Informe o título da tarefa."
            idDisciplina == 0L -> "Escolha a disciplina da tarefa."
            vencimento == null -> "Selecione o prazo da tarefa."
            else -> null
        }
        if (erro != null) {
            formularioMutavel.value = atual.copy(erro = erro)
            return
        }
        val tarefa = TarefaAcademica(
            id, idDisciplina, titulo.trim(), descricao.trim(), tipo, prioridade, checkNotNull(vencimento),
            foto = foto.value,
        )
        formularioMutavel.value = atual.copy(salvando = true, erro = null)
        viewModelScope.launch {
            formularioMutavel.value = try {
                repositorioAcademico.salvarTarefa(tarefa)
                estado.remove<String>("newPhoto")
                atual.copy(salvo = true, erro = null)
            } catch (erro: CancellationException) {
                throw erro
            } catch (_: Exception) {
                atual.copy(erro = "Não foi possível salvar. Confira se a disciplina ainda está cadastrada.")
            }
        }
    }

    override fun onCleared() {
        if (!formularioMutavel.value.salvando) {
            estado.remove<String>("pendingCapture")?.let(repositorioAcademico::excluirFoto)
            estado.remove<String>("newPhoto")?.let(repositorioAcademico::excluirFoto)
        }
    }
}
