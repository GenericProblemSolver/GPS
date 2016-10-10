## Generic Problem Solver

The Generic Problem Solver is a generic Java-Framework to solve certain computer
science problems.

Right now it can solve a limited number of problems from the following areas:

* Constraint Satisfaction Problems
* Optimization Problems
* Generic Game Playing

### Example Usage

PythagoreanTriple.java
```java
class PythagoreanTriple {
    @Variable int a;

    @Variable int b;

    @Variable int c;

    @Constraint
    boolean check() {
        if (a <= 0 || b <= 0 || c <= 0) {
            return false;
        }
        return a*a + b*b == c*c;
    }
}
```

    ./GPS.sh PythagoreanTriple.java satisfy

### Dependencies

Most of the dependencies are fetched by the gradle build system. However to
solve Constraint satisfaction problems via SMT solving, installation of Z3 as a
library is required.

### Building

GPS uses gradle as its build system.

Running

    gradle fatjar

creates a Jarfile in `build/libs/` that can be used.

### License

Most of the Code of the Generic Problem Solver is licensed under GPLv3 or later.
See [COPYING]

### Documentation

* GPS Handbook (German)
* GPS Projektbericht (German)
