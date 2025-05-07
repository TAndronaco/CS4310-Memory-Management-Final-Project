import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The main GUI class for the Clock Page Replacement Algorithm simulator.
 */
public class ClockGUI extends JFrame {

    private ClockAlgorithm algorithm;
    private ClockVisualizationPanel clockPanel;
    private JTextField pageRequestInput;
    private JButton requestButton;
    private JTextArea statusArea;
    private JLabel statsLabel;
    private int numFrames; // Store the number of frames

    /**
     * Constructor for the GUI.
     * @param numFrames The number of memory frames to simulate.
     */
    public ClockGUI(int numFrames) {
        this.numFrames = numFrames;
        this.algorithm = new ClockAlgorithm(numFrames);

        setTitle("Clock Page Replacement Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5)); // Use BorderLayout

        // Create panels
        clockPanel = new ClockVisualizationPanel();
        JPanel controlPanel = createControlPanel();

        // Add panels to frame
        add(clockPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        // Finalize frame setup
        pack(); // Adjusts window size to fit components
        setLocationRelativeTo(null); // Center on screen
        setMinimumSize(new Dimension(400, 450)); // Set a reasonable minimum size
    }

    /**
     * Creates the control panel with input field, button, status area, and stats.
     * @return The configured control panel.
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout()); // Use GridBagLayout for flexibility
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Page Request Input
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("Request Page:"), gbc);

        pageRequestInput = new JTextField(5); // Input field size
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        panel.add(pageRequestInput, gbc);

        // Request Button
        requestButton = new JButton("Request");
        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(requestButton, gbc);

        // Status Area
        statusArea = new JTextArea(3, 30); // Rows, Columns
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(statusArea);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3; // Span across 3 columns
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0; // Allow vertical expansion
        panel.add(scrollPane, gbc);

        // Statistics Label
        statsLabel = new JLabel("Hits: 0 | Faults: 0 | Ratio: 0.00");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0; // No vertical expansion
        panel.add(statsLabel, gbc);

        // --- Action Listener for the Button ---
        requestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlePageRequest();
            }
        });
        // Allow pressing Enter in the text field to trigger the button
        pageRequestInput.addActionListener(e -> handlePageRequest());


        return panel;
    }

    /**
     * Handles the logic when the "Request Page" button is clicked or Enter is pressed.
     */
    private void handlePageRequest() {
        try {
            String inputText = pageRequestInput.getText().trim();
            if (inputText.isEmpty()) {
                statusArea.setText("Please enter a page number.");
                return;
            }
            int pageNumber = Integer.parseInt(inputText);
            if (pageNumber < 0) {
                 statusArea.setText("Page number cannot be negative.");
                 return;
            }

            String result = algorithm.requestPage(pageNumber);
            statusArea.setText(result); // Display result from algorithm
            updateStats();
            clockPanel.repaint(); // Redraw the clock visualization
            pageRequestInput.setText(""); // Clear input field
            pageRequestInput.requestFocusInWindow(); // Set focus back to input

        } catch (NumberFormatException ex) {
            statusArea.setText("Invalid input. Please enter an integer page number.");
        } catch (Exception ex) {
            statusArea.setText("An error occurred: " + ex.getMessage());
            ex.printStackTrace(); // Log error for debugging
        }
    }

    /**
     * Updates the statistics label based on the current state of the algorithm.
     */
    private void updateStats() {
        statsLabel.setText(String.format("Hits: %d | Faults: %d | Ratio: %.2f",
                algorithm.getPageHits(),
                algorithm.getPageFaults(),
                algorithm.getHitRatio()));
    }


    // --- Inner Class for Clock Visualization ---
    private class ClockVisualizationPanel extends JPanel {
        private static final int PADDING = 30;
        private static final int FRAME_SIZE = 50; // Size of the box representing a frame

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight() - PADDING; // Leave some space at the bottom
            int centerX = width / 2;
            int centerY = height / 2;
            int radius = Math.min(width, height) / 2 - PADDING - FRAME_SIZE / 2; // Radius of the circle where frames sit

            if (radius <= 0) return; // Avoid drawing if panel is too small

            ClockFrame[] frames = algorithm.getFrames();
            int currentHandPos = algorithm.getClockHand();

            for (int i = 0; i < numFrames; i++) {
                double angle = 2 * Math.PI * i / numFrames - (Math.PI / 2); // Start from top (-90 degrees)
                int frameX = centerX + (int) (radius * Math.cos(angle)) - FRAME_SIZE / 2;
                int frameY = centerY + (int) (radius * Math.sin(angle)) - FRAME_SIZE / 2;

                // Draw frame box
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.fillRect(frameX, frameY, FRAME_SIZE, FRAME_SIZE);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(frameX, frameY, FRAME_SIZE, FRAME_SIZE);

                // Draw frame content (Page Number and Reference Bit)
                ClockFrame frame = frames[i];
                String pageText = frame.isEmpty() ? "-" : String.valueOf(frame.getPageNumber());
                String bitText = String.valueOf(frame.getReferenceBit());

                g2d.drawString("P:" + pageText, frameX + 5, frameY + FRAME_SIZE / 2 - 5);
                g2d.drawString("R:" + bitText, frameX + 5, frameY + FRAME_SIZE / 2 + 15);

                // Draw Clock Hand if pointing at this frame
                if (i == currentHandPos) {
                    g2d.setColor(Color.RED);
                    g2d.setStroke(new BasicStroke(2)); // Thicker line for hand


                    // Draw hand from center towards the frame
                    int handStartX = centerX + (int) ((radius - FRAME_SIZE / 2 - 5) * Math.cos(angle)); // Start slightly inside frame radius
                    int handStartY = centerY + (int) ((radius - FRAME_SIZE / 2 - 5) * Math.sin(angle));

                    g2d.drawLine(handStartX, handStartY, centerX, centerY); // Line from frame edge to center
                    // Optional: Draw a small circle at the center
                    g2d.fillOval(centerX - 3, centerY - 3, 6, 6);

                    g2d.setStroke(new BasicStroke(1)); // Reset stroke
                }
            }
        }

         // Set preferred size based on number of frames (heuristic)
        @Override
        public Dimension getPreferredSize() {
            int minDim = (int) (FRAME_SIZE * numFrames * 0.8); // Estimate based on circumference
            return new Dimension(Math.max(350, minDim), Math.max(300, minDim));
        }
    }

    /**
     * Main method to launch the simulator.
     * Asks the user for the number of frames.
     */
    public static void main(String[] args) {
        // Ask for number of frames
        int frames = 0;
        while (frames <= 0) {
            String input = JOptionPane.showInputDialog(null, "Enter the number of memory frames:", "Setup", JOptionPane.QUESTION_MESSAGE);
            if (input == null) {
                System.out.println("Setup cancelled by user.");
                System.exit(0); // Exit if user cancels
            }
            try {
                frames = Integer.parseInt(input);
                if (frames <= 0) {
                    JOptionPane.showMessageDialog(null, "Number of frames must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid input. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        final int finalFrames = frames; // Need final variable for lambda
        // Ensure GUI creation happens on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            ClockGUI gui = new ClockGUI(finalFrames);
            gui.setVisible(true);
        });
    }
}