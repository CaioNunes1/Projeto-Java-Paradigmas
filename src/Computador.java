import java.util.Queue;

public class Computador implements Runnable {
    private int id;
    private Queue<String> filaDePedidos;
    private SistemaImpressao sistema;

    public Computador(int id, Queue<String> filaDePedidos, SistemaImpressao sistema) {
        this.id = id;
        this.filaDePedidos = filaDePedidos;
        this.sistema = sistema;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= 3; i++) {
                String documento = "Documento " + i + " do Computador " + id;
                synchronized (filaDePedidos) {
                    filaDePedidos.add(documento);
                    sistema.incrementarDocumentosEnviados();
                    System.out.println(sistema.getTimeStamp() + " - [Computador " + id + "] enviou: " + documento);
                }
                Thread.sleep((long) (Math.random() * 3000));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
