# FlowRide Automated Testing Framework

Ovaj projekt sadrži napredni automatizirani testni okvir za FlowRide aplikaciju, razvijen prema zahtjevima laboratorijskih vježbi.

## Korišteni alati
- **Jezik**: Kotlin
- **Okvir za testiranje**: Jetpack Compose UI Test / Android Espresso
- **Arhitektura**: Page Object Model (POM)
- **Baza podataka**: Persistent Local Storage (SharedPreferences + JSON)
- **Build alat**: Gradle

## Implementirane napredne tehnike
- **Page Object Model (POM)**: Svaki ekran aplikacije ima svoju namjensku klasu za interakciju (`AuthPage`, `HomePage`, `ReservationPage`, `RentalsPage`, `ProfilePage`, `AdminPage`), što osigurava čitljivost i lako održavanje testova.
- **Persistence (Postojanost)**: Implementirana je lokalna baza podataka koja sprema registrirane korisnike i njihove rezervacije. Sesija korisnika ostaje aktivna čak i nakon potpunog zatvaranja i ponovnog pokretanja aplikacije.
- **Visual Grading Support**: Svi testni koraci su usporeni (`slowDown` funkcija s odgodom od 1.0s - 1.5s) kako bi se omogućilo lakše praćenje i ocjenjivanje procesa u realnom vremenu.
- **Test-Only Hooks**: Implementirani su posebni programski "zahvati" (`simulateScanAction`) koji omogućuju testiranje admin funkcionalnosti (skeniranje QR koda) bez potrebe za fizičkom kamerom.
- **Advanced Logic Testing**: Testovi provjeravaju kompleksnu logiku poput filtriranja lokacija na temelju kategorije vozila i automatskog povezivanja rezervacija s korisničkim računom.

## Testni Slučajevi (8 E2E Scenarija)
Testovi se izvode po strogo definiranim redoslijedom (`@FixMethodOrder`) kako bi se simulirao cijeli životni vijek korisničkog računa:

1. **`test01_RegisterNewUser`**: Registracija novog korisnika s perzistencijom podataka i odjava radi testiranja prijave.
2. **`test02_LoginFlow`**: Provjera prijave s perzistentnim računom kreiranim u prethodnom koraku.
3. **`test03_NavigateTabs`**: Provjera ispravnosti navigacije kroz sve glavne dijelove aplikacije.
4. **`test04_ReservationProcess`**: Napredni proces rezervacije koji uključuje odabir vozila, unos datuma u budućnosti, postavljanje trajanja (4h+), odabir filtrirane lokacije i PayPal plaćanje. Test potvrđuje da se rezervacija pojavljuje u korisničkom popisu "Moji najmi".
5. **`test05_AdminScannerAccess`**: Provjera da administrator ima pristup skeneru ikoni u gornjoj traci.
6. **`test06_AdminConfirmRental`**: Simulacija skeniranja QR koda od strane admina i potvrda najma, čime se status rezervacije mijenja u "completed".
7. **`test07_LocationFiltering`**: Validacija poslovne logike - provjera da se specifične lokacije (npr. Sljeme) prikazuju samo za odgovarajuće kategorije vozila (Classic), a skrivaju za druge (E-Bicikli).
8. **`test08_DeleteAccount`**: Završno čišćenje - brisanje korisničkog računa i svih njegovih podataka iz lokalne baze.

## Admin Podaci za Testiranje
- **Email**: `admin@flowride.com`
- **Lozinka**: `admin123` (bilo koja lozinka se prihvaća za perzistentne račune u ovoj verziji)

## Kako pokrenuti testove
1. Otvorite projekt u **Android Studiju**.
2. Idite na folder `app/src/androidTest/java/com/example/flowride/tests/`.
3. Desni klik na `FlowRideE2ETest.kt` i odaberite **Run 'FlowRideE2ETest'**.
4. Pratite izvođenje na emulatoru - proces je usporen radi lakše vidljivosti.
