// import java.util.LinkedList;
// import java.util.Queue;
// import java.util.concurrent.Semaphore;

// public class SistemaImpressao {
//     // Definindo o número de impressoras e computadores
//     private static final int NUM_IMPRESSORAS = 3; // Número de impressoras disponíveis
//     private static final int NUM_COMPUTADORES = 5; // Número de computadores (threads)

//     // Semáforo para controlar o acesso às impressoras
//     private static Semaphore impressorasDisponiveis = new Semaphore(NUM_IMPRESSORAS);

//     // Fila de pedidos de impressão
//     private static Queue<String> filaDePedidos = new LinkedList<>();

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
//                         System.out.println("Computador " + id + " enviou: " + documento);
//                     }
//                     Thread.sleep((long) (Math.random() * 3000)); // Tempo de intervalo entre pedidos
//                 }
//             } catch (InterruptedException e) {
//                 e.printStackTrace();
//             }
//         }
//     }

//     // Classe que representa o Escalonador (que distribui os pedidos de impressão)
//     static class Escalonador implements Runnable {
//         @Override
//         public void run() {
//             while (true) {
//                 synchronized (filaDePedidos) {
//                     // Verifica se há pedidos na fila
//                     if (!filaDePedidos.isEmpty()) {
//                         String pedido = filaDePedidos.poll(); // Remove o primeiro da fila
//                         // Cria uma thread para lidar com o pedido de impressão
//                         new Thread(new Impressora(pedido)).start();
//                     }
//                 }

//                 try {
//                     Thread.sleep(100); // Intervalo para escalonar os pedidos
//                 } catch (InterruptedException e) {
//                     e.printStackTrace();
//                 }
//             }
//         }
//     }

//     // Classe que representa a Impressora (que processa o pedido)
//     static class Impressora implements Runnable {
//         private String documento;

//         public Impressora(String documento) {
//             this.documento = documento;
//         }

//         @Override
//         public void run() {
//             try {
//                 // Tenta adquirir uma impressora disponível
//                 impressorasDisponiveis.acquire();
//                 System.out.println("Imprimindo: " + documento + " (Impressoras disponíveis: "
//                         + impressorasDisponiveis.availablePermits() + ")");

//                 // Simula o tempo de impressão
//                 Thread.sleep((long) (Math.random() * 5000));

//                 // Libera a impressora após a impressão
//                 System.out.println("Documento impresso: " + documento);
//                 impressorasDisponiveis.release();

//             } catch (InterruptedException e) {
//                 e.printStackTrace();
//             }
//         }
//     }
// }


// import java.util.concurrent.*;
// import java.util.Arrays;
// import java.text.SimpleDateFormat;
// import java.util.Date;

// import java.util.concurrent.*;
// import java.text.SimpleDateFormat;
// import java.util.Date;

// public class SistemaImpressao {
//     private static final int NUM_IMPRESSORAS = 4;
//     private static final int CAPACIDADE_POR_IMPRESSORA = 3;
//     private static final int NUM_COMPUTADORES = 5;
//     private static ExecutorService[] impressoras = new ExecutorService[NUM_IMPRESSORAS];
//     private static ExecutorService executorComputadores = Executors.newFixedThreadPool(NUM_COMPUTADORES);
//     private static BlockingQueue<String> filaDePedidos = new LinkedBlockingQueue<>();
//     private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

//     public static void main(String[] args) {
//         // Inicializa as impressoras com uma fila controlada
//         for (int i = 0; i < NUM_IMPRESSORAS; i++) {
//             impressoras[i] = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
//                                                     new LinkedBlockingQueue<>(CAPACIDADE_POR_IMPRESSORA));
//         }

//         // Inicia a thread do escalonador
//         Thread escalonador = new Thread(new Escalonador());
//         escalonador.start();

