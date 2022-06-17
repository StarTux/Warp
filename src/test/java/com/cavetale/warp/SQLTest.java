package com.cavetale.warp;

import com.winthier.sql.SQLDatabase;
import org.junit.Test;

public final class SQLTest {
    @Test
    public void main() {
        System.out.println(SQLDatabase.testTableCreation(SQLWarp.class));
    }
}
