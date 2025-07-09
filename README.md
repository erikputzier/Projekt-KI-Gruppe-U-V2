# How to Use

This guide explains two ways to run the AI client:

---

## Option 1: Run the `Client` Class

1. Open the source code and locate the `Client` class.
2. Modify the default server settings in the class definition:

   ```java
   // In Client.java
   private static final String DEFAULT_SERVER_HOST = "localhost";  // Change as needed
   private static final int    DEFAULT_SERVER_PORT = 5555;         // Change as needed
3. Run the `Client` class from your IDE.

## Option 2: Run the `.exe` with Command-Line Arguments

A zip archive (`ProjektKIGruppeU.zip`) is provided for easy use. 
Here is a step-by-step guide to run the executable:
1. **Download and Extract**: Download the `ProjektKIGruppeU.zip` file and extract it to a folder of your choice.
2. **Locate the Executable**: Inside the extracted folder, find the `ProjektKIGruppeU.exe` file.
3. **Open Command Prompt**: Open a command prompt window (cmd) and navigate to the folder containing the executable.
4. **Visual Guide**: https://youtu.be/Mqsy8rqi6NM

You can specify the server host and port when launching it:

```bash
  ProjektKIGruppeU.exe [host] [port]
```
### Example Usage

1. **Custom host and port**:

   ```bash
   ProjektKIGruppeU.exe localhost 1234
2. **Default settings**:

   ```bash
   ProjektKIGruppeU.exe
   ```
   Uses the default host (localhost) and port (5555).
