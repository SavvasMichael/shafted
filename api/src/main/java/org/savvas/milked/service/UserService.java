package org.savvas.milked.service;

import org.savvas.milked.controller.request.RegistrationRequest;
import org.savvas.milked.controller.request.UpdateUserRequest;
import org.savvas.milked.domain.MilkedUser;
import org.savvas.milked.domain.MilkedUserRepository;
import org.savvas.milked.domain.MilkingGroup;
import org.savvas.milked.domain.MilkingGroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Service
public class UserService {
    private MilkedUserRepository milkedUserRepository;
    private MilkingGroupRepository milkingGroupRepository;
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(MilkedUserRepository repository, MilkingGroupRepository milkingGroupRepository) {
        this.milkedUserRepository = repository;
        this.milkingGroupRepository = milkingGroupRepository;
    }

    public void sendRegistrationEmail(MilkedUser user) {
        try {
            String host = "smtp.gmail.com";
            String from = "savvas.a.michael@gmail.com";
            String pass = "pass";
            Properties props = System.getProperties();
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.user", from);
            props.put("mail.smtp.password", pass);
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.debug", "true");

            Session session = Session.getInstance(props, new GMailAuthenticator("savvas.a.michael@gmail.com", "cstrike54321"));
            MimeMessage message = new MimeMessage(session);
            Address fromAddress = new InternetAddress(from);
            Address toAddress = new InternetAddress(user.getEmail());
            message.setFrom(fromAddress);
            message.setRecipient(Message.RecipientType.TO, toAddress);
            message.setSubject("Welcome to milked, " + user.getName() + " !");
            message.setText("Activation link: http://localhost:7070/activation/" + user.getUuid());
            Transport transport = session.getTransport("smtp");
            transport.connect(host, from, pass);
            message.saveChanges();
            Transport.send(message);
            transport.close();
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
        }
    }

    public void sendInvitationEmail(MilkedUser user, Long groupId) {

        try {
            String host = "smtp.gmail.com";
            String from = "savvas.a.michael@gmail.com";
            String pass = "pass";
            Properties props = System.getProperties();
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.user", from);
            props.put("mail.smtp.password", pass);
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.debug", "true");

            Session session = Session.getInstance(props, new GMailAuthenticator("savvas.a.michael@gmail.com", "cstrike54321"));
            MimeMessage message = new MimeMessage(session);
            Address fromAddress = new InternetAddress(from);
            Address toAddress = new InternetAddress(user.getEmail());
            message.setFrom(fromAddress);
            message.setRecipient(Message.RecipientType.TO, toAddress);
            message.setSubject("You are invited to join milked!");
            message.setText("A friend has invited you to join a group. If you wish to join click: http://localhost:7070/user/" + user.getId() + "/group/" + groupId + "/accept");
            Transport transport = session.getTransport("smtp");
            transport.connect(host, from, pass);
            message.saveChanges();
            Transport.send(message);
            transport.close();
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
        }
    }

    public MilkedUser createUser(RegistrationRequest registrationRequest) {
        String uuid = UUID.randomUUID().toString();
        MilkedUser milkedUser = new MilkedUser(registrationRequest.getEmail(), registrationRequest.getName(), registrationRequest.getPassword(), uuid);
        MilkedUser savedMilkedUser = milkedUserRepository.save(milkedUser);
        sendRegistrationEmail(savedMilkedUser);
        return savedMilkedUser;
    }

    public MilkedUser createInvitedUser(RegistrationRequest registrationRequest, Long groupId) {
        String uuid = UUID.randomUUID().toString();
        MilkedUser milkedUser = new MilkedUser(registrationRequest.getEmail(), "", registrationRequest.getPassword(), uuid);
        MilkedUser savedMilkedUser = milkedUserRepository.save(milkedUser);
        sendInvitationEmail(milkedUser, groupId);
        return savedMilkedUser;
    }
    public MilkedUser getUser(Long id) {
        return milkedUserRepository.findOne(id);
    }

    public boolean userExists(String email) {
        return milkedUserRepository.findByEmail(email) != null;
    }

    public List<MilkingGroup> getUserGroups(Long userId) {
        MilkedUser user = milkedUserRepository.findOne(userId);
        List<MilkingGroup> groupsContainingUserId = milkingGroupRepository.findGroupsContainingUserId(user);
        return groupsContainingUserId;
    }

    public void leaveGroup(Long userId, Long groupId) {
        MilkingGroup group = milkingGroupRepository.findById(groupId);
        group.deleteUserFromGroup(milkedUserRepository.findOne(userId));
        milkingGroupRepository.save(group);
    }

    public List<MilkedUser> getGroupUsers(Long groupId) {
        MilkingGroup group = milkingGroupRepository.findById(groupId);
        if (group == null) {
            return new ArrayList<>();
        }
        return group.getMilkedUsers();
    }
}
