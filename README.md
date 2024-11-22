# java-distributed-systems
Hema Shaswar Rasool - hs21123@auis.edu.krd
Ronak Salah Mohsin - rs18169@auis.edu.krd

This system distributes words from a user-input paragraph to five worker processes, collects them back, and reconstructs the paragraph while synchronizing the processes using Lamport clocks.

## How to Run the System

### Step 1: Start the Worker Processes

Run the following worker processes in **separate terminal windows** in any order:

1. **Alpha process**:
   ```bash
   java Alpha
   ```
2. **Beta process**:
   ```bash
   java Beta
   ```
3. **Charlie process**:
   ```bash
   java Charlie
   ```
4. **Delta process**:
   ```bash
   java Delta
   ```
5. **Echo process**:
   ```bash
   java Echo
   ```

### Step 2: Run the Main Process

After starting all the worker processes, run the `Main` process in a **new terminal window**:

```bash
java Main
```

### Step 3: Enter a Paragraph

When prompted, enter a paragraph. The system will distribute the words to the workers, wait for processing, and then reconstruct the paragraph.

## Notes

- Ensure all worker processes are running **before** starting the `Main` process.
```