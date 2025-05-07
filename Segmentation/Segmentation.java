import java.util.*;

public class Segmentation {
    static final int MEMORY_SIZE = 5000;

    static List<FreeMem> freeMemory = new ArrayList<>();
    static List<Segment> segments = new ArrayList<>();
    static Map<Integer, Segment> segT = new HashMap<>();

    // mimic segment
    static class Segment {
        int base;
        int limit;
        String name;

        Segment(String name, int base, int limit) 
        {
            this.name = name;
            this.base = base;
            this.limit = limit;
        }
    }

    // represents free memory slot
    static class FreeMem {
        int base;
        int size;

        FreeMem(int base, int size) 
        {
            this.base = base;
            this.size = size;
        }
    }

    // creates a segment based on the given method
    public static Segment createSegment(String name, int size, String method) 
    {
        FreeMem tobeallocated = null;

        switch (method) 
        {
            // looks for the first available free slot
            case "First-Fit": 
                // sort the list of free memory slots by base address to ensure the first available will be chosen
                freeMemory.sort(Comparator.comparingInt(f -> f.base));
                for (FreeMem fm : freeMemory)
                 {
                    if (fm.size >= size) 
                    {
                        tobeallocated = fm;
                        break;
                    }
                }
                break;
            // looks for the smallest slot available that will fit the segment
            case "Best-Fit": 
                int minSize = Integer.MAX_VALUE;
                for (FreeMem fm : freeMemory)
                 {
                    if (fm.size >= size && fm.size < minSize) 
                    {
                        tobeallocated = fm;
                        minSize = fm.size;
                    }
                }
                break;
            // looks for the largest available slot that will fit the segment
            case "Worst-Fit": 
                int maxSize = -1;
                for (FreeMem fm : freeMemory) 
                {
                    if (fm.size >= size && fm.size > maxSize) 
                    {
                        tobeallocated = fm;
                        maxSize = fm.size;
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid method");
        }

        // if no appropiate free space as found, return null
        if (tobeallocated == null) 
        {
            return null;
        }

        // create the segment and add it to the table & list of segments
        Segment newSeg = new Segment(name, tobeallocated.base, size);
        segments.add(newSeg);
        segT.put(segments.size() - 1, newSeg);

        // check to see if the entirety of the free space was used, otherwise split it up so the extra free space is accounted for
        if (tobeallocated.size == size) 
        {
            freeMemory.remove(tobeallocated);
        } 
        else
         {
            tobeallocated.base += size;
            tobeallocated.size -= size;
        }

        return newSeg;
    }

    // manually creates a segment with a given initial base address. 
    // this function is separated as it has GUI elements and will be called in the GUI file
    public static Segmentation.Segment manualSegment(String name, int size, int base) 
    {
        boolean overlaps = false;

        // check for any overlaps
        for (Segment seg : segments) 
        {
            int segEnd = seg.base + seg.limit;
            int newEnd = base + size;
            if (!(newEnd <= seg.base || base >= segEnd)) 
            {
                overlaps = true;
                break;
            }
        }
    
        // returns null if there is overlap, the size is larger than memory, or the address is negative
        if (overlaps || base + size > MEMORY_SIZE || base < 0) 
        {
            return null;
        }
    
        // create the segment
        Segment manualSeg = new Segment(name, base, size);
        segments.add(manualSeg);
        segT.put(segments.size() - 1, manualSeg);
    
        // adjust the free memory space affected and makes sure to account for the extra free space if the segment does not take up the entirety of it
        for (Iterator<FreeMem> it = freeMemory.iterator(); it.hasNext(); ) 
        {
            FreeMem mem = it.next();
            int memEnd = mem.base + mem.size;
            int mEnd = base + size;
            if (base >= mem.base && mEnd <= memEnd) 
            {
                it.remove();
                if (base > mem.base)
                 {
                    freeMemory.add(new FreeMem(mem.base, base - mem.base));
                }
                if (mEnd < memEnd)
                 {
                    freeMemory.add(new FreeMem(mEnd, memEnd - mEnd));
                }
                break;
            }
        }
    
        return manualSeg;
    }

    // removes any free memory between segments and moves it to the end of the memory
    static void compactMemory()
     {
        // sorts segments so it will compact properly
        segments.sort(Comparator.comparingInt(f -> f.base));
        int currentBase = 0;

        // adjust the segments location so that it will all line up properly
        for (Segment seg : segments) 
        {
            seg.base = currentBase;
            currentBase += seg.limit;
        }

        // clear all of the free memory slots and make one large one that is the total free space
        freeMemory.clear();
        if (currentBase < MEMORY_SIZE) 
        {
            freeMemory.add(new FreeMem(currentBase, MEMORY_SIZE - currentBase));
        }
    }

    // removes a segment from memory
    public void removeSegment(String name) 
    {
        Segment toRemove = null;

        // looks for the segment with the matching name
        for (Segment seg : segments) 
        {
            if (seg.name.equals(name)) 
            {
                toRemove = seg;
                break;
            }
        }

        if (toRemove != null) 
        {
            // removes the segment from the list and creates a new free memory slot at the location
            segments.remove(toRemove);
            freeMemory.add(new FreeMem(toRemove.base, toRemove.limit));
            mergeFreeMemory();
        }
    }
    
    // combines two free memory slots that are next to each other into one big one
    public void mergeFreeMemory() 
    {
        // sort the free memroy slots by base address
        freeMemory.sort(Comparator.comparingInt(f -> f.base));

        // checks each free slot to see if there are any that are touching
        for (int i = 0; i < freeMemory.size() - 1; ) 
        {
            FreeMem current = freeMemory.get(i);
            FreeMem next = freeMemory.get(i + 1);

            // if they are touching
            if (current.base + current.size == next.base) 
            {
                // combines the size and removes the next slot from memory
                current.size += next.size;
                freeMemory.remove(i + 1);
            } 
            else 
            {
                i++;
            }
        }
    }
    
}
