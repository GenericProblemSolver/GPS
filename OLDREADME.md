GPS Prototype2

[![build status](https://gitlab.informatik.uni-bremen.de/ci/projects/93/status.png?ref=master)](https://gitlab.informatik.uni-bremen.de/ci/projects/93?ref=master)

## Build Instructions
Benötigt wird gradle >= 2.0. Alternativ wrappt das Skript `gradlew` den
Download und das Ausführen von einer aktuellen Version.

Außerdem muss gradle Java 8 verwenden. Wenn ihr standardmäßig Java 7 verwendet
(was sich mit `echo $JAVA_HOME` in der shell überprüfen lässt), dann müsst ihr
gradle explizit sagen, dass es Java 8 verwenden soll. Das tut ihr, indem ihr die
Option `-Dorg.gradle.java.home=/path/to/java-8/` hinzufügt, wobei ihr für
/path/to/java-8/ den Pfad zu eurer individuellen Installation von Java 8 
einsetzt. Z.B. verwende ich unter Ubuntu 
`gradle -Dorg.gradle.java.home=/usr/lib/jvm/java-8-oracle/`.  

(Wenn ihr vorhabt, mehrmals Dinge zu builden, gradle nur mit Java 8 verwenden
wollt und statt `gradle -Dorg.gradle.java.home=/usr/lib/jvm/java-8-oracle/` nur
gradle eingeben möchtet, könnt ihr euch auch ein alias 
`gradle="gradle -Dorg.gradle.java.home=/usr/lib/jvm/java-8-oracle/"` in eure 
~/.bashrc schreiben. Nach einem Neustart der Shell sollte `gradle` dann 
automatisch durch den spezifischeren Befehl ersetzt werden.)


* Bauen: `gradle build` bzw. `./gradlew build`
* Eclipse
  * Eclipse Projekt erzeugen: `gradle eclipse` bzw `./gradlew eclipse`
  * In Eclipse importieren: Rechtsklick in den Project Explorer, Import,
  Existing Project into Workspace
* IntelliJ Projekt erzeugen: `gradle intellij` bzw. `./gradlew intellij`
* Packagen: `gradle fatJar` bzw. `./gradlew fatJar`

## Leon installieren (unter Linux)

Einer der beiden Solver, die wir verwenden, ist Leon. Leider ist die Installation
unter Umständen etwas tricky.

Versuch zunächst, den Anweisungen auf der [Leon-Website](https://leon.epfl.ch/doc/installation.html)
zu folgen, also die folgenden Befehle auszuführen.
```
$ git clone https://github.com/epfl-lara/leon.git
$ cd leon
$ sbt clean compile
$ sbt script
```
Wenn das alles funktioniert, hast du Glück gehabt. (Der Fehlerfall wird weiter
unten beschrieben.) Wahrscheinlich musst du die erzeugte leon-Datei noch mit
```
$ sudo chmod +x leon
```
ausführbar machen. Als letztes musst du eine Verknüpfung zu leon in einem
der Ordner in ```$PATH``` hinzufügen. Also z.B.:
```
$ echo $PATH
/usr/local/texlive/2015/bin/x86_64-linux:/home/caspar/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/local/games:/snap/bin:/usr/lib/jvm/java-7-oracle/:/bin:/home/caspar/go/bin:/home/caspar/bin
$ cd /usr/local/bin
$ sudo ln -s /path/to/leon/file leon
```
Und schon kann es losgehen.

Wenn du Pech hast, dann hast du bei der Installation von Ubuntu deine
Festplatte verschlüsselt. In diesem Fall kann es passieren, dass
```sbt clean compile``` Fehler wirft, etwa
```
[info] Compiling 405 Scala sources and 16 Java sources to /home/caspar/leon/target/scala-2.11/classes...
[error] /home/caspar/leon/src/main/scala/leon/frontends/scalac/CodeExtraction.scala:338: File name too long
[error] This can happen on some encrypted or legacy file systems.  Please see SI-3623 for more details.
[error]           for (c <- (cd.ancestors.toSet ++ cd.root.knownDescendants + cd) if !c.methods.exists(_.isInvariant)) {
[error]                                                                                                  ^
[error] /home/caspar/leon/src/main/scala/leon/frontends/scalac/CodeExtraction.scala:341: File name too long
[error] This can happen on some encrypted or legacy file systems.  Please see SI-3623 for more details.
[error]             fd.addFlags(c.flags.collect { case annot : purescala.Definitions.Annotation => annot })
[error]                                         ^
[error] /home/caspar/leon/src/main/scala/leon/frontends/scalac/CodeExtraction.scala:640: File name too long
[error] This can happen on some encrypted or legacy file systems.  Please see SI-3623 for more details.
[error]                     for (cd <- relevant; vd <- cd.fields) {
[error]                                             ^
[error] /home/caspar/leon/src/main/scala/leon/solvers/isabelle/Functions.scala:104: File name too long
[error] This can happen on some encrypted or legacy file systems.  Please see SI-3623 for more details.
[error]         typ <- types.typ(fun.returnType, strict = true)
[error]             ^
[error] /home/caspar/leon/src/main/scala/leon/solvers/isabelle/Functions.scala:129: File name too long
[error] This can happen on some encrypted or legacy file systems.  Please see SI-3623 for more details.
[error]         fun.statement.map(expr => translator.term(expr, Nil, globalLookup(data)).map(fun.id.mangledName -> _))
[error]                                ^
[error] /home/caspar/leon/src/main/scala/leon/solvers/isabelle/Functions.scala:129: File name too long
[error] This can happen on some encrypted or legacy file systems.  Please see SI-3623 for more details.
[error]         fun.statement.map(expr => translator.term(expr, Nil, globalLookup(data)).map(fun.id.mangledName -> _))
[error]                                                                          ^
[error] /home/caspar/leon/src/main/scala/leon/solvers/isabelle/Functions.scala:129: File name too long
[error] This can happen on some encrypted or legacy file systems.  Please see SI-3623 for more details.
[error]         fun.statement.map(expr => translator.term(expr, Nil, globalLookup(data)).map(fun.id.mangledName -> _))
[error]                                                                                                         ^
[error] /home/caspar/leon/src/main/scala/leon/solvers/unrolling/QuantificationManager.scala:446: File name too long
[error] This can happen on some encrypted or legacy file systems.  Please see SI-3623 for more details.
[error]         (qarg, arg) <- (qargs zip args)
[error]                               ^
[error] /home/caspar/leon/src/main/scala/leon/solvers/unrolling/QuantificationManager.scala:446: File name too long
[error] This can happen on some encrypted or legacy file systems.  Please see SI-3623 for more details.
[error]         (qarg, arg) <- (qargs zip args)
[error]                     ^
[error] /home/caspar/leon/src/main/scala/leon/solvers/unrolling/QuantificationManager.scala:394: File name too long
[error] This can happen on some encrypted or legacy file systems.  Please see SI-3623 for more details.
[error]       m <- ms if !matchers(m) && maxDepth(m) <= depth
[error]                               ^
[error] /home/caspar/leon/src/main/scala/leon/solvers/unrolling/QuantificationManager.scala:394: File name too long
[error] This can happen on some encrypted or legacy file systems.  Please see SI-3623 for more details.
[error]       m <- ms if !matchers(m) && maxDepth(m) <= depth
[error]         ^
[error] 11 errors found
[error] (root/compile:compile) Compilation failed
```
Teilweise kann man die Dateilänge wohl nach oben begrenzen, indem man eine Datei
    ```~/.sbt/0.13/local.sbt``` erstellt und da
```scalacOptions ++= Seq("-Xmax-classfile-name","78")```
reinschreibt. Wenn ihr als root (i.e. "mit sudo") kompiliert, müsst ihr
stattdessen an der Datei ```/root/.sbt/0.13/local.sbt``` arbeiten.

Siehe:
* http://stackoverflow.com/questions/28565837/filename-too-long-sbt
* https://issues.scala-lang.org/browse/SI-3623

Wenn das immer noch nicht funktioniert, dann könnt ihr leon auch auf einer
anderen Festplatte installieren, auf der konventionellere Dateiformate
verwendet werden. Meistens ist es dann nicht nötig/möglich, leon executable zu
machen. Es reicht dann entsprechend auch kein einfacher symbolic link in
```/usr/local/bin``` (oder einem anderen Pfad in PATH), stattdessen braucht man
dort ein Skript mit dem Namen ```leon```, das leon ausführt. Z.B.:
```
#!/bin/bash
bash /media/caspar/6DE5-E9862/leon/leon "$@"
```
Das Problem bei diesem Ansatz ist natürlich, dass man Leon nur verwenden kann,
wenn man die entsprechende Festplatte angeschlossen hat.

## Codestyle
Die Datei `GPSFormat.xml` definiert die Formatierungsregeln für
eclipse-kompatible Formatierer. Diese sollte in den IDE-Einstellungen
importiert werden.

Das spotless-Plugin für gradle überprüft diese Formatierungsregeln beim
bauen.  Sollten sie nicht eingehalten sein, schlägt das Bauen fehl.
Zusätzlich bietet es die automatische Formatierung mit
`gradle spotlessApply` an.

## Windows-Fehlermeldung bei `gradlew build`
Sollte bei der Ausführung des genannten Kommandos eine Fehlermeldung kommen, welche auf Zeile 17 verweist:

1. Rechtsklick auf Arbeitsplatz/Dieser PC/o.ä.
2. Eigenschaften
3. Erweiterte Systemeinstellungen
4. Tab Erweitert
5. Umgebungsvariablen...
6. neue Benutzervariable: Name: JAVA_HOME
7. Wert: Pfad zum JDK, Beispiel: C:\Program Files\Java\jdk1.8.0_74
8. Bestätigen und Shell neu starten