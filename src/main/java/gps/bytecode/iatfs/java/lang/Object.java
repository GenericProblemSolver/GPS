package gps.bytecode.iatfs.java.lang;

/**
 * Reimplementation of java.lang.Object
 * 
 * Since several of the Methods in this class are native and cannot be
 * reimplemented by us, they only have an empty method stub.
 * 
 * Several of the Object methods are final and thus cannot be overwritten here.
 * Our implementations of these methods have an added "f" at the start of their
 * names for this reason.
 * 
 * @author akueck@tzi.de, ihritil@tzi.de
 *
 */
public class Object {

    public Object() {

    }

    public Object(java.lang.Object o) {

    }

    /**
     * Since a java.lang.Object object has no fields, cloning only involves
     * creating a new Object
     */
    protected Object clone() {
        return new Object();
    }

    public boolean equals(Object obj) {
        return (this == obj);
    }

    /**
     * Needed for garbage collecting. Since we have not yet invented a symbolic
     * garbage collector, this does nothing.
     */
    protected void finalize() {

    }

    /**
     * handled in FunctionalMapping
     * 
     */
    public final Class<?> fgetClass() {
        return null; // just to compile
    }

    /**
     * Handled in FunctionalMapping
     */
    public int hashCode() {
        return 0;
    }

    /**
     * Multiple Threads are not (yet) supported
     */
    public final void fnotify() {

    }

    /**
     * Multiple Threads are not (yet) supported
     */
    public final void fnotifyAll() {
    }

    private static void registerNatives() {

    }

    /**
     * Uses getClass instead of fgetClass, because the functionality is handled
     * in FunctionalMapping
     */
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }

    /**
     * Multiple Threads are not (yet) supported
     */
    public final void fwait() {
        fwait(0);
    }

    /**
     * Multiple Threads are not (yet) supported
     */
    public final void fwait(long timeout) {
        // only InterruptedExcption
    }

    /**
     * Multiple Threads are not (yet) supported
     */
    public final void fwait(long timeout, int nanos) {
        if (timeout < 0) {
            // only InterruptedExcption
        }
        if (nanos < 0) {
            // only InterruptedExcption
        }
        if (nanos >= 500000 || (nanos != 0 && timeout == 0)) {
            timeout++;
        }
        fwait(timeout);
    }
}
