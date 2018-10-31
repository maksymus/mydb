package org.mydb.util;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class IdGeneratorTest {

    @Test
    public void generate_seq() {
        long connectionId1 = IdGenerator.generate(IdGenerator.Type.CONNECTION);
        long connectionId2 = IdGenerator.generate(IdGenerator.Type.CONNECTION);
        long statementId1 = IdGenerator.generate(IdGenerator.Type.STATEMENT);
        long connectionId3 = IdGenerator.generate(IdGenerator.Type.CONNECTION);

        Assert.assertThat(Arrays.asList(connectionId1, connectionId2, statementId1, connectionId3),
                Matchers.contains(0, 1, 0, 2));
    }
}