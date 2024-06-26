package object_orienters.techspot.reaction;

import object_orienters.techspot.content.ReactableContent;
import object_orienters.techspot.profile.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    List<Reaction> findByContent(ReactableContent content);

    Optional<Reaction> findByReactorAndContent(Profile reactor, ReactableContent content);

    void deleteByReactionID(Long reactionID);

}
