# JSI — Java Shellcode Injector  
### Runner & Payload Builder Framework

**JSI (Java Shellcode Injector)** is a lightweight red-team framework for **generating** and **executing** shellcode payloads using:

- **Donut** — converts EXE/DLL/.NET tools into position-independent shellcode  
- **JSI Java Loader** — executes payloads fully in memory with no EXE execution  

This toolchain enables execution of offensive and diagnostic tooling in highly restricted environments, achieving **100% AppLocker bypass** and **loader-level EDR evasion**.


<img width="928" height="631" alt="image" src="https://github.com/user-attachments/assets/5fa7e1ce-6592-49db-9b76-fefa43a40812" />

---

##   Key Capabilities

###   100% AppLocker Bypass
All payloads run **in-memory via Java**, not as external executables.  
This completely bypasses Applocker and application control policies.
Even if policy does not allow to run EXE, MSI, .ps1, etc and allows to run java we can run artitaty EXE inside memroy.

###   Loader-Level EDR Bypass
The JSI Loader:

- Allocates RWX memory via JNA  
- Injects shellcode directly  
- Executes it inside the Java process  
- Avoids EXE creation events and many user‑mode hooks  

⚠ 
---

##   Supported Payloads

Not every EXE converts cleanly into Donut shellcode.  
However, the following have been fully validated to work:

- **Nuclei**  
- **PingCastle**  
- **Mimikatz**  
- Common .NET tooling (Rubeus, SharpHound, Seatbelt, etc.)

---

##   Payload Generation Example (Donut)

Real payload creation example:

```
./donut.exe -i .\Rubeus.exe -p "currentluid" -a 2 -o payload.bin -z 3 -e 3 -t 5
```

Breakdown:

- `-i` → input binary  
- `-p` → arguments passed to payload  
- `-a 2` → AMD64  
- `-o` → output shellcode  
- `-z 3` → compression  
- `-e 3` → entropy/encryption  
- `-t 5` → exit thread  

---

##   Components

### **1. MakePayload.bat — Donut Wrapper**
Simplifies Donut usage by providing:

- Automatic naming (`<binary>.bin`)  
- Random filename mode via `-r`  
- Argument embedding  

Examples:

```
.\MakePayload.bat Rubeus.exe currentluid
```

Randomized:

```
.\MakePayload.bat Rubeus.exe currentluid -r
```

---

### **2. JSI Java Shellcode Runner**
Loads and executes Donut payloads entirely in memory.

Usage:

```
.\runner.bat .\Rubeus.bin currentluid
```

Runner actions:

- Loads shellcode  
- Allocates RWX memory  
- Executes payload  
- Writes logs to run_output.txt  

---

## 🛠 Compiling the Java Loader

Use Java 8 compatible bytecode for maximum compatibility.

```
javac -cp "jna-5.13.0.jar" --release 8 ShellcodeRunner.java
```

Notes:

- JNA version may vary  
- `--release 8` ensures Java 8 compatibility  
- Add more JARs using `-cp` if needed  

---

##   Project Structure

```
/projectx/
 ├── MakePayload.bat          → Donut wrapper
 ├── runner.bat               → Java loader wrapper (JSI)
 ├── ShellcodeRunner.java     → JSI core injector logic
 ├── jna-5.13.0.jar           → JNA dependency
 ├── payloads/                → Output shellcode files
 ├── run_output.txt           → Execution log
```

---

## ⚠ Legal Notice

This project is intended **only for authorized penetration testing, red-team exercises, research, and controlled security assessment**.  
The authors assume no responsibility for misuse.

---

## Summary

**JSI — Java Shellcode Injector**  
A powerful, compact framework for:

- Building shellcode payloads with Donut  
- Executing payloads in-memory via Java  
- Bypassing AppLocker and evading many EDR detections  
- Running tools like Nuclei, PingCastle, and Mimikatz **without dropping EXEs**  

A practical example of modern, stealthy in-memory execution for red-team operations and security research.
