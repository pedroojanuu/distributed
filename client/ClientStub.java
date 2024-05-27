package client;

import java.net.*;

import javax.net.ssl.SSLSocketFactory;

import java.io.*;
 
public class ClientStub {
    Socket socket;
    
    public void createSocket(String hostname, int port) throws UnknownHostException, IOException {
        String working_dir = System.getProperty("user.dir");
        String keyFilePath = working_dir + "/server/certificate/keystore.jks";
        String keyPassword = "trabalhoCPD";

        try {
            System.setProperty("javax.net.ssl.trustStore", keyFilePath);
            System.setProperty("javax.net.ssl.trustStorePassword", keyPassword);

            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = factory.createSocket(hostname, port);
        } catch (UnknownHostException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }
    }

    public void send(String message) throws IOException {
        try {

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println(message);
 
        } catch (IOException ex) {
            throw ex;
        }
    }
 
    public String receive() throws IOException {
        String message = "";
 
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
 
            message = reader.readLine();
 
        } catch (IOException ex) {
            throw ex;
        }

        return message;
    }

    public void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Error closing socket: " + e.getMessage());
        }
    }
}
