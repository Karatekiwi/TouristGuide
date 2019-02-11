package at.ac.tuwien.touristguide.tests;


import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import at.ac.tuwien.touristguide.entities.Poi;
import at.ac.tuwien.touristguide.entities.Section;
import at.ac.tuwien.touristguide.tools.DatabaseHandler;
import at.ac.tuwien.touristguide.tools.PoiHolder;


/**
 * @author Manu Weilharter
 */
public class TestDatabaseHandler extends AndroidTestCase {

    private DatabaseHandler dbHandler;


    @Override
    protected void setUp() throws Exception {
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");
        dbHandler = DatabaseHandler.getInstance(context);
        dbHandler.createTables(dbHandler.getWritableDatabase());
    }

    @Override
    protected void tearDown() throws Exception {
        dbHandler.dropTables();
        dbHandler.close();
        super.tearDown();
    }


    @SmallTest
    public void testAddAndGetPois() {
        dbHandler.saveCategories("0,1,2,3");

        List<Poi> pois_en = new ArrayList<Poi>();
        List<Poi> pois_de = new ArrayList<Poi>();

        for (int i = 0; i < 10; i++) {
            Poi poi = new Poi("poi" + i, "wiki" + i, 40.50, 30.50, "en", 0);
            List<Section> sections = new ArrayList<Section>();
            sections.add(new Section(i, 1, "General", "Content", ""));
            poi.setSections(sections);
            pois_en.add(poi);
        }

        dbHandler.addPois(pois_en);

        for (int i = 10; i < 20; i++) {
            Poi poi = new Poi("poi" + i, "wiki" + i, 40.50, 30.50, "de", 0);
            List<Section> sections = new ArrayList<Section>();
            sections.add(new Section(i, 1, "Allgemein", "Content", ""));
            poi.setSections(sections);
            pois_de.add(poi);
        }

        dbHandler.addPois(pois_de);

        assertEquals(20, dbHandler.getPoiCount(Locale.getDefault().getLanguage()));

        assertEquals(10, dbHandler.getAllPois(true).size());
        assertEquals(10, dbHandler.getAllPois(false).size());
    }


    @SmallTest
    public void testSaveCategories() {
        dbHandler.saveCategories("0");
        assertTrue(dbHandler.getCategories().length == 1);

        dbHandler.saveCategories("0,2");
        assertTrue(dbHandler.getCategories().length == 2);

        dbHandler.saveCategories("0,2,3");
        assertTrue(dbHandler.getCategories().length == 3);
    }


    @SmallTest
    public void testSaveLevel() {
        dbHandler.saveLevel(1);
        assertTrue(dbHandler.getLevel() == 1);

        dbHandler.saveLevel(2);
        assertTrue(dbHandler.getLevel() == 2);

        dbHandler.saveLevel(3);
        assertTrue(dbHandler.getLevel() == 3);
    }


    @SmallTest
    public void testSetInit() {
        dbHandler.setInitFalse();
        assertFalse(dbHandler.getInit());
    }

    @SmallTest
    public void testSaveDistance() {
        dbHandler.setDistance(250);
        assertEquals(250, dbHandler.getDistance());

        dbHandler.setDistance(1000);
        assertEquals(1000, dbHandler.getDistance());

    }


    @SmallTest
    public void testRetrieveNearPois() {
        dbHandler.saveCategories("0,1,2,3");
        List<Poi> pois = new ArrayList<Poi>();

        // current Location
        double latitude = 40;
        double longitude = 30;

        // add 5 pois that are far away
        for (int i = 0; i < 5; i++) {
            Poi poi = new Poi("poifar" + i, "wiki" + i, 2 + i, 3 + i, "en", 0);
            List<Section> sections = new ArrayList<Section>();
            sections.add(new Section(i, 1, "General", "Content", ""));
            poi.setSections(sections);
            pois.add(poi);
        }

        // add 5 pois that are 'near'
        for (int i = 0; i < 5; i++) {
            Poi poi = new Poi("poinear" + i, "wiki" + i, 40.50 + i, 30.50 + i, "en", 0);
            List<Section> sections = new ArrayList<Section>();
            sections.add(new Section(i, 1, "General", "Content", ""));
            poi.setSections(sections);
            pois.add(poi);
        }

        // add another 5 pois that are far away
        for (int i = 0; i < 5; i++) {
            Poi poi = new Poi("poifar" + i, "wiki" + i, 90 + i, 80 + i, "en", 0);
            List<Section> sections = new ArrayList<Section>();
            sections.add(new Section(i, 1, "General", "Content", ""));
            poi.setSections(sections);
            pois.add(poi);
        }

        dbHandler.addPois(pois);

        List<Poi> nearPois = PoiHolder.retrieveNearPois(latitude, longitude, 5, true);

        assertEquals(5, nearPois.size());

        for (int i = 0; i < nearPois.size(); i++) {
            assertEquals("poinear" + i, nearPois.get(i).getName());
        }

    }

}
