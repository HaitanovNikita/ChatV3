package Server;

public class Client {

   private String login="";
   private String password="";
   private String messages="";

    public Client(String login,String password){
        this.login=login;
        this.password=password;
    }

    public void addMessage(String message){
        messages+=message;
    }

    public String getAllMessage(){
        return messages;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