//         // Cria e inicia as threads dos computadores
//         for (int i = 1; i <= NUM_COMPUTADORES; i++) {
//             final int finalI = i;
//             executorComputadores.execute(() -> {
//                 try {
//                     for (int j = 1; j <= 3; j++) {  // Supondo que cada computador envia 3 documentos
//                         String documento = "Documento " + j + " do Computador " + finalI;
//                         filaDePedidos.put(documento);
//                         System.out.println("Computador " + finalI + " enviou: " + documento + " em " + sdf.format(new Date()));
//                         Thread.sleep(1000); // Delay entre envios
//                     }
//                 } catch (InterruptedException e) {
//                     Thread.currentThread().interrupt();
//                 }
//             });
//         }

//         executorComputadores.shutdown();
//     }

//     static class Escalonador implements Runnable {
//         @Override
//         public void run() {
//             try {
//                 while (!Thread.currentThread().isInterrupted()) {
//                     String documento = filaDePedidos.take();
//                     boolean documentHandled = false;
//                     for (int i = 0; i < NUM_IMPRESSORAS; i++) {
//                         ThreadPoolExecutor executor = (ThreadPoolExecutor) impressoras[i];
//                         if (executor.getQueue().remainingCapacity() > 0) {
//                             executor.execute(new Impressora(documento, i + 1));
//                             documentHandled = true;
//                             break;
//                         }
//                     }
//                     if (!documentHandled) {
//                         Thread.sleep(50); // Wait before retrying to allow some prints to complete
//                     }
//                 }
//             } catch (InterruptedException e) {
//                 Thread.currentThread().interrupt();
//             }
//         }
//     }

//     static class Impressora implements Runnable {
//         private String documento;
//         private int id;

//         public Impressora(String documento, int id) {
//             this.documento = documento;
//             this.id = id;
//         }

//         @Override
//         public void run() {
//             try {
//                 System.out.println("Imprimindo: " + documento + " na impressora " + id + " em " + sdf.format(new Date()));
//                 Thread.sleep((long) (Math.random() * 5000 + 1000));  // Simula tempo de impressão
//                 System.out.println("Documento impresso: " + documento + " na impressora " + id + " em " + sdf.format(new Date()));
//             } catch (InterruptedException e) {
//                 Thread.currentThread().interrupt();
//             }
//         }
//     }
// }


// import java.util.concurrent.*;
// import java.text.SimpleDateFormat;
// import java.util.Date;

// public class SistemaImpressao {
//     private static final int NUM_IMPRESSORAS = 2;
//     private static final int CAPACIDADE_POR_IMPRESSORA = 3;
//     private static final int NUM_COMPUTADORES = 3;
//     private static ExecutorService[] impressoras = new ExecutorService[NUM_IMPRESSORAS];
//     private static ExecutorService executorComputadores = Executors.newFixedThreadPool(NUM_COMPUTADORES);
//     private static BlockingQueue<String> filaDePedidos = new LinkedBlockingQueue<>();
//     private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

//     public static void main(String[] args) {
//         for (int i = 0; i < NUM_IMPRESSORAS; i++) {
//             impressoras[i] = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
//                                                     new LinkedBlockingQueue<>(CAPACIDADE_POR_IMPRESSORA));
//         }

//         // Inicia a thread do escalonador
//         Thread escalonador = new Thread(new Escalonador());
//         escalonador.start();

//         // Cria e inicia as threads dos computadores
//         for (int i = 1; i <= NUM_COMPUTADORES; i++) {
//             final int finalI = i;
//             executorComputadores.execute(() -> {
//                 try {
//                     for (int j = 1; j <= 3; j++) {
//                         String documento = "Documento " + j + " do Computador " + finalI;
//                         filaDePedidos.put(documento);
//                         System.out.println("Computador " + finalI + " enviou: " + documento + " em " + sdf.format(new Date()));
//                         Thread.sleep(1000); // Delay entre envios
//                     }
//                 } catch (InterruptedException e) {
//                     Thread.currentThread().interrupt();
//                 }
//             });
//         }

//         executorComputadores.shutdown();
//     }

