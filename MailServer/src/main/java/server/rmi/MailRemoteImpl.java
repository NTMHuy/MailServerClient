package server.rmi;

import common.Email;
import common.User;
import common.rmi.MailRemote;
import server.service.MailService;
import server.service.UserService;
import server.ui.ServerState;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class MailRemoteImpl extends UnicastRemoteObject
        implements MailRemote {

    private final MailService mailService;
    private final UserService userService;

    public MailRemoteImpl() throws RemoteException {
        super();
        this.mailService = new MailService();
        this.userService = new UserService();
    }

    // ================= USER =================

    @Override
    public boolean register(User user) throws RemoteException {
        return userService.register(user);
    }

    @Override
    public User login(String username, String aesPassword)
            throws RemoteException {

        User user = userService.login(username, aesPassword);

        if (user != null) {
            ServerState.getInstance().userOnline(user);
        }

        return userService.login(username, aesPassword);
    }

    @Override
    public boolean logout(String username)
            throws RemoteException {

        boolean ok = userService.logout(username);

        if (ok) {
            User u = new User();
            u.setUsername(username);
            ServerState.getInstance().userOffline(u);
        }

        return userService.logout(username);
    }
    // ================= MAIL =================

    @Override
    public String sendEmail(Email email) throws RemoteException {
        return mailService.sendEmail(email);
    }

    @Override
    public List<Email> getEmails(String username, String folderType)
            throws RemoteException {
        return mailService.getEmails(username, folderType);
    }

    @Override
    public boolean markNotSpam(int emailId) throws RemoteException {
        return mailService.markNotSpam(emailId);
    }

    @Override
    public boolean deleteEmail(int emailId) throws RemoteException {
        return mailService.deleteEmail(emailId);
    }
}
