import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class App {
    static int cacheSize;
    static int blockSize;
    static int ways;
    static int offsetBits;
    static int numSets;
    static int indexBits;
    static int tagBits;
    // Uses a stack to keep track of LRU.
    // The element that is popped is the LRU
    // Everytime an element is accessed, it gets back to the end of the list
    static List<Stack<Integer>> cache;

    public static void main(String[] args) throws Exception {
        Scanner scnr = new Scanner(System.in);
        // Requesting cache size, block size, and associativity from user
        System.out.print("Enter Cache Size: ");
        cacheSize = scnr.nextInt();
        System.out.print("Enter Block Size (Bytes): ");
        blockSize = scnr.nextInt();
        System.out.print("Enter associativity (ways): ");
        ways = scnr.nextInt();

        // Calculating each value and storing in variables.
        offsetBits = (int) (Math.log((double) blockSize) / Math.log(2)); // log2(blockSize)
        numSets = cacheSize / (blockSize * ways);
        indexBits = (int) (Math.log((double) numSets) / Math.log(2)); // log2(numSets)
        tagBits = 32 - indexBits - offsetBits;

        //1D array = Number of Sets
        //2D array = Number of Ways currently (max = ways)
        cache = new ArrayList<>();
        for (int i = 0; i < numSets; i++) {
            cache.add(new Stack<>());
        }

        System.out.println("offsetbits = " + offsetBits);
        System.out.println("numSets = " + numSets);
        System.out.println("index bits = " + indexBits);
        System.out.println("tagbits = " + tagBits);

        // Read the addresses file placed in the project's src folder
        String path = "src/address.txt";
        if (args.length > 0)
            path = args[0];

        try {
            List<Long> addresses = readAddresses(path);
            System.out.println("Read " + addresses.size() + " addresses from: " + path);
            int offset;
            int index;
            int tag;
            Stack<Integer> set;

            int numOfHits = 0;
            int numOfMisses = 0;            

            // Main for loop for program
            for (int i = 0; i < addresses.size(); i++) {
                int address = (int) addresses.get(i).longValue();
                offset = address & (blockSize - 1);
                index = (address >> offsetBits) & (numSets - 1);
                tag = (int)(address >> (offsetBits + indexBits));
                set = cache.get(index);

                System.out.printf("[ %2d] Addr=0x%04x", i + 1, addresses.get(i));
                System.out.printf(" Tag=0x%05x", tag);
                System.out.print(" Index=" + index);
                System.out.print(" Offset=" + offset);

                if(set.contains(tag)) {
                    set.remove((Integer) tag);
                    set.push(tag);
                    numOfHits += 1;
                    System.out.print(" --> HIT");
                } else {
                    
                    System.out.print(" --> MISS");
                    if(set.size() == ways) {
                        int evictedNum = set.remove(0);
                        System.out.printf(" Evicted=%05x", evictedNum);
                    } else {

                        System.out.print(" Evicted=-");
                    }
                    set.push(tag);
                    numOfMisses += 1;
                }

                //Printing out set
                System.out.print(" Set:[");
                for(int item : set) {
                    System.out.printf("%05x, ", item);
                }
                System.out.print("]\n");

            }

        } catch (IOException e) {
            System.err.println("Failed to read addresses: " + e.getMessage());
        }

        scnr.close();

    }

    /**
     * Read addresses from a plain-text file. Each non-empty line may contain
     * a single address in hex (0x...), binary (0b...), or decimal form.
     * Leading/trailing whitespace is trimmed; lines starting with '#' are ignored.
     */
    public static List<Long> readAddresses(String filePath) throws IOException {
        Path p = Paths.get(filePath);
        List<Long> out = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(p)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#"))
                    continue;
                String token = line.split("\\s+")[0];
                long value = parseAddress(token);
                out.add(value);
            }
        }
        return out;
    }

    /**
     * Parse a single numeric address supporting 0x (hex), 0b (binary), or decimal.
     */
    public static long parseAddress(String s) {
        if (s.startsWith("0x") || s.startsWith("0X")) {
            return Long.parseUnsignedLong(s.substring(2), 16);
        }
        if (s.startsWith("0b") || s.startsWith("0B")) {
            return Long.parseUnsignedLong(s.substring(2), 2);
        }
        return Long.parseUnsignedLong(s, 10);
    }

    public static void calculateWithAddress() {

    }

}