//     static class Escalonador implements Runnable {
//         @Override
//         public void run() {
//             try {
//                 while (!Thread.currentThread().isInterrupted()) {
//                     String documento = filaDePedidos.take();
//                     boolean documentHandled = false;
//                     int startIndex = 0;
//                     while (!documentHandled) {
//                         for (int i = startIndex; i < NUM_IMPRESSORAS; i++) {
//                             ThreadPoolExecutor executor = (ThreadPoolExecutor) impressoras[i];
//                             if (executor.getQueue().remainingCapacity() > 0) {
//                                 executor.execute(new Impressora(documento, i + 1));
//                                 documentHandled = true;
//                                 break;
//                             }
//                         }
//                         if (!documentHandled) {  // If no capacity was found, start from the first printer again
//                             startIndex = 0;
//                             Thread.sleep(50); // Wait before retrying
//                         }
//                     }
//                 }
//             } catch (InterruptedException e) {
//                 Thread.currentThread().interrupt();
//             }
//         }
//     }

//     static class Impressora implements Runnable {
//         private String documento;
//         private int id;

//         public Impressora(String documento, int id) {
//             this.documento = documento;
//             this.id = id;
//         }

//         @Override
//         public void run() {
//             try {
//                 System.out.println("Imprimindo: " + documento + " na impressora " + id + " em " + sdf.format(new Date()));
//                 Thread.sleep((long) (Math.random() * 5000 + 1000));  // Simula tempo de impressão
//                 System.out.println("Documento impresso: " + documento + " na impressora " + id + " em " + sdf.format(new Date()));
//             } catch (InterruptedException e) {
//                 Thread.currentThread().interrupt();
//             }
//         }
//     }
// }

// import java.util.concurrent.*;
// import java.text.SimpleDateFormat;
// import java.util.Date;

// public class SistemaImpressao {
//     private static final int NUM_IMPRESSORAS = 3;
//     private static final int CAPACIDADE_POR_IMPRESSORA = 4;
//     private static final int NUM_COMPUTADORES = 5;
//     private static ExecutorService[] impressoras = new ExecutorService[NUM_IMPRESSORAS];
//     private static ExecutorService executorComputadores = Executors.newFixedThreadPool(NUM_COMPUTADORES);
//     private static BlockingQueue<String> filaDePedidos = new LinkedBlockingQueue<>();
//     private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

//     public static void main(String[] args) {
//         // Inicializa as impressoras com uma fila controlada
//         for (int i = 0; i < NUM_IMPRESSORAS; i++) {
//             impressoras[i] = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
//                                                     new LinkedBlockingQueue<>(CAPACIDADE_POR_IMPRESSORA));
//         }

//         // Inicia a thread do escalonador
//         Thread escalonador = new Thread(new Escalonador());
//         escalonador.start();

//         // Cria e inicia as threads dos computadores
//         for (int i = 1; i <= NUM_COMPUTADORES; i++) {
//             final int finalI = i;
//             executorComputadores.execute(() -> {
//                 try {
//                     for (int j = 1; j <= 3; j++) {
//                         String documento = "Documento " + j + " do Computador " + finalI;
//                         filaDePedidos.put(documento);
//                         System.out.println("Computador " + finalI + " enviou: " + documento + " em " + sdf.format(new Date()));
//                         Thread.sleep(1500); // Delay entre envios
//                     }
//                 } catch (InterruptedException e) {
//                     Thread.currentThread().interrupt();
//                 }
//             });
//         }

//         // Espera que todos os computadores terminem
//         executorComputadores.shutdown();
//         try {
//             if (!executorComputadores.awaitTermination(1, TimeUnit.HOURS)) {
//                 executorComputadores.shutdownNow();
//             }
//         } catch (InterruptedException e) {
//             executorComputadores.shutdownNow();
//         }
//     }

