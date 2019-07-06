package Server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;

public class Server {

    static int id = 0;
    static HashMap<Integer,String> mapMessages = new HashMap<>();

    public Server()  {}

    public void starting(){
        System.out.println("server start");
        InetSocketAddress inetSocketAddress = new InetSocketAddress(3113);
        HttpServer server = null;
        try {
            server = HttpServer.create(inetSocketAddress,5);

            server.createContext("/api/post-msg",   new PostMsessageHandler());
            server.createContext("/api/get-hystory",new GetHystoryHandler());

            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class PostMsessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();

            if(query != null) {
                String[] strQuery = query.split("&message=|name=|&id=");
                id = Integer.parseInt(strQuery[3]);/*получаем id чата*/
                String response = ""+strQuery[1] + ":" + strQuery[2]+"\n";/*создаем ответ из имени и сообщения*/

                if (mapMessages.get(id) == null) {
                    mapMessages.put(id, response);/*если переписка чата с данным id еще не существует, создаем*/
                } else {
                    mapMessages.put(id, mapMessages.get(id)+response);/*если переписка чата с данным id существует, дозаписываем*/
                }

                System.out.println("response: "+response);
            }

            Server.addCors(exchange);


            exchange.sendResponseHeaders(200, 0);

            OutputStream os = exchange.getResponseBody();
            os.close();
        }


    }

    static class GetHystoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            String query = exchange.getRequestURI().getQuery();
            if(query != null) {
                String[] strQuery = query.split("id=");
                id = Integer.parseInt(strQuery[1]);
            }

            String answer = mapMessages.get(id)==null?"":mapMessages.get(id);

            Server.addCors(exchange);
            exchange.sendResponseHeaders(200, answer.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(answer.getBytes());
            os.close();

        }
    }



    static void addCors(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods","GET");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    }

}
