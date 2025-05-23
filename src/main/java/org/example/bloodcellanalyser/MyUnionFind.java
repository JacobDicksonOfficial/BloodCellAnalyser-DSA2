package org.example.bloodcellanalyser; // Declares this class is part of the 'bloodcellanalyser' package

// This class implements the Union-Find (Disjoint Set Union) data structure
// It's used for efficiently grouping connected pixels (clusters of blood cells)
public class MyUnionFind {

    // Array where parent[i] points to the parent of element i.
    // If parent[i] == i, then i is the root of its set
    private int[] parent;

    // Size of each tree rooted at an element
    // Used to optimize merging (attach smaller tree under larger one)
    private int[] size;

    // Constructor to initialize Union-Find with n elements (usually number of pixels)
    public MyUnionFind(int n) {
        parent = new int[n]; // Initialize parent array
        size = new int[n];   // Initialize size array

        // Initially, every element is its own parent (self-loop)
        // and every set has size 1
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }

    // Find the root of element x with path compression
    public int find(int x) {
        // If x is not the root, recursively find the root
        // and compress the path by pointing x directly to the root
        if (parent[x] != x) {
            parent[x] = find(parent[x]); // Path compression
        }
        return parent[x]; // Return the root
    }

    // Union two sets by connecting their roots
    public void union(int x, int y) {
        // Find roots of x and y
        int rootX = find(x);
        int rootY = find(y);

        // If they are in different sets, connect them
        if (rootX != rootY) {
            // Attach the smaller tree under the larger tree to keep it flat
            if (size[rootX] < size[rootY]) {
                parent[rootX] = rootY; // Make rootY the parent
                size[rootY] += size[rootX]; // Update size of the new root
            } else {
                parent[rootY] = rootX; // Make rootX the parent
                size[rootX] += size[rootY]; // Update size of the new root
            }
        }
    }

    // Returns the parent array (for inspection or debugging)
    public int[] getParent() {
        return parent;
    }

    // Returns the size array (size of each root's tree)
    public int[] getNodeSizes() {
        return size;
    }
}