//     static class Escalonador implements Runnable {
//         @Override
//         public void run() {
//             try {
//                 while (!Thread.currentThread().isInterrupted()) {
//                     String documento = filaDePedidos.take();
//                     boolean documentHandled = false;
//                     while (!documentHandled) {
//                         for (int i = 0; i < NUM_IMPRESSORAS; i++) {
//                             ThreadPoolExecutor executor = (ThreadPoolExecutor) impressoras[i];
//                             if (executor.getQueue().remainingCapacity() > 0) {
//                                 executor.execute(new Impressora(documento, i + 1));
//                                 documentHandled = true;
//                                 break;
//                             }
//                         }
//                         if (!documentHandled) {
//                             Thread.sleep(50); // Wait before retrying
//                         }
//                     }
//                 }
//             } catch (InterruptedException e) {
//                 Thread.currentThread().interrupt();
//             }
//         }
//     }

//     static class Impressora implements Runnable {
//         private String documento;
//         private int id;

//         public Impressora(String documento, int id) {
//             this.documento = documento;
//             this.id = id;
//         }

//         @Override
//         public void run() {
//             try {
//                 System.out.println("Imprimindo: " + documento + " na impressora " + id + " em " + sdf.format(new Date()));
//                 Thread.sleep((long) (Math.random() * 5000 + 1000));  // Simula tempo de impressão
//                 System.out.println("Documento impresso: " + documento + " na impressora " + id + " em " + sdf.format(new Date()));
//             } catch (InterruptedException e) {
//                 Thread.currentThread().interrupt();
//             }
//         }
//     }
// }


// import java.util.concurrent.*;
// import java.text.SimpleDateFormat;
// import java.util.Date;

// public class SistemaImpressao {
//     private static final int NUM_IMPRESSORAS = 2;
//     private static final int CAPACIDADE_POR_IMPRESSORA = 3;
//     private static final int NUM_COMPUTADORES = 3;
//     private static ExecutorService[] impressoras = new ExecutorService[NUM_IMPRESSORAS];
//     private static ExecutorService executorComputadores = Executors.newFixedThreadPool(NUM_COMPUTADORES);
//     private static BlockingQueue<String> filaDePedidos = new LinkedBlockingQueue<>();
//     private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

//     public static void main(String[] args) {
//         // Inicializa as impressoras com uma fila controlada
//         for (int i = 0; i < NUM_IMPRESSORAS; i++) {
//             impressoras[i] = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
//                                                     new LinkedBlockingQueue<>(CAPACIDADE_POR_IMPRESSORA));
//         }

//         // Inicia a thread do escalonador
//         Thread escalonador = new Thread(new Escalonador());
//         escalonador.start();

//         // Cria e inicia as threads dos computadores
//         for (int i = 1; i <= NUM_COMPUTADORES; i++) {
//             final int finalI = i;
//             executorComputadores.execute(() -> {
//                 try {
//                     for (int j = 1; j <= 3; j++) {
//                         String documento = "Documento " + j + " do Computador " + finalI;
//                         filaDePedidos.put(documento);
//                         System.out.println("Computador " + finalI + " enviou: " + documento + " em " + sdf.format(new Date()));
//                         Thread.sleep(1500); // Delay entre envios
//                     }
//                 } catch (InterruptedException e) {
//                     Thread.currentThread().interrupt();
//                 }
//             });
//         }

//         executorComputadores.shutdown();
//     }

//     static class Escalonador implements Runnable {
//         private int currentPrinterIndex = 0;

//         @Override
//         public void run() {
//             try {
//                 while (!Thread.currentThread().isInterrupted()) {
//                     String documento = filaDePedidos.take();
//                     boolean documentHandled = false;
//                     int attemptCount = 0; // To count the attempts before moving to next printer
//                     while (!documentHandled && attemptCount < NUM_IMPRESSORAS) {
//                         ThreadPoolExecutor executor = (ThreadPoolExecutor) impressoras[currentPrinterIndex];
//                         if (executor.getQueue().remainingCapacity() > 0) {
//                             executor.execute(new Impressora(documento, currentPrinterIndex + 1));
//                             System.out.println("Documento " + documento + " enviado para Impressora " + (currentPrinterIndex + 1));
//                             documentHandled = true;
//                         } else {
//                             System.out.println("Redirecionando documento " + documento + " para outra impressora...");
//                             currentPrinterIndex = (currentPrinterIndex + 1) % NUM_IMPRESSORAS;
//                             attemptCount++;
//                         }
//                         Thread.sleep(50); // Wait before retrying
//                     }
//                 }
//             } catch (InterruptedException e) {
//                 Thread.currentThread().interrupt();
//             }
//         }
//     }

