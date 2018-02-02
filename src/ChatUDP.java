import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by JDeek on 28.01.2018.
 */
//Поскольку поток будет выполнять действие несвязанные с теми, что выполняет основная программа => синхронизация не нужна
// Сокет - абстрактный объект для обмена данными
public class ChatUDP extends JFrame { // JFrame govno i eto nepravilno
    private JTextArea taMain;
    private JTextField tfMessage;
    private JTextField tfName;
    private final String FRM_TITLE = "DOMINION";
    private String clientName = "";
    private final int FRM_LOC_X = 300;
    private final int FRM_LOC_Y = 300;
    private final int FRM_WIDTH = 400;
    private final int FRM_HEIGHT = 400;

    private final int PORT = 9876;
    private final String IP_BROADCAST = "192.168.0.255";
    public String getClientName() {
        return this.clientName;
    }
    private class thdReceiver implements Runnable{ // Это поток. Его нужно создавать в нестатическом окружении чтобы потом вызвать

        //  public String getClientName() {
        //    return this.clientName;
        //   }

        @Override
        public void run(){
            //  super.run();
            try {
                customize();
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }

        private void customize() throws Exception{
            DatagramSocket receiveSocket = new DatagramSocket(PORT);
            Pattern regex = Pattern.compile("[\u0020-\uFFFF]"); // "[\u0020-\uFFFF]" - отсчечет различные символы табуляции новые строки и т.д.
            while(true){
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
                receiveSocket.receive(receivePacket);
                InetAddress IPAddress = receivePacket.getAddress(); // узнаем адресс отправителя
                int port = receivePacket.getPort();
                String sentence = new String(receivePacket.getData()); // Приводим данные в пакете к строке
                Matcher m = regex.matcher(sentence); // К классу Matcher паттерн будет применять своё регулярное выражение
               // taMain.append(": ");
                while(m.find())
                    taMain.append(sentence.substring(m.start(),m.end()));// Вытягиваем данные из пакета
                taMain.append("\r\n");
                taMain.setCaretPosition(taMain.getText().length()); // спускаемся вместе с текстом
            }
        }
    }

    private void btnSendHandler()throws Exception{
        DatagramSocket sendSocket = new DatagramSocket();
        InetAddress IPAdress = InetAddress.getByName(IP_BROADCAST);
        byte[] sendData;
        String sentence = tfName.getText()+": "+ tfMessage.getText();
        tfMessage.setText("");
        //tfName.setText("");
        sendData = sentence.getBytes("UTF-8");
        DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,IPAdress,PORT);
        sendSocket.send(sendPacket);
    }


    private void framDraw(JFrame frame){
        tfMessage  = new JTextField("Enter your message");
        tfName = new JTextField("Enter your name");
        taMain = new JTextArea(FRM_HEIGHT/19,50);
        JScrollPane spMain = new JScrollPane(taMain);
        spMain.setLocation(0,0);
        taMain.setLineWrap(true);
        taMain.setEditable(false);
        JButton btnSend = new JButton();
        btnSend.setText("Send");
        btnSend.setToolTipText("Broadcast a message");
        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // если имя клиента, и сообщение непустые, то отправляем сообщение
                if (!tfMessage.getText().trim().isEmpty() && !tfName.getText().trim().isEmpty()) {
                    clientName = tfName.getText();
                    try{
                        btnSendHandler();
                    }
                    catch(Exception ex){
                        ex.printStackTrace();}
                    // фокус на текстовое поле с сообщением
                    tfMessage.grabFocus();
                }
            }
        });
        tfName.addFocusListener(new FocusAdapter() {
            @Override
                    public void focusGained(FocusEvent e){
                tfName.setText("");
            }
        });
        tfMessage.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tfMessage.setText("");
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });
        JPanel bottomPanel = new JPanel(new BorderLayout());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle(FRM_TITLE);
        frame.setLocation(FRM_LOC_X, FRM_LOC_Y);
        frame.setSize(FRM_WIDTH,FRM_HEIGHT);
        frame.setResizable(true);
        frame.getContentPane().add(BorderLayout.NORTH,spMain);
        frame.getContentPane().add(BorderLayout.CENTER, tfMessage);
        frame.getContentPane().add(BorderLayout.EAST,btnSend);
        frame.getContentPane().add(BorderLayout.WEST, tfName);
        frame.setVisible(true);
    }

    private  void antistatic(){ // Не статический метод для вызова потока
        framDraw(new ChatUDP());
        System.out.println("Hello");
        new thdReceiver().run();
    }
    public static void main(String[] args){
        new ChatUDP().antistatic();
    }
}