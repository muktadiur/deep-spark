package com.stratio.deep.rdd;


import com.google.common.io.Resources;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.stratio.deep.commons.extractor.server.ExtractorServer;
import de.flapdoodle.embed.mongo.*;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.io.file.Files;
import de.flapdoodle.embed.process.runtime.Network;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.testng.Assert.assertEquals;

/**
 * Created by rcrespo on 16/07/14.
 */
@Test(suiteName = "mongoRddTests", groups = {"MongoJavaRDDTest"})
//@Test
public class MongoJavaRDDTest {


    public static MongodExecutable mongodExecutable = null;
    public static MongoClient mongo = null;

    public static DBCollection col = null;

    public static final String MESSAGE_TEST = "new message test";

    public static final Integer PORT = 27890;

    public static final String HOST = "localhost";

    public static final String DATABASE = "test";

    public static final String COLLECTION_INPUT = "input";

    public static final String COLLECTION_OUTPUT = "output";

    public static final String COLLECTION_OUTPUT_CELL = "outputcell";

    public static final String DATA_SET_NAME = "divineComedy.json";

    public final static String DB_FOLDER_NAME = System.getProperty("user.home") +
            File.separator + "mongoEntityRDDTest";

    public static final Long WORD_COUNT_SPECTED = 3833L;

    public static final String DATA_SET_URL = "http://docs.openstratio.org/resources/datasets/divineComedy.json";


    private static MongodProcess mongod;

    @BeforeSuite
    public static void init() throws IOException {
        Command command = Command.MongoD;


        new File(DB_FOLDER_NAME).mkdirs();

        IMongodConfig mongodConfig = new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .configServer(false)
                .replication(new Storage(DB_FOLDER_NAME, null, 0))
                .net(new Net(PORT, Network.localhostIsIPv6()))
                .cmdOptions(new MongoCmdOptionsBuilder()
                        .syncDelay(10)
                        .useNoPrealloc(true)
                        .useSmallFiles(true)
                        .useNoJournal(true)
                        .build())
                .build();


        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                .defaults(command)
                .artifactStore(new ArtifactStoreBuilder()
                        .defaults(command)
                        .download(new DownloadConfigBuilder()
                                .defaultsForCommand(command)
                                .downloadPath("https://s3-eu-west-1.amazonaws.com/stratio-mongodb-distribution/")))
                .build();

        MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);

        mongodExecutable = null;


        mongodExecutable = runtime.prepare(mongodConfig);

        mongod = mongodExecutable.start();

//TODO : Uncomment when drone.io is ready
//        Files.forceDelete(new File(System.getProperty("user.home") +
//                File.separator + ".embedmongo"));


        mongo = new MongoClient(HOST, PORT);
        DB db = mongo.getDB(DATABASE);
        col = db.getCollection(COLLECTION_INPUT);
        col.save(new BasicDBObject("message", MESSAGE_TEST));


        dataSetImport();


        ExecutorService es = Executors.newFixedThreadPool(1);
        final Future future = es.submit(new Callable() {
            public Object call() throws Exception {
                ExtractorServer.start();
                return null;
            }
        });


    }

    @Test
    public void testRDD() {
        assertEquals(true,true);
    }

    /**
     * Imports dataset
     *
     * @throws java.io.IOException
     */
    private static void dataSetImport() throws IOException {
        String dbName = "book";
        String collection = "input";
        URL url = Resources.getResource(DATA_SET_NAME);
        IMongoImportConfig mongoImportConfig = new MongoImportConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(PORT, Network.localhostIsIPv6()))
                .db(dbName)
                .collection(collection)
                .upsert(false)
                .dropCollection(true)
                .jsonArray(true)
                .importFile(url.getFile())
                .build();
        MongoImportExecutable mongoImportExecutable = MongoImportStarter.getDefaultInstance().prepare(mongoImportConfig);
        mongoImportExecutable.start();


    }


    @AfterSuite
    public static void cleanup() {
        if (mongodExecutable != null) {
            mongod.stop();
            mongodExecutable.stop();
        }

        Files.forceDelete(new File(DB_FOLDER_NAME));
        ExtractorServer.close();
    }
}
