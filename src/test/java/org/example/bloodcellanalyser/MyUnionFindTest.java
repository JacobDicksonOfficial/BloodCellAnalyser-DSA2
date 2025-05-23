package org.example.bloodcellanalyser; // This test class is part of the 'bloodcellanalyser' package

// Import JUnit 5 annotations and assertions
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*; // Allows use of assertEquals and other test assertions

// Unit test class for MyUnionFind
public class MyUnionFindTest {

    // Declare the union-find instance to be tested
    private MyUnionFind uf;

    // Set up a fresh instance of MyUnionFind with 10 elements before each test
    @BeforeEach
    public void setUp() {
        uf = new MyUnionFind(10); // initialize union-find structure with 10 nodes
    }

    // Test to ensure each element is initially its own root (no unions yet)
    @Test
    public void testInitialRoots() {
        for (int i = 0; i < 10; i++) {
            assertEquals(i, uf.find(i), "Each element should be its own root initially");
        }
    }

    // Test that union and find operations link elements into the same set
    @Test
    public void testUnionAndFind() {
        uf.union(1, 2); // union elements 1 and 2
        uf.union(2, 3); // union elements 2 and 3

        int root = uf.find(1); // should be the same for 1, 2, 3 after unions
        assertEquals(root, uf.find(2)); // check if 2 has the same root
        assertEquals(root, uf.find(3)); // check if 3 has the same root
    }

    // Test union by size: smaller tree should be merged under larger one
    @Test
    public void testUnionBySize() {
        uf.union(4, 5); // 4 and 5 form one set
        uf.union(6, 7); // 6 and 7 form another set
        uf.union(5, 6); // merging two sets should result in a larger root tree

        int root = uf.find(4); // root of the merged cluster
        assertEquals(root, uf.find(7)); // check if 7 belongs to same root
        assertEquals(4, uf.getNodeSizes()[root]); // total size should be 4 (nodes 4, 5, 6, 7)
    }

    // Test path compression: find operations should flatten tree structure
    @Test
    public void testPathCompression() {
        // Create a linear chain: 0-1-2-3-4
        uf.union(0, 1);
        uf.union(1, 2);
        uf.union(2, 3);
        uf.union(3, 4);

        int originalRoot = uf.find(0); // should be the root of the entire chain
        uf.find(4); // trigger path compression by accessing the deepest node

        // After path compression, all nodes should point directly to the root
        for (int i = 0; i <= 4; i++) {
            assertEquals(originalRoot, uf.find(i), "All elements should have same root after compression");
        }
    }
}
