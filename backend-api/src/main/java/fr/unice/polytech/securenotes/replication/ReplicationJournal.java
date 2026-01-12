package fr.unice.polytech.securenotes.replication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.securenotes.models.Note;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReplicationJournal {
    @Value("${DATA_DIR:/data}")
    private String STORAGE_DIR;

    private File journalFile;
    private final ObjectMapper mapper;

    public ReplicationJournal(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @PostConstruct
    private void init() {
        File replicationDir = new File(STORAGE_DIR, "replication");
        if (!replicationDir.exists()) replicationDir.mkdirs();

        journalFile = new File(replicationDir, "replication_journal.json");

        if (!journalFile.exists()) {
            try {
                writeAtomically(journalFile, new ArrayList<>());
            } catch (IOException e) {
                throw new RuntimeException("Impossible de créer le fichier journal", e);
            }
        }
    }

    public synchronized <T extends Replicable> void add(ReplicationEntityType type, T payload, String operation) throws IOException {
        List<ReplicationEntry> entries = loadAll();
        entries.add(new ReplicationEntry(type, payload, operation, false));
        writeAtomically(journalFile, entries);
    }

    public synchronized List<ReplicationEntry> loadAll() throws IOException {
        if (!journalFile.exists() || journalFile.length() == 0) {
            return new ArrayList<>();
        }

        try {
            return new ArrayList<>(List.of(
                    mapper.readValue(journalFile, ReplicationEntry[].class)
            ));
        } catch (JsonProcessingException e) {
            System.err.println("Corrupted replication journal, resetting");
            writeAtomically(journalFile, new ArrayList<>());
            return new ArrayList<>();
        }
    }

    public synchronized void markReplicated(ReplicationEntry entry) throws IOException {
        List<ReplicationEntry> entries = loadAll();
        for (ReplicationEntry e : entries) {
            if (e.getPayload().getId().equals(entry.getPayload().getId()) && e.getOperation().equals(entry.getOperation())) {
                e.setReplicated(true);
            }
        }
        writeAtomically(journalFile, entries);
    }

    public synchronized List<ReplicationEntry> getPending() throws IOException {
        List<ReplicationEntry> entries = loadAll();
        List<ReplicationEntry> pending = new ArrayList<>();
        for (ReplicationEntry e : entries) {
            if (!e.isReplicated()) pending.add(e);
        }
        return pending;
    }

    public synchronized void clear() throws IOException {
        writeAtomically(journalFile, new ArrayList<>());
    }

    private void writeAtomically(File target, Object data) throws IOException {
        File tmp = new File(target.getAbsolutePath() + ".tmp");

        mapper.writeValue(tmp, data);

        Files.move(
                tmp.toPath(),
                target.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
        );
    }
}
