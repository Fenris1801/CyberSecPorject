package fr.unice.polytech.securenotes.api.controller;

import fr.unice.polytech.securenotes.models.Note;
import fr.unice.polytech.securenotes.models.User;
import fr.unice.polytech.securenotes.security.AuthService;
import fr.unice.polytech.securenotes.services.NoteService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/internal/replication")
@Profile("secondary")
public class ReplicationController {
    private final AuthService authService;
    private final NoteService noteService;

    public ReplicationController(AuthService authService, NoteService noteService) {
        this.authService = authService;
        this.noteService = noteService;
    }

    @PostMapping("/user/create")
    public ResponseEntity<String> user_create(
            @RequestBody User user
    ) {
        try {
            authService.saveReplica(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok("User CREATE Replicated");
    }

    @PostMapping("/user/update")
    public ResponseEntity<String> update(
            @RequestBody User user
    ) {
        try {
            authService.saveReplica(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok("User UPDATE Replicated");
    }

    @GetMapping("/user/pull-updates")
    public List<User> user_pullUpdates(@RequestParam long since) throws IOException {
        List<User> updated = authService.getAll().stream()
                .filter(n -> n.getVersion() > since)
                .toList();
        return updated;
    }

    @PostMapping("/note/create")
    public ResponseEntity<String> note_create(
            @RequestBody Note note
    ) {
        try {
            noteService.saveReplica(note);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok("CREATE Replicated");
    }

    @PostMapping("/note/update")
    public ResponseEntity<String> note_update(
            @RequestBody Note note
    ) {
        try {
            noteService.saveReplica(note);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok("UPDATE Replicated");
    }

    @PostMapping("/note/delete")
    public ResponseEntity<String> note_delete(
            @RequestBody Note note
    ) {
        try {
            noteService.deleteNote(note.getId(), note.getOwnerId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok("DELETE Replicated");
    }

    @GetMapping("/note/pull-updates")
    public List<Note> note_pullUpdates(@RequestParam long since) throws IOException {
        List<Note> updated = noteService.getAll().stream()
                .filter(n -> n.getVersion() > since)
                .toList();
        return updated;
    }
}
