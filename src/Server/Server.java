package Server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Server {

    static int id = 0;
    static String[] arrMessages = new String[4];


    public Server()  {}

    public void starting(){
        for(int i=0;i<arrMessages.length;i++){
            arrMessages[i]="";
        }
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
                id = Integer.parseInt(strQuery[3]);
                String response = strQuery[1] + ":" + strQuery[2];
                arrMessages[id]+=response+"\n";
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

            Server.addCors(exchange);
            exchange.sendResponseHeaders(200, arrMessages[id].getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(arrMessages[id].getBytes());
            os.close();
        }
    }



    static void addCors(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods","GET");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    }

}
