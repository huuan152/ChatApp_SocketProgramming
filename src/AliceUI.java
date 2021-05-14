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

public class AliceUI implements ActionListener, Runnable {

    JPanel AliceInfo;
    JLabel AliceName;
    JLabel AliceStatus;
    JButton SendButton;
    JTextField TextInputField;

    static JFrame f1 = new JFrame();
    static JPanel Dialogue;

    static Box vertical = Box.createVerticalBox();

    BufferedWriter writer;
    BufferedReader reader;

    String myPreviousChat = "";

    Boolean typing;

    public AliceUI() {
        initComponents();
        try {
            Socket socketClient = new Socket("localhost", 2001);
            writer = new BufferedWriter(new OutputStreamWriter(socketClient.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
        } catch(Exception e) {}
    }

    private void initComponents() {
        f1.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        AliceInfo = new JPanel();
        AliceInfo.setLayout(null);
        AliceInfo.setBackground(new Color(0, 106, 255));
        AliceInfo.setBounds(0, 0, 450, 70);
        f1.add(AliceInfo);

        ImageIcon AliceAvatar = new ImageIcon(getClass().getResource("/icons/Alice.png"));
        Image temp = AliceAvatar.getImage().getScaledInstance(60, 60, Image.SCALE_DEFAULT);
        ImageIcon newAliceAvatar = new ImageIcon(temp);
        JLabel AliceAvatarLB = new JLabel(newAliceAvatar);
        AliceAvatarLB.setBounds(40, 5, 60, 60);
        AliceInfo.add(AliceAvatarLB);

        AliceName = new JLabel("Alice");
        AliceName.setFont(new Font("Helvetica Neue", Font.PLAIN, 18));
        AliceName.setForeground(Color.WHITE);
        AliceName.setBounds(110, 15, 100, 18);
        AliceInfo.add(AliceName);

        AliceStatus = new JLabel("Active Now");
        AliceStatus.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        AliceStatus.setForeground(Color.WHITE);
        AliceStatus.setBounds(110, 35, 100, 20);
        AliceInfo.add(AliceStatus);

        Timer t = new Timer(0, ae -> {
            if(!typing){
                AliceStatus.setText("Active Now");
            }
        });

        t.setInitialDelay(2000);

        Dialogue = new JPanel();
        Dialogue.setFont(new Font("Helvetica Neue", Font.PLAIN, 18));

        JScrollPane sp = new JScrollPane(Dialogue);
        sp.setBounds(5, 75, 440, 570);
        sp.setBorder(BorderFactory.createEmptyBorder());

        ScrollBarUI ui = new BasicScrollBarUI() {
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

        sp.getVerticalScrollBar().setUI(ui);
        f1.add(sp);

        TextInputField = new JTextField();
        TextInputField.setBounds(5, 648, 310, 40);
        TextInputField.setFont(new Font("Helvetica Neue", Font.PLAIN, 18)); // NOI18N
        f1.add(TextInputField);

        TextInputField.addKeyListener(new KeyAdapter(){
            public void keyPressed(KeyEvent ke){
                AliceStatus.setText("typing...");

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
        f1.add(SendButton);

        f1.setLayout(null);
        f1.setTitle("Messenger");
        f1.setSize(450, 715);
        f1.setLocation(200, 80);
        f1.setResizable(false);
        f1.setVisible(true);
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
                    f1.validate();
                }
            }

        } catch(Exception e) {}
    }

    public static void main(String[] args) {
        AliceUI a = new AliceUI();
        a.f1.setVisible(true);
        Thread thread = new Thread(a);
        thread.start();
    }
}
