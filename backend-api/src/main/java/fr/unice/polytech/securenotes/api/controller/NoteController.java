package fr.unice.polytech.securenotes.api.controller;

import fr.unice.polytech.securenotes.api.dto.note.*;
import fr.unice.polytech.securenotes.models.Note;
import fr.unice.polytech.securenotes.services.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    // BASE

    @PostMapping("/create")
    public ResponseEntity<NoteResponse> create(@RequestBody CreateNoteRequest request) {
        try {
            Note note = noteService.createNote(request.userId(), request.title(), request.content());
            return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(note));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/get")
    public ResponseEntity<NoteResponse> getNote(@RequestBody NoteRequest request) {
        try {
            Note note = noteService.getNote(request.noteId(), request.userId());
            return ResponseEntity.ok(mapToResponse(note));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/update")
    public ResponseEntity<NoteResponse> update(@RequestBody UpdateNoteRequest request) {
        try {
            Note note = noteService.updateNote(request.noteId(), request.userId(), request.title(), request.content());
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
    public ResponseEntity<Void> delete(@RequestBody NoteRequest request) {
        try {
            noteService.deleteNote(request.noteId(), request.userId());
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // --- PARTAGE ---

    @PostMapping("/share")
    public ResponseEntity<Void> share(@RequestBody ShareRequest request) {
        try {
            noteService.shareNote(request.noteId(), request.ownerId(), request.targetUserId(), request.permission());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/revoke-share")
    public ResponseEntity<Void> revokeShare(@RequestBody RevokeShareRequest request) {
        try {
            noteService.revokeShare(request.noteId(), request.ownerId(), request.targetUserId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // --- VERROUILLAGE ---

    @PostMapping("/lock")
    public ResponseEntity<Void> lock(@RequestBody NoteRequest request) {
        try {
            noteService.lockNote(request.noteId(), request.userId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/unlock")
    public ResponseEntity<Void> unlock(@RequestBody NoteRequest request) {
        try {
            noteService.unlockNote(request.noteId(), request.userId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // --- LISTES ---

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Note>> getUserNotes(@PathVariable String userId) {
        try {
            return ResponseEntity.ok(noteService.getUserNotes(userId));
        } catch (IOException e) {
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