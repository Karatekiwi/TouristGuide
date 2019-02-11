package at.ac.tuwien.touristguide.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Manu Weilharter
 */
public class GuiderTestSuite {

    @SuppressWarnings("rawtypes")
    public static Test suite() {
        //Class guiTests = TestGUI.class;
        Class dbTests = TestDatabaseHandler.class;
        TestSuite suite = new TestSuite(dbTests);

        return suite;
    }

}
