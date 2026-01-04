package fr.unice.polytech.securenotes.services;

import fr.unice.polytech.securenotes.models.Notes;
import fr.unice.polytech.securenotes.models.User;
import fr.unice.polytech.securenotes.storage.FileStorageService;


@Service
public class NoteService {

    private FileStorageService storageService;

    public NoteService() {
        this.storageService = new FileStorageService();
    }

    public Note createNote(String userId, String title, String content) throws IOException {
        Note note = new Note();
        note.setOwnerId(userId);
        note.setTitle(title);
        note.setContent(content);
        
        storageService.saveNote(note);
        return note;
    }

    public Note getNote(String noteId, String userId) throws IOException {
        Note note = storageService.loadNote(noteId);
        if (note == null) {
            throw new IllegalArgumentException("Note introuvable");
        }
        
        if (!hasReadPermission(note, userId)) {
            throw new SecurityException("Accès refusé");
        }
        
        return note;
    }
    
    public Note updateNote(String noteId, String userId, String title, String content) throws IOException {
        Note note = storageService.loadNote(noteId);
        if (note == null) {
            throw new IllegalArgumentException("Note introuvable");
        }
        
        if (!hasWritePermission(note, userId)) {
            throw new SecurityException("Accès refusé");
        }
        
        if (note.getLockedBy() != null && !note.getLockedBy().equals(userId)) {
            throw new IllegalStateException("Note verrouillée par un autre utilisateur");
        }
        
        note.setTitle(title);
        note.setContent(content);
        note.setUpdatedAt(LocalDateTime.now());
        note.setLastModifiedBy(userId);
        
        storageService.saveNote(note);
        return note;
    }
    
    public void deleteNote(String noteId, String userId) throws IOException {
        Note note = storageService.loadNote(noteId);
        if (note == null) {
            throw new IllegalArgumentException("Note introuvable");
        }
        
        if (!note.getOwnerId().equals(userId)) {
            throw new SecurityException("Seul le propriétaire peut supprimer cette note");
        }
        
        storageService.deleteNote(noteId);
    }
    
    public List<Note> getUserNotes(String userId) throws IOException {
        return storageService.loadUserNotes(userId);
    }
    
    public List<Note> getSharedNotes(String userId) throws IOException {
        return storageService.loadSharedNotes(userId);
    }
    
    public void shareNote(String noteId, String ownerId, String targetUserId, Share.SharePermission permission) throws IOException {
        Note note = storageService.loadNote(noteId);
        if (note == null) {
            throw new IllegalArgumentException("Note introuvable");
        }
        
        if (!note.getOwnerId().equals(ownerId)) {
            throw new SecurityException("Seul le propriétaire peut partager cette note");
        }
        
        Share newShare = new Share(targetUserId, permission);
        note.getShares().removeIf(s -> s.getUserId().equals(targetUserId));
        note.getShares().add(newShare);
        
        storageService.saveNote(note);
    }
    
    public void revokeShare(String noteId, String ownerId, String targetUserId) throws IOException {
        Note note = storageService.loadNote(noteId);
        if (note == null) {
            throw new IllegalArgumentException("Note introuvable");
        }
        
        if (!note.getOwnerId().equals(ownerId)) {
            throw new SecurityException("Seul le propriétaire peut révoquer un partage");
        }
        
        note.getShares().removeIf(s -> s.getUserId().equals(targetUserId));
        storageService.saveNote(note);
    }
    
    public void lockNote(String noteId, String userId) throws IOException {
        Note note = storageService.loadNote(noteId);
        if (note == null) {
            throw new IllegalArgumentException("Note introuvable");
        }
        
        if (!hasWritePermission(note, userId)) {
            throw new SecurityException("Accès refusé");
        }
        
        if (note.getLockedBy() != null && !note.getLockedBy().equals(userId)) {
            throw new IllegalStateException("Note déjà verrouillée");
        }
        
        note.setLockedBy(userId);
        storageService.saveNote(note);
    }
    
    public void unlockNote(String noteId, String userId) throws IOException {
        Note note = storageService.loadNote(noteId);
        if (note == null) {
            throw new IllegalArgumentException("Note introuvable");
        }
        
        if (note.getLockedBy() != null && !note.getLockedBy().equals(userId)) {
            throw new SecurityException("Seul celui qui a verrouillé peut déverrouiller");
        }
        
        note.setLockedBy(null);
        storageService.saveNote(note);
    }
    
    
    private boolean hasReadPermission(Note note, String userId) {
        if (note.getOwnerId().equals(userId)) {
            return true;
        }
        
        return note.getShares().stream()
                .anyMatch(share -> share.getUserId().equals(userId));
    }
    
    private boolean hasWritePermission(Note note, String userId) {
        if (note.getOwnerId().equals(userId)) {
            return true;
        }
        
        return note.getShares().stream()
                .anyMatch(share -> share.getUserId().equals(userId) 
                    && share.getPermission() == Share.SharePermission.READ_WRITE);
    }
}