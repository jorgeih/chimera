package de.uni_potsdam.hpi.bpt.bp2014.jcomparser.json;

import de.uni_potsdam.hpi.bpt.bp2014.AbstractDatabaseDependentTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.*;

/**
 *
 */
public class ScenarioDataTest {
    @After
    public void tearDown() throws IOException, SQLException {
        AbstractDatabaseDependentTest.resetDatabase();
    }

    @Test
    public void testSave() throws Exception {
        Assert.fail();
    }

    @Test
    public void testAssociateDataNodesWithDataClasses() {
        Assert.fail();
    }

    @Test
    public void testAssociateStatesWithDataClasses() {
        Assert.fail();
    }

    @Test
    public void testGenerateFragmentList() {
        Assert.fail();
    }
}