//     static class Impressora implements Runnable {
//         private String documento;
//         private int id;

//         public Impressora(String documento, int id) {
//             this.documento = documento;
//             this.id = id;
//         }

//         @Override
//         public void run() {
//             try {
//                 System.out.println("Imprimindo: " + documento + " na impressora " + id + " em " + sdf.format(new Date()));
//                 Thread.sleep((long) (Math.random() * 5000 + 1000));  // Simula tempo de impressão
//                 System.out.println("Documento impresso: " + documento + " na impressora " + id + " em " + sdf.format(new Date()));
//             } catch (InterruptedException e) {
//                 Thread.currentThread().interrupt();
//             }
//         }
//     }
// }

import java.util.concurrent.*;
import java.text.SimpleDateFormat;
import java.util.Date;

// import java.util.concurrent.*;
// import java.text.SimpleDateFormat;
// import java.util.Date;

// public class SistemaImpressao {
//     private static final int NUM_IMPRESSORAS = 2;
//     private static final int CAPACIDADE_POR_IMPRESSORA = 3;
//     private static final int NUM_COMPUTADORES = 3;
//     private static ExecutorService[] impressoras = new ExecutorService[NUM_IMPRESSORAS];
//     private static ExecutorService executorComputadores = Executors.newFixedThreadPool(NUM_COMPUTADORES);
//     private static BlockingQueue<DocumentPrintJob> filaDePedidos = new LinkedBlockingQueue<>();
//     private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

//     public static void main(String[] args) {
//         // Inicializa as impressoras com uma fila controlada
//         for (int i = 0; i < NUM_IMPRESSORAS; i++) {
//             impressoras[i] = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
//                                                     new LinkedBlockingQueue<>(CAPACIDADE_POR_IMPRESSORA));
//         }

//         // Inicia a thread do escalonador
//         Thread escalonador = new Thread(new Escalonador());
//         escalonador.start();

//         // Cria e inicia as threads dos computadores
//         for (int i = 1; i <= NUM_COMPUTADORES; i++) {
//             final int finalI = i;
//             executorComputadores.execute(() -> {
//                 try {
//                     for (int j = 1; j <= 3; j++) {
//                         String documento = "Documento " + j + " do Computador " + finalI;
//                         DocumentPrintJob job = new DocumentPrintJob(documento, finalI);
//                         filaDePedidos.put(job);
//                         Thread.sleep(5000); // Increased delay to 5 seconds
//                     }
//                 } catch (InterruptedException e) {
//                     Thread.currentThread().interrupt();
//                 }
//             });
//         }

//         executorComputadores.shutdown();
//     }

//     static class Escalonador implements Runnable {
//         private int currentPrinterIndex = 0;

//         @Override
//         public void run() {
//             try {
//                 while (!Thread.currentThread().isInterrupted()) {
//                     DocumentPrintJob job = filaDePedidos.take();
//                     boolean documentHandled = false;
//                     while (!documentHandled) {
//                         ThreadPoolExecutor executor = (ThreadPoolExecutor) impressoras[currentPrinterIndex];
//                         if (executor.getQueue().remainingCapacity() > 0) {
//                             executor.execute(new Impressora(job.documento, currentPrinterIndex + 1));
//                             System.out.println(job.documento + " enviado para Impressora " + (currentPrinterIndex + 1) + " por Computador " + job.computerId);
//                             documentHandled = true;
//                         } else {
//                             currentPrinterIndex = (currentPrinterIndex + 1) % NUM_IMPRESSORAS;
//                         }
//                         Thread.sleep(1000); // Reduced wait before retrying to 1 second
//                     }
//                 }
//             } catch (InterruptedException e) {
//                 Thread.currentThread().interrupt();
//             }
//         }
//     }

