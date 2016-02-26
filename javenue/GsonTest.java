package javenue;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class GsonTest {
    static Gson gson = new Gson();

    @Test public void serialize() {
        assertEquals("123", gson.toJson(123));
        assertEquals("\"hello\"", gson.toJson("hello"));
        assertEquals("10", gson.toJson(Long.valueOf(10)));

        assertEquals("[]", gson.toJson(new int[] { }));
        assertEquals("[10,100]", gson.toJson(new int[] { 10, 100}));
        assertEquals("[\"word\"]", gson.toJson(new String[] { "word" }));
    }

    @Test public void deserialize() {
        assertEquals(Integer.valueOf(1), gson.fromJson("1", int.class));
        assertEquals("world", gson.fromJson("\"world\"", String.class));
        assertTrue(gson.fromJson("true", Boolean.class));

        assertEquals(0, gson.fromJson("[]", int[].class).length);
        assertArrayEquals(new int[] {5, 6}, gson.fromJson("[5,6]", int[].class));
        assertArrayEquals(new String[] { "a" }, gson.fromJson("[\"a\"]", String[].class));
    }

    @Test public void object() {
        Entity entity = new Entity(100, "name");
        entity.random = 1234;

        String json = gson.toJson(entity);
        Entity read = gson.fromJson(json, Entity.class);

        assertEquals(entity.id, read.id);
        assertEquals(entity.name, read.name);
        assertEquals(0, read.random);
    }

    @Test public void collection() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("USD", 123);
        map.put("EUR", 321);

        String json = gson.toJson(map);

        Type type = new TypeToken<Map<String, Integer>>(){}.getType();
        Map<String, Integer> read = gson.fromJson(json, type);

        assertEquals(2, read.size());
        assertEquals(Integer.valueOf(123), read.get("USD"));
        assertEquals(Integer.valueOf(321), read.get("EUR"));
    }

    @Test public void custom() {
        class CustomConverter implements JsonSerializer<Custom>,
                    JsonDeserializer<Custom>  {
            public JsonElement serialize(Custom src, Type type,
                     JsonSerializationContext context) {
                JsonObject object = new JsonObject();
                object.addProperty("date", src.date.getTime());
                object.addProperty("integer", src.integer.toString());
                return object;
            }

            public Custom deserialize(JsonElement json, Type type,
                      JsonDeserializationContext context) throws JsonParseException {
                JsonObject object = json.getAsJsonObject();
                Date date = new Date(object.get("date").getAsLong());
                BigInteger integer = new BigInteger(object.get("integer").getAsString());
                return new Custom(date, integer);
            }
        }

        Custom custom = new Custom(new Date(), new BigInteger("1234567890"));
        String json = gson.toJson(custom);
        Custom read = gson.fromJson(json, Custom.class);

        assertNotEquals(custom.date, read.date);

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Custom.class, new CustomConverter());

        Gson gson = builder.create();
        json = gson.toJson(custom);
        read = gson.fromJson(json, Custom.class);

        assertEquals(custom.date, read.date);
        assertEquals(custom.integer, read.integer);
    }

    @Test public void pretty() {
        Entity entity = new Entity(100, "name");
        assertEquals("{\"id\":100,\"name\":\"name\"}", gson.toJson(entity));

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        assertEquals("{\n" +
                "  \"id\": 100,\n" +
                "  \"name\": \"name\"\n" +
                "}", gson.toJson(entity));
    }

    @Test public void exclude() {
        Gson gson = new GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.VOLATILE)
                .create();

        Entity entity = new Entity(100, "name");
        entity.random = 1234;

        String json = gson.toJson(entity);
        Entity read = gson.fromJson(json, Entity.class);

        assertEquals(0, read.id);
        assertEquals(entity.name, read.name);
        assertEquals(1234, read.random);
    }

    public static class Entity {
        volatile int id;
        String name;
        transient long random;

        public Entity(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static class Custom {
        Date date;
        BigInteger integer;

        public Custom(Date date, BigInteger integer) {
            this.date = date;
            this.integer = integer;
        }
    }
}

