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

An executable file (`ProjektKIGruppeU.exe`) is provided for easy use. You can specify the server host and port when launching it:

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
