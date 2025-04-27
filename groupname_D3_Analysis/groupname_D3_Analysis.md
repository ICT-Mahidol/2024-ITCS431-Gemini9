![](logo.png)

Project Phase 2: OO Analysis and Design ITCS431 Software Design and Development  Present to  

Asst. Prof. Dr. Morakot Choetkiertikul  Dr. Chaiyong Ragkhitwetsagul 

Compose by  

6588057 Nantipat Maneerattanaporn 6588079 Poottapol Poonna  
6588122 Weixian Deng  

6588188 Yotsapat Rattanaprasert  

Semester 2 of the academic year 2023  
Faculty of Information and Communication Technology  Mahidol University 

**Login**  

**Use case description** 



|**Use Case Name:** Login |**ID**: U001 |**Importance Level:** High ||
| - | - | - | :- |
|**Primary Actor:** Any system user (e.g., Astronomer, Science Observer, Telescope Operator, Support, Developer, Administrator) |**Use Case Type:** Essential |||
|<p>**Stakeholders and Interests:** </p><p>- **System Users:** Need access to the system according to their privileges. </p><p>- **System Administrator:** Ensures only authorized users gain access. </p><p>- **Gemini System:** Maintains security and operational integrity. </p>||||
|<p>**Brief Description:** </p><p>The use case describes the process by which users authenticate themselves into the Gemini system. Users enter their credentials, and the system verifies their identity before granting access to specific functionalities based on their role. </p>||||
|<p>**Trigger:** </p><p>A user wants to access the system to perform their respective tasks. </p><p>**Type:** User-initiated </p>||||
|<p>**Relationships:** </p><p>**Association: User </p><p> Include: -  </p><p> Extend: -  </p><p> Generalization: -**  </p>||||



|<p>**Normal Flow of Events:** </p><p>1. The user navigates to the login page. </p><p>2. The system prompts for a username and password. </p><p>3. The user enters their credentials and submits the form. </p><p>4. The system verifies the credentials against the user database. </p><p>5. the system determines the user's role and associated permissions. </p><p>6. The system grants access and redirects the user to the appropriate dashboard. </p>|
| - |
|<p>**Subflows:** </p><p>- If the user has multiple roles, the system may prompt them to select an active role for the session. </p><p>- If the user has multi-factor authentication enabled, they must complete an additional verification step. </p>|
|<p>**Alternate/Exceptional Flow:** </p><p>1. If the credentials are incorrect, the system displays an error message and prompts the user to try again. </p><p>2. If the user fails multiple attempts, the system locks the account temporarily. </p><p>3. If the system is under maintenance, the login attempt is denied with a maintenance notification. </p><p>4. If the user’s role is restricted from logging in at the current operational level, access is denied with a message. </p>|

**Activity Diagram**  

![](Login_activity_diagram.jpg)

**Sequence Diagram**   

![](Login_sequence_diagram.jpg)

**Create a science plan  Use case description**  



|**Use Case Name:** Create a Science Plan |**ID**: U002 |**Importance Level:** High ||
| :- | - | - | :- |
|**Primary Actor:** Astronomer |**Use Case Type:** Essential |||
|<p>**Stakeholders and Interests:** </p><p>- **Astronomer:** Needs to create a science plan for observations. </p><p>- **Science Observer:** Uses the plan to validate and transform it into an observing program. </p><p>- **Gemini System:** Ensures science plans are properly formatted and executable.** </p>||||
|<p>**Brief Description:** </p><p>This use case describes the process where an astronomer creates a science plan by specifying observation targets, instruments, and settings. The plan must meet system constraints and is submitted for validation before execution.** </p>||||
|<p>**Trigger:** </p><p>An astronomer wants to propose an observation by creating a science plan in the system. </p><p>**Type:** User-initiated </p>||||
|<p>**Relationships:** </p><p>**Association:** Astronomer </p><p>**Include: -** </p><p>**Extend: - </p><p>Generalization: -**  </p>||||



|<p>**Normal Flow of Events:** </p><p>1. The astronomer logs in to the Gemini website.  </p><p>2. The astronomer accesses the science planning interface. </p><p>3. The system provides a form or tool to input observation details. </p><p>4. The astronomer specifies the target celestial objects. </p><p>5. The system allows the astronomer to review and edit the plan. </p><p>6. The astronomer checks if it is satisfied, and they submit the plan for validation. </p><p>7. The astronomer goes back to reviewing and editing the plan. </p><p>8. The astronomer submits the plan for validation. </p><p>9. The system stores the plan and notifies the Science Observer for review. </p>|
| - |
|<p>**Subflows:** </p><p>- The astronomer can test the science plan using the virtual telescope before submission. </p><p>- The astronomer can save a draft and continue editing later. </p>|
|<p>**Alternate/Exceptional Flow:** </p><p>1. If required fields are missing, the system prompts the astronomer to complete them. </p><p>2. If instrument selection is incompatible, the system suggests alternatives. </p><p>3. If scheduling conflicts arise, the system provides an alert and alternative options. </p><p>4. If the network connection is lost, the system saves the draft for later submission. </p>|

