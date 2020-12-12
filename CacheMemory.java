
/**
* Simulates cache memory
*
* @author Mariana De Luna
*/

import java.util.*;

public class CacheMemory {

	/** Set to true to print additional messages */
	private static final boolean DEBUG = false;

	/** The Main Memory this cache is connected to */
	private MainMemory mainMemory;

	/** Simulate cache as an array of CacheSet objects */
	private CacheSet[] cache;

	/**
	 * Number of bits used for selecting one byte within a cache line. These are the
	 * least significant bits of the memory address.
	 */
	private int numByteBits;

	/**
	 * Number of bits used for specifying the cache set that a memory address
	 * belongs to. These are the middle bits of the memory address.
	 */
	private int numSetBits;

	/**
	 * Number of bits used for specifying the tag associated with the memory
	 * address. These are the most significant bits of the memory address
	 */
	private int numTagBits;

	/**
	 * Count of the total number of cache requests. This is used for implementing
	 * the least recently used replacement algorithm; and for reporting information
	 * about the cache simulation
	 */
	private int requestCount;

	/**
	 * Count of the number of times a cache request is a hit. This is used for
	 * reporting information about the cache simulation
	 */
	private int hitCount;

	/**
	 * DO NOT MODIFY THIS METHOD
	 *
	 * Constructor creates a CacheMemory object. Note the design rules for valid
	 * values of each parameter. The simulated computer reads or writes a unit of
	 * one word (4 Bytes).
	 *
	 * @param m
	 *            The MainMemory object this cache is connected to.
	 * @param size
	 *            The size of this cache, in Bytes. Must be a multiple of the
	 *            lineSize.
	 * @param lineSize
	 *            The size of one cache line, in Bytes. Must be a multiple of 4
	 *            Bytes.
	 * @param linesPerSet
	 *            The number of lines per set. The number of lines in the cache must
	 *            be a multiple of the linesPerSet.
	 *
	 * @exception IllegalArgumentExcepction
	 *                if a parameter value violates a design rule.
	 */
	public CacheMemory(MainMemory m, int size, int lineSize, int linesPerSet) {

		if (lineSize % 4 != 0) {
			throw new IllegalArgumentException("lineSize is not a multiple of 4.");
		}

		if (size % lineSize != 0) {
			throw new IllegalArgumentException("size is not a multiple of lineSize.");
		}

		// number of lines in the cache
		int numLines = size / lineSize;

		if (numLines % linesPerSet != 0) {
			throw new IllegalArgumentException("number of lines is not a multiple of linesPerSet.");
		}

		// number of sets in the cache
		int numSets = numLines / linesPerSet;

		// Set the main memory
		mainMemory = m;

		// Initialize the counters to zero
		requestCount = 0;
		hitCount = 0;

		// Determine the number of bits required for the byte within a line,
		// for the set, and for the tag.
		int value;
		numByteBits = 0; // initialize to zero
		value = 1; // initialize to 2^0
		while (value < lineSize) {
			numByteBits++;
			value *= 2; // increase value by a power of 2
		}

		numSetBits = 0;
		value = 1;
		while (value < numSets) {
			numSetBits++;
			value *= 2;
		}

		// numTagBits is the remaining memory address bits
		numTagBits = 32 - numSetBits - numByteBits;

		System.out.println("CacheMemory constructor:");
		System.out.println("    numLines = " + numLines);
		System.out.println("    numSets = " + numSets);
		System.out.println("    numByteBits = " + numByteBits);
		System.out.println("    numSetBits = " + numSetBits);
		System.out.println("    numTagBits = " + numTagBits);
		System.out.println();

		// Create the array of CacheSet objects and initialize each CacheSet object
		cache = new CacheSet[numSets];
		for (int i = 0; i < cache.length; i++) {
			cache[i] = new CacheSet(lineSize, linesPerSet, numTagBits);
		}
	} // end of constructor

	/**
	 * DO NOT MODIFY THIS METHOD
	 *
	 * Prints the total number of requests and the number of requests that resulted
	 * in a cache hit.
	 */
	public void reportStats() {
		System.out.println("Number of requests: " + requestCount);
		System.out.println("Number of hits: " + hitCount);
		System.out.println("hit ratio: " + (double) hitCount / requestCount);
	}

