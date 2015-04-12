package org.savvas.milked.controller;

import org.savvas.milked.controller.error.ValidationException;
import org.savvas.milked.controller.request.GroupInviteRequest;
import org.savvas.milked.domain.GroupInvite;
import org.savvas.milked.service.GroupInviteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

@RestController
public class GroupInviteController {

    private final GroupInviteService groupInviteService;

    @Autowired
    public GroupInviteController(@RequestBody GroupInviteService groupInviteService) {
        this.groupInviteService = groupInviteService;
    }

    @RequestMapping(value = "/group-user", method = RequestMethod.POST)
    public ResponseEntity inviteUser(@RequestBody @Valid GroupInviteRequest groupInviteRequest, BindingResult validation) {
        if (validation.hasErrors()) {
            throw new ValidationException("groupId");
        }
        Long groupId = groupInviteService.inviteGroupUser(groupInviteRequest);
        URI groupUserLocationURI = URI.create("/group-user/" + groupId);
        return ResponseEntity.created(groupUserLocationURI).build();
    }

    @RequestMapping(value = "/group-user/{uuid}/activate", method = RequestMethod.POST)
    public ResponseEntity activateGroupUser(@PathVariable("uuid") String uuid) {
        Long activatedGroupUserId = groupInviteService.activateGroupUser(uuid);
        URI activatedGroupUserLocationURI = URI.create("/group-user/" + activatedGroupUserId + "/activate");
        return ResponseEntity.created(activatedGroupUserLocationURI).build();
    }

    @RequestMapping(value = "/group-user/{id}", method = RequestMethod.GET)
    public GroupInvite getGroupInvite(@PathVariable("id") Long id) {
        return groupInviteService.getGroupInvite(id);
    }

    @RequestMapping(value = "/group-user/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteGroupUser(@PathVariable("id") Long id) {
        groupInviteService.deleteGroupUser(id);
        return ResponseEntity.ok().build();
    }
}
