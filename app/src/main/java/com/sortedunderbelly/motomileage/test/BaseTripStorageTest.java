package com.sortedunderbelly.motomileage.test;

import android.test.AndroidTestCase;

import com.sortedunderbelly.motomileage.StorageCallbacks;
import com.sortedunderbelly.motomileage.Trip;
import com.sortedunderbelly.motomileage.TripImpl;
import com.sortedunderbelly.motomileage.TripStorage;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by max.ross on 6/7/14.
 */
public abstract class BaseTripStorageTest extends AndroidTestCase {
    static final StorageCallbacks STORAGE_CALLBACKS = new StorageCallbacks() {
        @Override
        public void onNewTrip(Trip trip) {

        }

        @Override
        public void onUpdatedTrip(Trip updatedTrip) {

        }

        @Override
        public void onDeletedTrip(String deletedTripId) {

        }

        @Override
        public void onFullRefresh() {

        }
    };

    TripStorage storage;

    abstract TripStorage newTripStorage();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        storage = newTripStorage();
    }

    public void testAllTrips() {
        assertEquals(0, storage.allTrips().size());
    }

    public void testLookup() {
        assertNull(storage.lookup("33"));
        Date date = new Date();
        Trip expected = storage.save(new TripImpl("1", date, "my desc", 222));
        assertEquals(expected, storage.lookup(expected.getId()));
    }

    public void testSave() {
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(Calendar.YEAR, 1999);
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 21);

        Trip trip1 = new TripImpl(cal.getTime(), "this is the description", 44);
        assertEquals("1", storage.save(trip1).getId());
        cal.set(Calendar.DAY_OF_MONTH, 22);
        Trip trip2 = new TripImpl(cal.getTime(), null, 42);
        assertEquals("2", storage.save(trip2).getId());

        List<Trip> allTrips = storage.allTrips();
        assertEquals(2, allTrips.size());

        // trips come back ordered by date desc
        assertEquals(allTrips.get(1), trip1);
        assertEquals(allTrips.get(0), trip2);

        // now update the trips
        trip2 = allTrips.get(0);
        trip1 = allTrips.get(1);

        Trip updatedTrip1 = new TripImpl(trip1.getId(), trip1.getDate(), trip1.getDesc(), 34);
        assertEquals("1", storage.save(updatedTrip1).getId());
        Trip updatedTrip2 = new TripImpl(trip2.getId(), trip2.getDate(), trip2.getDesc(), 32);
        assertEquals("2", storage.save(updatedTrip2).getId());

        allTrips = storage.allTrips();
        assertEquals(2, allTrips.size());

        assertEquals(34, allTrips.get(1).getDistance());
        assertEquals(32, allTrips.get(0).getDistance());
    }

    public void testDelete() {
        assertFalse(storage.delete("8888"));

        Trip trip1 = storage.save(new TripImpl("1", new Date(), "this is the description", 44));
        assertEquals("1", trip1.getId());

        Trip trip2 = storage.save(new TripImpl("2", new Date(), "this is the description", 44));
        assertEquals("2", trip2.getId());
        assertEquals(2, storage.allTrips().size());
        assertTrue(storage.delete(trip1.getId()));

        assertEquals(1, storage.allTrips().size());
        assertTrue(storage.delete(trip2.getId()));
        assertEquals(0, storage.allTrips().size());
    }


    private void assertEquals(Trip t1, Trip t2) {
        assertEquals(t1.getDate(), t2.getDate());
        assertEquals(t1.getDesc(), t2.getDesc());
        assertEquals(t1.getDistance(), t2.getDistance());
    }
}
