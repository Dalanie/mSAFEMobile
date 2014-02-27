package melb.mSafe.database;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import melb.mSafe.chat.ServerMessage;
import melb.mSafe.chat.UserMessage;
import melb.mSafe.model.Model3D;
import melb.mSafe.model.RouteGraph;
import melb.mSafe.utilities.Toaster;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Message;

/**
 * The DatabaseHandler for the communication between Client and Client-Database
 * 
 * @author Daniel Langerenken based on
 *         http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
 */
public class DatabaseHandler extends SQLiteOpenHelper implements
		IDatabaseHandler {

	/**
	 * Database Name and Version
	 */
	private static final int DATABASE_VERSION = 17;
	private static final String DATABASE_NAME = "msafe_database";

	/**
	 * Table names
	 */
	private static final String TABLE_ROUTEGRAPH = "routeGraph";
	private static final String TABLE_BUILDING = "building";

    /**
     * Routegraph
     */
    private static final String ROUTEGRAPH_ID = "_id";
    private static final String ROUTEGRAPH_BLOB = "blob";
    private static final String ROUTEGRAPH_DATE = "created";
    private static final String ROUTEGRAPH_LAST_MODIFIED = "modified";

    /**
     * Building
     */
    private static final String BUILDING_ID = "_id";
    private static final String BUILDING_BLOB = "blob";
    private static final String BUILDING_DATE = "created";
    private static final String BUILDING_LAST_MODIFIED = "modified";

    private static SQLiteDatabase db;
    private static DatabaseHandler instance;

    /**
     * @return the singleton instance.
     */
    public static synchronized DatabaseHandler getInstance() {
        return instance;
    }


    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //
    }

    /*
         * (non-Javadoc)
         * @see android.database.sqlite.SQLiteOpenHelper#close()
         */
    @Override
    public synchronized void close() {
        if (instance != null){
            db.close();
        }
    }

    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /*
     * Retrieves a thread-safe instance of the singleton object {@link DatabaseHandler} and opens the database
     * with writing permissions.
     *
     * @param context the context to set.
     */
    public static void init(Context context) {
        if (instance == null) {
            instance = new DatabaseHandler(context);
            db = instance.getWritableDatabase();
        }
    }

	/*
	 * Creating Tables(non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
	 * .SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
        String createRouteGraphTable = "CREATE TABLE " + TABLE_ROUTEGRAPH + "(" +
                ROUTEGRAPH_ID + " INTEGER PRIMARY KEY," +
                ROUTEGRAPH_BLOB + " BLOB," +
                ROUTEGRAPH_DATE + " LONG," +
                ROUTEGRAPH_LAST_MODIFIED + " LONG" + ")";
        String createBuildingTable = "CREATE TABLE " + TABLE_BUILDING + "(" +
                BUILDING_ID + " INTEGER PRIMARY KEY," +
                BUILDING_BLOB + " BLOB," +
                BUILDING_DATE + " LONG," +
                BUILDING_LAST_MODIFIED + " LONG" + ")";
        db.execSQL(createBuildingTable);
        db.execSQL(createRouteGraphTable);
    }



	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		/*
		 * Drop older table if existed
		 */
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTEGRAPH);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUILDING);

		/*
		 * Create tables again
		 */
		onCreate(db);
	}

	@Override
	public RouteGraph getLatestRouteGraph()  {
        Cursor cursor = db.query(TABLE_ROUTEGRAPH, null,
                null, null, null,null, ROUTEGRAPH_DATE + " DESC", "1");
        RouteGraph routeGraph = null;
        if (cursor.moveToFirst()){
            byte[] blob = cursor.getBlob(1);
            try {
                routeGraph = (RouteGraph) fromBlob(blob);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        if (routeGraph != null){
            routeGraph.initIfNeeded();
        }
		return routeGraph;
	}


    public long addRouteGraph(RouteGraph graph){
        try {
            ContentValues values = new ContentValues();
            values.put(ROUTEGRAPH_BLOB, toBlob(graph));
            values.put(ROUTEGRAPH_DATE, new Date().getTime());
		    /*
		    * Inserting Row
		    */
            return db.insert(TABLE_ROUTEGRAPH, null, values);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void updateRouteGraph(RouteGraph graph){
        ContentValues values = new ContentValues();
        try {
            values.put(ROUTEGRAPH_BLOB, toBlob(graph));
            values.put(ROUTEGRAPH_LAST_MODIFIED, new Date().getTime());
        } catch (IOException e) {
            e.printStackTrace();
            Toaster.toast("blob error: " + e);
        }
        db.update(TABLE_ROUTEGRAPH, values, ROUTEGRAPH_ID + "=" + graph.getId(), null);
    }


	@Override
	public List<Message> getLatestMessages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ServerMessage> getLatestServerMessages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserMessage> getLatestUserMessages() {
		// TODO Auto-generated method stub
		return null;
	}

    /**
     * Write the object to a Base64 string. based on
     * http://stackoverflow.com/questions
     * /134492/how-to-serialize-an-object-into-a-string
     */
    private static byte[] toBlob(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return baos.toByteArray();
    }

    /** Read the object from Base64 string. */
    private static Object fromBlob(byte[] data) throws IOException,
            ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
                data));
        Object o = ois.readObject();
        ois.close();
        return o;
    }

    public Model3D getLatestModel() {
        Cursor cursor = db.query(TABLE_BUILDING, null,
                null, null, null,null, BUILDING_DATE + " DESC", "1");
        Model3D model = null;
        if (cursor.moveToFirst()){
            byte[] blob = cursor.getBlob(1);
            try {
                model = (Model3D) fromBlob(blob);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        return model;
    }

    public long addModel(Model3D model3d) {
        try {
            ContentValues values = new ContentValues();
            values.put(BUILDING_BLOB, toBlob(model3d));
            values.put(BUILDING_DATE, new Date().getTime());
		    /*
		    * Inserting Row
		    */
            return db.insert(TABLE_BUILDING, null, values);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
}