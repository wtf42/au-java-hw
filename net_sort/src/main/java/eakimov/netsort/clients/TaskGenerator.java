package eakimov.netsort.clients;

import eakimov.netsort.NetSortException;
import eakimov.netsort.protocol.SortArrayProtocol;
import eakimov.netsort.settings.RunArguments;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TaskGenerator {
    private final RunArguments arguments;
    private final Random random = new Random();

    public TaskGenerator(RunArguments arguments) {
        this.arguments = arguments;
    }

    public SortArrayProtocol.ArrayMessage generateArrayMessage() {
        return SortArrayProtocol.ArrayMessage.newBuilder()
                .addAllValues(random
                        .ints(arguments.getN())
                        .boxed()
                        .collect(Collectors.toList()))
                .build();
    }

    public void validateSortedArray(SortArrayProtocol.ArrayMessage message) throws NetSortException {
        List<Integer> values = message.getValuesList();
        for (int i = 0; i < values.size() - 1; i++) {
            if (values.get(i) > values.get(i + 1)) {
                throw new NetSortException("not sorted!");
            }
        }
    }
}
