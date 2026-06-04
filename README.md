# FlowRide Automated Testing Framework

Ovaj projekt sadrži automatizirani testni okvir za FlowRide aplikaciju, razvijen prema zahtjevima laboratorijskih vježbi.

## Korišteni alati
- **Jezik**: Kotlin
- **Alat za testiranje**: Android Espresso / Jetpack Compose UI Test
- **Build alat**: Gradle
- **VCS**: Git

## Implementirane napredne tehnike
- **Page Object Model (POM)**: Testovi su odvojeni od logike pronalaženja elemenata. Svaki ekran ima svoju klasu (`HomePage`, `AuthPage`, `ReservationPage`).
- **OOP (Object-Oriented Programming)**: Korištenje klasa, enkapsulacije i čiste strukture koda.
- **Wait Commands**: Korištenje `composeTestRule` za automatsko čekanje na renderiranje UI elemenata.
- **E2E Testing**: Pokriveni su kompletni scenariji od prijave do rezervacije.
- **.gitignore**: Projekt sadrži `.gitignore` datoteku za isključivanje nepotrebnih datoteka.

## Testni Slučajevi (5+)
1. `test01_LoginFlow`: Provjera funkcionalnosti prijave korisnika.
2. `test02_RegisterNewUser`: Provjera procesa registracije novog korisnika s dodatnim podacima (telefon, adresa).
3. `test03_NavigateTabs`: Provjera navigacije kroz donji izbornik (Home, Lokacije, Najmi).
4. `test04_ReservationProcess`: Kompletan proces odabira bicikla, unosa datuma i potvrde rezervacije.
5. `test05_AdminScannerAccess`: Provjera pristupa admin sučelju (skeneru) za zaposlenike.

## Kako pokrenuti testove
1. Otvorite projekt u **Android Studiju**.
2. Idite na folder `app/src/androidTest/java/com/example/flowride/tests/`.
3. Desni klik na `FlowRideE2ETest.kt` i odaberite **Run 'FlowRideE2ETest'**.
4. Testovi će se pokrenuti na spojenom uređaju ili emulatoru.
