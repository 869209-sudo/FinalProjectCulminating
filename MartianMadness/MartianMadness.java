import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.io.*;
import javax.swing.Timer;

public class MartianMadness extends JFrame {
    private GamePanel gamePanel;
    private static final String CREDENTIALS_FILE = "credentials.txt";
    private Map<String, String> credentials;
    private String currentUser;

    public MartianMadness() {
        setTitle("Martian Madness");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        credentials = new HashMap<>();
        loadCredentials();

        // Show login dialog first
        if (showLoginDialog()) {
            gamePanel = new GamePanel(currentUser);
            add(gamePanel);
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
        } else {
            System.exit(0);
        }
    }

    private boolean showLoginDialog() {
        JDialog loginDialog = new JDialog(this, "Login to Martian Madness", true);
        loginDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        loginDialog.setSize(350, 200);
        loginDialog.setLocationRelativeTo(null);
        loginDialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username Label
        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(usernameLabel, gbc);

        // Username Field
        JTextField usernameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(usernameField, gbc);

        // Password Label
        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(passwordLabel, gbc);

        // Password Field
        JPasswordField passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(passwordField, gbc);

        // Message Label
        JLabel messageLabel = new JLabel("");
        messageLabel.setForeground(new Color(220, 20, 60));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(messageLabel, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 240, 240));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        final boolean[] loginSuccess = { false };

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please enter username and password");
                messageLabel.setForeground(new Color(220, 20, 60));
                return;
            }

            if (credentials.containsKey(username)) {
                if (credentials.get(username).equals(password)) {
                    currentUser = username;
                    loginSuccess[0] = true;
                    loginDialog.dispose();
                } else {
                    messageLabel.setText("Incorrect password");
                    messageLabel.setForeground(new Color(220, 20, 60));
                }
            } else {
                messageLabel.setText("User not found");
                messageLabel.setForeground(new Color(220, 20, 60));
            }
        });

        registerButton.addActionListener(e -> {
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
            messageLabel.setText("Registration successful! Now login.");
            messageLabel.setForeground(new Color(34, 139, 34));
            usernameField.setText("");
            passwordField.setText("");
        });

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        loginDialog.add(panel);
        loginDialog.setVisible(true);

        return loginSuccess[0];
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
        SwingUtilities.invokeLater(MartianMadness::new);
    }
}

class GamePanel extends JPanel implements ActionListener {

    private static final int WIDTH = 900;
    private static final int HEIGHT = 500;
    private static final int GROUND_HEIGHT = 50;
    private static final int CEILING_HEIGHT = 30;

    // Player properties
    private double playerX = 100;
    private double playerY = HEIGHT - GROUND_HEIGHT - 60;
    private double playerVelocityY = 0;
    private static final int PLAYER_WIDTH = 40;
    private static final int PLAYER_HEIGHT = 60;
    private static final double GRAVITY = 0.6;
    private static final double JETPACK_THRUST = -1.2;
    private static final double MAX_FALL_SPEED = 12;
    private static final double MAX_RISE_SPEED = -10;

    // Game state
    private boolean jetpackActive = false;
    private boolean gameRunning = false;
    private boolean gameOver = false;
    private int score = 0;
    private int coinsCollected = 0;
    private int distance = 0;
    private double gameSpeed = 5;
    private int difficultyTimer = 0;
    private String currentUser;

    // Game objects
    private List<Obstacle> obstacles = new ArrayList<>();
    private List<Coin> coins = new ArrayList<>();
    private List<Particle> particles = new ArrayList<>();

    // Timing
    private Timer gameTimer;
    private int obstacleSpawnTimer = 0;
    private int coinSpawnTimer = 0;
    private Random random = new Random();

    // Animation
    private int animationFrame = 0;
    private int frameCounter = 0;

