package fr.unice.polytech.securenotes.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Share {
    private String userId;
    private SharePermission permission;

    public enum SharePermission {
        READ_ONLY("READ_ONLY"),
        READ_WRITE("READ_WRITE");

        private final String label;

        SharePermission(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
    
    public Share() {}
    
    public Share(String userId, String permission) {
        this.userId = userId;
        this.permission = SharePermission.valueOf(permission);
    }
}