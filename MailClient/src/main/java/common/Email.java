package common;

import java.io.Serializable;
import java.sql.Timestamp;

public class Email implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String sender;
    private String receiver;
    private String subject;
    private String body;
    private String folder; // INBOX hoặc SPAM
    private Timestamp createdAt;

    public Email() {}
    public Email(String sender, String receiver, String subject, String body) {
        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.body = body;
    }
    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getFolder() { return folder; }
    public void setFolder(String folder) { this.folder = folder; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() { return "[" + id + "] Từ: " + sender + " | " + subject; }
}
