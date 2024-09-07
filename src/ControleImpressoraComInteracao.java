import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class ControleImpressoraComInteracao {
    private static final int NUM_IMPRESSORAS = 2;  // Número de impressoras disponíveis
    private static Semaphore impressoras = new Semaphore(NUM_IMPRESSORAS);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Bem-vindo ao Sistema de Impressão!");
        System.out.println("Número de impressoras disponíveis: " + NUM_IMPRESSORAS);

        // Loop de interação com o usuário
        while (true) {
            System.out.println("Digite 'imprimir' para enviar um documento ou 'sair' para encerrar:");

            // Lê a entrada do usuário
            String comando = scanner.nextLine();

            if (comando.equalsIgnoreCase("sair")) {
                System.out.println("Encerrando o sistema...");
                break;
            } else if (comando.equalsIgnoreCase("imprimir")) {
                System.out.println("Digite o nome do documento para impressão:");
                String documento = scanner.nextLine();

                // Cria uma nova thread para simular o usuário enviando um documento para impressão
                new Thread(new Usuario(documento)).start();
            } else {
                System.out.println("Comando inválido. Tente novamente.");
            }
        }

        scanner.close();
    }

    // Classe que representa um Usuário (Thread)
    static class Usuario implements Runnable {
        private String documento;

        public Usuario(String documento) {
            this.documento = documento;
        }

        @Override
        public void run() {
            try {
                System.out.println("Documento '" + documento + "' está aguardando para imprimir.");

                // Tenta adquirir uma impressora
                impressoras.acquire();
                System.out.println("Documento '" + documento + "' está sendo impresso...");

                // Simula o tempo de impressão
                Thread.sleep((long) (Math.random() * 10000));

                // Libera a impressora após a impressão
                System.out.println("Documento '" + documento + "' foi impresso com sucesso.");
                impressoras.release();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
