package fr.unice.polytech.securenotes.replication;

import fr.unice.polytech.securenotes.models.Note;
import fr.unice.polytech.securenotes.models.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

@Service
@Profile("primary")
public class ReplicationService {
    private final ReplicationJournal journal;
    private final RestTemplate restTemplate;

    @Value("${BACKUP_URL}")
    private String peerUrl;

    @Value("${REPLICATION_SECRET}")
    private String internalSecret;

    public ReplicationService(ReplicationJournal journal) {
        this.journal = journal;
        this.restTemplate = new RestTemplate();
    }

    @Scheduled(fixedDelay = 5000)
    public void retryPending() throws IOException {
        List<ReplicationEntry> pending = journal.getPending();
        for (ReplicationEntry entry : pending) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-Internal-Secret", internalSecret);
                headers.setContentType(MediaType.APPLICATION_JSON);


                HttpEntity<Object> request;
                if (entry.getEntityType().equals(ReplicationEntityType.USER)) {
                    request = new HttpEntity<>((User) entry.getPayload(), headers);
                }
                else if (entry.getEntityType().equals(ReplicationEntityType.NOTE)) {
                    request = new HttpEntity<>((Note) entry.getPayload(), headers);
                }
                else
                    throw new IllegalStateException("Unknown entity type");

                restTemplate.postForEntity(peerUrl + "/internal/replication/" + (entry.getEntityType().equals(ReplicationEntityType.USER) ? "user/" : "note/") + entry.getOperation(), request, String.class);
                journal.markReplicated(entry);
            } catch (Exception e) {
                System.err.println("Replication failed: " + e.getMessage() + " next retry in 5s");
            }
        }
    }
}
