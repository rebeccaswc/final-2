import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO; 
import java.io.File;         
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.*;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;

public class DrawingApp extends JFrame {
    private JComboBox<String> monthComboBox;
    private JButton saveButton;
    private JButton playButton;
    private DrawingPanel drawingPanel;
    private Map<String, String> monthAudioMap;
    private ExecutorService audioExecutor;
    private MusicPlayer musicPlayer;
    private String currentAudioFilePath = null;
    private Clip clip;

    public DrawingApp() {

        // 初始化元件
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        monthComboBox = new JComboBox<>(months);
        saveButton = new JButton("Save");
        playButton = new JButton("Play");
        drawingPanel = new DrawingPanel();
        initMonthAudioMap(); // 初始化音频
        audioExecutor = Executors.newSingleThreadExecutor(); // 初始化音频执行器
        musicPlayer = new MusicPlayer(); 

        // 設定按鈕
        monthComboBox.addActionListener(e -> loadMonthImage());
        saveButton.addActionListener(e -> saveImage());
        playButton.addActionListener(e -> {
            String month = (String) monthComboBox.getSelectedItem();
            String audioFilePath = monthAudioMap.get(month);
            if (audioFilePath != null) {
                playWav(audioFilePath);// 將音訊檔案路徑傳入 playAudio 方法
            }
        });
        // 組合介面
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(monthComboBox);
        buttonPanel.add(saveButton);
        buttonPanel.add(playButton);
        this.add(buttonPanel, BorderLayout.SOUTH);
        this.add(drawingPanel, BorderLayout.CENTER);

        // 設定視窗
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(540, 700);
        this.setVisible(true);
    }

    private void initMonthAudioMap() {
        monthAudioMap = new HashMap<>();
        monthAudioMap.put("January", "/audio/January.wav"); 
        monthAudioMap.put("February", "/audio/February.wav");
        monthAudioMap.put("March", "/audio/March.wav");
        monthAudioMap.put("April", "/audio/April.wav");
        monthAudioMap.put("May", "/audio/May.wav");
        monthAudioMap.put("June", "/audio/June.wav");
        monthAudioMap.put("July", "/audio/July.wav");
        monthAudioMap.put("August", "/audio/August.wav");
        monthAudioMap.put("September", "/audio/September.wav");
        monthAudioMap.put("October", "/audio/October.wav");
        monthAudioMap.put("November", "/audio/November.wav");
        monthAudioMap.put("December", "/audio/December.wav");
    }

