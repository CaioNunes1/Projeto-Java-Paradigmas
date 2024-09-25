public class Impressora {
    private int id;
    private boolean ocupada;

    public Impressora(int id) {
        this.id = id;
        this.ocupada = false;
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

    public void imprimir(String documento, SistemaImpressao sistema) throws InterruptedException {
        synchronized (this) {
            System.out.println(sistema.getTimeStamp() + " - [Impressora " + (id + 1) + "] Imprimindo: " + documento);
            Thread.sleep((long) (Math.random() * 5000));
            System.out.println(sistema.getTimeStamp() + " - [Impressora " + (id + 1) + "] Documento impresso: " + documento);
        }
    }
}