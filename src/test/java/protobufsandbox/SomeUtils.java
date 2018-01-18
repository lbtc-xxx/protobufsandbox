package protobufsandbox;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SomeUtils {

    private static final Pattern HEX_DUMP_PATTERN = Pattern.compile("([0-9a-fA-F]{2})");

    private static void toHexString(final Supplier<InputStream> isSupplier,
                                    final Consumer<String> stringConsumer) {
        final StringBuilder sb = new StringBuilder();

        long bytes = 0;

        try (final InputStream is = isSupplier.get()) {
            int i;

            while ((i = is.read()) != -1) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }

                final String hex = Integer.toHexString(i);
                if (hex.length() < 2) {
                    sb.append('0');
                }
                sb.append(hex);

                if (sb.length() >= 47) {
                    stringConsumer.accept(sb.toString());
                    sb.setLength(0); // clear
                }

                bytes++;
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        stringConsumer.accept(sb.toString());
    }

    static String toHexString(final byte[] bytes) {
        return hexDumpToString(() -> new ByteArrayInputStream(bytes));
    }

    static void toHexString(final Supplier<InputStream> isSupplier) {
        toHexString(isSupplier, System.out::println);
    }

    static String hexDumpToString(final Supplier<InputStream> isSupplier) {
        final StringBuilder sb = new StringBuilder();
        toHexString(isSupplier, (s) -> {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(s);
        });
        return sb.toString();
    }

    static byte[] toBytes(final String hexString) {
        try {
            return toBytes(() -> new StringReader(hexString));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static byte[] toBytes(final Supplier<Reader> readerSupplier) throws IOException {
        return toByte0(() -> new BufferedReader(readerSupplier.get()));
    }

    private static byte[] toByte0(final Supplier<BufferedReader> hexDumpSupplier) throws IOException {
        try (final BufferedReader br = hexDumpSupplier.get();
             final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            String line;
            while ((line = br.readLine()) != null) {
                final Matcher m = HEX_DUMP_PATTERN.matcher(line);
                while (m.find()) {
                    final String oneByteHexDump = m.group();
                    final byte[] bytes = hexStringToByteArray(oneByteHexDump);
                    assert bytes.length == 1;
                    os.write(bytes[0]);
                }
            }
            return os.toByteArray();
        }
    }

    // https://stackoverflow.com/a/140861/3591946
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    // https://stackoverflow.com/a/12310078/3591946
    static String toBinaryString(final byte b) {
        return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
    }
}
