import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class LoginApplication extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JButton clearButton;
    private JLabel messageLabel;
    private static final String CREDENTIALS_FILE = "credentials.txt";
    private Map<String, String> credentials;

    public LoginApplication() {
        setTitle("Login Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);

        credentials = new HashMap<>();
        loadCredentials();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        // Username Label
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(usernameLabel, gbc);

        // Username Field
        usernameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(usernameField, gbc);

        // Password Label
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(passwordLabel, gbc);

        // Password Field
        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(passwordField, gbc);

        // Message Label
        messageLabel = new JLabel("");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        messageLabel.setForeground(new Color(220, 20, 60));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(messageLabel, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 240, 240));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // Login Button
        loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(80, 30));
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
        buttonPanel.add(loginButton);

        // Register Button
        registerButton = new JButton("Register");
        registerButton.setPreferredSize(new Dimension(80, 30));
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegister();
            }
        });
        buttonPanel.add(registerButton);

        // Clear Button
        clearButton = new JButton("Clear");
        clearButton.setPreferredSize(new Dimension(80, 30));
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                usernameField.setText("");
                passwordField.setText("");
                messageLabel.setText("");
            }
        });
        buttonPanel.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buttonPanel, gbc);

        add(panel);
        setVisible(true);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter username and password");
            messageLabel.setForeground(new Color(220, 20, 60));
            return;
        }

        if (credentials.containsKey(username)) {
            if (credentials.get(username).equals(password)) {
                messageLabel.setText("Login successful!");
                messageLabel.setForeground(new Color(34, 139, 34));
                usernameField.setText("");
                passwordField.setText("");
            } else {
                messageLabel.setText("Incorrect password");
                messageLabel.setForeground(new Color(220, 20, 60));
            }
        } else {
            messageLabel.setText("User not found");
            messageLabel.setForeground(new Color(220, 20, 60));
        }
    }

    private void handleRegister() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter username and password");
            messageLabel.setForeground(new Color(220, 20, 60));
            return;
        }

        if (username.length() < 3) {
            messageLabel.setText("Username must be at least 3 characters");
            messageLabel.setForeground(new Color(220, 20, 60));
            return;
        }

        if (password.length() < 4) {
            messageLabel.setText("Password must be at least 4 characters");
            messageLabel.setForeground(new Color(220, 20, 60));
            return;
        }

        if (credentials.containsKey(username)) {
            messageLabel.setText("Username already exists");
            messageLabel.setForeground(new Color(220, 20, 60));
            return;
        }

        credentials.put(username, password);
        saveCredentials();
        messageLabel.setText("Registration successful!");
        messageLabel.setForeground(new Color(34, 139, 34));
        usernameField.setText("");
        passwordField.setText("");
    }

    private void loadCredentials() {
        File file = new File(CREDENTIALS_FILE);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    credentials.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveCredentials() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CREDENTIALS_FILE))) {
            for (Map.Entry<String, String> entry : credentials.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginApplication();
            }
        });
    }
}