    public GamePanel(String username) {
        this.currentUser = username;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(30, 30, 50));
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_UP) {
                    if (!gameRunning && !gameOver) {
                        startGame();
                    }
                    jetpackActive = true;
                } else if (e.getKeyCode() == KeyEvent.VK_R) {
                    if (gameOver) {
                        restartGame();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_UP) {
                    jetpackActive = false;
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!gameRunning && !gameOver) {
                    startGame();
                }
                jetpackActive = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                jetpackActive = false;
            }
        });

        gameTimer = new Timer(16, this); // ~60 FPS
        gameTimer.start();
    }

    private void startGame() {
        gameRunning = true;
        gameOver = false;
        score = 0;
        coinsCollected = 0;
        distance = 0;
        gameSpeed = 10;
        difficultyTimer = 0;
        playerY = HEIGHT - GROUND_HEIGHT - PLAYER_HEIGHT;
        playerVelocityY = 0;
        obstacles.clear();
        coins.clear();
        particles.clear();
    }

    private void restartGame() {
        startGame();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameRunning && !gameOver) {
            update();
        }
        repaint();
    }

    private void update() {
        frameCounter++;
        if (frameCounter % 5 == 0) {
            animationFrame = (animationFrame + 1) % 4;
        }

        // Update player physics
        if (jetpackActive) {
            playerVelocityY += JETPACK_THRUST;
            spawnJetpackParticles();
        }
        playerVelocityY += GRAVITY;

        // Clamp velocity
        playerVelocityY = Math.max(MAX_RISE_SPEED, Math.min(MAX_FALL_SPEED, playerVelocityY));

        playerY += playerVelocityY;

        // Ground and ceiling collision
        double groundLevel = HEIGHT - GROUND_HEIGHT - PLAYER_HEIGHT;
        double ceilingLevel = CEILING_HEIGHT;

        if (playerY > groundLevel) {
            playerY = groundLevel;
            playerVelocityY = 0;
        }
        if (playerY < ceilingLevel) {
            playerY = ceilingLevel;
            playerVelocityY = 0;
        }

        // Update distance and score
        distance += (int) gameSpeed;
        score = distance / 10 + coinsCollected * 50;

        // Increase difficulty over time
        difficultyTimer++;
        if (difficultyTimer % 500 == 0 && gameSpeed < 12) {
            gameSpeed += 1;
        }

        // Spawn obstacles
        obstacleSpawnTimer++;
        int spawnRate = Math.max(45, 100 - difficultyTimer / 30);
        if (obstacleSpawnTimer >= spawnRate) {
            spawnObstacle();
            obstacleSpawnTimer = 0;
        }

        // Spawn coins
        coinSpawnTimer++;
        if (coinSpawnTimer >= 40) {
            spawnCoins();
            coinSpawnTimer = 0;
        }

        // Update obstacles
        Iterator<Obstacle> obstacleIterator = obstacles.iterator();
        while (obstacleIterator.hasNext()) {
            Obstacle obstacle = obstacleIterator.next();
            obstacle.update(gameSpeed);

            if (obstacle.isOffScreen()) {
                obstacleIterator.remove();
            } else if (obstacle.collidesWith(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT)) {
                gameOver = true;
                spawnExplosion();
            }
        }

        // Update coins
        Iterator<Coin> coinIterator = coins.iterator();
        while (coinIterator.hasNext()) {
            Coin coin = coinIterator.next();
            coin.update(gameSpeed);

            if (coin.isOffScreen()) {
                coinIterator.remove();
            } else if (coin.collidesWith(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT)) {
                coinsCollected++;
                spawnCoinParticles(coin.x, coin.y);
                coinIterator.remove();
            }
        }

        // Update particles
        Iterator<Particle> particleIterator = particles.iterator();
        while (particleIterator.hasNext()) {
            Particle particle = particleIterator.next();
            particle.update();
            if (particle.isDead()) {
                particleIterator.remove();
            }
        }
    }

    private void spawnObstacle() {
        int type = random.nextInt(3);

        switch (type) {
            case 0: // Zapper (horizontal)
                double zapperY = CEILING_HEIGHT + 50
                        + random.nextDouble() * (HEIGHT - GROUND_HEIGHT - CEILING_HEIGHT - 150);
                obstacles.add(new Zapper(WIDTH, zapperY, false));
                break;
            case 1: // Zapper (vertical)
                double vertZapperY = CEILING_HEIGHT + 30
                        + random.nextDouble() * (HEIGHT - GROUND_HEIGHT - CEILING_HEIGHT - 200);
                obstacles.add(new Zapper(WIDTH, vertZapperY, true));
                break;
            case 2: // Missile
                double missileY = CEILING_HEIGHT + 50
                        + random.nextDouble() * (HEIGHT - GROUND_HEIGHT - CEILING_HEIGHT - 100);
                obstacles.add(new Missile(WIDTH + 100, missileY));
                break;
        }
    }

    private void spawnCoins() {
        if (random.nextDouble() < 0.6) {
            int pattern = random.nextInt(3);
            double startY = CEILING_HEIGHT + 50 + random.nextDouble() * (HEIGHT - GROUND_HEIGHT - CEILING_HEIGHT - 150);

            switch (pattern) {
                case 0: // Horizontal line
                    for (int i = 0; i < 5; i++) {
                        coins.add(new Coin(WIDTH + i * 40, startY));
                    }
                    break;
                case 1: // Arc
                    for (int i = 0; i < 5; i++) {
                        double arcY = startY - Math.sin(i * Math.PI / 4) * 50;
                        coins.add(new Coin(WIDTH + i * 40, arcY));
                    }
                    break;
                case 2: // Diagonal
                    for (int i = 0; i < 5; i++) {
                        coins.add(new Coin(WIDTH + i * 40, startY + i * 20));
                    }
                    break;
            }
        }
    }

    private void spawnJetpackParticles() {
        for (int i = 0; i < 2; i++) {
            double px = playerX + 5 + random.nextDouble() * 10;
            double py = playerY + PLAYER_HEIGHT - 5;
            double vx = -2 - random.nextDouble() * 2;
            double vy = 2 + random.nextDouble() * 3;
            Color color = random.nextBoolean() ? new Color(0, 0, 15) : new Color(0, 128, 255);
            particles.add(new Particle(px, py, vx, vy, color, 15 + random.nextInt(10)));
        }
    }

    private void spawnCoinParticles(double x, double y) {
        for (int i = 0; i < 8; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 2 + random.nextDouble() * 3;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            particles.add(new Particle(x, y, vx, vy, new Color(255, 51, 153), 20 + random.nextInt(10)));
        }
    }

    private void spawnExplosion() {
        for (int i = 0; i < 30; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 3 + random.nextDouble() * 5;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            Color color = random.nextBoolean() ? new Color(255, 100, 50) : new Color(255, 200, 50);
            particles.add(new Particle(playerX + PLAYER_WIDTH / 2, playerY + PLAYER_HEIGHT / 2,
                    vx, vy, color, 30 + random.nextInt(20)));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background gradient
        GradientPaint bgGradient = new GradientPaint(0, 0, new Color(20, 20, 40),
                0, HEIGHT, new Color(40, 40, 80));
        g2d.setPaint(bgGradient);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw ceiling
        g2d.setColor(new Color(60, 60, 80));
        g2d.fillRect(0, 0, WIDTH, CEILING_HEIGHT);
        g2d.setColor(new Color(100, 100, 120));
        g2d.fillRect(0, CEILING_HEIGHT - 5, WIDTH, 5);

        // Draw ground
        g2d.setColor(new Color(60, 60, 80));
        g2d.fillRect(0, HEIGHT - GROUND_HEIGHT, WIDTH, GROUND_HEIGHT);
        g2d.setColor(new Color(100, 100, 120));
        g2d.fillRect(0, HEIGHT - GROUND_HEIGHT, WIDTH, 5);

        // Draw floor tiles
        g2d.setColor(new Color(70, 70, 100));
        for (int i = 0; i < WIDTH; i += 50) {
            int offset = (int) ((distance / 2) % 50);
            g2d.drawLine(i - offset, HEIGHT - GROUND_HEIGHT, i - offset, HEIGHT);
        }

        // Draw particles (behind player)
        for (Particle particle : particles) {
            particle.draw(g2d);
        }

        // Draw coins
        for (Coin coin : coins) {
            coin.draw(g2d, frameCounter);
        }

        // Draw obstacles
        for (Obstacle obstacle : obstacles) {
            obstacle.draw(g2d, frameCounter);
        }

        // Draw player
        if (!gameOver) {
            drawPlayer(g2d);
        }

        // Draw HUD
        drawHUD(g2d);

        // Draw start/game over screen
        if (!gameRunning) {
            drawStartScreen(g2d);
        } else if (gameOver) {
            drawGameOverScreen(g2d);
        }
    }

    private void drawPlayer(Graphics2D g2d) {
        int px = (int) playerX;
        int py = (int) playerY;

        // Jetpack
        g2d.setColor(new Color(80, 80, 100));
        g2d.fillRoundRect(px - 8, py + 15, 15, 35, 5, 5);
        g2d.setColor(new Color(100, 100, 130));
        g2d.fillRoundRect(px - 6, py + 18, 11, 8, 3, 3);

        // Jetpack flames when active
        if (jetpackActive) {
            int flameOffset = animationFrame % 2 == 0 ? 0 : 3;
            g2d.setColor(new Color(0, 128, 255));
            g2d.fillOval(px - 5, py + 50 + flameOffset, 12, 20 - flameOffset);
            g2d.setColor(new Color(0, 0, 153));
            g2d.fillOval(px - 3, py + 52 + flameOffset, 8, 12 - flameOffset);
        }

        // Body
        g2d.setColor(new Color(50, 50, 70));
        g2d.fillRoundRect(px + 5, py + 20, 25, 30, 8, 8);

        // Head
        g2d.setColor(new Color(51, 255, 153));
        g2d.fillOval(px + 8, py, 24, 24);

        // Helmet
        g2d.setColor(new Color(100, 100, 140));
        g2d.fillArc(px + 6, py - 2, 28, 20, 0, 180);

        // Visor
        g2d.setColor(new Color(102, 102, 255, 200));
        g2d.fillArc(px + 20, py + 5, 14, 12, -30, 120);

        // Legs
        g2d.setColor(new Color(50, 50, 70));
        int legOffset = jetpackActive ? (animationFrame % 2 == 0 ? 2 : -2) : 0;
        g2d.fillRoundRect(px + 8, py + 48, 10, 15, 4, 4);
        g2d.fillRoundRect(px + 20 + legOffset, py + 48, 10, 15, 4, 4);

        // Boots
        g2d.setColor(new Color(80, 80, 100));
        g2d.fillRoundRect(px + 6, py + 58, 14, 6, 3, 3);
        g2d.fillRoundRect(px + 18 + legOffset, py + 58, 14, 6, 3, 3);
    }

    private void drawHUD(Graphics2D g2d) {
        g2d.setFont(new Font("Arial", Font.BOLD, 20));

        // Score background
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(WIDTH - 160, 40, 150, 90, 10, 10);

        // Player name
        g2d.setColor(new Color(0, 204, 204));
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.drawString("Player: " + currentUser, WIDTH - 145, 58);

        // Distance
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString(String.format("%dm", distance / 10), WIDTH - 145, 85);

        // Coins
        g2d.setColor(new Color(255, 102, 178));
        g2d.fillOval(WIDTH - 145, 95, 20, 20);
        g2d.setColor(new Color(255, 51, 153));
        g2d.fillOval(WIDTH - 142, 98, 14, 14);
        g2d.setColor(Color.WHITE);
        g2d.drawString(String.format("× %d", coinsCollected), WIDTH - 120, 112);
    }

    private void drawStartScreen(Graphics2D g2d) {
        // Overlay
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Title
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        g2d.setColor(new Color(255, 51, 153));
        String title = "MARTIAN MADNESS";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(title, (WIDTH - fm.stringWidth(title)) / 2, HEIGHT / 2 - 50);

        // Instructions
        g2d.setFont(new Font("Arial", Font.PLAIN, 24));
        g2d.setColor(new Color(0, 204, 204));
        String instructions = "Hold SPACE or CLICK to fly";
        fm = g2d.getFontMetrics();
        g2d.drawString(instructions, (WIDTH - fm.stringWidth(instructions)) / 2, HEIGHT / 2 + 10);

        String start = "Press SPACE to start";
        g2d.drawString(start, (WIDTH - fm.stringWidth(start)) / 2, HEIGHT / 2 + 50);
    }

    private void drawGameOverScreen(Graphics2D g2d) {
        // Overlay
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Game Over text
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        g2d.setColor(new Color(255, 102, 178));
        String gameOverText = "GAME OVER";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(gameOverText, (WIDTH - fm.stringWidth(gameOverText)) / 2, HEIGHT / 2 - 60);

        // Final score
        g2d.setFont(new Font("Arial", Font.BOLD, 32));
        g2d.setColor(new Color(0, 204, 204));
        String distanceText = String.format("Distance: %dm", distance / 10);
        fm = g2d.getFontMetrics();
        g2d.drawString(distanceText, (WIDTH - fm.stringWidth(distanceText)) / 2, HEIGHT / 2);

        String coinsText = String.format("Coins: %d", coinsCollected);
        g2d.drawString(coinsText, (WIDTH - fm.stringWidth(coinsText)) / 2, HEIGHT / 2 + 40);

        String scoreText = String.format("Score: %d", score);
        g2d.drawString(scoreText, (WIDTH - fm.stringWidth(scoreText)) / 2, HEIGHT / 2 + 80);

        // Restart prompt
        g2d.setFont(new Font("Arial", Font.PLAIN, 24));
        g2d.setColor(Color.WHITE);
        String restart = "Press R to restart";
        fm = g2d.getFontMetrics();
        g2d.drawString(restart, (WIDTH - fm.stringWidth(restart)) / 2, HEIGHT / 2 + 120);
    }
}

