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

/**
 * Graphical User Interface
 * <p>This class contains a graphical user interface that allows de user
 * to control the chat in an easy and visual manner. It doesn't contain any of the logic that
 * makes the chat work, it contains only a interface for the user to control the chat; all the logic is inside EchoClient.</p>
 *
 * <p>It contains at the left the list of users connected with a button
 * that allows the user to update that list. When the user cn a username the chat allows him to
 * write private messages to that user.</p>
 *
 * <p>In the top there is the username with a button to update and the ip of the host server.</p>
 *
 * <p>In the middle we have all the messages sent, note that messages sent from the server
 * indicating the users connected are not showed in this chat</p>
 *
 * <p>In the bottom there is a text field that allows de users to write the message and to send it
 * by clicking enter or by clicking the button send</p>
 */
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

    /**
     * Constructor of the user interface
     * @param client Client that is using the chat
     * @param host IP of the host, just to show it to the user
     * @param username username of the client
     */
    public ClientInterface(EchoClient client,String host,String username) {
        frame = new JFrame("Client");
        this.client = client;

        //area where messages are showed
        contentArea = new JTextArea();
        contentArea.setEditable(false);
        contentArea.setForeground(COLOR_CHAT);

        //area where user writes messages
        txt_message = new JTextField();
        txt_message.addActionListener(this);

        //area where username is showed and can be cahnged
        txt_username = new JTextField(username);
        txt_username.addActionListener(this);

        //area where ip host is shown
        txt_host = new JTextField(host);
        txt_host.setEditable(false);

        //buttons for sending messages, updating the users list and setting the username
        btn_send = new JButton("send");
        btn_send.addActionListener(this);
        btn_setUsername = new JButton("Set username:");
        btn_setUsername.addActionListener(this);
        btn_update = new JButton("update");
        btn_update.addActionListener(this);


        //list of username with a listener
        listModel = new DefaultListModel();
        userList = new JList(listModel);
        //because personal messages are done by writting an @<username>
        //before the message, this action listener does this automatically
        //when the user click on a username
        userList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                txt_message.setText("@"+userList.getSelectedValue()+" ");
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

    /**
     * Prints a message in the content area so the user can read it followed by an endline
     * @param msg message to be printed
     */
    public void printMessage(String msg){
        contentArea.append(msg +"\n");
        contentArea.setCaretPosition(contentArea.getText().length() - 1);
    }

    /**
     * Updates the list of users to a list of users received via an array of strings
     * @param users array of strings that contains a list of users
     */
    public synchronized void updateUsers(String[] users){
        listModel.removeAllElements();
        for(int i=1;i<users.length;i++){
            listModel.addElement(users[i]);
        }
    }

    /**
     * This method contains all the "reactions" of the chat to the user orders.
     * It sends a message when the user wants to send a message.
     * It changes the username when the user wants to change its username.
     * And it updates the list of users when the user wants to update the list of users.
     * @param e
     */
    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();
        //sends the message written inside txt_message
        if(o == btn_send || o==txt_message){
            client.SendMessage(txt_message.getText());
            //if its a personal message, it keeps the prefix
            //so the user can write easily more than one private
            //messages followed
            if(txt_message.getText().charAt(0)=='@'){
                printMessage(txt_message.getText());
                txt_message.setText("@"+userList.getSelectedValue()+" ");
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

        //after sending a message is nice to put the cursor again inside the field to write messages
        txt_message.requestFocus();

    }


}
