package fr.unice.polytech.securenotes.api.controller;

import fr.unice.polytech.securenotes.api.dto.note.*;
import fr.unice.polytech.securenotes.models.Note;
import fr.unice.polytech.securenotes.models.Share;
import fr.unice.polytech.securenotes.replication.ReplicationEntityType;
import fr.unice.polytech.securenotes.replication.ReplicationJournal;
import fr.unice.polytech.securenotes.services.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {
    private final NoteService noteService;
    private final ReplicationJournal journal;

    public NoteController(NoteService noteService, ReplicationJournal journal) {
        this.noteService = noteService;
        this.journal = journal;
    }

    // BASE

    @PostMapping("/create")
    public ResponseEntity<NoteResponse> create(@RequestBody CreateNoteRequest request, @AuthenticationPrincipal String userId) {
        try {
            Note note = noteService.createNote(userId, request.title(), request.content());

            journal.add(ReplicationEntityType.NOTE, note, "create");

            return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(note));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/get")
    public ResponseEntity<NoteResponse> getNote(@RequestBody NoteRequest request, @AuthenticationPrincipal String userId) {
        try {
            Note note = noteService.getNote(request.noteId(), userId);
            return ResponseEntity.ok(mapToResponse(note));
        } catch (SecurityException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/update")
    public ResponseEntity<NoteResponse> update(@RequestBody UpdateNoteRequest request, @AuthenticationPrincipal String userId) {
        try {
            Note note = noteService.updateNote(request.noteId(), userId, request.title(), request.content());
            journal.add(ReplicationEntityType.NOTE, note, "update");
            return ResponseEntity.ok(mapToResponse(note));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // Verrouillée
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> delete(@RequestBody NoteRequest request, @AuthenticationPrincipal String userId) {
        try {
            Note note = noteService.deleteNote(request.noteId(), userId);
            journal.add(ReplicationEntityType.NOTE, note, "delete");
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // --- PARTAGE ---

    @PostMapping("/get-owner-name")
    public ResponseEntity<GetOwnerNameResponse> getOwnerName(@RequestBody GetOwnerNameRequest request, @AuthenticationPrincipal String userId) {
        try {
            return ResponseEntity.ok(new GetOwnerNameResponse(noteService.getOwnerName(request.ownerId())));
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/share")
    public ResponseEntity<Void> share(@RequestBody ShareRequest request, @AuthenticationPrincipal String userId) {
        try {
            Note note = noteService.shareNote(request.noteId(), userId, request.targetUserId(), request.permission());
            journal.add(ReplicationEntityType.NOTE, note, "update");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/revoke-share")
    public ResponseEntity<Void> revokeShare(@RequestBody RevokeShareRequest request, @AuthenticationPrincipal String userId) {
        try {
            Note note = noteService.revokeShare(request.noteId(), userId, request.targetUserId());
            journal.add(ReplicationEntityType.NOTE, note, "update");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // --- LISTE DES ACCÈS (PARTAGES) ---

    @GetMapping("/{noteId}/access")
    // TODO: change return to dto
    public ResponseEntity<List<Share>> getNoteAccessList(@PathVariable String noteId, @AuthenticationPrincipal String userId) {
        try {
            Note note = noteService.getNote(noteId, userId);

            if (!note.getOwnerId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok(note.getShares());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // --- VERROUILLAGE ---

    @PostMapping("/lock")
    public ResponseEntity<Void> lock(@RequestBody NoteRequest request, @AuthenticationPrincipal String userId) {
        try {
            Note note = noteService.lockNote(request.noteId(), userId);
            journal.add(ReplicationEntityType.NOTE, note, "update");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/unlock")
    public ResponseEntity<Void> unlock(@RequestBody NoteRequest request, @AuthenticationPrincipal String userId) {
        try {
            Note note = noteService.unlockNote(request.noteId(), userId);
            journal.add(ReplicationEntityType.NOTE, note, "update");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // --- LISTES ---

    @GetMapping("/user")
    // TODO: should return a List<NoteResponse>
    public ResponseEntity<List<Note>> getUserNotes(@AuthenticationPrincipal String userId) {
        try {
            List<Note> userNotes = noteService.getUserNotes(userId);
            userNotes.addAll(noteService.getSharedNotes(userId));
            return ResponseEntity.ok(userNotes);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    private NoteResponse mapToResponse(Note note) {
        return new NoteResponse(
                note.getId(),
                note.getOwnerId(),
                note.getTitle(),
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt(),
                note.getLastModifiedBy(),
                note.getShares(),
                note.getLockedBy()
        );
    }
}
