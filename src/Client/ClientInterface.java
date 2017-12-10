package Client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class ClientInterface implements ActionListener {
    private JFrame frame;
    public JTextArea contentArea;
    public JTextField txt_message;
    private JTextField txt_host;
    private JTextField txt_username;
    private JButton btn_send;
    private JButton btn_setUsername;
    private JButton btn_update;
    private JPanel northPanel;
    private JPanel southPanel;
    private JPanel eastPanel;
    private JScrollPane rightPanel;
    private JScrollPane leftPanel;
    private JSplitPane centerSplit;
    private JList userList;
    private DefaultListModel<String> listModel;
    private EchoClient client;
    private final Color COLOR_CHAT = Color.BLUE;

    public ClientInterface(EchoClient client,String host,String username) {
        frame = new JFrame("Client");
        this.client = client;

        //frame.setIconImage(Toolkit.getDefaultToolkit().createImage(Client.class.getResource("qq.png")));
        //frame.setIconImage(Toolkit.getDefaultToolkit().createImage(Server.class.getResource("qq.png")));
        contentArea = new JTextArea();
        contentArea.setEditable(false);
        contentArea.setForeground(COLOR_CHAT);


        txt_message = new JTextField();
        txt_message.addActionListener(this);
        txt_username = new JTextField(username);
        txt_username.addActionListener(this);

        txt_host = new JTextField(host);
        txt_host.setEditable(false);

        btn_send = new JButton("send");
        btn_send.addActionListener(this);
        btn_setUsername = new JButton("Set username:");
        btn_setUsername.addActionListener(this);
        btn_update = new JButton("update");
        btn_update.addActionListener(this);



        listModel = new DefaultListModel();
        userList = new JList(listModel);
        userList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                txt_message.setText("@"+(String) userList.getSelectedValue()+" ");
                txt_message.requestFocus();

            } });

        southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(new TitledBorder("write"));
        southPanel.add(txt_message, "Center");
        southPanel.add(btn_send, "East");
        leftPanel = new JScrollPane(userList);
        leftPanel.setBorder(new TitledBorder("users online"));

        eastPanel = new JPanel();
        eastPanel.setLayout(new BoxLayout(eastPanel,BoxLayout.Y_AXIS));
        eastPanel.add(leftPanel);
        eastPanel.add(btn_update);

        //updates the list of users when starting the chat
        client.SendMessage("USERS?");

        contentArea.setText(null);
        rightPanel = new JScrollPane(contentArea);
        rightPanel.setBorder(new TitledBorder("message"));

        centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, eastPanel, rightPanel);
        centerSplit.setDividerLocation(100);
        northPanel = new JPanel();
        northPanel.setLayout(new GridLayout(1, 4));
        northPanel.add(btn_setUsername);
        northPanel.add(txt_username);
        northPanel.add(new JLabel("IP host:"));
        northPanel.add(txt_host);



        frame.setLayout(new BorderLayout());
        frame.add(northPanel, "North");
        frame.add(centerSplit, "Center");
        frame.add(southPanel, "South");
        frame.setSize(600, 400);

        int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
        frame.setLocation((screen_width - frame.getWidth()) / 2,
                (screen_height - frame.getHeight()) / 2);


        frame.setVisible(true);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        contentArea.requestFocus();

    }

    public void printMessage(String msg){

        contentArea.append(msg +"\n");
        contentArea.setCaretPosition(contentArea.getText().length() - 1);

    }


    public synchronized void updateUsers(String[] users){
        listModel.removeAllElements();
        for(int i=1;i<users.length;i++){
            listModel.addElement(users[i]);
        }
    }

    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();

        if(o == btn_send || o==txt_message){
            client.SendMessage(txt_message.getText());
            if(txt_message.getText().charAt(0)=='@'){
                printMessage(txt_message.getText());
                txt_message.setText("@"+(String)userList.getSelectedValue()+" ");
            }
            else {
                txt_message.setText("");
            }
        }
        else if(o==btn_setUsername || o==txt_username){
            client.SendMessage("SET USERNAME "+txt_username.getText());

        }
        else if(o==btn_update){
            client.SendMessage("USERS?");

        }
        txt_message.requestFocus();

    }


}