// Base obstacle class
abstract class Obstacle {
    double x, y;
    int width, height;

    abstract void update(double speed);

    abstract void draw(Graphics2D g2d, int frame);

    abstract boolean collidesWith(double px, double py, int pw, int ph);

    boolean isOffScreen() {
        return x + width < -50;
    }
}

// Zapper obstacle
class Zapper extends Obstacle {
    boolean vertical;
    int animFrame = 0;

    Zapper(double x, double y, boolean vertical) {
        this.x = x;
        this.y = y;
        this.vertical = vertical;
        this.width = vertical ? 20 : 100;
        this.height = vertical ? 120 : 20;
    }

    @Override
    void update(double speed) {
        x -= speed;
        animFrame++;
    }

    @Override
    void draw(Graphics2D g2d, int frame) {
        // End caps
        g2d.setColor(new Color(80, 80, 100));
        if (vertical) {
            g2d.fillOval((int) x, (int) y - 10, width, 25);
            g2d.fillOval((int) x, (int) y + height - 15, width, 25);
        } else {
            g2d.fillOval((int) x - 10, (int) y, 25, height);
            g2d.fillOval((int) x + width - 15, (int) y, 25, height);
        }

        // Electric beam
        int flicker = animFrame % 4;
        g2d.setColor(new Color(0, 204, 204, 200 + flicker * 10));
        g2d.setStroke(new BasicStroke(vertical ? 8 : 8));
        if (vertical) {
            g2d.drawLine((int) x + width / 2, (int) y, (int) x + width / 2, (int) y + height);
        } else {
            g2d.drawLine((int) x, (int) y + height / 2, (int) x + width, (int) y + height / 2);
        }

        // Glow
        g2d.setColor(new Color(0, 255, 255, 100));
        g2d.setStroke(new BasicStroke(vertical ? 16 : 16));
        if (vertical) {
            g2d.drawLine((int) x + width / 2, (int) y, (int) x + width / 2, (int) y + height);
        } else {
            g2d.drawLine((int) x, (int) y + height / 2, (int) x + width, (int) y + height / 2);
        }

        g2d.setStroke(new BasicStroke(1));
    }

