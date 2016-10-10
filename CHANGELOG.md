# Woche 11.03
## Additions
* !35 -  fügt Minesweeper als Problem hinzu
* !9  -  fügt Go als Problem hinzu
* !20 -  Implementiert mehrere Transformationsschritte, die die Ausgabe der
  symbolischen Ausführung kleiner machen

## Changes
* !39 -  ändert den Typ von bisher nicht dekompilierten SEFunctions zu Undef
* !44 -  erweitert die ladbaren Klassen für die Disassemblierung auf Nested
  Classes
* !47 -  verbessert die Typherleitung der symbolischen Ausführung in bestimmten
  Fällen

## Fixes
* !36 -  korrigiert die Interpretation der IINC Instruktion
* !41 -  behebt einen Fehler im Preprocessing mit Methoden, die void zurückgeben
* !42 -  behebt einen Fehler, sodass Constraints mit 0 Parametern für Z3 korrekt
  ausgegeben werden
* !45 -  korrigiert die Implementierung der Go Spielregeln
* !46 -  korrigiert die Reihenfolge der ausgegebenen Z3 Funktionen

# Woche 04.03
## Additions
* !30 -  fügt eine Beschreibung zur Gradlenutzung unter Windows hinzu
* !31 -  fügt ein Würfelspiel als Problem hinzu
* !15 -  fügt eine Implementierung des Monte Carlo Tree Search Algorithmus hinzu
* !17 -  fügt Methoden hinzu, um die symbolische Bytecoderepräsentation für
  bestimmte Backends zu transformieren
* !26 -  fügt Interpretationen für Typecast Opcodes hinzu

## Changes
* !33 -  ändert den Returntype einer Heuristik zu Number

## Fixes
* !34 -  passt !15 and !33 an
* !29 -  behebt Fehler, die die Tests in eclipse nicht durchlaufen ließen
* !32 -  korrigiert die Reihenfolge von Operanden bei binären Operationen

# Woche 19.02
## Additions
* !1  -  fügt die grundlegende Struktur der Spielegruppe hinzu
* !8  -  fügt den erarbeiteten Prototypen der Bytecodegruppe hinzu
* !2  -  fügt ein einfaches Wegfindungsproblem hinzu
* !3  -  fügt Vector Racer als Problem hinzu
* !4  -  fügt Gempuzzle als Problem hinzu
* !5  -  fügt Connect4 als Problem hinzu
* !6  -  fügt Türme von Hanoi als Problem hinzu
* !13 -  fügt ein Preprocessingsystem hinzu, dass automatisch Wrapperklassen
  für Probleme generiert
* !16 -  fügt viele IF-Bytecodeinstruktionen zur symbolischen Ausführung
  hinzu

## Changes
* !7  -  updated unsere build.gradle auf das neue Plugin-System von Gradle
* !14 -  erlaubt Mac-Rechner wieder als CI-Runner
* !21 -  erlaubt Züge als Methoden mit @Move Annotation
* !23 -  optimiert das Parameter propagations System der SEFunctions
* !19 -  ersetzt lange SEFunction Namen durch kurze Hashes

## Fixes
* !10 -  korrigiert die Sortierung der Ausgabe der generierten Funktionen
* !12 -  behebt mehrere Probleme mit Schleifen im Bytecode
* !16 -  entfernt eine Referenz auf guava-Klassen im Vector Racer
* !22 -  korrigiert das Weitergeben von lokalen Variablen durch generierte
  Funkionen
* !24 -  fixt Tests die wegen fehlendem AnnotationProcessor in Eclipse
  fehlgeschlagen sind
* !28 -  Ignoriert einen Test, der von !28 und !21 kaputt gegangen ist
