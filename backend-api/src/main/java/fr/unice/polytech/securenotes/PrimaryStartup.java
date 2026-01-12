package fr.unice.polytech.securenotes;

import fr.unice.polytech.securenotes.replication.PullUpdatesService;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("primary")
public class PrimaryStartup {
    private final PullUpdatesService pullUpdatesService;

    public PrimaryStartup(PullUpdatesService pullUpdatesService) {
        this.pullUpdatesService = pullUpdatesService;
    }

    @PostConstruct
    public void init() {
        pullUpdatesService.pullUsersFromSecondary();
        pullUpdatesService.pullNotesFromSecondary();
    }
}