**Activity Diagram**  

![](Create_Plan_activity_diagram.jpg)

**Sequence Diagram**   

![](Create_Plan_sequence_diagram.jpg)

**Test a science plan  Use case description**  



|**Use Case Name:** Test a science plan |**ID**: U003 |**Importance Level:** High ||
| :- | - | - | :- |
|**Primary Actor:** Astronomer  |**Use Case Type:**  Detail, Essential |||



|<p>**Stakeholders and Interests:** </p><p>- **Astronomer:** Needs to verify the feasibility of the science plan before submission. </p><p>- **Science Observer:** Ensures that the plan meets observation requirements. </p><p>- **Gemini System:** Provides a simulation environment to check for potential issues.** </p>|
| - |
|<p>**Brief Description:** </p><p>This use case describes how an astronomer tests a science plan using the virtual telescope or interactive observing mode to verify its feasibility before submission. The test helps detect issues related to target visibility, instrument configuration, and environmental conditions. </p>|
|<p>**Trigger:** </p><p>An astronomer wants to verify the correctness of a science plan before submitting it. </p><p>**Type:** User-initiated** </p>|
|<p>**Relationships:** </p><p>**Association:** Astronomer**  </p><p>**Include:** Login, Simulate Observation, Validate Target Visibility, Check Instrument Compatibility** </p><p>**Extend: -** </p><p>**Generalization: -**  </p>|
|<p>**Normal Flow of Events:** </p><p>1. The astronomer logs in to the Gemini website.  </p><p>2. The astronomer selects a science plan to test. </p><p>3. The system loads the science plan and initializes the virtual telescope. </p><p>4. The system simulates the observation. </p><p>5. The system generates a test report with potential issues or warnings. </p><p>6. The astronomer reviews the report and makes necessary adjustments. </p><p>7. The astronomer either fixes issues and re-tests the plan or submits the tested plan for validation. </p>|



|<p>**Subflows:** </p><p>- If no major issues are detected, the system marks the plan as "Tested" for submission. </p><p>- The astronomer can adjust parameters and re-run the test multiple times. </p>|
| - |
|<p>**Alternate/Exceptional Flow:** </p><p>1. If the target is not visible during the selected time, the system suggests alternative timings. </p><p>2. If the selected instrument is unavailable, the system recommends compatible options. </p><p>3. If weather conditions are unsuitable, the system alerts the astronomer with predicted conditions. </p><p>4. If system resources are overloaded, the test is queued, and the astronomer is notified when it is completed. </p>|

**Activity Diagram**   

![](Test_plan_activity_diagram.jpg)

**Sequence Diagram**   

![](Test_plan_sequence_diagram.png)

**Manage astronomical data**  

**Use case description** 



|**Use Case Name:** Manage astronomical data  |**ID**: U004 |**Importance Level:** High ||
| :- | - | - | :- |
|**Primary Actor: Science Observer** |**Use Case Type: Detail,** Essential  |||
|<p>**Stakeholders and Interests:** </p><p>- **Astronomer:** Needs to access, analyze, and manage collected data. </p><p>- **Science Observer:** Ensures data integrity and assists with validation. </p><p>- **Gemini System:** Stores, organizes, and secures astronomical data for research and future access. </p>||||
|<p>**Brief Description:** </p><p>This use case describes how an astronomer accesses, processes, validates, and organizes collected astronomical data after an observation session. The data must be securely stored and made available for further analysis.** </p>||||
|<p>**Trigger:** </p><p>An astronomer wants to review, validate, or archive astronomical data after an observation session. </p><p>**Type:** User-initiated** </p>||||
|<p>**Relationships:** </p><p>**Association: Science Observer </p><p>Include: -** </p><p>**Extend: -** </p><p>**Generalization: -**  </p>||||



|<p>**Normal Flow of Events:** </p><p>1. The astronomer logs into the system and navigates to the data management interface. </p><p>2. The system displays available astronomical data, including metadata such as observation date and instrument settings. </p><p>3. The astronomer selects a dataset to review. </p><p>4. The system provides visualization tools to inspect and analyze the data. </p><p>5. The astronomer validates data integrity by checking for missing or corrupted data, annotates or adds comments for further analysis, and filters and organizes data based on observation details. </p><p>6. The astronomer chooses to either download the data for offline analysis or archive the data in the Gemini system for future access. </p><p>7. The system confirms the action and updates the data status. </p>|
| - |
|<p>**Subflows:**   </p><p>- If needed, the astronomer can **apply basic image processing** to enhance data quality. </p><p>- The system can **automatically archive** validated data in the Gemini Archive subsystem.** </p>|
|<p>**Alternate/Exceptional Flow:** </p><p>1. If the data is corrupted or incomplete, the system alerts the astronomer and suggests recovery options. </p><p>2. If network issues occur, the system allows offline mode for later synchronization. </p><p>3. If storage space is insufficient, the system prompts the astronomer to manage existing archives. </p>|

