package common.rmi;

import common.Email;
import common.User;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface MailRemote extends Remote {

    //User ----------
    boolean register(User user) throws RemoteException;
    User login(String username, String password) throws RemoteException;
    User logout(String username) throws RemoteException;
    //Mail ----------
    String sendEmail(Email email) throws RemoteException;
    List<Email> getEmails(String username, String folderType) throws RemoteException;
    boolean markNotSpam(int emailId) throws RemoteException;
    boolean deleteEmail(int emailId) throws RemoteException;
}
