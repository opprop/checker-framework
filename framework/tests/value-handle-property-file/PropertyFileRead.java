import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.checkerframework.common.value.qual.StringVal;

class PropertyFileRead {

    public static final String propFile = "tests/value-handle-property-file/a.properties";

    void a() throws IOException {
        Properties prop = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFile);
        prop.load(inputStream);
        @StringVal("http://www.example.com") String url = prop.getProperty("URL");
    }

    void b() throws IOException {
        Properties prop = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFile);
        prop.load(inputStream);
        @StringVal({"localhost", "127.0.0.1"}) String host = prop.getProperty("HOST", "127.0.0.1");
    }

    void c() throws IOException {
        Properties prop = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFile);
        prop.load(inputStream);
        // :: warning: (key.not.exist.in.properties.file)
        @StringVal("default value") String host = prop.getProperty("NOSUCHKEY", "default value");
    }

    void d() throws IOException {
        Properties prop = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFile);
        prop.load(inputStream);
        // :: error: (assignment.type.incompatible)
        @StringVal("8081") String port = prop.getProperty("PORT");
    }

    void e() throws IOException {
        Properties prop = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFile);
        prop.load(inputStream);
        // :: error: (assignment.type.incompatible)
        @StringVal("8081") String port = prop.getProperty("PORT");
        @StringVal("http://www.example.com") String url = prop.getProperty("URL");
    }
}
