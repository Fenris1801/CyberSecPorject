package fr.unice.polytech.securenotes.replication;

import fr.unice.polytech.securenotes.models.Note;
import fr.unice.polytech.securenotes.models.User;
import fr.unice.polytech.securenotes.security.AuthService;
import fr.unice.polytech.securenotes.services.NoteService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
@Profile("primary")
public class PullUpdatesService {
    private final AuthService authService;
    private final NoteService noteService;
    private final ReplicationJournal journal;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${BACKUP_URL}")
    private String peerUrl;

    @Value("${REPLICATION_SECRET}")
    private String internalSecret;

    public PullUpdatesService(AuthService authService, NoteService noteService, ReplicationJournal journal) {
        this.authService = authService;
        this.noteService = noteService;
        this.journal = journal;
    }

    public void pullUsersFromSecondary() {
        try {
            long lastTimestamp = authService.getAll().stream()
                    .mapToLong(User::getVersion)
                    .max()
                    .orElse(0);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Secret", internalSecret);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<User[]> response =
                    restTemplate.exchange(
                            peerUrl + "/internal/replication/user/pull-updates?since=" + lastTimestamp,
                            HttpMethod.GET,
                            entity,
                            User[].class
                    );

            User[] updates = response.getBody();

            if (updates != null) {
                for (User user : updates) {
                    authService.saveReplica(user);
                    journal.add(ReplicationEntityType.USER, user, "update"); // register if secondary down
                }
            }
        } catch (Exception e) {
            // secondary down, will retry
        }
    }

    public void pullNotesFromSecondary() {
        try {
            long lastTimestamp = noteService.getAll().stream()
                    .mapToLong(Note::getVersion)
                    .max()
                    .orElse(0);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Secret", internalSecret);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Note[]> response =
                    restTemplate.exchange(
                            peerUrl + "/internal/replication/note/pull-updates?since=" + lastTimestamp,
                            HttpMethod.GET,
                            entity,
                            Note[].class
                    );

            Note[] updates = response.getBody();

            if (updates != null) {
                for (Note note : updates) {
                    noteService.saveReplica(note);
                    journal.add(ReplicationEntityType.NOTE, note, "update"); // register if secondary down
                }
            }
        } catch (Exception e) {
            // secondary down, will retry
        }
    }
}
