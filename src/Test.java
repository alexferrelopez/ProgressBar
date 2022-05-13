import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Test {

    private boolean mousePressed;

    public static void main(String[] args) {
        new Test();
    }

    public Test() {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
            }

            JFrame frame = new JFrame("Testing");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new TestPane());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);


        });
    }

    public class TestPane extends JPanel {

        public class EJSlider extends JSlider {

            public EJSlider(int i, int i1) {
                super(i,i1);
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        Point p = e.getPoint();
                        double percent = p.x / ((double) getWidth());
                        int range = getMaximum() - getMinimum();
                        double newVal = range * percent;
                        int result = (int)(getMinimum() + newVal);
                        setValue(result);
                        int frame = getDesiredFrame();
                        if (frame >= frameCount) {
                            frame = 0;
                        }
                        clip.setFramePosition(frame);
                        mousePressed = false;
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        mousePressed = true;
                        Point p = e.getPoint();
                        double percent = p.x / ((double) getWidth());
                        int range = getMaximum() - getMinimum();
                        double newVal = range * percent;
                        int result = (int)(getMinimum() + newVal);
                        setValue(result);
                        int frame = getDesiredFrame();
                        if (frame >= frameCount) {
                            frame = 0;
                        }
                        clip.setFramePosition(frame);
                    }
                });
            }


        }

        private final EJSlider slider = new EJSlider(0, 100);
        private long frameCount;
        private double duration;
        private AudioFormat format;
        private Clip clip;
        private final JLabel currentFrame;
        private final JLabel currentDuration;
        private boolean playing = false;
        private final Timer playTimer;

        public TestPane() {
            AudioInputStream ais;
            try {
                File file = new File("./files/file_example_WAV_10MG.wav");
                ais = AudioSystem.getAudioInputStream(file);
                format = ais.getFormat();
                frameCount = ais.getFrameLength();
                duration = ((double) frameCount) / format.getFrameRate();

                clip = AudioSystem.getClip();
                clip.open(ais);
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
                ex.printStackTrace();
            }
            this.setBackground(Color.darkGray);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            setLayout(new GridBagLayout());
            slider.setBackground(Color.darkGray);
            add(slider, gbc);
            slider.setValue(0);

            add(new JLabel("Total Frames: " + frameCount), gbc);
            add(new JLabel("Total Duration: " + duration), gbc);

            currentFrame = new JLabel("Current frame: 0");
            currentDuration = new JLabel("Current duration: 0");

            add(currentFrame, gbc);
            add(currentDuration, gbc);

            JButton action = new JButton("Play");
            action.setBackground(Color.darkGray);
            action.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!playing) {
                        int frame = getDesiredFrame();
                        if (frame >= frameCount) {
                            frame = 0;
                        }
                        clip.setFramePosition(frame);
                        clip.start();
                        action.setText("Stop");
                        playing = true;
                        playTimer.start();
                    } else {
                        clip.stop();
                        action.setText("Play");
                        playing = false;
                        playTimer.stop();
                    }
                }
            });

            clip.addLineListener(new LineListener() {
                @Override
                public void update(LineEvent event) {
                    if (event.getType().equals(Type.STOP)
                            || event.getType().equals(Type.CLOSE)) {
                        action.setText("Play");
                        playing = false;
                        playTimer.stop();
                        updateState();
                    }
                }
            });

            add(action, gbc);

            playTimer = new Timer(100, e -> {
                if (!mousePressed) {
                    updateState();
                }
            });
        }

        public void updateState() {
            int frame = clip.getFramePosition();
            int progress = (int) (((double) frame / (double) frameCount) * 100);
            slider.setValue(progress);

            currentFrame.setText("Current frame: " + getDesiredFrame());
            currentDuration.setText("Current duration: " + getCurrentTime());
        }

        public double getCurrentTime() {
            int currentFrame = clip.getFramePosition();
            return (double) currentFrame / format.getFrameRate();
        }

        public int getDesiredFrame() {
            int progress = slider.getValue();
            double frame = ((double) frameCount * ((double) progress / 100.0));
            return (int) frame;
        }
    }
}