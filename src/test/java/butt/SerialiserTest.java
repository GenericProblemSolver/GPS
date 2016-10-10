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
package butt;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import org.junit.Test;
import org.objenesis.strategy.StdInstantiatorStrategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import gps.common.Benchmark;
import gps.common.BenchmarkField;
import gps.common.IBenchmark;
import gps.util.KryoHelper;

/**
 * 
 * @author haker@uni-bremen.de
 *
 */
public class SerialiserTest {
    @Test
    public void serialize() {
        IBenchmark ibm = new Benchmark(BenchmarkField.values());
        String s = KryoHelper.objectToString(ibm);
        //assertEquals(new String(Base64.getEncoder().encode(Base64.getDecoder().decode(s))), s);

        IBenchmark nbm = KryoHelper.stringToObject(s, Benchmark.class);
        assertTrue(Arrays.equals(nbm.getUsedFields(), ibm.getUsedFields()));
    }

    @Test
    public void testKryoSerialization() throws Exception {
        IBenchmark skv = new Benchmark(BenchmarkField.values());
        // Serialize to a byte array in ram.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Output ko = new Output(bos);
        Kryo kry = new Kryo();
        ((Kryo.DefaultInstantiatorStrategy) kry.getInstantiatorStrategy())
                .setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());

        kry.writeObject(ko, skv);
        ko.flush();
        // Deserialize.
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        Input ki = new Input(bis);
        IBenchmark des = (IBenchmark) kry.readObject(ki, Benchmark.class);
        assertTrue(Arrays.equals(skv.getUsedFields(), des.getUsedFields()));
    }

}
