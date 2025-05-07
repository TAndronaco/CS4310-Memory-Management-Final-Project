import java.util.Arrays;

/**
 * Implements the Clock Page Replacement Algorithm.
 */
public class ClockAlgorithm {

    private ClockFrame[] frames;
    private int numFrames;
    private int clockHand;
    private int pageHits;
    private int pageFaults;

    /**
     * Constructor for ClockAlgorithm.
     * @param numFrames The number of frames available in memory.
     */
    public ClockAlgorithm(int numFrames) {
        if (numFrames <= 0) {
            throw new IllegalArgumentException("Number of frames must be positive.");
        }
        this.numFrames = numFrames;
        this.frames = new ClockFrame[numFrames];
        for (int i = 0; i < numFrames; i++) {
            frames[i] = new ClockFrame(); // Initialize with empty frames
        }
        this.clockHand = 0;
        this.pageHits = 0;
        this.pageFaults = 0;
    }

    /**
     * Simulates a request for a specific page.
     * Handles page hits and page faults according to the Clock algorithm.
     * @param pageNumber The page number being requested.
     * @return A String describing the result (e.g., "Hit", "Fault - Replaced page X", "Fault - Loaded into empty frame").
     */
    public String requestPage(int pageNumber) {
        // 1. Check for Page Hit
        int foundIndex = findPage(pageNumber);
        if (foundIndex != -1) {
            frames[foundIndex].setReferenceBit(1); // Set reference bit on hit
            pageHits++;
            return "Hit for page " + pageNumber;
        }

        // 2. Page Fault - Find a frame to replace
        pageFaults++;
        while (true) {
            ClockFrame currentFrame = frames[clockHand];

            if (currentFrame.getReferenceBit() == 0) {
                // Found a frame to replace
                int oldPage = currentFrame.getPageNumber();
                String result;
                if (currentFrame.isEmpty()) {
                    result = "Fault - Loaded page " + pageNumber + " into empty frame " + clockHand;
                } else {
                    result = "Fault - Replaced page " + oldPage + " with page " + pageNumber + " at frame " + clockHand;
                }
                currentFrame.setPageNumber(pageNumber);
                currentFrame.setReferenceBit(1); // New page gets reference bit 1
                advanceClockHand();
                return result;
            } else {
                // Reference bit is 1, set to 0 and move hand
                currentFrame.setReferenceBit(0);
                advanceClockHand();
            }
        }
    }

    /**
     * Searches for a page number within the frames.
     * @param pageNumber The page number to search for.
     * @return The index of the frame containing the page, or -1 if not found.
     */
    private int findPage(int pageNumber) {
        for (int i = 0; i < numFrames; i++) {
            if (!frames[i].isEmpty() && frames[i].getPageNumber() == pageNumber) {
                return i;
            }
        }
        return -1; // Not found
    }

    /**
     * Advances the clock hand to the next frame, wrapping around circularly.
     */
    private void advanceClockHand() {
        clockHand = (clockHand + 1) % numFrames;
    }

    // --- Getters for GUI ---

    public ClockFrame[] getFrames() {
        // Return a copy to prevent external modification
        return Arrays.copyOf(frames, numFrames);
    }

    public int getClockHand() {
        return clockHand;
    }

    public int getPageHits() {
        return pageHits;
    }

    public int getPageFaults() {
        return pageFaults;
    }

    public int getNumFrames() {
        return numFrames;
    }

    public double getHitRatio() {
        int totalRequests = pageHits + pageFaults;
        if (totalRequests == 0) {
            return 0.0;
        }
        return (double) pageHits / totalRequests;
    }
}