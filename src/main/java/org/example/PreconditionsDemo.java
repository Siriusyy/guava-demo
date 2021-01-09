package org.example;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import org.junit.Test;

import java.util.ArrayList;


public class PreconditionsDemo {
    @Test(expected = IllegalArgumentException.class)
    public void testCheckArgument() {
        Preconditions.checkArgument(false);
    }

    @Test(expected = NullPointerException.class)
    public void testCheckNotNull() {
        Preconditions.checkNotNull(null);
    }

    @Test(expected = IllegalStateException.class)
    public void testCheckState() {
        Preconditions.checkState(false);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testCheckElementIndex() {
        ArrayList<Integer> list = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            list.add(i);
        }
        Preconditions.checkElementIndex(5, list.size());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test() {
        String str = "led,do you want power?";
        Preconditions.checkPositionIndexes(12, 36, str.length());
    }
}
