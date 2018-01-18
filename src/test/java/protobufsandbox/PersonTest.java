package protobufsandbox;

import com.example.tutorial.AddressBookProtos.Person;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static protobufsandbox.SomeUtils.toBinaryString;
import static protobufsandbox.SomeUtils.toBytes;
import static protobufsandbox.SomeUtils.toHexString;

public class PersonTest {

    private final ByteArrayOutputStream os = new ByteArrayOutputStream();

    @Test
    public void writeJohnToOutputStream() throws IOException {
        final Person person = Person.newBuilder()
                .setName("John")
                .setId(1)
                .build();

        person.writeTo(os);

        assertThat(toHexString(os.toByteArray()), is("0a 04 4a 6f 68 6e 10 01"));
    }

    @Test
    public void explain1stByte() {
        final String s = toBinaryString(toBytes("0a")[0]);

        /*
        this means: tag=1 (00001), wire type=2 (010): Length-delimited.
        04 means four bytes in hex.
         */
        assertThat(s, is("00001010"));
    }

    @Test
    public void explainName() {
        final byte[] bytes = toBytes("4a 6f 68 6e");

        assertThat(new String(bytes, StandardCharsets.UTF_8), is("John"));
    }

    @Test
    public void parseJohnFromBytes() throws InvalidProtocolBufferException {
        final byte[] bytes = toBytes("0a 04 4a 6f 68 6e 10 01");

        final Person actual = Person.parseFrom(bytes);

        assertThat(actual.getId(), is(1));
        assertThat(actual.getName(), is("John"));
    }

    @Test
    public void writeJohnToOutputStreamDelimitedTo() throws IOException {
        final Person person = Person.newBuilder()
                .setId(1)
                .setName("John")
                .build();

        person.writeDelimitedTo(os);

        assertThat(toHexString(os.toByteArray()), is("08 0a 04 4a 6f 68 6e 10 01"));
    }

    @Test
    public void parseJohnFromDelimitedToBytes() throws IOException {
        final byte[] bytes = toBytes("08 0a 04 4a 6f 68 6e 10 01");

        final Person actual = Person.parseDelimitedFrom(new ByteArrayInputStream(bytes));

        assertThat(actual.getId(), is(1));
        assertThat(actual.getName(), is("John"));
    }

    @Test
    public void parse2JohnFromDelimitedToBytes() throws IOException {
        final byte[] bytes = toBytes(
                "08 0a 04 4a 6f 68 6e 10 01"
                        + "08 0a 04 4a 6f 68 6e 10 01");
        final ByteArrayInputStream input = new ByteArrayInputStream(bytes);

        final Person john1 = Person.parseDelimitedFrom(input);
        assertThat(john1.getId(), is(1));
        assertThat(john1.getName(), is("John"));
        final Person john2 = Person.parseDelimitedFrom(input);
        assertThat(john2.getId(), is(1));
        assertThat(john2.getName(), is("John"));
    }
}
