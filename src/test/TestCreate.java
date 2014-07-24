package test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import org.feiteira.sha1map.KeyPairIterator;
import org.feiteira.sha1map.SHA1PervasiveHashOffsetMap;
import org.junit.Test;

public class TestCreate {

	static SHA1PervasiveHashOffsetMap map;
	int count;
	int count_success;
	HashMap<byte[], byte[]> tmp;

	// @Test
	public void test_create() {
		map = new SHA1PervasiveHashOffsetMap(5000 * 1000 * 1000, "C:\\testmap",
				"testmapfile");
		map.create();
	}

	@Test
	public void test_store_and_get() throws UnsupportedEncodingException {
		test_create();
		byte[] testkey1 = "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$".getBytes();
		byte[] testkey2 = "F123456789012345678901234567890123456789".getBytes();
		byte[] testvalue1 = "0123456789".getBytes();
		System.out.println(map);
		map.put(testkey1, testvalue1);

		// System.out.println("TT " + new String(testkey1,"UTF-8"));

		byte[] v = map.get(testkey1);
		assertNotEquals(v, null);
		System.out.println(">> " + new String(v, "UTF-8"));

		byte[] v2 = map.get(testkey2);
		// System.out.println(">> " + new String(v2,"UTF-8"));

		assertNull(v2);
	}

	private static byte[] randomb(int size) {
		byte[] ret = new byte[size];
		new Random().nextBytes(ret);
		return ret;
	}

	@Test
	public void test_performance_write() {
		map = new SHA1PervasiveHashOffsetMap(50 * 1000 * 1000, "C:\\testmap",
				"testmapfile");
		map.create();
		int tot = 1 * 1000;

		long start = System.currentTimeMillis();

		for (long cnt = 0; cnt < tot; cnt++) {
			byte[] rkey = randomb(40);
			byte[] rval = randomb(8);
			map.put(rkey, rval);
			// assertArrayEquals(rval, map.get(rkey));
			if (cnt % (tot / 10) == 0)
				System.out.println("" + cnt + " out of " + tot + "  or "
						+ (cnt * 100.0f / tot) + "%");
		}

		long dur = System.currentTimeMillis() - start;
		System.out.println("Total records " + tot);
		System.out.println("Duration (secs) " + (dur / 1000.0f));
		System.out.println("Avg writes per second " + (tot / (dur / 1000.0f)));
	}

