package io.github.mirvmir.identity.application.event;

import io.github.mirvmir.identity.application.service.port.repository.UserRepository;
import io.github.mirvmir.identity.domain.User;
import io.github.mirvmir.profile.api.event.ProfileCompletedEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
@AllArgsConstructor
@Component
public class ProfileEventListener {

    private final UserRepository userRepository;

    @EventListener
    @Transactional
    public void handle(ProfileCompletedEvent event) {
        User user = userRepository.findById(event.userId());

        if (user == null) {
            return;
        }

        user.markProfileCompleted();

        userRepository.save(user);
    }
}