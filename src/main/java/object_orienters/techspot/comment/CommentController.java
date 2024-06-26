package object_orienters.techspot.comment;

import object_orienters.techspot.content.ContentNotFoundException;
import object_orienters.techspot.post.ContentIsPrivateException;
import object_orienters.techspot.post.ImplePostService;
import object_orienters.techspot.post.PostController;
import object_orienters.techspot.post.PostNotFoundException;

import org.slf4j.Logger;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/content/{contentID}")
public class CommentController {
    private final CommentModelAssembler assembler;
    private final ImpleCommentService commentService;
    private final ImplePostService postService;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final Logger logger = org.slf4j.LoggerFactory.getLogger(CommentController.class);

    CommentController(CommentModelAssembler commentModelAssembler, ImpleCommentService commentService,
            ImplePostService postService) {
        this.assembler = commentModelAssembler;
        this.commentService = commentService;
        this.postService = postService;
    }

    @GetMapping("/comments")
    public ResponseEntity<?> getComments(@PathVariable long contentID) {
        try {
            logger.info(">>>>Retrieving Comments... @ " + getTimestamp() + "<<<<");
            List<Comment> commentList = commentService.getComments(contentID);
            CollectionModel<EntityModel<Comment>> commentModel = CollectionModel.of(
                    commentList.stream().map(assembler::toModel).collect(Collectors.toList()),
                    linkTo(methodOn(CommentController.class).getComments(contentID)).withSelfRel(),
                    linkTo(methodOn(PostController.class).getPost(contentID,
                            postService.getPost(contentID).getContentAuthor().getUsername()))
                            .withRel("post"));
            logger.info(">>>>Comments Retrieved. @ " + getTimestamp() + "<<<<");
            return ResponseEntity.ok(commentModel);
        } catch (ContentNotFoundException e) {
            logger.info(">>>>Error Occurred: " + e.getMessage() + " @ " + getTimestamp() + "<<<<");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Problem.create().withTitle("Content Not Found").withDetail(e.getMessage()));
        } catch (PostNotFoundException e) {
            logger.info(">>>>Error Occurred: " + e.getMessage() + " @ " + getTimestamp() + "<<<<");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Problem.create().withTitle("Post Not Found").withDetail(e.getMessage()));
        } catch (ContentIsPrivateException e) {
            logger.info(">>>>Error Occurred: " + e.getMessage() + " @ " + getTimestamp() + "<<<<");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Problem.create().withTitle("Unauthorized").withDetail(e.getMessage()));
        }
    }

    @GetMapping("/comments/{commentID}")
    public ResponseEntity<?> getComment(@PathVariable Long commentID, @PathVariable Long contentID) {
        try {
            logger.info(">>>>Retrieving Comment... @ " + getTimestamp() + "<<<<");
            Comment comment = commentService.getComment(commentID);
            EntityModel<Comment> commentModel = assembler.toModel(comment);
            logger.info(">>>>Comment Retrieved. @ " + getTimestamp() + "<<<<");
            return ResponseEntity.ok(commentModel);
        } catch (ContentNotFoundException e) {
            logger.info(">>>>Error Occurred: " + e.getMessage() + " @ " + getTimestamp() + "<<<<");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Problem.create().withTitle("Not Found").withDetail(e.getMessage()));
        }
    }

    @PutMapping("/comments/{commentID}")
    @PreAuthorize("@impleCommentService.isCommentAuthor(authentication.principal.username,#commentID)")
    public ResponseEntity<?> updateComment(@PathVariable long contentID, @PathVariable Long commentID,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "text", required = false) String text) throws IOException {
        try {
            logger.info(">>>>Updating Comment... @ " + getTimestamp() + "<<<<");
            Comment updatedComment = commentService.updateComment(contentID, commentID, file, text);
            EntityModel<Comment> commentModel = assembler.toModel(updatedComment);
            logger.info(">>>>Comment Updated. @ " + getTimestamp() + "<<<<");
            return ResponseEntity.ok(commentModel);
        } catch (ContentNotFoundException | CommentNotFoundException e) {
            logger.info(">>>>Error Occurred: " + e.getMessage() + " @ " + getTimestamp() + "<<<<");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Problem.create().withTitle("Not Found").withDetail(e.getMessage()));
        }
    }

    @PostMapping("/comments")
    @PreAuthorize("#commenter == authentication.principal.username")
    public ResponseEntity<?> addComment(
            @PathVariable long contentID,
            @RequestParam(value = "commenter") String commenter,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "text", required = false) String text) throws IOException {
        try {
            logger.info(">>>>Adding Comment... @ " + getTimestamp() + "<<<<");

            Comment createdComment = commentService.addComment(contentID,
                    commenter, file, text);
            EntityModel<Comment> commentModel = assembler.toModel(createdComment);
            logger.info(">>>>Comment Added. @ " + getTimestamp() + "<<<<");
            return ResponseEntity.status(HttpStatus.CREATED).body(commentModel);
        } catch (IllegalArgumentException | ContentNotFoundException e) {
            logger.info(">>>>Error Occurred: " + e.getMessage() + " @ " + getTimestamp() + "<<<<");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Problem.create().withTitle("Not Found").withDetail(e.getMessage()));
        }
    }

    @DeleteMapping("/comments/{commentID}")
    @PreAuthorize("@impleCommentService.isCommentAuthor(authentication.principal.username,#commentID)")
    public ResponseEntity<?> deleteComment(@PathVariable long contentID, @PathVariable Long commentID) {
        try {
            logger.info(">>>>Comment Added. @ " + getTimestamp() + "<<<<");
            commentService.deleteComment(contentID, commentID);
            logger.info(">>>>Comment Deleted. @ " + getTimestamp() + "<<<<");
            return ResponseEntity.noContent().build();
        } catch (ContentNotFoundException e) {
            logger.info(">>>>Error Occurred: " + e.getMessage() + " @ " + getTimestamp() + "<<<<");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Problem.create().withTitle("Not Found").withDetail(e.getMessage()));
        }
    }

    private static String getTimestamp() {
        return LocalDateTime.now().format(formatter) + " ";
    }

}
