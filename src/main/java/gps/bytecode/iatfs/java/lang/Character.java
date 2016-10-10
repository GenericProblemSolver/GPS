/*
 * Copyright 2002-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package gps.bytecode.iatfs.java.lang;

/**
 * 
 * @author akueck@uni-bremen.de, Lee Boynton, Guy Steele, Akira Tanaka for
 *         documentation see original java class
 */
public class Character {

    public static final int MAX_RADIX = 36;
    public static final int MIN_RADIX = 2;
    public static final byte DECIMAL_DIGIT_NUMBER = 9;
    static final int ERROR = 0xFFFFFFFF;
    public static final byte DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING = 14;
    public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING = 16;
    public static final byte DIRECTIONALITY_POP_DIRECTIONAL_FORMAT = 18;
    public static final byte DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE = 15;
    public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE = 17;
    public static final byte DIRECTIONALITY_UNDEFINED = -1;
    public static final byte UNASSIGNED = 0;
    public static final byte PRIVATE_USE = 18;
    public static final byte DIRECTIONALITY_LEFT_TO_RIGHT = 0;

    public static int digit(int codePoint, int radix) {
        return CharacterData.of(codePoint).digit(codePoint, radix);
    }

    public static int digit(char ch, int radix) {
        return digit((int) ch, radix);
    }
}
