import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class SistemaImpressao {
    // Definindo o número de impressoras e computadores
    private static final int NUM_IMPRESSORAS = 3; // Número de impressoras disponíveis
    private static final int NUM_COMPUTADORES = 3; // Número de computadores (threads)

    // Semáforo para controlar o acesso às impressoras
    private static Semaphore impressorasDisponiveis = new Semaphore(NUM_IMPRESSORAS);

    // Fila de pedidos de impressão
    private static Queue<String> filaDePedidos = new LinkedList<>();

    // Impressoras identificadas
    private static boolean[] impressoras = new boolean[NUM_IMPRESSORAS];

    // Formato para as horas
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    public static void main(String[] args) {
        // Cria e inicia threads para os computadores (que fazem pedidos de impressão)
        for (int i = 1; i <= NUM_COMPUTADORES; i++) {
            new Thread(new Computador(i)).start();
        }

        // Inicia o escalonador
        new Thread(new Escalonador()).start();
    }

    // Classe que representa o Computador (que faz pedidos)
    static class Computador implements Runnable {
        private int id;

        public Computador(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {
                // Simula múltiplos pedidos de impressão
                for (int i = 1; i <= 3; i++) {
                    String documento = "Documento " + i + " do Computador " + id;
                    synchronized (filaDePedidos) {
                        filaDePedidos.add(documento);
                        System.out.println(getTimeStamp() + " - [ Computador " + id + "]" + " enviou: " + documento);
                    }
                    Thread.sleep((long) (Math.random() * 3000)); // Tempo de intervalo entre pedidos
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Classe que representa o Escalonador (que distribui os pedidos de impressão)
    static class Escalonador implements Runnable {
        @Override
        public void run() {
            while (true) {
                synchronized (filaDePedidos) {
                    // Verifica se há pedidos na fila
                    if (!filaDePedidos.isEmpty()) {
                        String pedido = filaDePedidos.poll(); // Remove o primeiro da fila
                        // Cria uma thread para lidar com o pedido de impressão
                        new Thread(new Impressora(pedido)).start();
                    }
                }

                try {
                    Thread.sleep(100); // Intervalo para escalonar os pedidos
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Classe que representa a Impressora (que processa o pedido)
    static class Impressora implements Runnable {
        private String documento;
        private int impressoraId;

        public Impressora(String documento) {
            this.documento = documento;
        }

        @Override
        public void run() {
            try {
                // Tenta adquirir uma impressora disponível
                impressorasDisponiveis.acquire();//impressoras totais
                impressoraId = getImpressoraDisponivel();
                if (impressoraId != -1) {
                    impressoras[impressoraId] = true;
                    System.out.println(getTimeStamp() + " -[ Redirecionando para Impressora " + (impressoraId + 1)+"] "+ documento);
                }

                System.out.println("");
                System.out.println(getTimeStamp() + " - [ Imprimindo:] " + documento + " (Impressoras disponíveis: "
                        + impressorasDisponiveis.availablePermits() + ")");

                // Simula o tempo de impressão
                Thread.sleep((long) (Math.random() * 5000));

                // Libera a impressora após a impressão
                System.out.println("");
                System.out.println(getTimeStamp() + " - [Documento impresso]: " + documento);
                System.out.println("");

                impressoras[impressoraId] = false; // Impressora fica livre
                System.out.println(getTimeStamp() + " - [Impressora " + (impressoraId + 1)+"]" + " está livre.");
                impressorasDisponiveis.release();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Método para retornar uma impressora disponível
        private int getImpressoraDisponivel() {
            synchronized (impressoras) {
                for (int i = 0; i < impressoras.length; i++) {
                    if (!impressoras[i]) {
                        return i; // Retorna o índice da impressora disponível
                    }
                }
            }
            return -1; // Nenhuma impressora disponível
        }
    }

    // Método para retornar o horário atual formatado
    private static String getTimeStamp() {
        return sdf.format(new Date());
    }
}
