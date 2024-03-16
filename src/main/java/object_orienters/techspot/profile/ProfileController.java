package object_orienters.techspot.profile;

import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
public class ProfileController {
    private final ProfileModelAssembler assembler;
    private final ImpleProfileService profileService;

    public ProfileController(ProfileModelAssembler assembler, ImpleProfileService profileService) {
        this.assembler = assembler;
        this.profileService = profileService;
    }

    // TODO: IMPLEMENT THIS METHOD
    // @GetMapping("path")
    // public String getUserPosts(@RequestParam String param) {
    // return new String();
    // }

    // TODO: IMPLEMENT THIS METHOD
    // @GetMapping("path")
    // public String getUserChats(@RequestParam String param) {
    // return new String();
    // }

    // get user profile
    @GetMapping("/profiles/{username}")
    public EntityModel<Profile> one(@PathVariable String username) throws UserNotFoundException {
        return assembler.toModel(profileService.getUserByUsername(username));
    }

    // create new user profile
    @PostMapping("/profiles")
    public ResponseEntity<EntityModel<Profile>> createUser(@RequestBody Profile newUser) {
        EntityModel<Profile> entityModel = assembler.toModel(profileService.createNewUser(newUser));
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    // update user profile
    @PutMapping("/profiles/{username}")
    ResponseEntity<EntityModel<Profile>> updateProfile(@RequestBody Profile newUser, @PathVariable String username)
            throws UserNotFoundException {
        Profile updatedUser = profileService.updateUserProfile(newUser, username);
        EntityModel<Profile> entityModel = assembler.toModel(updatedUser);
        return ResponseEntity.ok().location(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    };

    // get user followers
    @GetMapping("/profiles/{username}/followers")
    public ResponseEntity<CollectionModel<EntityModel<Profile>>> Followers(@PathVariable String username)
            throws UserNotFoundException {
        List<EntityModel<Profile>> followers = profileService.getUserFollowersByUsername(username).stream()
                .map(userModel -> assembler.toModel(userModel))
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(followers,
                linkTo(methodOn(ProfileController.class).one(username)).withSelfRel()));
    }

    // get specific user follower
    @GetMapping("/profiles/{username}/followers/{followerUserName}")
    public EntityModel<Profile> getSpecificFollower(@PathVariable String username,
            @PathVariable String followerUserName) {
        Profile follower = profileService.getFollowerByUsername(username, followerUserName);
        return assembler.toModel(follower);

    }

    // get user followers
    @GetMapping("/profiles/{username}/following")
    public ResponseEntity<CollectionModel<EntityModel<Profile>>> Following(@PathVariable String username)
            throws UserNotFoundException {
        List<EntityModel<Profile>> following = profileService.getUserFollowingByUsername(username).stream()
                .map(userModel -> assembler.toModel(userModel))
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(following,
                linkTo(methodOn(ProfileController.class).one(username)).withSelfRel()));
    }

    // get specific user following
    @GetMapping("/profiles/{username}/following/{followingUsername}")
    public EntityModel<Profile> getSpecificFollowing(@PathVariable String username,
            @PathVariable String followingUsername) throws UserNotFoundException {
        Profile follower = profileService.getFollowingByUsername(username, followingUsername);
        return assembler.toModel(follower);
    }

    // add new follower to user
    @PostMapping("/profiles/{username}/followers")
    public EntityModel<Profile> newFollower(@PathVariable String username, @RequestBody Profile newFollower)
            throws UserNotFoundException {
        return assembler.toModel(profileService.addNewFollower(username, newFollower));
    }

    // delete follower from user
    @DeleteMapping("/profiles/{username}/followers/{UserName}")
    ResponseEntity<Profile> deleteFollower(@PathVariable String username, @RequestBody Profile deletedUser)
            throws UserNotFoundException {
        profileService.deleteFollower(username, deletedUser);
        return ResponseEntity.noContent().build();
    }

}