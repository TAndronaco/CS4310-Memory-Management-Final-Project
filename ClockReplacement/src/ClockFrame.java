/**
 * Represents a single frame in the clock algorithm's memory.
 * Stores the page number and the reference bit.
 */
public class ClockFrame {
    private int pageNumber; // -1 indicates an empty frame
    private int referenceBit; // 0 or 1

    /**
     * Constructor for ClockFrame. Initializes with an empty page (-1) and reference bit 0.
     */
    public ClockFrame() {
        this.pageNumber = -1; // Indicates empty frame initially
        this.referenceBit = 0;
    }

    /**
     * Gets the page number currently stored in this frame.
     * @return The page number, or -1 if the frame is empty.
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * Sets the page number for this frame.
     * @param pageNumber The page number to store.
     */
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    /**
     * Gets the reference bit for this frame.
     * @return The reference bit (0 or 1).
     */
    public int getReferenceBit() {
        return referenceBit;
    }

    /**
     * Sets the reference bit for this frame.
     * @param referenceBit The reference bit (should be 0 or 1).
     */
    public void setReferenceBit(int referenceBit) {
        if (referenceBit == 0 || referenceBit == 1) {
            this.referenceBit = referenceBit;
        } else {
            // Optionally throw an exception or handle invalid input
            System.err.println("Warning: Attempted to set invalid reference bit: " + referenceBit);
            // Defaulting to 0 for safety, though ideally input validation happens earlier.
            this.referenceBit = 0;
        }
    }

    /**
     * Checks if the frame is currently empty (holding page number -1).
     * @return true if the frame is empty, false otherwise.
     */
    public boolean isEmpty() {
        return this.pageNumber == -1;
    }
}