package fr.unice.polytech.securenotes.models;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Share {
    private String userId;
    private SharePermission permission;
    
    public enum SharePermission {
        READ_ONLY,
        READ_WRITE
    }
    
    public Share() {}
    
    public Share(String userId, SharePermission permission) {
        this.userId = userId;
        this.permission = permission;
    }
}