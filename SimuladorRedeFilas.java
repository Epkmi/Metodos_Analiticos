import java.io.*;
import java.util.*;

public class SimuladorRedeFilas {

    static class Fila {
        int servidores;
        int capacidade;
        Queue<Double> clientes;
        double[] intervaloChegada;
        double[] intervaloAtendimento;
        Map<String, Double> roteamento;

        public Fila(double[] chegada, double[] atendimento, int servidores, int capacidade) {
            this.servidores = servidores;
            this.capacidade = capacidade;
            this.intervaloChegada = chegada;
            this.intervaloAtendimento = atendimento;
            this.clientes = new LinkedList<>();
            this.roteamento = new HashMap<>();
        }

        public void adicionarRoteamento(String destino, double probabilidade) {
            roteamento.put(destino, probabilidade);
        }

        public boolean adicionaCliente(double tempoChegada) {
            if (clientes.size() < capacidade) {
                clientes.add(tempoChegada);
                return true;
            }
            return false;
        }

        public double gerarTempoAleatorio(double min, double max) {
            return min + new Random().nextDouble() * (max - min);
        }
    }

    static class Simulacao {
        Map<String, Fila> filas;
        double tempoInicio;
        int numAleatorios;
        double tempoAtual;
        int clientesPerdidos;
        Random random;

        public Simulacao(double tempoInicio, int numAleatorios) {
            this.filas = new HashMap<>();
            this.tempoInicio = tempoInicio;
            this.numAleatorios = numAleatorios;
            this.tempoAtual = tempoInicio;
            this.clientesPerdidos = 0;
            this.random = new Random();
        }

        public void adicionarFila(String nome, Fila fila) {
            filas.put(nome, fila);
        }

        public void executar() {
            for (int i = 0; i < numAleatorios; i++) {
                processarEventos();
            }
            reportarResultados();
        }

        public void processarEventos() {
            for (Map.Entry<String, Fila> entry : filas.entrySet()) {
                Fila fila = entry.getValue();
        
                // Processar chegada
                double tempoChegada = fila.gerarTempoAleatorio(fila.intervaloChegada[0], fila.intervaloChegada[1]);
                if (!fila.adicionaCliente(tempoChegada)) {
                    clientesPerdidos++;
                }
        
                // Simular atendimento e roteamento
                double tempoAtendimento = fila.gerarTempoAleatorio(fila.intervaloAtendimento[0], fila.intervaloAtendimento[1]);
                tempoAtual += tempoAtendimento;
        
                // Roteamento para outras filas
                for (Map.Entry<String, Double> roteamento : fila.roteamento.entrySet()) {
                    String destino = roteamento.getKey();
                    double probabilidade = roteamento.getValue();
                    if (random.nextDouble() <= probabilidade) {
                        if ("saida".equals(destino)) {
                            // Tratar fila de saída
                            clientesPerdidos++;
                        } else {
                            Fila filaDestino = filas.get(destino);
                            if (filaDestino != null) {
                                filaDestino.adicionaCliente(tempoAtual);
                            } else {
                                System.err.println("Fila destino não encontrada: " + destino);
                            }
                        }
                    }
                }
            }
        }
        

        public void reportarResultados() {
            System.out.println("Simulação encerrada.");
            System.out.println("Tempo global da simulação: " + tempoAtual);
            System.out.println("Clientes perdidos: " + clientesPerdidos);
        }
    }

    public static void main(String[] args) throws IOException {
        // Carregar dados da configuração
        Simulacao simulacao = carregarConfiguracao("C:\\Users\\USER\\Desktop\\Trabalhos\\Metodos_analiticos\\config_simulacao.txt");

        // Executar a simulação
        if (simulacao != null) {
            simulacao.executar();
        } else {
            System.err.println("Não foi possível carregar a configuração.");
        }
    }

    public static Simulacao carregarConfiguracao(String arquivo) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(arquivo));
        String linha;
        Simulacao simulacao = null;
        double tempoInicio = 0; // Declaração da variável fora do bloco condicional

        while ((linha = reader.readLine()) != null) {
            if (linha.trim().isEmpty()) {
                // Pula linhas vazias
                continue;
            }

            System.out.println("Lendo linha: " + linha); // Log para depuração
            if (linha.startsWith("simulacao:")) {
                linha = reader.readLine();
                if (linha != null && !linha.trim().isEmpty()) {
                    System.out.println("Lendo tempo_inicio: " + linha); // Log para depuração
                    tempoInicio = Double.parseDouble(linha.split(": ")[1]);
                }
                linha = reader.readLine();
                if (linha != null && !linha.trim().isEmpty()) {
                    System.out.println("Lendo num_aleatorios: " + linha); // Log para depuração
                    int numAleatorios = Integer.parseInt(linha.split(": ")[1]);
                    simulacao = new Simulacao(tempoInicio, numAleatorios); // Agora o tempoInicio está definido
                }
            } else if (linha.startsWith("fila_")) {
                String nomeFila = linha.split(":")[0];
                System.out.println("Lendo fila: " + nomeFila); // Log para depuração

                linha = reader.readLine();
                double[] chegada = parseIntervalo(linha.split(": ")[1]);

                linha = reader.readLine();
                double[] atendimento = parseIntervalo(linha.split(": ")[1]);

                linha = reader.readLine();
                int servidores = Integer.parseInt(linha.split(": ")[1]);

                linha = reader.readLine();
                int capacidade = Integer.parseInt(linha.split(": ")[1]);

                Fila fila = new Fila(chegada, atendimento, servidores, capacidade);

                // Adiciona roteamento
                while ((linha = reader.readLine()) != null && linha.startsWith("  ")) {
                    if (linha.trim().isEmpty()) {
                        // Pula linhas vazias
                        continue;
                    }
                    System.out.println("Lendo roteamento: " + linha); // Log para depuração
                    String[] roteamento = linha.split(": ");
                    if (roteamento.length > 1) {
                        String destino = roteamento[0].trim();
                        double probabilidade = Double.parseDouble(roteamento[1]);
                        fila.adicionarRoteamento(destino, probabilidade);
                    }
                }

                simulacao.adicionarFila(nomeFila, fila);
            }
        }

        reader.close();
        return simulacao;
    }

    public static double[] parseIntervalo(String texto) {
        String[] valores = texto.replace("[", "").replace("]", "").split(", ");
        return new double[]{Double.parseDouble(valores[0]), Double.parseDouble(valores[1])};
    }
}
