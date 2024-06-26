package object_orienters.techspot.content;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import object_orienters.techspot.comment.Comment;
import object_orienters.techspot.post.Post;
import object_orienters.techspot.postTypes.DataType;
import object_orienters.techspot.profile.Profile;
import object_orienters.techspot.reaction.Reaction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name = "ReactableContent")
@Data
public abstract class ReactableContent extends Content {

    @ManyToOne(cascade = CascadeType.ALL)
    private Profile contentAuthor;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "datatype_id", referencedColumnName = "datatype_id", nullable = true)
    private DataType mediaData;
    private String textData;

    @JsonIgnore
    @OneToMany(mappedBy = "content", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reaction> reactions;

    @JsonIgnore
    @OneToMany(mappedBy = "commentedOn", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;
    private int numOfComments;
    private int numOfReactions;

    public ReactableContent() {
        this.reactions = new ArrayList<>();
        this.comments = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Post))
            return false;
        return this.getContentID() != null && this.getContentID().equals(((Post) o).getContentID());
    }

    public Profile getContentAuthor() {
        return contentAuthor;
    }
}
