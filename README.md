ISHIMWE ALAIN 28422

AUCA_ID - School Management System

Console-based Java application that manages departments, students, teachers, administrators, courses, classrooms, enrollments, assessments, fee payments, library books and book loans.

Run (Java 21, Windows PowerShell)
From the project root:

    javac -d bin (Get-ChildItem src -Recurse -Filter *.java).FullName
    java -cp bin com.auca.main.Main

 Structure (`src/com/auca`)
- `interfaces` - `IdentifiabLe`, Registrable
- `abstracts` - `Person`, `AcademicMember`
- `student`, `teacher`, `administrator`, `department`, `course`, `classroom`, `enrollment`, `assessment`, `feepayment`, `librarybook`, `bookloan` - one concrete class each
- `exceptions` - 5 custom exceptions
- `main` - `Main` (menu and smart forms)

 Features
- 19-option menu: register, enroll, assess, pay, borrow/return books, view, search, update, reports
- Guided multi-step forms with BACK / CANCEL, review and edit before saving
- Pick existing records from numbered lists, with nested "register it now" and resume
- Validation, duplicate detection, safe recovery from errors

 OOP concepts shown
Encapsulation, multilevel and hierarchical inheritance, interfaces, abstract classes, polymorphism, constructor chaining, method overriding, custom exceptions.