**Activity Diagram**   
![](Manage_data_activity_diagram.jpg)

**Sequence Diagram**   
![](Manage_data_sequence_diagram.png)

**Submit a Science Plan Use case description**



|**Use Case Name:** Submit a Science Plan |**ID**: U005 |**Importance Level:** High ||
| :- | - | - | :- |
|**Primary Actor:** Astronomer |**Use Case Type:** Essential  |||
|<p>**Stakeholders and Interests:** </p><p>- **Astronomer:** Needs to submit a completed science plan for validation and execution. </p><p>- **Science Observer:** Reviews and validates the plan before transforming it into an observing program. </p><p>- **Gemini System:** Ensures submitted plans meet scientific and operational requirements. </p>||||
|<p>**Brief Description:** </p><p>This use case describes how an astronomer submits a completed science plan for validation and further processing. The system checks for completeness and sends the plan to the Science Observer for review. </p>||||
|<p>**Trigger:** </p><p>An astronomer wants to finalize and submit a science plan for validation. </p><p>**Type:** User-initiated** </p>||||
|<p>**Relationships:** </p><p>**Association:** Astronomer </p><p>**Include: -**  </p><p>**Extend: -  </p><p>Generalization: -**  </p>||||



|<p>**Normal Flow of Events:**</p><p>1. The astronomer logs in to the Gemini website.  </p><p>2. The astronomer accesses the science planning interface. </p><p>3. The system displays a list of saved science plans. </p><p>4. The astronomer selects a plan to submit. </p><p>5. The system performs basic validation, including checking for missing or incomplete fields, ensuring required instruments are available, and verifying target visibility and scheduling feasibility. </p><p>6. The astronomer confirms submission. </p><p>7. The system assigns a status, such as "Pending Validation." </p><p>8. The system notifies the Science Observer for further review. </p>|
| - |
|<p>**Subflows:** </p><p>- The astronomer can add final comments or notes before submission. </p><p>- The system can suggest minor corrections before finalizing submission. </p>|
|<p>**Alternate/Exceptional Flow:**   </p><p>1. If required fields are missing, the system prompts the astronomer to complete them. </p><p>2. If the selected instrument is unavailable, the system suggests alternatives. </p><p>3. If the submission fails due to network issues, the system saves a draft for later submission. </p><p>4. If the Science Observer rejects the plan, it is returned to the astronomer for modifications. </p>|

**Activity Diagram**   

![](Submit_plan_activity_diagram.jpg)

**Sequence Diagram**   

![](Submit_plan_sequence_diagram.png)

**Validate a Science Plan Use case description** 



|**Use Case Name:** Validate a Science Plan |**ID**: U006 |**Importance Level:** High ||
| :- | - | - | :- |
|**Primary Actor:** Science Observer |**Use Case Type:** Essential  |||
|<p>**Stakeholders and Interests:** </p><p>- **Science Observer**: Ensures the science plan meets scientific and technical requirements. </p><p>- **Astronomer**: Needs confirmation that the science plan is valid and executable. </p><p>- **Gemini System**: Maintains the integrity of submitted science plans and ensures compatibility with operational constraints.** </p>||||
|<p>**Brief Description:** </p><p>This use case describes how a Science Observer reviews and validates a submitted science plan to ensure it meets observational constraints, instrument configurations, and scheduling feasibility before it is transformed into an observing program.** </p>||||
|<p>**Trigger:** </p><p>A science plan is submitted and needs validation before execution. </p><p>**Type:** User-initiated </p>||||
|<p>**Relationships:** </p><p>**Association:** Science Observer </p><p>**Include: -**  </p><p>**Extend: -**  </p><p>**Generalization: -**  </p>||||



|<p>**Normal Flow of Events** </p><p>1. The astronomer logs in to the Gemini website.  </p><p>2. The Science Observer accesses the science plan review interface. </p><p>3. The system presents a list of submitted science plans. </p><p>4. The observer selects a plan for validation. </p><p>5. The system performs automated checks. </p><p>6. The observer reviews the system’s validation results and performs manual checks for additional issues. </p><p>7. The observer decides to either between Approve the plan for execution and Request modifications from the astronomer. </p><p>8. System transforms the approved science plan into an observing program. </p>|
| - |
|<p>**Subflows:** </p><p>- If minor issues are detected, the observer can suggest corrections rather than rejecting the plan. </p><p>- The observer can prioritize certain plans based on scientific importance or scheduling availability. </p>|
|<p>**Alternate/Exceptional Flow:** </p><p>1. If the science plan is incomplete, the system marks it as "Requires Revision" and sends feedback to the astronomer. </p><p>2. If instrument availability changes, the observer can propose alternative configurations. </p><p>3. If there are scheduling conflicts, the system suggests adjustments or alternative time slots. </p>|

**Activity Diagram**   

![](Validate_plan_activity_diagram.png)

**Sequence Diagram**   

![](Validate_plan_sequence_diagram.jpg)

Class diagram

![](Class_diagram.png)

