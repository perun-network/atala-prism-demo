## Description
This is a working demo application for issuing a residence permit using a **thresholdized signature scheme**. The demo showcases the specifics of the residence permit issuance process and how threshold signatures are used to secure and verify the process.

### Key Features:
- A working demo application that demonstrates how threshold signatures are used in residence permit issuance.
- Publicly accessible hosted instance for easy user interaction.
- Complete project documentation available on GitHub.
- A 5–10 minute video walkthrough providing a step-by-step guide.

---

## 📌 **Acceptance Criteria**
✅ A functional demo application for issuing residence permits using threshold signatures.  
✅ A user-friendly and publicly accessible website.  
✅ Comprehensive project documentation available on GitHub.  
✅ A clear 5–10 minute demo video providing a step-by-step guide.

---

## 🎯 **Demo Scenario**
In the demo, **Alice** wants the **Faber foreign institutions** to sign her residence permit.

- **Signer 1**, **Signer 2**, and **Signer 3** are members of the Faber Institute.
- Each signer reserves a partial precomputation and can be contacted to sign the residence permit.
- Alice needs to contact **at least two of them** to sign the permit and produce a valid signature (2-out-of-3 secret sharing).
- The signed credential can be verified using the generated public key to confirm Alice's residence permit validity.

---

## 🚀 **Milestone Plan** (Feb 2025 – Mar 2025)

| Milestone | Timeline |
|-----------|----------|
| **Tech Stack Research** | Feb 12 – Feb 28 |
| **Backend Development** | Mar 1 – Mar 12 |
| **Frontend Development** | Mar 12 – Mar 20 |
| **Testing** | Mar 20 – Mar 25 |
| **Project Closure & Final Demo** | Mar 25 – Mar 31 |
| **Final Submission** | Mar 31 |

---

## 🏗️ **Design**
The demo will be built as a simple **web application** to demonstrate the use of the **Threshold ECDSA library**.
- Clean and minimalistic UI.
- Focus on usability and ease of understanding.

---

## **Tech Stack**
### **Backend**
- **Web server**: Ktor (Kotlin)
- **Cryptographic library**: `perun_network.threshold_ecdsa`
- **Serialization**: `kotlinx.serialization`

### **Frontend**
- **HTML/CSS/JavaScript** (Minimalistic, custom styling)
- **Responsive layout** using Flexbox and Grid
- **Vanilla JavaScript** for interactivity

---

## **Architecture**
The project is structured as a Kotlin-based full-stack application:

### **Frontend**
- HTML files for rendering
- Static resources for styling and assets

### **Backend**
- Ktor-based backend
- Handles routing, serialization, and application logic
- Integrates the **Threshold ECDSA library** for secure signing

### **Threshold Signature Integration**
- Data classes for processing input
- Backend signers for handling signing requests
- Combination of partial signatures to produce a valid signature

---

## 🔥 **Frontend Flow**
1. **Landing Page** – Alice and Faber options.
2. **Alice** – Document selection and request for signing.
3. **Faber** – Selection of signers and signing process.
4. **Alice Import** – Import partial signatures.
5. **Alice Verify** – Combine partial signatures and verify the final result.

---

## 💻 **Prerequisites**
Ensure you have the following installed before running the project:
- **Java 11+**
- **Kotlin 1.6+**
- **Gradle 7.x**
- Compatible browsers: Chrome, Firefox, Edge

---

## 📢 **How to Run the Project**
1. Clone the repository:
    ```bash
    git clone https://github.com/perun-network/atala-prism-demo.git
    ```
2. Navigate to the project folder:
    ```bash
   cd atala-prism-demo
    ```
3. Build and run:
    ```bash
   ./gradlew build
   ./gradlew run
    ```
4. Open the local browser:
   ```
   http://localhost:8080
   ```
## 💡 Troubleshooting

| **Issue**                             | **Solution**                                                                 |
|---------------------------------------|-----------------------------------------------------------------------------|
| **Port already in use**               | Kill the process using the port: `kill -9 $(lsof -t -i:8080)`               |   
| **Build error**                       | Run `./gradlew clean` and rebuild                                            |
| **Signing failure**                   | Verify that the threshold key and signer availability meet the 2-out-of-3 threshold |
| **Backend endpoint returning 404**    | Verify that routing is configured correctly in the backend                  |
| **Frontend not loading**              | Check console logs for JavaScript errors and confirm file paths             |

## 📜 License
**Copyright 2024 PolyCrypt GmbH**. 

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Use of the source code is governed by the Apache 2.0 license that can be found in the [LICENSE](LICENSE) file.


## 🌐 Links
- Library Repository: https://github.com/perun-network/atala-prism-threshold
- Documentation: https://github.com/perun-network/atala-prism-threshold/wiki