    @Override
    boolean collidesWith(double px, double py, int pw, int ph) {
        Rectangle2D playerRect = new Rectangle2D.Double(px + 8, py + 5, pw - 16, ph - 10);
        Rectangle2D zapperRect = new Rectangle2D.Double(x, y, width, height);
        return playerRect.intersects(zapperRect);
    }
}

// Missile obstacle
class Missile extends Obstacle {
    double speed;
    boolean warning = true;
    int warningTimer = 40;
    double targetY;

    Missile(double x, double y) {
        this.x = x;
        this.targetY = y;
        this.y = y;
        this.width = 90;
        this.height = 30;
        this.speed = (int) (Math.random() * (60 - 15 + 1)) + 15;
        ;
    }

    @Override
    void update(double gameSpeed) {
        if (warning) {
            warningTimer--;
            if (warningTimer <= 0) {
                warning = false;
            }
        } else {
            x -= speed;
        }
    }

    @Override
    void draw(Graphics2D g2d, int frame) {
        if (warning) {
            // Warning indicator
            int alpha = (warningTimer % 10 < 5) ? 255 : 100;
            g2d.setColor(new Color(0, 204, 204, alpha));
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            g2d.drawString("⚠", 850, (int) targetY + 10);

            // Warning line
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 10 }, 0));
            g2d.drawLine(0, (int) targetY + height / 2, 840, (int) targetY + height / 2);
            g2d.setStroke(new BasicStroke(1));
        } else {
            // Missile head (pointing left)
            int[] xPoints = { (int) x + 15, (int) x, (int) x + 15 };
            int[] yPoints = { (int) y, (int) y + height / 2, (int) y + height };
            g2d.setColor(new Color(0, 102, 102));
            g2d.fillPolygon(xPoints, yPoints, 3);

            // Missile body
            g2d.setColor(new Color(24, 80, 80));
            g2d.fillRoundRect((int) x + 15, (int) y, width - 15, height, 5, 5);

            // Fins
            g2d.setColor(new Color(80, 80, 100));
            g2d.fillPolygon(
                    new int[] { (int) x, (int) x - 10, (int) x },
                    new int[] { (int) y + 2, (int) y - 8, (int) y + 8 }, 3);
            g2d.fillPolygon(
                    new int[] { (int) x, (int) x - 10, (int) x },
                    new int[] { (int) y + height - 2, (int) y + height + 8, (int) y + height - 8 }, 3);

            // Exhaust new Color(0, 0, 15) : new Color(0, 128, 255);
            int flameSize = 10 + (frame % 4) * 2;
            g2d.setColor(new Color(0, 128, 255));
            g2d.fillOval((int) x + width - 5, (int) y + 3, flameSize, height - 6);
            g2d.setColor(new Color(0, 0, 15));
            g2d.fillOval((int) x + width - 2, (int) y + 5, flameSize / 2, height - 10);
        }
    }

    @Override
    boolean collidesWith(double px, double py, int pw, int ph) {
        if (warning)
            return false;
        Rectangle2D playerRect = new Rectangle2D.Double(px + 8, py + 5, pw - 16, ph - 10);
        Rectangle2D missileRect = new Rectangle2D.Double(x, y, width, height);
        return playerRect.intersects(missileRect);
    }
}

