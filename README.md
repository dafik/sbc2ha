# Sbc2ha 
## Single Board Computer 2 Home Assistant


![Stan kompilacji](https://github.com/dafik/sbc2ha/actions/workflows/build.yml/badge.svg)
![Licencja](https://img.shields.io/badge/license-MIT-blue)

## Spis treści
- [Wprowadzenie](#wstęp)
- [Funkcje](#funkcje)
- [Tryby działania](#tryby-działania)
- [Instalacja](#instalacja)
- [Wkład](#wkład-w-projekt)
- [FAQ](#faq)
- [Zrzuty ekranu](#zrzuty-ekranu)
- [Wideo](#wideo)
- [Demo panelu](https://sbc2ha.mieszkadlo.eu/app/)

## Wstęp
Sbc2ha to wszechstronne i kompleksowe rozwiązanie zaprojektowane z myślą o szerokiej gamie użytkowników i aplikacji,
służące do kontrolowania i zarządzania sensorami i aktuatorami podłączonymi fizycznie do komputerów (SBC).

System może działać na całej gamie SBC: 
Raspberry Pi, Odroid, BeagleBone, Orange Pi, [wiecej](https://www.diozero.com/#supported-boards)

System przeznaczony jest dla następujących grup użytkowników:

- **Właściciele [BoneIo Black](https://boneio.eu/#boneio-black)**: System służy jako projekt bazowy dla tych użytkowników, zapewniając im potężne możliwości
  platformę do kontrolowania i zarządzania operacjami wejścia/wyjścia urządzeń.

- **Użytkownicy Home Assistant zainstalowani na Raspberry Pi**: Dla tych użytkowników system działa jako dodatek do Home
  Assistant, umożliwiający pełne wykorzystanie możliwości sprzętowych Pi, w tym GPIO, PWM, UART i SPI.

- **Użytkownicy innych [komputerów jednopłytkowych](https://www.diozero.com/#supported-boards)**: Użytkownicy ci mogą wykorzystać system do wykorzystania wspomnianego sprzętu
  powyżej, pod warunkiem,że ich SBC mogą uruchamiać Javę. Dla tych, których SBC nie mogą uruchamiać Java, system zapewnia wsparcie poprzez
  dostępne protokoły, takie jak firmata, szeregowy i voodoSpark, a sbc2ha pełni rolę ich kontrolera. 



## Funkcje
Sbc2ha zostało początkowo opracowany jako port oryginalnego oprogramowania [BoneIo Black](https://github.com/boneIO-eu/app_black),
które zostało napisane w języku Python i było dostępne wyłącznie dla platformy BeagleBone.  
Obecny system jest jednak napisany w silnie typowanym języku programowania JAVA — dzięki temu jest przenaszalny pomiędzy różnymi platformami (SBC).  
Oprogramowanie opiera się na wspaniałej biblioteki [diozero](https://www.diozero.com/), zawiera również małe wstawki w języku C (JNI) do komunikacji z systemem Linux.  
Z biegiem czasu oprogramowanie znacznie ewoluowało, rozwiązując i korygując mniej udane wybory dokonane przez twórców oryginalnego oprogramowania.  
Obecnie oferuje ono wsparcie dla większej liczby czujników i siłowników, dzięki
wykorzystaniu biblioteki diozero i standaryzacji obsługi urządzeń.

System może pochwalić się szeroką gamą funkcjonalności, w tym:

- **Obsługa różnych czujników i urządzeń wykonawczych**: System obsługuje szeroką gamę czujników i urządzeń wykonawczych,
  w tym czujniki analogowe (napięcia, rezystancji, termometry NTC), urządzenia 1-Wire (termometr DS18B20), urządzenia I2C/SPI
  (ekspander wejść/wyjść Mcp23017, 16-kanałowe wyjście PWM PCA9685), urządzenia Modbus i termometry cyfrowe (LM75).

- **Interfejs sieciowy**: System jest wyposażony w łatwy w obsłudze interfejs sieciowy, który umożliwia interakcję z systemem i sterowanie urządzeniami.
  Interfejs sieciowy zapewnia graficzny interfejs użytkownika do zarządzania konfiguracją systemu,
  przeglądania dzienników urządzeń na żywo, kontrolowania i monitorowania urządzeń oraz zarządzania systemem.

- **Łatwa integracja dodatkowego sprzętu**: Jeśli obsługa sprzętowa tych komponentów została już napisana
  w diozero, można je łątwo zintegrować z systemem. Ta funkcja znacznie zwiększa wszechstronność systemu
  i możliwości adaptacji, czyniąc z niego potężne narzędzie do szerokiego zakresu zastosowań.

- **Tryb autonomiczny**: W trybie autonomicznym system umożliwia bezpośrednie sterowanie podłączonymi urządzeniami w oparciu o różne zdarzenia i odczyty.
  różnych zdarzeń i odczytów. Jest to najbardziej podstawowy tryb pracy dla wieloplatformowego systemu kontroli wejść/wyjść urządzeń.
  System.

- **Integracja MQTT**: System może nasłuchiwać tematów MQTT i wykonywać działania w oparciu, o otrzymywane wiadomości. 
  Może również publikować wiadomości do tematów MQTT, gdy wystąpią zdarzenia lub zmienią się odczyty czujników

- **Integracja z Home Assistant**: W trybie Home Assistant system płynnie integruje się z Home Assistant,
  popularna platforma automatyki domowej typu open source. Pozwala to na sterowanie i monitorowanie tych urządzeń bezpośrednio z pulpitu
  Home Assistant, wraz z innymi inteligentnymi urządzeniami w domu. 


## Tryby działania
Sbc2ha może działa w trzech różnych trybach:

1. [Tryb autonomiczny](#tryb-samodzielny)
2. [Tryb MQTT](#tryb-mqtt) 
3. [Tryb HomeAssistant](#tryb-homeassistant)

### Tryb samodzielny

Tryb autonomiczny jest najbardziej podstawowym trybem działania sbc2ha.   
Pozwala on na bezpośrednie sterowanie podłączonymi urządzeniami w oparciu, o różne zdarzenia i odczyty.   
W tym trybie nie są wymagane żadnie inne komponenty do jego działania.  
Obecnie obsługiwane są sensory binary i button (switch) oraz actuator relay. 


#### Kontrola oparta na zdarzeniach

System może reagować na różne zdarzenia takie jak:

- **Wykrywanie kliknięć**: System może wykrywać kliknięcia (pojedyncze, podwójne, długie przytrzymanie, zwolnienie) i wykonywać działania na ich podstawie.
  Może to być przydatne dla urządzeń takich jak przyciski lub przełączniki.

- **Naciśnięcie i zwolnienie dla czujników binarnych**: Czujniki binarne, takie jak czujniki ruchu lub czujniki kontaktowe, 
  mogą wyzwalać akcje, gdy są aktywowane (naciśnięcie) lub dezaktywowane (zwolnienie).


#### Wyjścia sterujące

System może sterować wyjściami na trzy sposoby:

- **ON**: Włącza urządzenie.

- **OFF**: Wyłącza urządzenie. 

- **PRZEŁĄCZANIE**: Przełącza stan urządzenia z włączonego na wyłączony lub odwrotnie.



#### Odczyty czujnika

System może odczytywać dane z różnych typów czujników:

- **Czujniki analogowe**: Obejmują one czujniki napięcia, czujniki rezystancyjne (Ohm) i termometry NTC.

- **Urządzenia 1-Wire**: Urządzenia te komunikują się za pomocą jednego przewodu i obejmują urządzenia takie jak termometr DS18B20. The
  system obsługuje urządzenia 1-Wire podłączone bezpośrednio do pinów GPIO lub poprzez interfejs Ds2482 (1-wire@i2c).

- **Urządzenia I2C/SPI**: Te urządzenia komunikują się za pośrednictwem protokoły I2C lub SPI. Obsługiwane urządzenia obejmują Mcp23017 (
  ekspander wejść/wyjść) i PCA9685 (16-kanałowe wyjście PWM).

- **Urządzenia Modbus**: Urządzenia te komunikują się za pośrednictwem protokół Modbus. Należy pamiętać, że konieczne będzie zdefiniowanie
  adresy podłączonych urządzeń Modbus.

- **Termometry cyfrowe**: Obejmują one urządzenia takie jak termometr LM75, który komunikuje się za pośrednictwem protokołu I2C.
  
Odczyty czujników sa dostępne w interfejsie GUI, a także przez websocket.


### Tryb MQTT

MQTT (Message Queuing Telemetry Transport) to lekki protokół przesyłania wiadomości używany do komunikacji między
urządzeniami w konfiguracji Internetu rzeczy (IoT).   
Tryb MQTT umożliwia sbc2ha komunikowanie się z innymi systemami za pomocą protokołu MQTT.
Ten tryb rozszerza tryb samodzielny, wszystkie funkcjonalności są dostępne.

W tym trybie system publikuje wiadomości do tematów MQTT, gdy wystąpią zdarzenia lub zmienią się odczyty czujników.  
Inne systemy mogą subskrybować te tematy i wykonywać działania na podstawie otrzymanych wiadomości.

System może również subskrybować tematy MQTT i wykonywać działania na podstawie otrzymywanych komunikatów.
Pozwala to na sterowanie urządzeniami podłączonymi do systemu z innych systemów za pomocą MQTT.

Aby korzystać z tego trybu, w sieci musi być skonfigurowany broker MQTT.
Należy również skonfigurować ustawienia MQTT w systemie, w tym adres brokera oraz tematy do publikowania i subskrybowania.


#### Sterowanie oparte na zdarzeniach

**Zdarzenia MQTT**: System może nasłuchiwać tematów MQTT i wykonywać akcje na podstawie otrzymywanych wiadomości. 📡

#### Odczyty czujnika
Wszystkie odczyty czujników są udostępniane w za pośrednictwem protokołu i brokera MQTT, umożliwiając innym systemom dostęp do danych.

#### Sterowanie urządzeniami wykonawczymi
System może sterować urządzeniami wykonawczymi, takimi jak przekaźniki i urządzenia PWM.   
Nasłuchuje przychodzących komunikatów MQTT i zmienia stan urządzeń wykonawczych na podstawie tych komunikatów.


### Tryb HomeAssistant

W trybie Home Assistant sbc2ha bezproblemowo integruje się z Home Assistant, popularną platformą automatyki domowej typu open source.  
Tryb ten wykorzystuje protokół MQTT do informowania Home Assistant o urządzeniach kontrolowanych przez system. do Home Assistant.

Urządzenia są ogłaszane przez MQTT za pośrednictwem funkcji Autodiscovery.
Autodiscovery to funkcja aplikacji Home Assistant,
która automatycznie wykrywa i dodaje urządzenia do pulpitu Home Assistant.

Urządzenia, które można wykryć to m.in. czujniki binarne, rolety, wyzwalacze urządzeń, światła i czujniki.

Po ogłoszeniu urządzeń są one automatycznie rozpoznawane i dodawane do Home Assistant.
Umożliwia to kontrolować i monitorować te urządzenia bezpośrednio z pulpitu Home Assistant, 
wraz z innymi inteligentnymi urządzeniami w domu.

Aby korzystać z tego trybu, w sieci musi być zainstalowany i skonfigurowany Home Assistant. 
Należy również włączyć MQTT zarówno w aplikacji Home Assistant, jak i w sbc2ha. 

Instrukcje dotyczące konfiguracji MQTT w Home Assistant można znaleźć w
[dokumentacja Home Assistant MQTT] (https://www.home-assistant.io/integrations/mqtt/).

## Instalacja

W tej sekcji znajdują się szczegółowe instrukcje dotyczące instalowania sbc2ha.   
Istnieją trzy metody główne metody instalacji: 
 - instalacja [natywnie na urządzeniu](#instalacja-natywna), 
 - przy użyciu [kontenera Docker](#kontener-docker) 
 - jako [dodatek do HomeAssistant](#dodatek-do-homeassistant).



### Instalacja natywna

W przypadku instalacji natywnej na urządzeniu wykonaj następujące kroki:

1. Zainstaluj środowisko wykonawcze [Java w wersji 17](https://adoptium.net/blog/2021/12/eclipse-temurin-linux-installers-available) lub większej.
2. Przejdź do repozytorium GitHub do sekcji [Packages](https://github.com/dafik?tab=packages&repo_name=sbc2ha)
3. Wejdź w pakiet com.dfi.sbc2ha.app.
4. Pobierz pakiet sbc2ha w interesujacej Cie wersji z zależnościami np. app-0.0.5-with-dep.jar i zachowaj go na urządzeniu w preferowanym miejscu.
5. Pobierz przykładowy [plik konfiguracji](https://github.com/dafik/sbc2ha/blob/master/docker/std/config.yaml) z repozytorium i zachowaj go w tym samym miejscu, co poprzedni plik.
6. uruchom aplikacje
    ```bash
    java -jar app-0.0.5-with-dep.jar config.yaml
    ```
7. Interfejs GUI bedzie nasłuchiwał na porcie 8080 urządzenia, na którym została uruchomiona aplikacja.
8. W katalogu [tools](https://github.com/dafik/sbc2ha/tree/master/app/tools) repozytorium znajdują się dwa katalogi bin i service.  
   Zawierają przykładowy plik demona system.d **sbc2ha.service** uruchamianego w trakcie startu systemu,   
   skrypt **addService** do instalacji i włączenia tego serwisu,   
   a w katalogu bin różne skrypty do jego kontrolowania.  (start/stop/logs/run/debug)



### Kontener Docker

Docker to platforma, która pozwala spakować aplikację i jej zależności w znormalizowaną jednostkę do tworzenia oprogramowania.
Instalacja sbc2ha jako kontenera Docker to szybki i łatwy sposób na rozpoczęcie pracy.
Ta metoda jest idealna do testowania lub zapoznania się z systemem. 

#### Korzystanie z Docker Hub

Docker Hub to oparta na chmurze usługa rejestru, która umożliwia łączenie się z repozytoriami kodu, tworzenie obrazów i testowanie ich.
Przechowuje ręcznie wypychane obrazy i łączy się z Docker Cloud, dzięki czemu można wdrażać obrazy na swoich hostach.
Oto jak pobrać obraz Docker z Docker Hub:


1. Otwórz terminal na swoim urządzeniu.

2. Uruchom następujące polecenie, aby pobrać obraz Dockera:
    ```bash
    docker pull sbc2ha/sbc2ha
    ```
3. Po pobraniu obrazu możesz uruchomić kontener za pomocą następującego polecenia:
    ```bash
    doker run -p 8080:8080 sbc2ha/sbc2ha
    ```
    To polecenie uruchamia kontener Docker i mapuje port 8080 na Twoim urządzeniu na port 8080 w kontenerze Docker.



### Dodatek do HomeAssistant

Sbc2ha można zainstalować jako dodatek do Home Assistant, popularnej platformy automatyki domowej typu open-source.
Pozwala to kontrolować i monitorować urządzenia podłączone do systemu bezpośrednio z pulpitu Home Assistant,
wraz z innymi inteligentnymi urządzeniami w domu.

Sbc2ha wymaga zainstalowanego dodatku piGpio który można zainstalować z repozytorium [Poeschl](https://github.com/Poeschl/Hassio-Addons)

Poniżej przedstawiono szczegółowe kroki instalacji sbc2ha jako dodatku do aplikacji Home Assistant:

1. **Otwórz Home Assistant**: Zacznij od otwarcia Home Assistant na swoim urządzeniu. 
   Można to zrobić, przechodząc do adresu IP serwera Home Assistant w przeglądarce internetowej.

2. **Przejdź do sklepu z dodatkami**: Po otwarciu aplikacji Home Assistant przejdź do sklepu z dodatkami. 
   Można to zrobić, klikając "Supervisor" w menu po lewej stronie, a następnie wybierając "Add-on Store" z wyświetlonych opcji.

3. **Dodaj repozytorium**: W Sklepie z dodatkami kliknij trzy kropki w prawym górnym rogu ekranu. 
   Spowoduje to otwarcie się menu z kilkoma opcjami. 
   Wybierz "Repozytoria" z tego menu. Otworzy się okno dialogowe, w którym można dodać nowe repozytoria. 
   Wprowadź adres URL repozytorium sbc2ha https://github.com/dafik/Hassio-Addons i kliknij "Dodaj".
   Spowoduje to dodanie repozytorium do instancji Home Assistant.

4. **Zainstaluj dodatek**: Po dodaniu repozytorium dodatek sbc2ha powinien być już dostępny w sklepie z dodatkami. 
   Przewiń listę dostępnych dodatków, aż znajdziesz sbc2ha. 
   Kliknij go, aby otworzyć stronę szczegółów dodatku. Na tej stronie kliknij "Zainstaluj", aby zainstalować dodatek.
   Może to potrwać kilka minut.

5. **Konfiguracja dodatku**: Po zainstalowaniu dodatku należy go skonfigurować. 
   Kliknij "Konfiguracja" na stronie szczegółów dodatku i zmień w razie potrzeby port,
   na którym będzie dostępny interfejs sieciowy sbc2ha.  

6. **Uruchom dodatek**: Po skonfigurowaniu dodatku można go uruchomić, klikając "Start" na stronie szczegółów dodatku.
   sbc2ha uruchomi się i rozpocznie sterowanie urządzeniami. 

7. **Sprawdź logi dodatku**: Po uruchomieniu dodatku warto sprawdzić logi, aby upewnić się, że wszystko działa poprawnie.
   Możesz to zrobić, klikając "Dziennik" na stronie szczegółów dodatku. 
   Dzienniki pokażą proces uruchamiania dodatku oraz wszelkie błędy i ostrzeżenia, które wystąpiły.

8. **Użyj dodatku**: Dodatek sbc2ha jest teraz zainstalowany i uruchomiony. 
   Możesz kontrolować i monitorować swoje urządzenia z pulpitu Home Assistant.
   Klikajac "Otwórz interfejs użytkownika" przejdź do panelu dodatku.
   Dodatek będzie również udostępniał urządzenia dla Home Assistant MQTT Autodiscovery, 
   umożliwiając ich automatyczne dodawanie do pulpitu Home Assistant.
   
## Użytkowanie

Sbc2ha jest wyposażony w łatwy w obsłudze interfejs sieciowy, który umożliwia interakcję z systemem i sterowanie urządzeniami.
Poniżej znajduje się szczegółowy opis funkcji zapewnianych przez interfejs sieciowy:

**Aby uzyskać dostęp do interfejsu sieciowego***, otwórz przeglądarkę internetową i przejdź do adresu IP urządzenia,
na którym działa sbc2ha, po którym następuje numer portu (np. `http://192.168.1.100:8080`).

### 1. Zarządzanie konfiguracją

Interfejs sieciowy zapewnia graficzny interfejs użytkownika do zarządzania konfiguracją systemu. Obejmuje to:

- **Kreator nowej konfiguracji**: Kreator ten prowadzi użytkownika przez proces tworzenia nowej konfiguracji systemu.
  konfiguracji systemu. Pyta o niezbędne informacje i tworzy plik konfiguracyjny na podstawie wprowadzonych danych. 

- **Edycja bieżącej konfiguracji**: Można przeglądać i edytować bieżącą konfigurację systemu. 
  Pozwala to na dodawanie, usuwanie lub modyfikowanie urządzeń bez konieczności ręcznej edycji pliku konfiguracyjnego. 

- **Konwersja konfiguracji**: W przypadku migracji z oprogramowania BoneIo,
  system może przekonwertować istniejącą konfigurację BoneIo do formatu zgodnego z sbc2ha 

### 2. Podgląd dziennika urządzenia na żywo

Interfejs sieciowy zapewnia podgląd dzienników urządzenia na żywo. 
Umożliwia to monitorowanie działania systemu w czasie rzeczywistym i pomaga w rozwiązywaniu wszelkich problemów.

### 3. Sterowanie i monitorowanie urządzeń

Interfejs sieciowy umożliwia sterowanie i monitorowanie urządzeń:

- **Widok stanu czujnika**: Można wyświetlić aktualny stan czujników. Obejmuje to bieżące odczyty z
  analogowych, stan czujników binarnych oraz stan urządzeń 1-Wire, I2C/SPI i Modbus.

- **Edycja statusu urządzenia wykonawczego**: Możesz kontrolować urządzenia wykonawcze, takie jak przekaźniki, bezpośrednio z interfejsu internetowego. 
  Obejmuje to włączanie i wyłączanie urządzenia wykonawczego lub przełączanie jego stanu. 

- Emulacja zdarzeń**: Można emulować zdarzenia dla przełączników i czujników binarnych. 
  Może to być przydatne do testowania konfiguracji lub do ręcznego wyzwalania działań.

### 4. Zarządzanie systemem

Interfejs WWW udostępnia kilka narzędzi do zarządzania systemem:

- **Przeładowanie konfiguracji**: Jeśli dokonałeś zmian w pliku konfiguracyjnym, możesz użyć tej opcji, aby ponownie załadować
  konfigurację bez konieczności ponownego uruchamiania całego systemu. 

- **Przeładuj aplikację**: Ta opcja umożliwia ponowne uruchomienie systemu. Może to być przydatne, jeśli dokonałeś zmian w pliku
  ustawień systemu lub jeśli system nie działa prawidłowo. 

- **Zatrzymanie aplikacji**: Ta opcja umożliwia zatrzymanie systemu. Może to być przydatne, jeśli chcesz zamknąć
  system do konserwacji lub rozwiązywania problemów. 

- **Kasowanie zapamiętanych stanów**: Jeśli używasz elementów wykonawczych, które pamiętają swój stan (takich jak przekaźniki), 
  możesz użyj tej opcji, aby wyczyścić zapamiętane stany.
  Może to być przydatne, jeśli chcesz zresetować siłowniki do znanego stanu.

- **Czyszczenie pamięci podręcznej konfiguracji**: System buforuje bieżącą konfigurację w celu szybszego działania.
  Jeśli doświadczasz problemów, możesz użyć tej opcji, aby wyczyścić pamięć podręczną konfiguracji.

- **Zapisywanie konfiguracji w pamięci podręcznej**: Jeśli dokonałeś zmian w konfiguracji i jesteś z nich zadowolony, możesz z tego skorzystać
  opcja zapisania bieżącej konfiguracji do pamięci podręcznej. 
  

- **Zapisywanie w oryginalnej konfiguracji**: Jeśli dokonałeś zmian w konfiguracji i chcesz, aby były one trwałe, możesz
  można użyć tej opcji, aby zapisać bieżącą konfigurację z powrotem do oryginalnego pliku konfiguracyjnego. Spowoduje to zastąpienie
  istniejącego pliku konfiguracyjny z bieżącą konfiguracją.

### [DEMO ](https://sbc2ha.mieszkadlo.eu/app/) tylko panel

 
## Wkład w projekt
Wkład w projekt jest mile widziany. Prześlij pull request lub utwórz problem, aby omówić proponowane zmiany.

## FAQ

**P1: Jak dodać urządzenie do sbc2ha?**

Aby dodać urządzenie należy utworzyć plik konfiguracyjny urządzenia. Plik ten określa typ urządzenia, pin GPIO lub
inny interfejs, z którego korzysta, oraz stan początkowy urządzenia. Po utworzeniu pliku konfiguracyjnego możesz go dodać
do systemu poprzez interfejs WWW. Przejdź do sekcji konfiguracji, kliknij „Dodaj urządzenie” i prześlij plik
plik konfiguracyjny.

**Pyt. 2: Jak sterować urządzeniem za pomocą sbc2ha?**

Istnieją dwa główne sposoby sterowania urządzeniem:

1. **Interfejs sieciowy**: Interfejs sieciowy zapewnia graficzny interfejs użytkownika umożliwiający sterowanie urządzeniami. Możesz obejrzeć
   status swoich urządzeń, steruj urządzeniaami wykonawczymi, takimi jak przekaźniki, i emuluj zdarzenia dla przełączników i czujników binarnych. 

2. **Protokół MQTT**: Jeśli używasz systemu w trybie MQTT lub w trybie Home Assistant, możesz sterować swoimi urządzeniami za pomocą
   Wiadomości MQTT. System nasłuchuje przychodzących komunikatów MQTT i na ich podstawie zmienia stan urządzeń
   wiadomości. Możesz opublikować wiadomość MQTT w temacie powiązanym z urządzeniem, którym chcesz sterować. 

**Pyt. 3: Jak uzyskać pomoc, jeśli napotkam problemy z sbc2ha?**

Jeśli napotkasz jakiekolwiek problemy lub potrzebujesz pomocy z sbc2ha, możesz:

1. **Odwiedź repozytorium GitHub**: Sprawdź [repozytorium GitHub](https://github.com/dafik/sbc2ha)
   dla projektu. Możesz przeglądać istniejące problemy, aby sprawdzić, czy ktoś inny nie napotkał tego samego problemu.
   Jeśli nie, możesz otworzyć nowy temat i szczegółowo opisać swój problem.

2. **Dołącz do społeczności Home Assistant**: [Społeczność Home Assistant](https://community.home-assistant.io/) to
   świetne miejsce, aby uzyskać pomoc. Możesz zadawać pytania, dzielić się swoimi doświadczeniami i uczyć się od innych użytkowników Home Assistant. Jeśli
   używasz systemu w trybie Asystenta Domowego, społeczność może okazać się szczególnie pomocna. 

Pamiętaj, im więcej szczegółów na temat swojego problemu podasz, tym łatwiej będzie innym Ci pomóc.

## Licencja

Ten projekt jest projektem open source i jest dostępny na licencji [BSD-3-Clause license](https://github.com/dafik/sbc2ha/blob/master/LICENSE)

## Zrzuty ekranu

Ta sekcja zawiera wizualną reprezentację projektu, prezentującą interfejs użytkownika, integrację sprzętu i
inne godne uwagi cechy.

### Jako dodatek do HomeAssistant (RaspberyPI 4)

1. Edytor konfiguracji
![sbc2ha-ha-rpi-config-editor](https://github.com/dafik/sbc2ha/assets/3379462/f6297167-dec0-4db2-8a5a-4917b0a7dc1f)
2. Podgląd na żywo logów  
![sbc2ha-ha-rpi-logs](https://github.com/dafik/sbc2ha/assets/3379462/a910f731-a4e7-48f2-83d2-f9e1464d08c1)

### Natwna instalacja (BeagleBone@BoneIo)
1. Dashbord
![sbc2ha-native-bbb-dashbord](https://github.com/dafik/sbc2ha/assets/3379462/d3db114b-d579-4066-be56-9a64a2fd6030)
2. Ustawienia
![sbc2ha-native-bbb-settings](https://github.com/dafik/sbc2ha/assets/3379462/cab2295e-04a4-40bc-9c7e-ae924b1f6fa5)
4. Stany
![sbc2ha-native-bbb-states](https://github.com/dafik/sbc2ha/assets/3379462/ea933614-baaf-47f4-a392-dbf7477e288a)
5. Edytor
![sbc2ha-native-bbb-editor-main](https://github.com/dafik/sbc2ha/assets/3379462/fff85507-c129-471d-9110-1921b3b4d447)
6. Kreator konfiguracji
![sbc2ha-native-bbb-config-creator](https://github.com/dafik/sbc2ha/assets/3379462/e54db910-20a6-4348-ba6d-900127916e25)
7. Edycja sensora
![sbc2ha-native-bbb-edit-actuator-sensor](https://github.com/dafik/sbc2ha/assets/3379462/cbc37c23-b9d2-43a8-b026-401fbd4fd84e)
8. Edycja aktuatora (wlementu wykonawczego)
![sbc2ha-native-bbb-edit-actuator](https://github.com/dafik/sbc2ha/assets/3379462/005dd1d7-f871-42ed-b586-1e4e8b07ee53)
9. Edycja komponentów platformy
![sbc2ha-native-bbb-edit-platform](https://github.com/dafik/sbc2ha/assets/3379462/c68850c6-7756-4b71-80ac-e6c509780a12)
10. Wynikowa konfiguracja YAML
![sbc2ha-native-bbb-config-yaml](https://github.com/dafik/sbc2ha/assets/3379462/530aa218-19bb-4012-9190-a7081123ea37)
10. Wynikowa konfiguracja JSON
![sbc2ha-native-bbb-config-json](https://github.com/dafik/sbc2ha/assets/3379462/88f12f85-b158-4cf9-b72b-ef705393e6ac)

### Wideo

[sbc2ha-demo.webm](https://github.com/dafik/sbc2ha/assets/3379462/c564bb1f-e857-4909-8a8d-697567c64443)



## Informacje kontaktowe 
* Jeśli masz jakieś pytania lub uwagi, skontaktuj się z programistami pod adresem zbyszek.wieczorek AT gmail.com