    private void loadMonthImage() {
        String month = (String) monthComboBox.getSelectedItem();
        if (month != null) {
            try {
                BufferedImage image = ImageIO.read(getClass().getResourceAsStream("/" + month + ".png"));
                drawingPanel.setImage(image);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "無法讀取輸入文件：" + month + ".png", "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }
    
    private void playAudio(String audioFilePath) {
        String month = (String) monthComboBox.getSelectedItem();
        audioFilePath = monthAudioMap.get(month);
        if (audioFilePath != null) {
            musicPlayer.play(audioFilePath);
        }
    }
    
    // 修改 playWav 方法
    private void playWav(String audioFilePath) {
        // 停止目前正在播放的音訊
        stopCurrentlyPlayingAudio();
        try {
        // 建立 File 物件表示音訊檔案
            File audioFile = new File(getClass().getResource(audioFilePath).toURI());
        
        // 檢查檔案是否存在
        if (audioFile.exists()) {
            // 載入音訊檔案
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            currentAudioFilePath = audioFilePath;
            // 建立 Clip 物件
            clip = AudioSystem.getClip();
            
            // 開啟音訊串流
            clip.open(audioInputStream);
            
            // 播放音訊
            clip.start();
            } else {
                System.err.println("File does not exist: " + audioFilePath);
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 停止目前正在播放的音訊
    private void stopCurrentlyPlayingAudio() {
        if (currentAudioFilePath != null) {
            clip.stop();
        // 在這裡添加停止目前播放音訊的程式碼，根據你的播放器或 API
        // 停止目前正在播放的音訊，使用 currentAudioFilePath
        }
    }

    private void saveImage() {
        try {
            BufferedImage image = drawingPanel.getImage();
            ImageIO.write(image, "png", new File("card.png")); 
            JOptionPane.showMessageDialog(this, "Image saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving image!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    static class DrawingPanel extends JPanel {
        private BufferedImage image;
        private Graphics2D g2d;
        private int currentX, currentY, oldX, oldY;
        private Color currentColor = Color.BLACK;
        private int brushSize = 5;
    
        public DrawingPanel() {
            setDoubleBuffered(false);
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    oldX = e.getX();
                    oldY = e.getY();
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    currentX = e.getX();
                    currentY = e.getY();
                    if (g2d != null) {
                        g2d.setColor(currentColor);
                        g2d.setStroke(new BasicStroke(brushSize));
                        g2d.drawLine(oldX, oldY, currentX, currentY);
                        repaint();
                        oldX = currentX;
                        oldY = currentY;
                    }
                }
            });
    
            JButton colorButton = new JButton("Choose Color");
            colorButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Color newColor = JColorChooser.showDialog(null, "Choose Color", currentColor);
                    if (newColor != null) {
                        currentColor = newColor;
                        if (g2d != null) {
                            g2d.setColor(currentColor);
                        }
                    }
                }
            });
    
            JSlider lineWidthSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, brushSize);
            lineWidthSlider.setMajorTickSpacing(1);
            lineWidthSlider.setPaintTicks(true);
            lineWidthSlider.setPaintLabels(true);
            lineWidthSlider.addChangeListener(e -> {
                brushSize = lineWidthSlider.getValue();
                if (g2d != null) {
                    g2d.setStroke(new BasicStroke(brushSize));
                }
            });
    
            // 將 colorButton 和 lineWidthSlider 添加到 DrawingPanel 中
            JPanel controlsPanel = new JPanel();
            controlsPanel.add(colorButton);
            controlsPanel.add(lineWidthSlider);
            add(controlsPanel, BorderLayout.NORTH);
        }
    
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
    
            if (image == null) {
                image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                g2d = (Graphics2D) image.getGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setPaint(Color.black);
                g2d.setStroke(new BasicStroke(brushSize));
                clear();
            }
            g.drawImage(image, 0, 0, null);
        }
    
        public void clear() {
            g2d.setPaint(Color.white);
            g2d.fillRect(0, 0, getSize().width, getSize().height);
            g2d.setPaint(Color.black);
            repaint();
        }
        
        public BufferedImage getImage() {
            return image;
        }
    
        public void setImage(BufferedImage newImage) {
            int newWidth = newImage.getWidth();
            int newHeight = newImage.getHeight();

            // 新的起始繪製位置，將圖片往下移動150像素
            int newX = 0;
            int newY = 70;
            int adjustedHeight = Math.max(newHeight + newY, getHeight());

            //Image resizedImage = newImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            //BufferedImage resizedBufferedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            //Graphics2D resizedGraphics = resizedBufferedImage.createGraphics();
            //resizedGraphics.drawImage(resizedImage, 0, 0, null);
            //resizedGraphics.dispose();
            BufferedImage movedImage = new BufferedImage(newWidth, adjustedHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = movedImage.createGraphics();
            g.drawImage(newImage, newX, newY, null);
            g.dispose();  

            //this.image = resizedBufferedImage;
            //this.g2d = (Graphics2D) this.image.getGraphics();
            //this.g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            //this.g2d.setColor(currentColor);
            //this.g2d.setStroke(new BasicStroke(brushSize));
            this.image = movedImage;
            this.g2d = (Graphics2D) this.image.getGraphics();
            this.g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            this.g2d.setColor(currentColor);
            this.g2d.setStroke(new BasicStroke(brushSize));            
            repaint();
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DrawingApp());
    }

    public class MusicPlayer {

        private Clip clip;

        public void stop() {
            if (clip != null && clip.isRunning()) {
                clip.stop();
            }
        }
    }
}