package cs3331.hw5;

import cs3331.hw4.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Author: Cesar Valenzuela
 * Date: 7/27/2018
 * Course:
 * Assignment:
 * Instructor:
 * T.A:
 */
public class NetworkController extends Controller implements NetworkAdapter.MessageListener {

    public NetworkGUI view;
    private Board model;
    private NetworkAdapter network;
    // 1 is Server, 2 is client
    private int isServer;

    private NetworkController(Board model, NetworkGUI gui) {

        super(model, gui);
        view = gui;
        this.model = model;

        view.addOnlineButtonListener(new OnlineListener());
        view.addMouseListener(new ClickAdapter());
        view.addConnectListener(new ClientListener());
        view.addHostButtonListener(new ServerListener());
        view.addDisconnectListener(e -> disconnectListener());
    }

    @Override
    public void messageReceived(NetworkAdapter.MessageType type, int x, int y, int z, int[] others) {
        switch (type) {
            case JOIN:
                int n = JOptionPane.showConfirmDialog(null, "Join client?");
                if (n == JOptionPane.YES_OPTION) {
                    network.writeJoinAck(15);
                } else {
                    network.writeJoinAck();
                }


                break;
            case JOIN_ACK:

                new Thread(() ->{
                    int jc = popUpAns();
                    if(jc == 0){
                        System.out.println("Yes, game joined");
                    } else{
                        System.out.println("Game declined");
                    }
                }).start();


                break;
            case NEW:

                System.out.println("NEW");
                writeNewPopUP();

                break;
            case NEW_ACK:

                System.out.println("NEW ACK");

                break;
            case FILL:
                System.out.println("FILL CASE");
                network.writeFillAck(x,y,0);
                break;
            case FILL_ACK:
                System.out.println("FILL ACK");
                break;
            case QUIT:
                System.out.println("Quitting : One moment");
//                if(popUpAns() == 0){
//                    System.exit(-1);
//                }

                break;
            case CLOSE:
                System.out.println("Connection Severed.");
                disconnectListener();

                break;
            case UNKNOWN:
                System.out.println("unknown");
                break;
        }
    }
    class ClickAdapter extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            int x = view.locateXY(e.getX());
            int y = view.locateXY(e.getY());
            System.out.println("network connected: " + isNetwork());

            if (network != null) {
                network.writeFill(x, y,isServer);
            }
        }
    }

    private void onlineGame(int x, int y){

    }

    private int popUpAns(){
        int reponse = JOptionPane.showConfirmDialog(null, "GAME VERIFICATION");
        if(reponse == JOptionPane.YES_OPTION){
            return 0;
        }else {
            return 1;
        }
    }

    private void writeNewPopUP() {
        int respon = JOptionPane.showConfirmDialog(null, "Hey Someone wants a new board");
        if (respon == JOptionPane.YES_OPTION)
            network.writeNewAck(true);
        else {
            network.writeNewAck(false);
        }
    }

    private void pairAServer(Socket socket) {

        network = new NetworkAdapter(socket);
        network.setMessageListener(this);
        network.receiveMessagesAsync();

    }

    private void pairAsClient(Socket socket) {

        network = new NetworkAdapter(socket);
        network.setMessageListener(this);

        network.writeJoin();
        network.receiveMessagesAsync();
    }

    // creates server
    class ServerListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(() -> {
                try {
                    System.out.println("Server Starting");
                    isServer = 1;
                    ServerSocket servSocket = new ServerSocket(NetworkGUI.getPortNumber());
                    Socket incoming = servSocket.accept();
                    pairAServer(incoming);
                } catch (Exception ex) {
                    System.out.println("SERVER FAILURE");
                }
            }).start();
        }


    }

    /**
     * Client button listener
     */
    class ClientListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(() -> {
                System.out.println("client starting");
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(NetworkGUI.getNameField(), NetworkGUI.getPortField2()), 5000);
                    isServer = 2;

                    pairAsClient(socket);

                } catch (Exception e1) {
                    System.out.println("CLIENT FAILURE");
                }
            }).start();
        }
    }

    static class OnlineListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            NetworkGUI.createOnlinePanel();
        }
    }

    private void disconnectListener() {
        network.close();
        isNetwork();

    }

    private boolean isNetwork() {
        if (network == null) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        Board model = new Board(15);
        NetworkGUI view = new NetworkGUI();
        new NetworkController(model, view);
    }
}
