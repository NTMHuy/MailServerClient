package client;

import common.Email;
import common.User;
import common.rmi.MailRemote;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class RMIClient {

    private MailRemote mailRemote;

    public RMIClient() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            mailRemote = (MailRemote) registry.lookup("MailService");
            System.out.println("✅ Connected to RMI Server");
        } catch (Exception e) {
            System.err.println("❌ Cannot connect to RMI Server");
            e.printStackTrace();
        }
    }

    public void logout(String username) {
        try {
            mailRemote.logout(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= USER =================

    public boolean register(User user) {
        try {
            return mailRemote.register(user);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public User login(String username, String aesPassword) {
        try {
            return mailRemote.login(username, aesPassword);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    // ================= MAIL =================

    public String sendEmail(Email email) {
        try {
            return mailRemote.sendEmail(email);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    public List<Email> getEmails(String username, String folderType) {
        try {
            return mailRemote.getEmails(username, folderType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean markNotSpam(int emailId) {
        try {
            return mailRemote.markNotSpam(emailId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteEmail(int emailId) {
        try {
            return mailRemote.deleteEmail(emailId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