//     static class Impressora implements Runnable {
//         private String documento;
//         private int id;

//         public Impressora(String documento, int id) {
//             this.documento = documento;
//             this.id = id;
//         }

//         @Override
//         public void run() {
//             try {
//                 System.out.println("Imprimindo: " + documento + " na impressora " + id + " em " + sdf.format(new Date()));
//                 Thread.sleep(10000);  // Simulated printing time fixed to 10 seconds
//                 System.out.println("Documento impresso: " + documento + " na impressora " + id + " em " + sdf.format(new Date()));
//             } catch (InterruptedException e) {
//                 Thread.currentThread().interrupt();
//             }
//         }
//     }

//     static class DocumentPrintJob {
//         String documento;
//         int computerId;

//         DocumentPrintJob(String documento, int computerId) {
//             this.documento = documento;
//             this.computerId = computerId;
//         }
//     }
// }

import java.util.concurrent.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.concurrent.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.concurrent.*;
import java.text.SimpleDateFormat;
import java.util.Date;

// public class SistemaImpressao {
//     private static final int NUM_IMPRESSORAS = 2;
//     private static final int CAPACIDADE_POR_IMPRESSORA = 3;
//     private static final int NUM_COMPUTADORES = 3;
//     private static ExecutorService[] impressoras = new ExecutorService[NUM_IMPRESSORAS];
//     private static ExecutorService executorComputadores = Executors.newFixedThreadPool(NUM_COMPUTADORES);
//     private static BlockingQueue<DocumentPrintJob> filaDePedidos = new LinkedBlockingQueue<>();
//     private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

//     public static void main(String[] args) {
//         // Inicializa as impressoras com uma fila controlada
//         for (int i = 0; i < NUM_IMPRESSORAS; i++) {
//             impressoras[i] = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
//                                                     new LinkedBlockingQueue<>(CAPACIDADE_POR_IMPRESSORA));
//         }

//         // Inicia a thread do escalonador
//         Thread escalonador = new Thread(new Escalonador());
//         escalonador.start();

//         // Cria e inicia as threads dos computadores
//         for (int i = 1; i <= NUM_COMPUTADORES; i++) {
//             final int finalI = i;
//             executorComputadores.execute(() -> {
//                 try {
//                     for (int j = 1; j <= 3; j++) {
//                         String documento = "Documento " + j + " do Computador " + finalI;
//                         DocumentPrintJob job = new DocumentPrintJob(documento, finalI);
//                         filaDePedidos.put(job);
//                         System.out.println("Computador " + finalI + " enviou: " + documento + " em " + sdf.format(new Date()));
//                         Thread.sleep(5000); // Increased delay to 5 seconds
//                     }
//                 } catch (InterruptedException e) {
//                     Thread.currentThread().interrupt();
//                 }
//             });
//         }

//         executorComputadores.shutdown();
//     }

//     static class Escalonador implements Runnable {
//         private int currentPrinterIndex = 0;

//         @Override
//         public void run() {
//             try {
//                 while (!Thread.currentThread().isInterrupted()) {
//                     DocumentPrintJob job = filaDePedidos.take();
//                     boolean documentHandled = false;
//                     while (!documentHandled) {
//                         ThreadPoolExecutor executor = (ThreadPoolExecutor) impressoras[currentPrinterIndex];
//                         if (executor.getQueue().remainingCapacity() > 0) {
//                             executor.execute(new Impressora(job.documento, currentPrinterIndex + 1));
//                             System.out.println("Computador " + job.computerId + " enviou: " + job.documento + " para a impressora " + (currentPrinterIndex + 1) + " em " + sdf.format(new Date()));
//                             documentHandled = true;
//                         } else {
//                             System.out.println("Impressora " + (currentPrinterIndex + 1) + " cheia. Redirecionando documento " + job.documento + " para outra impressora...");
//                             currentPrinterIndex = (currentPrinterIndex + 1) % NUM_IMPRESSORAS;
//                             Thread.sleep(1000); // Wait before trying the next printer
//                         }
//                     }
//                 }
//             } catch (InterruptedException e) {
//                 Thread.currentThread().interrupt();
//             }
//         }
//     }

