# JSI — Java Shellcode Injector  
### Runner & Payload Builder Framework

**JSI (Java Shellcode Injector)** is a lightweight red-team framework for **generating** and **executing** shellcode payloads using:

- **Donut** — converts EXE/DLL/.NET tools into position-independent shellcode  
- **JSI Java Loader** — executes payloads fully in memory with no EXE execution  

This toolchain enables execution of offensive and diagnostic tooling in highly restricted environments, achieving **100% AppLocker bypass** and **loader-level EDR evasion**.

AppLocker Execution Bypass via Java In-Memory Shellcode Injection (JSI)

**Test was performed inside environment which did not allow to run ANY exe and had windows defender and all other modules turned up. Basically there should be no way to run EXE/MSI or to bypass antivirus, but by using framework we created it is possible. There is no other working way to run mimikatz from box or Rubeus if AppLocker enabled and windows defender enabled. If needed I can come to your office in Bay Area and show you how it works.**

**Finding Type:** Application Control Bypass

**Category:** AppLocker / Application Whitelisting

**Severity:** **Critical**

**Impact:** Full arbitrary code execution despite enforced AppLocker policies

**Executive Summary**

A critical weakness was identified in the current **AppLocker enforcement model as well as all windows defender features**, allowing **complete bypass of executable restrictions** through the use of a Java-based in-memory loader framework (**JSI – Java Shellcode Injector**).

Even when AppLocker policies **explicitly block execution of EXE, DLL, MSI, PowerShell, and script files**, an attacker can execute **arbitrary native and .NET tooling fully in memory** as long as **Java execution is permitted**.

This results in **100% AppLocker bypass**, enabling execution of offensive tooling (e.g., credential dumping, AD reconnaissance) **without dropping binaries to disk** and without triggering standard execution controls.

**Technical Description**

**Root Cause**

AppLocker enforces **file-based execution control**, not **memory-resident execution**.

When Java is allowed by policy, it can be abused as a **trusted loader** to execute native shellcode in memory.

The JSI framework combines:

- **Donut** – converts EXE/DLL/.NET binaries into position-independent shellcode
- **Java Native Access (JNA)** – allocates executable memory and runs shellcode
- **Pure in-memory execution** – no EXE, DLL, or script is launched by the OS

AppLocker **never sees an executable start**, only java.exe, which is already trusted.

**Attack Flow**

1.  **Payload Preparation**
    - A legitimate EXE (e.g., Rubeus, Mimikatz) is converted into shellcode using Donut.
2.  **Java Loader Execution**
    - A Java program loads the shellcode from disk or memory.
3.  **In-Memory Execution**
    - Shellcode is executed directly inside the Java process using RWX memory.
4.  **Result**
    - The blocked EXE runs successfully despite AppLocker restrictions.

**Proof of Concept**

**Payload Generation (Donut)**

donut.exe -i Rubeus.exe -p "currentluid" -a 2 -o Rubeus.bin -z 3 -e 3 -t 5

**Explanation:**

- Converts Rubeus.exe into position-independent shellcode
- Embeds arguments directly
- Produces an encrypted, compressed shellcode payload

**Java Execution (JSI Runner)**

runner.bat Rubeus.bin currentluid

**Observed Behavior:**

- No EXE execution event
- No file dropped
- Payload executes fully in memory
- AppLocker enforcement is bypassed

Execution output is written to run_output.txt.

**Validated Payloads**

The following tools were confirmed to execute successfully via this method:

- **Mimikatz**
- **Rubeus**
- **SharpHound**
- **Seatbelt**
- **PingCastle**
- **Nuclei**
- Other .NET offensive and diagnostic tooling

Note: Not all EXEs convert cleanly with Donut, but many widely used red-team tools do.

**Security Impact**

- **Complete AppLocker bypass**
- **Credential theft and domain escalation possible**
- **Stealthy execution with minimal EDR visibility**
- **No disk artifacts**
- **Bypasses EXE, DLL, MSI, PowerShell, and script restrictions**

If Java is allowed, AppLocker **cannot be relied upon** as a standalone execution control.

**Detection Challenges**

- Execution occurs inside java.exe
- No child EXE process
- No dropped binaries
- No PowerShell or script execution
- Many EDRs miss shellcode execution inside trusted JVM processes
<img width="814" height="566" alt="image" src="https://github.com/user-attachments/assets/f60564a4-cd4b-481b-99a9-1992afba6206" />


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
