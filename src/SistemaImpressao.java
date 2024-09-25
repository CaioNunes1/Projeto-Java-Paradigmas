import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class SistemaImpressao {

    private final int NUM_IMPRESSORAS = 3;
    private final int NUM_COMPUTADORES = 3;

    // Semáforo para controlar o número de impressoras disponíveis
    private Semaphore impressorasDisponiveis = new Semaphore(NUM_IMPRESSORAS);

    // Fila de pedidos de impressão
    private Queue<String> filaDePedidos = new LinkedList<>();

    // Array de impressoras
    private Impressora[] impressoras = new Impressora[NUM_IMPRESSORAS];

    // Pool de threads para gerenciar tarefas
    private ExecutorService threadPool = Executors.newFixedThreadPool(NUM_IMPRESSORAS);

    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    // Contadores globais
    private int documentosEnviados = 0;
    private int documentosImpressos = 0;

    public static void main(String[] args) {
        SistemaImpressao sistema = new SistemaImpressao();
        sistema.iniciar();
    }

    public void iniciar() {
        // Inicializa impressoras
        for (int i = 0; i < NUM_IMPRESSORAS; i++) {
            impressoras[i] = new Impressora(i);
        }

        // Inicia threads de computadores
        for (int i = 1; i <= NUM_COMPUTADORES; i++) {
            new Thread(new Computador(i, filaDePedidos, this)).start();
        }

        // Inicia escalonador
        new Thread(new Escalonador(filaDePedidos, this)).start();
    }

    public String getTimeStamp() {
        return sdf.format(new Date());
    }

    public Impressora alocarImpressora() throws InterruptedException {
        impressorasDisponiveis.acquire();
        synchronized (impressoras) {
            for (Impressora impressora : impressoras) {
                if (!impressora.isOcupada()) {
                    impressora.setOcupada(true);
                    return impressora;
                }
            }
        }
        return null;
    }

    public void devolverImpressora(Impressora impressora) {
        synchronized (impressoras) {
            impressora.setOcupada(false);
            System.out.println(getTimeStamp() + " - [Impressora " + (impressora.getId() + 1) + "] está livre.");
        }
        impressorasDisponiveis.release();
    }

    public void executarTarefa(Runnable tarefa) {
        threadPool.submit(tarefa);
    }

    synchronized void incrementarDocumentosEnviados() {
        documentosEnviados++;
    }

    synchronized void incrementarDocumentosImpressos() {
        documentosImpressos++;
    }

    public void verificarFimImpressao() {
        if (documentosImpressos == documentosEnviados) {
            System.out.println("Todos os documentos foram enviados e impressos.");
            System.out.println("Total de documentos enviados: " + documentosEnviados);
            System.out.println("Total de documentos impressos: " + documentosImpressos);
            threadPool.shutdown();
        }
    }
}