	/**
	 * STUDENT MUST COMPLETE THIS METHOD
	 *
	 * Returns the 32-bits that begin at the specified memory address.
	 *
	 * @param address
	 *            The byte address where the 32-bit value begins.
	 * @return The 32-bit value read from memory. Index 0 of the array holds the
	 *         least significant bit of the binary value.
	 * @exception IllegalArgumentExcepction
	 *                if the address is not valid.
	 */
	public boolean[] read32(boolean[] address) {
		if (address.length > 32) {
			throw new IllegalArgumentException("address parameter must be 32 bits");
		}
		// Programming Assignment 5: Complete this method
		// The comments provide a guide for this method.

		// Where does the address map in the cache?

		// If data from the given address is not currently in cache, read the
		// corresponding line from memory
		// Call the readLineFromMemory method

		// Update CacheMemory data members and CacheLine data members
		// as needed for tracking cache hit rate and implementing the
		// least recently used replacement algorithm in the cache set.

		requestCount++;

		// determining the cache set
		int linesPerSet = cache[0].size();
		int numOfSets = cache.length;
		int bitsForCacheLineIndex = (int) (Math.log(numOfSets) / Math.log(2));
		boolean[] cacheSetBits = new boolean[Binary.BINARY_LENGTH];
		boolean[] cacheTagBits = new boolean[numTagBits];

		int k = numByteBits;
		for (int i = 0; i < bitsForCacheLineIndex; i++) {
			cacheSetBits[i] = address[k];
			k++;
		} // end for
		for (int i = k + numSetBits; i < Binary.BINARY_LENGTH; i++) {
			cacheSetBits[i] = false;
		} // end for
		long cacheSetIndex = Binary.binToUDec(cacheSetBits);

		// sets the tag bits into an array
		k = numByteBits + numSetBits;
		for (int i = 0; i < numTagBits; i++) {
			cacheTagBits[i] = address[k];
			k++;
		} // end for

		// check whether tag bits match the tag bits in the cache line
		for (int j = 0; j < linesPerSet; j++) {
			if (cache[(int) cacheSetIndex].getLine(j).isValid()) {
				
				for (int i = 0; i < numTagBits; i++) {
					if (cache[(int) cacheSetIndex].getLine(j).getTag()[i] == cacheTagBits[i]) {
						if(i == numTagBits-1) {
							hitCount++;
							cache[(int) cacheSetIndex].getLine(j).setLastUsed(requestCount);
						}// end if
					} else if (j == linesPerSet - 1) {
						//else tag bits are not the same
						readLineFromMemory(address, (int) cacheSetIndex, cacheTagBits);
					} else {
						break;
					} // end if
				} // end for
			} else {
				//else is not valid
				if (j == linesPerSet - 1) {
					readLineFromMemory(address, (int) cacheSetIndex, cacheTagBits);
				} // end if
			} // end if
		} // end for

		// replace this placeholder return with the data copied from the cache line
		return address;
	}

	/**
	 * STUDENT MUST COMPLETE THIS METHOD
	 *
	 * Copies a line of data from memory into cache. Selects the cache line to
	 * replace. Uses the Least Recently Used (LRU) algorithm when a choice must be
	 * made between multiple cache lines that could be replaced.
	 *
	 * @param address
	 *            The requested memory address.
	 * @param setNum
	 *            The set number where the address maps to.
	 * @param tagBits
	 *            The tag bits from the memory address.
	 *
	 * @return The line that was read from memory. This line is also written into
	 *         the cache.
	 */
	private CacheLine readLineFromMemory(boolean[] address, int setNum, boolean[] tagBits) {

		// Programming Assignment 5: Complete this method
		// The comments provide a guide for this method.

		// Use the LRU (least recently used) replacement scheme to select a line
		// within the set.

		// Read the line from memory. The memory address to read is the
		// first memory address of the line that contains the requested address.
		// The MainMemory read method should be called.

		// Copy the line read from memory into the cache

		int linesPerSet = cache[0].size();
		int indexOfLine = 0;
		for (int n = 0; n < linesPerSet; n++) {
			if (cache[setNum].getLine(indexOfLine).getLastUsed() < cache[setNum].getLine(n).getLastUsed()) {
				indexOfLine = n;
			} // end if
			if (cache[setNum].getLine(indexOfLine).getLastUsed() == -1) {
				break;
			} // end if
		} // end for

		boolean[] cacheTagBits = new boolean[numTagBits];
		int k = numByteBits + numSetBits;
		for (int i = 0; i < numTagBits; i++) {
			cacheTagBits[i] = address[k];
			k++;
		} // end for

		// sets tag and data into cache line
		boolean[][] data = mainMemory.read(address, cache[setNum].getLine(indexOfLine).size());
		cache[setNum].getLine(indexOfLine).setValid();
		cache[setNum].getLine(indexOfLine).setData(data);
		cache[setNum].getLine(indexOfLine).setTag(cacheTagBits);
		int lineSize = cache[setNum].getLine(indexOfLine).size();

		// replace this placeholder return with the correct line to return
		return cache[setNum].getLine(indexOfLine);

	} // end readLineFromMemory
} // end CacheMemory class
