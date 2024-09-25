import java.util.Queue;

public class Escalonador implements Runnable {
    private Queue<String> filaDePedidos;
    private SistemaImpressao sistema;

    public Escalonador(Queue<String> filaDePedidos, SistemaImpressao sistema) {
        this.filaDePedidos = filaDePedidos;
        this.sistema = sistema;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (filaDePedidos) {
                if (!filaDePedidos.isEmpty()) {
                    String pedido = filaDePedidos.poll();
                    try {
                        Impressora impressora = sistema.alocarImpressora();
                        if (impressora != null) {
                            sistema.executarTarefa(() -> {
                                try {
                                    impressora.imprimir(pedido, sistema);
                                    sistema.incrementarDocumentosImpressos();
                                    sistema.devolverImpressora(impressora);
                                    sistema.verificarFimImpressao();
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
