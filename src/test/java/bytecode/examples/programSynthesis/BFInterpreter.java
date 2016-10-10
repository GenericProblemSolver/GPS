/*
 * Copyright 2016  Generic Problem Solver Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bytecode.examples.programSynthesis;

import java.math.BigInteger;
import java.util.LinkedList;

/**
 * Interpreter for BF programs.
 * 
 * @author caspar
 *
 */
public class BFInterpreter {

    /**
     * Interprets programs in the esoteric programming language https://en.wikipedia.org/wiki/Brainfuck
     * @param input
     * @param time
     * @param code
     * @return
     */
    public static ComputationResult execute(final String input,
            final BigInteger time, String code) {
        //Initialize field on which BF operates. The number is arbitrary, but we don't want a static variable, because getstatic does not seem to work, yet.
        int fieldCnt = 10;

        final char[] arr = code.toCharArray();
        int pointer = 0;
        StringBuilder out = new StringBuilder();
        char[] fields = new char[fieldCnt];
        LinkedList<Integer> openedLoops = new LinkedList<Integer>();
        String leftInp = input;

        try {
            BigInteger j = BigInteger.ZERO;
            for (int i = 0; i < arr.length; i++) {
                switch (arr[i]) {
                case '+':
                    fields[pointer] += 1;
                    if (fields[pointer] > 255) {
                        //the maximum size of values is 255, because each field saves a char. To be backend-friendly, we explicitly check this.
                        fields[pointer] = 0;
                    }
                    break;
                case '-':
                    fields[pointer] -= 1;
                    if (fields[pointer] < 0) {
                        //the maximum size of values is 255, because each field saves a char. To be backend-friendly, we explicitly check this.
                        fields[pointer] = (char) 255;
                    }
                    break;
                case '<':
                    pointer--;
                    if (pointer < 0) {
                        pointer = fields.length - 1;
                    }
                    break;
                case '>':
                    pointer++;
                    if (pointer == fields.length) {
                        pointer = 0;
                    }
                    break;
                case '[':
                    if (fields[pointer] == 0) {
                        i++;
                        for (int cnt = 1;; i++) {
                            if (arr[i] == ']') {
                                cnt--;
                            } else if (arr[i] == '[') {
                                cnt++;
                            }
                            if (cnt == 0) {
                                break;
                            }
                        }
                    } else {
                        openedLoops.offerFirst(i);
                    }
                    break;
                case ']':
                    i = openedLoops.pollFirst() - 1;
                    break;
                case '.':
                    int outp = (int) fields[pointer];
                    if (outp < 0) {
                        outp += 256;
                    }
                    out.append((char) outp);
                    break;
                case ',':
                    if (!leftInp.equals("")) {
                        fields[pointer] = leftInp.charAt(0);
                        leftInp = leftInp.substring(1);
                    } else {
                        fields[pointer] = 0;//(byte) ' ';
                    }
                    if (fields[pointer] > 255) {
                        fields[pointer] = 255;
                    } else if (fields[pointer] < 0) {
                        fields[pointer] = 0;
                    }
                }
                j = j.add(BigInteger.ONE);
                //System.out.println((int) fields[0]+"   "+(int) fields[1]);
                if (j.equals(time)) {
                    return new ComputationResult(out.toString(), j, false);
                }
            }
            return new ComputationResult(out.toString(), j, true);
        } catch (Exception e) {
            //throw e;
            //System.out.println(e);
            return new ComputationResult("fail!", BigInteger.valueOf(-42),
                    true);
        }
    }

}