//     static class Impressora implements Runnable {
//         private String documento;
//         private int id;

//         public Impressora(String documento, int id) {
//             this.documento = documento;
//             this.id = id;
//         }

//         @Override
//         public void run() {
//             try {
//                 System.out.println("Imprimindo: " + documento + " na impressora " + id + " em " + sdf.format(new Date()));
//                 Thread.sleep(10000);  // Simulated printing time fixed to 10 seconds
//                 System.out.println("Documento impresso: " + documento + " na impressora " + id + " em " + sdf.format(new Date()));
//             } catch (InterruptedException e) {
//                 Thread.currentThread().interrupt();
//             }
//         }
//     }

//     static class DocumentPrintJob {
//         String documento;
//         int computerId;

//         DocumentPrintJob(String documento, int computerId) {
//             this.documento = documento;
//             this.computerId = computerId;
//         }
//     }
// }

import java.util.concurrent.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SistemaImpressao {
    private static final int NUM_IMPRESSORAS = 4;
    private static final int CAPACIDADE_POR_IMPRESSORA = 3;
    private static final int NUM_COMPUTADORES = 5;
    private static ExecutorService[] impressoras = new ExecutorService[NUM_IMPRESSORAS];
    private static ExecutorService executorComputadores = Executors.newFixedThreadPool(NUM_COMPUTADORES);
    private static BlockingQueue<DocumentPrintJob> filaDePedidos = new LinkedBlockingQueue<>();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    public static void main(String[] args) {
        for (int i = 0; i < NUM_IMPRESSORAS; i++) {
            impressoras[i] = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                                    new LinkedBlockingQueue<>(CAPACIDADE_POR_IMPRESSORA));
        }

        Thread escalonador = new Thread(new Escalonador());
        escalonador.start();

        for (int i = 1; i <= NUM_COMPUTADORES; i++) {
            final int finalI = i;
            executorComputadores.execute(() -> {
                try {
                    for (int j = 1; j <= 3; j++) {
                        String documento = "Documento " + j + " do Computador " + finalI;
                        DocumentPrintJob job = new DocumentPrintJob(documento, finalI);
                        filaDePedidos.put(job);
                        Thread.sleep(5000);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        executorComputadores.shutdown();
    }

    static class Escalonador implements Runnable {
        private int currentPrinterIndex = 0;

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    DocumentPrintJob job = filaDePedidos.take();
                    boolean documentHandled = false;
                    while (!documentHandled) {
                        ThreadPoolExecutor executor = (ThreadPoolExecutor) impressoras[currentPrinterIndex];
                        if (executor.getQueue().remainingCapacity() > 0) {
                            executor.execute(new Impressora(job.documento, currentPrinterIndex + 1));
                            System.out.println("Computador " + job.computerId + " enviou: " + job.documento + " para a impressora " + (currentPrinterIndex + 1) + " em " + sdf.format(new Date()));
                            documentHandled = true;
                        } else {
                            System.out.println("Impressora " + (currentPrinterIndex + 1) + " cheia. Redirecionando documento " + job.documento + " para outra impressora...");
                            currentPrinterIndex = (currentPrinterIndex + 1) % NUM_IMPRESSORAS;
                        }
                        Thread.sleep(1000);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Impressora implements Runnable {
        private String documento;
        private int id;

        public Impressora(String documento, int id) {
            this.documento = documento;
            this.id = id;
        }

        @Override
        public void run() {
            try {
                System.out.println("Imprimindo: " + documento + " na impressora " + id + " em " + sdf.format(new Date()));
                Thread.sleep(1000);
                System.out.println("Documento impresso: " + documento + " na impressora " + id + " em " + sdf.format(new Date()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class DocumentPrintJob {
        String documento;
        int computerId;

        DocumentPrintJob(String documento, int computerId) {
            this.documento = documento;
            this.computerId = computerId;
        }
    }
}
