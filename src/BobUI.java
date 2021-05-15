import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ScrollBarUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BobUI implements ActionListener, Runnable {

    private JPanel BobInfo;
    private JLabel BobName;
    private JLabel BobStatus;
    private JScrollPane ChatScrollPane;
    private ScrollBarUI ui;
    private JButton SendButton;
    private JTextField TextInputField;

    private static JFrame mainFrame = new JFrame();
    private static JPanel Dialogue;

    private static Box vertical = Box.createVerticalBox();

    private BufferedWriter writer;
    private BufferedReader reader;

    private String myPreviousChat = "";

    Boolean typing;

    public BobUI() {
        initComponents();
        try {
            Socket socketClient = new Socket("localhost", 2001);
            writer = new BufferedWriter(new OutputStreamWriter(socketClient.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
        } catch(Exception e) {}
    }

    private void initComponents() {
        mainFrame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        BobInfo = new JPanel();
        BobInfo.setLayout(null);
        BobInfo.setBackground(new Color(0, 106, 255));
        BobInfo.setBounds(0, 0, 450, 70);
        mainFrame.add(BobInfo);

        ImageIcon BobAvatar = new ImageIcon(getClass().getResource("/icons/Bob.png"));
        Image temp = BobAvatar.getImage().getScaledInstance(60, 60, Image.SCALE_DEFAULT);
        ImageIcon newBobAvatar = new ImageIcon(temp);
        JLabel BobAvatarLB = new JLabel(newBobAvatar);
        BobAvatarLB.setBounds(40, 5, 60, 60);
        BobInfo.add(BobAvatarLB);

        BobName = new JLabel("Bob");
        BobName.setFont(new Font("Helvetica Neue", Font.PLAIN, 18));
        BobName.setForeground(Color.WHITE);
        BobName.setBounds(110, 15, 100, 18);
        BobInfo.add(BobName);

        BobStatus = new JLabel("Active Now");
        BobStatus.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        BobStatus.setForeground(Color.WHITE);
        BobStatus.setBounds(110, 35, 100, 20);
        BobInfo.add(BobStatus);

        Timer t = new Timer(1, ae -> {
            if(!typing){
                BobStatus.setText("Active Now");
            }
        });

        t.setInitialDelay(2000);

        Dialogue = new JPanel();
        Dialogue.setFont(new Font("Helvetica Neue", Font.PLAIN, 18));

        ChatScrollPane = new JScrollPane(Dialogue);
        ChatScrollPane.setBounds(5, 75, 440, 570);
        ChatScrollPane.setBorder(BorderFactory.createEmptyBorder());

        ui = new BasicScrollBarUI() {
            protected JButton createDecreaseButton(int orientation) {
                JButton button = super.createDecreaseButton(orientation);
                button.setBackground(new Color(0, 106, 255));
                button.setForeground(Color.WHITE);
                this.thumbColor = new Color(0, 106, 255);
                return button;
            }
            protected JButton createIncreaseButton(int orientation) {
                JButton button = super.createDecreaseButton(orientation);
                button.setBackground(new Color(0, 106, 255));
                button.setForeground(Color.WHITE);
                this.thumbColor = new Color(0, 106, 255);
                return button;
            }
        };

        ChatScrollPane.getVerticalScrollBar().setUI(ui);
        mainFrame.add(ChatScrollPane);

        TextInputField = new JTextField();
        TextInputField.setBounds(5, 648, 310, 40);
        TextInputField.setFont(new Font("Helvetica Neue", Font.PLAIN, 18)); // NOI18N
        mainFrame.add(TextInputField);

        TextInputField.addKeyListener(new KeyAdapter(){
            public void keyPressed(KeyEvent ke){
                BobStatus.setText("typing...");

                t.stop();

                typing = true;
            }

            public void keyReleased(KeyEvent ke){
                typing = false;

                if(!t.isRunning()){
                    t.start();
                }
            }
        });

        SendButton = new JButton("Send");
        SendButton.setBounds(320, 648, 123, 40);
        SendButton.setForeground(new Color(0, 106, 255));
        SendButton.setFont(new Font("SAN_SERIF", Font.PLAIN, 16));

        SendButton.addActionListener(this);
        mainFrame.add(SendButton);

        mainFrame.setLayout(null);
        mainFrame.setTitle("Messenger");
        mainFrame.setSize(450, 715);
        mainFrame.setLocation(700, 80);
        mainFrame.setResizable(false);
        mainFrame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        try {
            String out = TextInputField.getText();

            if (!out.equals("")) {

                myPreviousChat = out;

                JPanel p2 = formatLabel(out);

                Dialogue.setLayout(new BorderLayout());

                JPanel right = new JPanel(new BorderLayout());
                right.add(p2, BorderLayout.LINE_END);
                vertical.add(right);
                vertical.add(Box.createVerticalStrut(15));

                Dialogue.add(vertical, BorderLayout.PAGE_START);

                writer.write(out);
                writer.write("\r\n");
                writer.flush();

                TextInputField.setText("");
                ChatScrollPane.getViewport().setViewPosition(new Point(10000,10000));
                mainFrame.validate();
            }
        } catch (Exception e) {}
    }

    public JPanel formatLabel(String out) {
        JPanel temp = new JPanel();
        temp.setLayout(new BoxLayout(temp, BoxLayout.Y_AXIS));

        JLabel t = new JLabel("<html><p style = \"width : 150px\">" + out + "</p></html>");
        t.setFont(new Font("Helvetica Neue", Font.PLAIN, 16));
        t.setBackground(new Color(0, 106, 255));
        t.setOpaque(true);
        t.setBorder(new EmptyBorder(15, 15, 15, 50));

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        JLabel time = new JLabel();
        time.setText(sdf.format(cal.getTime()));

        temp.add(t);
        temp.add(time);
        return temp;
    }

    @Override
    public void run() {
        try {
            String msginput;

            while(!(msginput = reader.readLine()).equals("")){
                if (msginput.equals(myPreviousChat)) {
                    myPreviousChat = "";
                } else {
                    Dialogue.setLayout(new BorderLayout());
                    JPanel p2 = formatLabel(msginput);
                    JPanel left = new JPanel(new BorderLayout());
                    left.add(p2, BorderLayout.LINE_START);

                    vertical.add(left);
                    vertical.add(Box.createVerticalStrut(15));
                    Dialogue.add(vertical, BorderLayout.PAGE_START);

                    ChatScrollPane.getViewport().setViewPosition(new Point(10000,10000));
                    mainFrame.validate();
                }
            }

        } catch(Exception e) {}
    }

    public static void main(String[] args) {
        BobUI a = new BobUI();
        a.mainFrame.setVisible(true);
        Thread thread = new Thread(a);
        thread.start();
    }
}
