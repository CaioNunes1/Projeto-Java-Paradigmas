// import java.text.SimpleDateFormat;
// import java.util.Date;
// import java.util.LinkedList;
// import java.util.Queue;
// import java.util.concurrent.Semaphore;


// public class SistemaImpressao {
//     // Definindo o número de impressoras e computadores
//     private static final int NUM_IMPRESSORAS = 3; // Número de impressoras disponíveis
//     private static final int NUM_COMPUTADORES = 3; // Número de computadores (threads)

//     // Semáforo para controlar o acesso às impressoras
//     private static Semaphore impressorasDisponiveis = new Semaphore(NUM_IMPRESSORAS);

//     // Fila de pedidos de impressão
//     private static Queue<String> filaDePedidos = new LinkedList<>();

//     // Impressoras identificadas
//     private static boolean[] impressoras = new boolean[NUM_IMPRESSORAS];

//     // Formato para as horas
//     private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

//     public static void main(String[] args) {
//         // Cria e inicia threads para os computadores (que fazem pedidos de impressão)
//         for (int i = 1; i <= NUM_COMPUTADORES; i++) {
//             new Thread(new Computador(i)).start();
//         }

//         // Inicia o escalonador
//         new Thread(new Escalonador()).start();
//     }

//     // Classe que representa o Computador (que faz pedidos)
//     static class Computador implements Runnable {
//         private int id;

//         public Computador(int id) {
//             this.id = id;
//         }

//         @Override
//         public void run() {
//             try {
//                 // Simula múltiplos pedidos de impressão
//                 for (int i = 1; i <= 3; i++) {
//                     String documento = "Documento " + i + " do Computador " + id;
//                     synchronized (filaDePedidos) {
//                         filaDePedidos.add(documento);
//                         System.out.println(getTimeStamp() + " - [ Computador " + id + "]" + " enviou: " + documento);
//                     }
//                     Thread.sleep((long) (Math.random() * 3000)); // Tempo de intervalo entre pedidos
//                 }
//             } catch (InterruptedException e) {
//                 e.printStackTrace();
//             }
//         }
//     }


//     // Classe que representa a Impressora (que processa o pedido)
//     static class Impressora implements Runnable {
//         private String documento;
//         private int impressoraId;

//         public Impressora(String documento) {
//             this.documento = documento;
//         }

//         @Override
//         public void run() {
//             try {
//                 // Tenta adquirir uma impressora disponível
//                 impressorasDisponiveis.acquire();//impressoras totais
//                 impressoraId = getImpressoraDisponivel();
//                 if (impressoraId != -1) {
//                     impressoras[impressoraId] = true;
//                     System.out.println(getTimeStamp() + " -[ Redirecionando para Impressora " + (impressoraId + 1)+"] "+ documento);
//                 }

//                 System.out.println("");
//                 System.out.println(getTimeStamp() + " - [ Imprimindo:] " + documento + " (Impressoras disponíveis: "
//                         + impressorasDisponiveis.availablePermits() + ")");

//                 // Simula o tempo de impressão
//                 Thread.sleep((long) (Math.random() * 5000));

//                 // Libera a impressora após a impressão
//                 System.out.println("");
//                 System.out.println(getTimeStamp() + " - [Documento impresso]: " + documento);
//                 System.out.println("");

//                 impressoras[impressoraId] = false; // Impressora fica livre
//                 System.out.println(getTimeStamp() + " - [Impressora " + (impressoraId + 1)+"]" + " está livre.");
//                 impressorasDisponiveis.release();

//             } catch (InterruptedException e) {
//                 e.printStackTrace();
//             }
//         }

//         // Método para retornar uma impressora disponível
//         private int getImpressoraDisponivel() {
//             synchronized (impressoras) {
//                 for (int i = 0; i < impressoras.length; i++) {
//                     if (!impressoras[i]) {
//                         return i; // Retorna o índice da impressora disponível
//                     }
//                 }
//             }
//             return -1; // Nenhuma impressora disponível
//         }
//     }

