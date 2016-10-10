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
package gps.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;

import org.objenesis.strategy.StdInstantiatorStrategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Serialize Objects to Strings and vice versa. Also can provide full and deep
 * clones of objects.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class KryoHelper {

    /**
     * The kryo instance. Since kryo is not thread safe we create an instance
     * for every thread.
     * 
     * {@link ThreadLocal} {@link Kryo} instances used for generating the deep
     * copies with {@link #copy()}. For each {@link Kryo} instance a fallback
     * instantiator strategy is set for the case that there is no empty
     * zero-argument constructors in the classes, whose objects have to be
     * copied.
     * <p>
     * This fallback strategy may cause some issues with certain JVMs.
     */
    private static final ThreadLocal<Kryo> kryo = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kr = new Kryo();
            ((Kryo.DefaultInstantiatorStrategy) kr.getInstantiatorStrategy())
                    .setFallbackInstantiatorStrategy(
                            new StdInstantiatorStrategy());
            return kr;
        }
    };

    /**
     * Get a deep copy of an arbitrary object.
     * 
     * @param obj
     *            The object to copy.
     * @return Another reference with the very same data as of in obj.
     */
    public static <T> T deepCopy(T obj) {
        return kryo.get().copy(obj);
    }

    /**
     * Serialize an object to a byte array.
     * 
     * @param pObj
     *            The object
     * @return The byte array that represents this object.
     * 
     * @throws RuntimeException
     *             if a problem occurred.
     */
    public static byte[] objectToBytes(Object pObj) {
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        Output byteArrayOutputStream = new Output(ba);
        kryo.get().writeObject(byteArrayOutputStream, pObj);
        byteArrayOutputStream.flush();
        return ba.toByteArray();
    }

    /**
     * Convert an input data to an object.
     * 
     * @param in
     *            The input data
     * @param clazz
     *            The class of the object
     * @return The newly constructed object.
     */
    private static <T> T toObject(final Input in, final Class<T> clazz) {
        try {
            return (T) kryo.get().readObject(in, clazz);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "Cannot convert serializer data to object", e);
        }
    }

    /**
     * Serialize a byte array to an Java Object.
     * 
     * @param pBytes
     *            The byte array
     * @return The Object
     * 
     * @throws RuntimeException
     *             if a problem occurred.
     */
    public static <T> T bytesToObject(byte[] pBytes, Class<T> clazz) {
        return toObject(new Input(new ByteArrayInputStream(pBytes)), clazz);
    }

    /**
     * Serialize a byte stream to an Java Object.
     * 
     * @param pBytes
     *            The byte array
     * @return The Object
     * 
     * @throws RuntimeException
     *             if a problem occurred.
     */
    public static <T> T streamToObject(InputStream is, Class<T> clazz) {
        return toObject(new Input(is), clazz);
    }

    /**
     * Serialize an object to a String.
     * 
     * @param pObj
     *            The Object
     * @return The String
     * 
     * @throws RuntimeException
     *             if a problem occurred.
     */
    public static String objectToString(Object pObj) {
        return new String(Base64.getEncoder().encode(objectToBytes(pObj)));
    }

    /**
     * Serialize a String to an Java Object.
     * 
     * @param pStr
     *            The String
     * @return The Object
     * 
     * @throws RuntimeException
     *             if a problem occurred.
     */
    public static <T> T stringToObject(String pStr, Class<T> clazz) {
        return bytesToObject(Base64.getDecoder().decode(pStr.getBytes()),
                clazz);
    }

}
