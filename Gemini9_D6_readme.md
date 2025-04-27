# Gemini Science Plan Application (D6 Prototype)

This is a Spring Boot web application for managing Science Plans for the Gemini telescope, created as part of Project Deliverable D6. This application supports 3 main use cases (Create, Test, Submit) and utilizes the OCS Library for certain operations.

## Group Members

* 6588057 Nantipat Maneerattanaporn
* 6588122 Weixian Deng
* 6588188 Yotsapat Rattanaprasert
* 6588079 Poottapol Poonna


## Technologies Used

* **Backend:** Spring Boot (Java)
* **Frontend:** Thymeleaf (Server-Side Templating)
* **Database:** H2 (In-Memory Database for development)
* **Build Tool:** Gradle (or Maven if used)
* **Security:** Spring Security
* **External Library:** OCS Library (https://github.com/cragkhit/SpringBootOCS)

## How to Run the Application

1.  **Clone Repository:**
    ```bash
    git clone <(https://github.com/ICT-Mahidol/2024-ITCS431-Gemini9.git)>
    cd <Gemini9_D6/Gemini_D6/D6/Gemini9>

    ```
2.  **Run using IDE (e.g., IntelliJ IDEA):**
    * Open the project in IntelliJ IDEA.
    * Locate the `Gemini9Application.java` file (or your main Application class).
    * Right-click the file and select "Run 'Gemini9Application.main()'".
3.  **Run using Gradle (we use this):**
    * Open a Terminal or Command Prompt in the project's root directory.
    * Run the command: `./gradlew bootRun` (for Linux/macOS) or `gradlew.bat bootRun` (for Windows).
4.  **Run using Maven (if applicable):**
    * Open a Terminal or Command Prompt in the project's root directory.
    * Run the command: `mvn spring-boot:run`

5.  **Access the Application:**
    * Open a web browser and navigate to: `http://localhost:8080` (or another port if configured in `application.properties`).

* **Admin:**
    * Username: `admin`
    * Password: `adminpass`
    * Role: `ROLE_ADMIN`
* **Astronomer:**
    * Username: `astro1`
    * Password: `astroPa55` (or as configured)
    * Role: `ROLE_ASTRONOMER`
* **Science Observer:** (if this role exists/is used)
    * Username: `sciObs1`
    * Password: `sciPa55w0rd`
    * Role: `ROLE_SCIENCE_OBSERVER`

*(Note: If DataInitializer is not used, specify how to create users or list existing ones.)*

## Model Mapping Strategy

To ensure compatibility with the external OCS Library while maintaining flexibility and independence within our application, a mapping strategy is employed, primarily within the `SciencePlanController`.

**Rationale:**

* **Decoupling:** Our application's internal models (e.g., `th.ac.mahidol.ict.Gemini_d6.model.SciencePlan`, `SciencePlanStatus`, `TelescopeLocation`) are distinct from the OCS library's models (e.g., `edu.gemini.app.ocs.model.SciencePlan`, `edu.gemini.app.ocs.model.SciencePlan.STATUS`, `edu.gemini.app.ocs.model.SciencePlan.TELESCOPELOC`). This prevents our core application logic and database schema from being tightly bound to the OCS library's specific implementation details. If the OCS library changes internally, we only need to update the mapping layer.
* **Flexibility:** Our local models can be tailored for our application's needs, including JPA annotations and potentially additional fields not relevant to OCS.
* **Clear Boundary:** This approach creates a clear separation between the application's internal data representation and the data structure required by the external OCS API.

**Mechanism:**

* The `mapToOcsPlan` private helper method within `SciencePlanController.java` acts as the primary **adapter/translator**.
* Before calling OCS API methods like `ocs.createSciencePlan`, `ocs.testSciencePlan`, or `ocs.submitSciencePlan`, this method is invoked.
* It takes our local `SciencePlan` entity (including the embedded `DataProcessingRequirements`) as input.
* It creates an instance of the `edu.gemini.app.ocs.model.SciencePlan` object expected by the OCS library.
* It then manually maps the corresponding fields, performing necessary **value and format transformations**.

**Mapping Examples:**

* **Status:** Our local `SciencePlanStatus.CREATED` is mapped to `edu.gemini.app.ocs.model.SciencePlan.STATUS.SAVED` before calling OCS methods like `testSciencePlan`, as OCS expects this state for testing. Other statuses (like `TESTED`, `SUBMITTED`) are mapped to their corresponding OCS enum values.
* **Telescope Location:** Our local `TelescopeLocation.HAWAII` enum is mapped to `edu.gemini.app.ocs.model.SciencePlan.TELESCOPELOC.HAWAII`.
* **Dates:** Our internal `LocalDateTime` objects for start/end dates are formatted into the specific `String` representation "dd/MM/yyyy HH:mm:ss" expected by the OCS `SciencePlan` model's setters.
* **Data Processing Enums:** Local enums like `FileType.PNG`, `FileQuality.LOW`, `ColorType.COLOR` are mapped to the corresponding String values expected by the OCS `DataProcRequirement` object (e.g., "PNG", "Low", "Color mode").
* **Numeric DPR Values:** Decimal values (Contrast, Brightness, etc.) are converted to `double` if necessary.

This mapping ensures that while our application uses its own well-defined internal models, it correctly communicates with the OCS library using the specific data types, formats, and enum values required by its API.

## Steps to Test Use Cases

**Prerequisite:** The application must be running, and you need to log in with a user possessing the role specified for each use case.

### Use Case 1: Create Science Plan

* **User Role:** Astronomer (e.g., `astro1`)
* **Steps:**
    1.  Log in with the Astronomer's username and password.
    2.  Navigate to the Welcome page (usually the redirect target after login).
    3.  Click the "Create Science Plan" link or button.
    4.  The system displays the "Create Science Plan" page. The `Creator` field is automatically filled based on the logged-in user. The `Plan ID` will be automatically generated upon saving.
    5.  Fill out the form completely:
        * **Plan Name:** A short descriptive name.
        * **Funding:** Numerical value (USD).
        * **Objective:** Detailed description of the observation goals.
        * **Start Date & Time:** Select from the calendar/time input.
        * **End Date & Time:** Select from the calendar/time input. (Must be after or equal to Start Date).
        * **Target Star System:** Select from the dropdown list of star systems.
        * **Telescope Location:** Select either HAWAII or CHILE.
        * **Data Processing Requirements:** Select or enter values for:
            * FileType (PNG, JPEG, RAW)
            * FileQuality (Low, Fine)
            * Color Type (Color mode, B&W mode)
            * Contrast (decimal)
            * Brightness (decimal, color mode only)
            * Saturation (decimal, color mode only)
            * Highlights (decimal, B&W mode only)
            * Exposure (decimal)
            * Shadows (decimal, B&W mode only)
            * Whites (decimal, B&W mode only)
            * Blacks (decimal, B&W mode only)
            * Luminance (decimal, color mode only)
            * Hue (decimal, color mode only)
        * **Caution:** Ensure data validity according to form hints (e.g., Telescope Location must match Star System quadrant, dates must cover visibility month). The system will also perform basic validation (e.g., Start Date before End Date).
    6.  Click the "Create Science Plan" button.
    7.  The system validates the input. If invalid (e.g., start date after end date), an error is shown on the form.
    8.  If valid, the system sends data to the OCS Library (`ocs.createSciencePlan`) for initial validation/creation and saves the plan to the application's database.
    9.  On success, you will be redirected to the "View Science Plans" page with a success message "Science Plan (ID: [Local_ID]) created successfully! OCS reported: [OCS_Message]", and the new plan will appear in the table with status `CREATED`. If OCS reports an incompatibility error during creation, it will be shown on the form.

### Use Case 2: Test Science Plan

* **User Role:** Astronomer (e.g., `astro1`)
* **Prerequisite:** A Science Plan with status `CREATED` must exist.
* **Steps:**
    1.  Log in with the Astronomer's username and password.
    2.  Navigate to the "View Science Plans" page.
    3.  Find the Science Plan you want to test (must have status `CREATED`).
    4.  Click the "Test" button at the end of that plan's row.
    5.  The system sends the plan data to the OCS Library for testing (`ocs.testSciencePlan`). The OCS performs various checks, likely including:
        * Star System Selection validity.
        * Image Processing Configuration compatibility.
        * Telescope Location alignment with requirements.
        * Observation Duration constraints.
    6.  The test result from OCS will be displayed as a message at the top of the "View Science Plans" page:
        * **If successful:** The message will indicate "tested successfully", and the plan's status in the table will change to `TESTED`.
        * **If failed:** The message will indicate "testing failed" with error details from OCS, and the plan's status will remain `CREATED`.

### Use Case 3: Submit Science Plan

* **User Role:** Astronomer (e.g., `astro1`)
* **Prerequisite:** A Science Plan with status `TESTED` must exist.
* **Steps:**
    1.  Log in with the Astronomer's username and password.
    2.  Navigate to the "View Science Plans" page.
    3.  Find the Science Plan you want to submit (must have status `TESTED`).
    4.  Click the "Submit" button at the end of that plan's row.
    5.  Confirm the submission in the pop-up dialog.
    6.  The system sends the plan data to the OCS Library for submission (`ocs.submitSciencePlan`).
    7.  If successful, a success message "submitted successfully" will be displayed, and the plan's status in the table will change to `SUBMITTED` (awaiting validation by an observer).
    8.  If unsuccessful, an error message will be displayed, and the status will remain `TESTED`.

### Additional Use Case: Edit Science Plan

* **User Role:** Admin or Astronomer
* **Prerequisite:** The Science Plan must have the status `CREATED` or `TESTED`.
* **Steps:**
    1.  Log in with an authorized user (Admin or Astronomer).
    2.  Navigate to the "View Science Plans" page.
    3.  Find the plan to edit (status must be `CREATED` or `TESTED`).
    4.  Click the "Edit" button.
    5.  Modify the information in the form as needed.
    6.  Click the "Update Science Plan" button.
    7.  On success, you will be redirected back to the "View Science Plans" page with a success message, and the data in the table will be updated (Note: This edit only affects the local application database, it is not sent to OCS again).

### Additional Use Case: Delete Science Plan

* **User Role:** Admin only
* **Prerequisite:** None (Admin can delete plans in any status in this implementation).
* **Steps:**
    1.  Log in with an Admin user.
    2.  Navigate to the "View Science Plans" page.
    3.  Find the plan to delete.
    4.  Click the "Delete" button.
    5.  Confirm the deletion in the pop-up confirmation box.
    6.  On success, the plan will be removed from the application's database, and you will be redirected back to the "View Science Plans" page with a success message (Note: This deletion does not affect OCS).

## Design Pattern Used

This application utilizes the **Model-View-Controller (MVC)** pattern, a standard architectural pattern commonly used in Spring Boot Web Applications.

* **Model:** Represents the data and business logic. It includes:
    * **Entity Classes** (e.g., `SciencePlan.java`, `User.java`, `DataProcessingRequirements.java`): Define the structure of data stored in the database. Mapped using JPA annotations.
    * **Repository Interfaces** (e.g., `SciencePlanRepository.java`, `UserRepository.java`): Handle database operations (Create, Read, Update, Delete - CRUD) using Spring Data JPA.
    * **Service Classes** (e.g., `UserDetailsServiceImpl.java`): Can contain more complex business logic (though in this prototype, much logic resides in the Controller).
* **View:** Represents the user interface and presentation layer. It includes:
    * **HTML Templates** (e.g., `view_science_plans.html`, `create_science_plan.html`, `login.html`): Use Thymeleaf for dynamically rendering data received from the Controller.
* **Controller:** Acts as an intermediary, handling user requests, interacting with the Model (Repositories/Services) to process data, and selecting the appropriate View to return the response. It includes:
    * **Controller Classes** (e.g., `SciencePlanController.java`, `LoginController.java`, `ViewPlansController.java`): Annotated with `@Controller`, containing methods mapped to specific URLs (`@GetMapping`, `@PostMapping`) to handle incoming HTTP requests.

**Why MVC was chosen:**

* **Separation of Concerns:** MVC clearly separates data logic (Model), presentation logic (View), and request handling logic (Controller). This makes the codebase organized, easier to understand, maintain, and modify without significantly impacting other parts.
* **Testability:** The separation allows for easier unit testing of Controllers and Model components (Services/Repositories) independently of the UI.
* **Reusability:** Model components (like Repositories) can be reused across different Controllers or parts of the application.
* **Framework Support:** Spring Boot provides excellent built-in support for the MVC pattern, offering annotations and tools that simplify and accelerate web application development using this architecture.