//     // Método para retornar o horário atual formatado
//     private static String getTimeStamp() {
//         return sdf.format(new Date());
//     }
// }

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
    private ExecutorService threadPool = Executors.newFixedThreadPool(NUM_IMPRESSORAS); // Pool fixo de threads

    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    // Contadores globais
    private int documentosEnviados = 0;
    private int documentosImpressos = 0;

    public static void main(String[] args) {
        SistemaImpressao sistema = new SistemaImpressao();
        sistema.iniciar();
    }

    // Método que inicia o sistema de impressão
    public void iniciar() {
        // Inicializa as impressoras previamente (criando um pool de impressoras disponíveis)
        for (int i = 0; i < NUM_IMPRESSORAS; i++) {
            impressoras[i] = new Impressora(i);
        }

        // Cria e inicia as threads para os computadores
        for (int i = 1; i <= NUM_COMPUTADORES; i++) {
            new Thread(new Computador(i, filaDePedidos)).start();
        }

        // Inicia o escalonador
        new Thread(new Escalonador(filaDePedidos)).start();
    }

    // Método para obter o timestamp atual
    public String getTimeStamp() {
        return sdf.format(new Date());
    }

    // Método para alocar uma impressora disponível
    public Impressora alocarImpressora() throws InterruptedException {
        impressorasDisponiveis.acquire(); // Adquire uma permissão do semáforo (indica que uma impressora será usada)
        synchronized (impressoras) {
            for (Impressora impressora : impressoras) {
                if (!impressora.isOcupada()) { // Verifica se a impressora está livre
                    impressora.setOcupada(true); // Marca como ocupada
                    return impressora;
                }
            }
        }
        return null; // Se não encontrar impressora disponível (não deve ocorrer)
    }

    // Método para devolver a impressora e liberá-la para o pool
    public void devolverImpressora(Impressora impressora) {
        synchronized (impressoras) {
            impressora.setOcupada(false); // Marca como livre
            System.out.println(getTimeStamp() + " - [Impressora " + (impressora.getId() + 1) + "] está livre.");
        }
        impressorasDisponiveis.release(); // Libera a permissão no semáforo (indica que a impressora está disponível)
    }

    // Método para enviar uma tarefa para o pool de threads
    public void executarTarefa(Runnable tarefa) {
        threadPool.submit(tarefa); // Envia a tarefa para o pool de threads
    }

    // Classe interna Escalonador
    class Escalonador implements Runnable {
        private Queue<String> filaDePedidos;

        public Escalonador(Queue<String> filaDePedidos) {
            this.filaDePedidos = filaDePedidos;
        }

        @Override
        public void run() {
            while (true) {
                synchronized (filaDePedidos) {
                    if (!filaDePedidos.isEmpty()) {
                        String pedido = filaDePedidos.poll(); // Remove o pedido da fila
                        try {
                            Impressora impressora = alocarImpressora();
                            if (impressora != null) {
                                // Submete a tarefa de impressão ao pool de threads
                                executarTarefa(() -> {
                                    try {
                                        impressora.imprimir(pedido);
                                        incrementarDocumentosImpressos();
                                        devolverImpressora(impressora);
                                        verificarFimImpressao();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                });
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Classe interna Impressora
    class Impressora {
        private int id;
        private boolean ocupada;

        public Impressora(int id) {
            this.id = id;
            this.ocupada = false; // Começa livre
        }

        public int getId() {
            return id;
        }

        public boolean isOcupada() {
            return ocupada;
        }

        public void setOcupada(boolean ocupada) {
            this.ocupada = ocupada;
        }

        public void imprimir(String documento) throws InterruptedException {
            synchronized (this) { // Garante que apenas uma thread use a impressora ao mesmo tempo
                System.out.println(getTimeStamp() + " - [Impressora " + (id + 1) + "] Imprimindo: " + documento);
                Thread.sleep((long) (Math.random() * 5000)); // Simula o tempo de impressão
                System.out.println(getTimeStamp() + " - [Impressora " + (id + 1) + "] Documento impresso: " + documento);
            }
        }
    }

    // Classe interna Computador
    class Computador implements Runnable {
        private int id;
        private Queue<String> filaDePedidos;

        public Computador(int id, Queue<String> filaDePedidos) {
            this.id = id;
            this.filaDePedidos = filaDePedidos;
        }

        @Override
        public void run() {
            try {
                for (int i = 1; i <= 3; i++) {
                    String documento = "Documento " + i + " do Computador " + id;
                    synchronized (filaDePedidos) {
                        filaDePedidos.add(documento);
                        incrementarDocumentosEnviados();
                        System.out.println(getTimeStamp() + " - [Computador " + id + "] enviou: " + documento);
                    }
                    Thread.sleep((long) (Math.random() * 3000)); // Simula o tempo entre os envios de documentos
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para incrementar o contador de documentos enviados
    private synchronized void incrementarDocumentosEnviados() {
        documentosEnviados++;
    }

    // Método para incrementar o contador de documentos impressos
    private synchronized void incrementarDocumentosImpressos() {
        documentosImpressos++;
    }

    // Método para verificar se todos os documentos foram impressos
    private void verificarFimImpressao() {
        if (documentosImpressos == documentosEnviados) {
            System.out.println("Todos os documentos foram enviados e impressos.");
            System.out.println("Total de documentos enviados: " + documentosEnviados);
            System.out.println("Total de documentos impressos: " + documentosImpressos);
            threadPool.shutdown(); // Finaliza o pool de threads
        }
    }
}
