package fr.unice.polytech.securenotes.replication;

import lombok.Getter;
import lombok.Setter;

@Getter
public class ReplicationEntry<T extends Replicable> {
    @Setter
    private ReplicationEntityType entityType;
    @Setter
    T payload;
    @Setter
    private String operation;
    @Setter
    private boolean replicated;

    public ReplicationEntry() {}

    public ReplicationEntry(ReplicationEntityType entityType, T payload, String operation, boolean replicated) {
        this.entityType = entityType;
        this.payload = payload;
        this.operation = operation;
        this.replicated = replicated;
    }
}