// Coin collectible
class Coin {
    double x, y;
    int size = 25;

    Coin(double x, double y) {
        this.x = x;
        this.y = y;
    }

    void update(double speed) {
        x -= speed;
    }

    void draw(Graphics2D g2d, int frame) {
        // Coin spin effect
        double scale = Math.abs(Math.cos(frame * 0.1));
        int drawWidth = (int) (size * scale);
        if (drawWidth < 3)
            drawWidth = 3;

        // Outer coin
        g2d.setColor(new Color(255, 102, 178));
        g2d.fillOval((int) x + (size - drawWidth) / 2, (int) y, drawWidth, size);

        // Inner shine
        g2d.setColor(new Color(255, 51, 153));
        if (drawWidth > 8) {
            g2d.fillOval((int) x + (size - drawWidth) / 2 + 3, (int) y + 3, drawWidth - 6, size - 6);
        }
    }

    boolean collidesWith(double px, double py, int pw, int ph) {
        double centerX = x + size / 2;
        double centerY = y + size / 2;
        double playerCenterX = px + pw / 2;
        double playerCenterY = py + ph / 2;

        double dx = centerX - playerCenterX;
        double dy = centerY - playerCenterY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        return distance < (size / 2 + Math.min(pw, ph) / 3);
    }

    boolean isOffScreen() {
        return x + size < 0;
    }
}

// Particle effect
class Particle {
    double x, y, vx, vy;
    Color color;
    int life, maxLife;

    Particle(double x, double y, double vx, double vy, Color color, int life) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.color = color;
        this.life = life;
        this.maxLife = life;
    }

    void update() {
        x += vx;
        y += vy;
        vy += 0.1; // Gravity
        life--;
    }

    void draw(Graphics2D g2d) {
        float alpha = (float) life / maxLife;
        int size = (int) (6 * alpha) + 2;
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255)));
        g2d.fillOval((int) x - size / 2, (int) y - size / 2, size, size);
    }

    boolean isDead() {
        return life <= 0;
    }
}
