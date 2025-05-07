import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class SegmentationGUI extends JFrame {
    private Segmentation segmentation = new Segmentation();
    private JTextField nameField;
    private JTextField sizeField;
    private JComboBox<String> methodBox;
    private JComboBox<String> removeSegmentBox;
    private MemoryPanel memoryPanel;

    public SegmentationGUI() {
        // set the title and window setup
        setTitle("Segmentation Simulation");
        setSize(1000, 500);  
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // input panel using GridBagLayout to hold the input
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // input fields
        nameField = new JTextField();
        sizeField = new JTextField();
        String[] methods = { "First-Fit", "Best-Fit", "Worst-Fit", "Manual" };
        methodBox = new JComboBox<>(methods);
        JButton allocateButton = new JButton("Allocate Segment");

        // first row (segment name, segment size, allocation method, and allocation button)
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Segment Name:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1;
        inputPanel.add(nameField, gbc);

        gbc.gridx = 3; gbc.gridwidth = 1; gbc.weightx = 0;
        inputPanel.add(new JLabel("Segment Size:"), gbc);
        gbc.gridx = 4; gbc.gridwidth = 2;
        inputPanel.add(sizeField, gbc);

        gbc.gridx = 6; gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Allocation Method:"), gbc);
        gbc.gridx = 7; gbc.gridwidth = 2;
        inputPanel.add(methodBox, gbc);

        gbc.gridx = 9; gbc.gridwidth = 1;
        inputPanel.add(allocateButton, gbc);

        // second row (remove segment, and compact)
        removeSegmentBox = new JComboBox<>();
        JButton removeButton = new JButton("Remove Segment");
        JButton compactButton = new JButton("Compact Memory");

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Select Segment to Remove:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        inputPanel.add(removeSegmentBox, gbc);

        gbc.gridx = 3; gbc.gridwidth = 1;
        inputPanel.add(removeButton, gbc);

        gbc.gridx = 4; gbc.gridwidth = 1;
        inputPanel.add(compactButton, gbc);

        add(inputPanel, BorderLayout.NORTH);

        // memory panel (shows current memory allocation)
        memoryPanel = new MemoryPanel();
        add(memoryPanel, BorderLayout.CENTER);

        // allocate button function
        allocateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                int size;

                // make sure the size is valid input
                try {
                    size = Integer.parseInt(sizeField.getText());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Size must be an integer.");
                    return;
                }

                String method = (String) methodBox.getSelectedItem();
                Segmentation.Segment seg = null;

                // check to see if the method is "manual"
                if ("Manual".equals(method)) {
                    // create the GUI to get the base address
                    String baseInput = JOptionPane.showInputDialog("Enter base address for manual segment:");

                    // if Cancel is pressed or input is empty, do nothing
                    if (baseInput == null || baseInput.trim().isEmpty()) 
                    {
                        return;
                    }


                    // make sure the input is valid
                    try {
                        int base = Integer.parseInt(baseInput);
                        seg = Segmentation.manualSegment(name, size, base);
                        if (seg == null) {
                            // if the segment overlaps with another or exceeds the memory limit
                            JOptionPane.showMessageDialog(null, "Invalid manual allocation: overlaps or out of bounds.");
                            return;
                        }
                    } catch (NumberFormatException ex) {
                        // if the base address is not an integer
                        JOptionPane.showMessageDialog(null, "Base address must be a valid integer.");
                        return;
                    }
                } 
                else {
                    // create a segment
                    seg = Segmentation.createSegment(name, size, method);
                    if (seg == null) {
                        // the segment could not fit anywhere
                        JOptionPane.showMessageDialog(null, "No suitable block found for " + name + ". Try again after compacting.");
                        return;
                    }
                }

                // repaints the memory panel to properly display the updated memory
                memoryPanel.repaint();

                // refreshes the remove segment dropdown so the newly added segment is included
                updateRemoveSegmentBox();

                // reset input fields
                nameField.setText("");
                sizeField.setText("");
                methodBox.setSelectedIndex(0);
            }
        });

        // function to remove segment
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedSegment = (String) removeSegmentBox.getSelectedItem();
                if (selectedSegment != null) {
                    // removes the segment, and updates the dropdown and memory visual
                    segmentation.removeSegment(selectedSegment);
                    memoryPanel.repaint();
                    updateRemoveSegmentBox();
                }
            }
        });

        // compacts the segments in the memory
        compactButton.addActionListener(e -> {
            Segmentation.compactMemory();
            memoryPanel.repaint();
        });

        setVisible(true);
    }

    // updates the remove segment drop down box whenever a segment is created/removed
    private void updateRemoveSegmentBox() {
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) removeSegmentBox.getModel();
        model.removeAllElements();
        for (Segmentation.Segment seg : Segmentation.segments) {
            model.addElement(seg.name);
        }
    }

    // adjust the MemoryPanel class for drawing the memory
    class MemoryPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int panelHeight = getHeight();
            int panelWidth = getWidth();

            double scale = (double) panelHeight / Segmentation.MEMORY_SIZE;

            g.setColor(Color.WHITE);
            g.fillRect(0, 0, panelWidth, panelHeight);

            // draw segments
            for (Segmentation.Segment seg : Segmentation.segments) {
                int y = (int) (seg.base * scale);
                int height = (int) (seg.limit * scale);

                g.setColor(Color.CYAN);
                g.fillRect(0, y, panelWidth, height);
                g.setColor(Color.BLACK);
                g.drawRect(0, y, panelWidth, height);
                g.drawString(seg.name + " (" + seg.limit + ")", 5, y + 15);
            }

            // draw free memory
            g.setColor(Color.LIGHT_GRAY);
            for (Segmentation.FreeMem free : Segmentation.freeMemory) {
                int y = (int) (free.base * scale);
                int height = (int) (free.size * scale);

                g.fillRect(0, y, panelWidth, height);
                g.setColor(Color.BLACK);
                g.drawRect(0, y, panelWidth, height);
                g.drawString("Free (" + free.size + ")", 5, y + 15);
                g.setColor(Color.LIGHT_GRAY);
            }
        }
    }

    public static void main(String[] args) {
        // add free space to the entire size of memory
        Segmentation.freeMemory.add(new Segmentation.FreeMem(0, Segmentation.MEMORY_SIZE));
        SwingUtilities.invokeLater(SegmentationGUI::new);
    }
}
