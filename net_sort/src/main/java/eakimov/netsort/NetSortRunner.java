package eakimov.netsort;

import eakimov.netsort.clients.ClientBase;
import eakimov.netsort.clients.OneConnectionTCPClient;
import eakimov.netsort.clients.ManyConnectionsTCPClient;
import eakimov.netsort.clients.UDPClient;
import eakimov.netsort.servers.*;
import eakimov.netsort.settings.ClientSettings;
import eakimov.netsort.settings.RunArguments;
import eakimov.netsort.settings.RunSettings;
import eakimov.netsort.settings.ServerSettings;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NetSortRunner implements Runnable {
    private final RunSettings settings;
    private final ProgressHandler progressHandler;
    private final Results results;

    public NetSortRunner(RunSettings settings, ProgressHandler progressHandler) {
        this.settings = settings;
        this.progressHandler = progressHandler;
        results = new Results(settings);
    }

    @Override
    public void run() {
        progressHandler.setProgress(settings.getProgressStart());
        for (int n = settings.getnStart(); n <= settings.getnEnd(); n += settings.getnStep()) {
            for (int m = settings.getmStart(); m <= settings.getmEnd(); m += settings.getmStep()) {
                for (int d = settings.getdStart(); d <= settings.getdEnd(); d += settings.getdStep()) {
                    try {
                        runOne(new RunArguments(settings.getArchId(), settings.getX(), n, m, d));
                    } catch (IOException e) {
                        progressHandler.setError(e.getMessage());
                    }
                    progressHandler.setProgress(settings.getProgressValue(n, m, d));
                }
            }
        }
        progressHandler.setCompleted();
    }

    private void runOne(RunArguments arguments) throws IOException {
        Metrics metrics = new Metrics();
        ServerSettings serverSettings = new ServerSettings(arguments, NetSortOptions.serverPort);
        ClientSettings clientSettings = new ClientSettings(arguments, serverSettings.getAddress());

        Architectures arch = Architectures.values()[arguments.getArch()];
        ServerBase server = createServer(arch, serverSettings, metrics);
        List<ClientBase> clients = Stream.generate(() -> createClient(arch, clientSettings, metrics))
                .limit(arguments.getM())
                .collect(Collectors.toList());

        Thread serverThread = new Thread(server);
        List<Thread> clientThreads = clients.stream().map(Thread::new).collect(Collectors.toList());

        serverThread.start();
        clientThreads.forEach(Thread::start);

        try {
            for (Thread clientThread : clientThreads) {
                clientThread.join();
            }
            server.stop();
            serverThread.join();
        } catch (InterruptedException ignored) {
        }

        results.addResult(new Results.Result(arguments, metrics));
    }

    public Results getResults() {
        return results;
    }


    private ServerBase createServer(Architectures arch, ServerSettings settings, Metrics metrics) throws IOException {
        switch (arch) {
            case TCP1:
                return new TCPServer1(settings, metrics);
            case TCP2:
                return new TCPServer2(settings, metrics);
            case TCP3:
                return new TCPServer3(settings, metrics);
            case TCP4:
                return new TCPServer4(settings, metrics);
            case TCP5:
                return new TCPServer5(settings, metrics);
            case UDP1:
                return new UDPServer1(settings, metrics);
            case UDP2:
                return new UDPServer2(settings, metrics);
            default:
                throw new IllegalArgumentException();
        }
    }

    private ClientBase createClient(Architectures arch, ClientSettings settings, Metrics metrics) {
        switch (arch) {
            case TCP1:
            case TCP2:
            case TCP3:
            case TCP5:
                return new OneConnectionTCPClient(settings, metrics);
            case TCP4:
                return new ManyConnectionsTCPClient(settings, metrics);
            case UDP1:
            case UDP2:
                return new UDPClient(settings, metrics);
            default:
                throw new IllegalArgumentException();
        }
    }

    private enum Architectures {
        TCP1, TCP2, TCP3, TCP4, TCP5, UDP1, UDP2
    }
}
