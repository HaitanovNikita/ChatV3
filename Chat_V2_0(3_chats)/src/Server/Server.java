package Server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Executors;

public class Server {

    static int id = 0;
    static HashMap<Integer, String> mapMessages = new HashMap<>();
    static HashMap<String, String> mapLoginDetails = new HashMap<>();


    public Server() {
    }

    public void starting() {
        System.out.println("server start");
        InetSocketAddress inetSocketAddress = new InetSocketAddress(3113);
        HttpServer server = null;
        try {
            server = HttpServer.create(inetSocketAddress, 5);
            server.createContext("/api/post-msg", new PostMsessageHandler());
            server.createContext("/api/get-hystory", new GetHystoryHandler());
            server.createContext("/api/register", new RegistrationClient());
            server.createContext("/", new StaticHandler());

            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class StaticHandler implements HttpHandler {

        private static String pathFile ="C:\\Users\\User\\IdeaProjects\\Chat_V2_0(3_chats)\\static\\";

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String answer="";
            String nameFile="";
            String query = exchange.getRequestURI().getQuery();

            if (query != null) {
                String[] strQuery = query.split("/");
                nameFile = strQuery[0];
                answer = readFile(nameFile);
            }
            else{
                nameFile = "login.html";
                answer = readFile(nameFile);
            }

//            addCors(exchange);
            exchange.sendResponseHeaders(200, answer.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(answer.getBytes());
            os.close();
        }

        private String readFile(String nameFile){
            String fileСontents="";
            try(FileReader reader = new FileReader(pathFile+nameFile))
            {
                int c;
                while((c=reader.read())!=-1){
                    //System.out.print((char)c);
                    fileСontents+=(char)c;
                }
//                System.out.println(fileСontents);
            }
            catch(IOException ex){
                System.out.println(ex.getMessage());
            }
            return fileСontents;
        }
    }

    static class PostMsessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            String query = exchange.getRequestURI().getQuery();
            if (query != null) {

                String[] strQuery = query.split("&message=|name=|&id=|&AuthenticationData=");
                id = Integer.parseInt(strQuery[3]);/*получаем id чата*/
                String response = "" + strQuery[1] + ":" + strQuery[2] + "\n";/*создаем ответ из имени и сообщения*/
                String[] authenticationData = strQuery[4].split(" ");
                System.out.println(Arrays.toString(authenticationData));

                validateClientCheck(authenticationData,exchange);

                if (mapMessages.get(id) == null) {
                    mapMessages.put(id, response);/*если переписка чата с данным id еще не существует, создаем*/
                } else {
                    mapMessages.put(id, mapMessages.get(id) + response);/*если переписка чата с данным id существует, дозаписываем*/
                }

                System.out.println("response: " + response);
            }

//            Server.addCors(exchange);
            exchange.sendResponseHeaders(200, 0);

            OutputStream os = exchange.getResponseBody();
            os.close();
        }

        private void validateClientCheck(String[] authenticationData,HttpExchange exchange) throws IOException {
            //Если в чате не авторизированный
            if(mapLoginDetails.get(authenticationData[0])==null){
                System.out.println("Злоумышленик");
                String unauthorized401 = "401 - Unauthorized";
//                    Server.addCors(exchange);
                exchange.sendResponseHeaders(401, unauthorized401.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(unauthorized401.getBytes());
                os.close();
                return;
            }else {
                System.out.println("Все нормально");
            }
        }
    }

    static class GetHystoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            String query = exchange.getRequestURI().getQuery();

            if (query != null) {
                String[] strQuery = query.split("id=");
                id = Integer.parseInt(strQuery[1]);
            }

            String answer = mapMessages.get(id) == null ? "" : mapMessages.get(id);

//            Server.addCors(exchange);
            exchange.sendResponseHeaders(200, answer.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(answer.getBytes());
            os.close();

        }
    }

    static class RegistrationClient implements HttpHandler {
        private String answer = "";
        private String login = "";
        private String password = "";

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            if (query != null) {
                String[] arrQuery = query.split("&password=|login=|&id=");
                /*id = Integer.parseInt(arrQuery[3]);*//*получаем id чата*/
                String id = arrQuery[3];
                login = arrQuery[1];
                password = arrQuery[2];
                checkID(id);
            }
            System.out.println("answer about login: " + answer);
//            Server.addCors(exchange);
            exchange.sendResponseHeaders(200, answer.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(answer.getBytes());
            os.close();


        }

        private void logIn() {
            if ((login == null && login.isEmpty()) && (password == null && password.isEmpty())) {
                answer = "Enter password or login";
                return;
            }
            if (mapLoginDetails.get(login) != null) {
                String[] arr = mapLoginDetails.get(login).split("login:| password:");//данные из хранилища
                if (login.equals(arr[1]) && password.equals(arr[2])) {
                    answer = "login successful";
                } else {
                    answer = "Wrong login or password";
                }
            } else {
                answer = "Customer with such data does not exist, register!";
            }
        }

        private void registration() {
            String value = "login:" + login + " password:" + password;
            if (mapLoginDetails.get(login) == null) {
                mapLoginDetails.put(login, value);
                System.out.println("login:"+login   );
                answer = "Registration completed successfully!";
            } else {
                answer = "A user with this login already exists, select something else.";
            }
            System.out.println(answer);
        }

        private void checkID(String id) {
            switch (id) {
                case "registration":
                    registration();
                    break;
                case "logIn":
                    logIn();
                    break;
                default:
                    System.out.println("ID - not found!");
                    break;
            }

        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RegistrationClient that = (RegistrationClient) o;
            return answer.equals(that.answer) &&
                    login.equals(that.login);
        }

        @Override
        public int hashCode() {
            return Objects.hash(answer, login);
        }
    }


    static void addCors(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    }

}