	public byte[] sampleEncodeForTesing(byte[] key) {
		BigInteger bi = new BigInteger(key);
		long l = bi.longValue();
		l *= 1481 + 31; // 1481 is just a 'random' prime for this sample/test

		ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE / 8);// should be 8
		buffer.putLong(l);
		return buffer.array();
	}

	@Test
	public void test_performance_write_read() throws IOException {

		final int ELEMENT_COUNT = 5000000;

		map = new SHA1PervasiveHashOffsetMap(ELEMENT_COUNT, "C:\\testmap",
				"testmapfile");
		map.create();

		System.out.println("** Test WRITTING **");

		// writting
		{
			long start = System.currentTimeMillis();
			long burst_start = System.currentTimeMillis();

			for (long cnt = 0; cnt < ELEMENT_COUNT; cnt++) {
				byte[] rkey = randomb(40);
				byte[] rval = sampleEncodeForTesing(rkey);
				map.put(rkey, rval);
				// map.get(rkey);
				if (cnt % (ELEMENT_COUNT / 100) == 0) {
					float reqsec = cnt
							/ ((System.currentTimeMillis() - burst_start) / 1000.0f);
					System.out.println("write " + cnt + " out of "
							+ ELEMENT_COUNT + "  or "
							+ (cnt * 100.0f / ELEMENT_COUNT)
							+ "%   requests/secs => " + reqsec);
					burst_start = System.currentTimeMillis();
				}
			}

			long dur = System.currentTimeMillis() - start;
			System.out.println("Total records " + ELEMENT_COUNT);
			System.out.println("Duration (secs) " + (dur / 1000.0f));
			System.out.println("Avg  writes per second "
					+ (ELEMENT_COUNT / (dur / 1000.0f)));
		}

		System.out.println("** Test READING **");
		// reading
		{
			long start = System.currentTimeMillis();

			count = 0;
			count_success = 0;
			map.iterate(new KeyPairIterator() {

				@Override
				public void pair(long index, byte[] key, byte[] val) {
					if (count % (ELEMENT_COUNT / 100) == 0) {
						// System.out.println("[" + count + "] @ index " + index
						// + " Repeated: " + new BigInteger(key));
						System.out.println("reading at "
								+ ((count * 100) / ELEMENT_COUNT) + "%");
					}
					count++;
					// mini checksum to verify the validity of the data
					if (Arrays.equals(val, sampleEncodeForTesing(key))) {
						count_success++;
					}
				}
			});

			System.out.println("Total reads: " + count);
			System.out.println("Valid reads: " + count_success);

			long dur = System.currentTimeMillis() - start;
			System.out.println("Total records " + ELEMENT_COUNT);
			System.out.println("Duration (secs) " + (dur / 1000.0f));
			System.out.println("Avg  reads per second "
					+ (ELEMENT_COUNT / (dur / 1000.0f)));

		}
	}

	@Test
	public void test_performance_read() throws IOException {

		final int ELEMENT_COUNT = 5000000;

		map = new SHA1PervasiveHashOffsetMap(ELEMENT_COUNT, "C:\\testmap",
				"testmapfile");
		map.load();

		System.out.println("** Test READING **");
		// reading
		{
			long start = System.currentTimeMillis();

			count = 0;
			count_success = 0;
			map.iterate(new KeyPairIterator() {

				@Override
				public void pair(long index, byte[] key, byte[] val) {
					if (count % (ELEMENT_COUNT / 100) == 0) {
						// System.out.println("[" + count + "] @ index " + index
						// + " Repeated: " + new BigInteger(key));
						System.out.println("reading at "
								+ ((count * 100) / ELEMENT_COUNT) + "%");
					}
					count++;
					// mini checksum to verify the validity of the data
					if (Arrays.equals(val, sampleEncodeForTesing(key))) {
						count_success++;
					}
				}
			});

			System.out.println("Total reads: " + count);
			System.out.println("Valid reads: " + count_success);

			long dur = System.currentTimeMillis() - start;
			System.out.println("Total records " + ELEMENT_COUNT);
			System.out.println("Duration (secs) " + (dur / 1000.0f));
			System.out.println("Avg  reads per second "
					+ (ELEMENT_COUNT / (dur / 1000.0f)));

		}
	}

	@Test
	public void test_performance_get() throws IOException {

		final int ELEMENT_COUNT = 5000000;
		final int TESTED_ELEMENTS_COUNT = 50000;

		final int mod = ELEMENT_COUNT / TESTED_ELEMENTS_COUNT;

		map = new SHA1PervasiveHashOffsetMap(ELEMENT_COUNT, "C:\\testmap",
				"testmapfile");
		map.load();

		final HashMap<byte[], byte[]> testMap = new HashMap<byte[], byte[]>();

		System.out.println("** Test READING **");
		// reading
		{
			long start = System.currentTimeMillis();

			count = 0;
			count_success = 0;
			map.iterate(new KeyPairIterator() {

				@Override
				public void pair(long index, byte[] key, byte[] val) {
					if (count % mod == 0) {
						testMap.put(key, val);
					}

					if (count % (ELEMENT_COUNT / 100) == 0) {
						// System.out.println("[" + count + "] @ index " + index
						// + " Repeated: " + new BigInteger(key));
						System.out.println("reading at "
								+ ((count * 100) / ELEMENT_COUNT) + "%");
					}
					count++;
					// mini checksum to verify the validity of the data
					if (Arrays.equals(val, sampleEncodeForTesing(key))) {
						count_success++;
					}
				}
			});

			System.out.println("Total reads: " + count);
			System.out.println("Valid reads: " + count_success);

			long dur = System.currentTimeMillis() - start;
			System.out.println("Total records " + ELEMENT_COUNT);
			System.out.println("Duration (secs) " + (dur / 1000.0f));
			System.out.println("Avg  reads per second "
					+ (ELEMENT_COUNT / (dur / 1000.0f)));

		}

		// getting
		
		System.out.println("** Testing READ ELEMENTS **");

		{
			count = 0;
			long start = System.currentTimeMillis();

			for (byte[] k : testMap.keySet()) {
				count++;
				byte[] mapval = map.get(k);
				byte[] exp = testMap.get(k);
//				System.out.println("Expect:" + new BigInteger(exp));
//				System.out.println("Receiv:" + new BigInteger(mapval));
				assertArrayEquals(exp, mapval);
			}
			System.out.println("Total reads: " + count);
			long dur = System.currentTimeMillis() - start;
			System.out.println("Duration (secs) " + (dur / 1000.0f));
			System.out.println("Avg  reads per second "
					+ (ELEMENT_COUNT / (dur / 1000.0f)));
		}
	}

}
