package org.feiteira.sha1map;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hamcrest.core.IsNull;

public class SHA1PervasiveHashOffsetMap implements Map<byte[], byte[]> {

	public static final long MEGABYTES = 1024 * 1024;
	public static final long MAX_FILESIZE_BYTES = 256 * MEGABYTES; // * MB
	public static final int KEY_SIZE = 40;// 40 bytes
	public static final int OFFSET_SIZE = 8;// 8 bytes

	public static final int KEYPAIR_SIZE = KEY_SIZE + OFFSET_SIZE;// 48 bytes

	public static final long HASHMAP_RELATIVE_SIZE = 3;// the hashmap will store
														// X the size of
														// max_elements

	public static final int KEYPAIRS_IN_CHUNK = 128;// one chunk has size =
													// (KEY_SIZE+OFFSET_SIZE)*KEYSPAIRS_IN_CHUNK

	public static final int CHUNK_SIZE_BYTES = KEYPAIR_SIZE * KEYPAIRS_IN_CHUNK;

	private long planned_elements;
	private long available_elements;

	private String folder;
	private String prefix;
	private int nfiles;

	private static final long serialVersionUID = 1L;

	private RandomAccessFile files[];
	private long elementsPerFile;

	public SHA1PervasiveHashOffsetMap(long planned_elements, String folder,
			String pref) {
		this.planned_elements = planned_elements;
		this.folder = folder;
		this.prefix = pref;

		elementsPerFile = MAX_FILESIZE_BYTES / (KEYPAIR_SIZE)
				- KEYPAIRS_IN_CHUNK;

		nfiles = 1 + (int) ((planned_elements * HASHMAP_RELATIVE_SIZE) / elementsPerFile);

		this.available_elements = nfiles
				* (elementsPerFile + KEYPAIRS_IN_CHUNK);

		long total_disk_space = nfiles;
		total_disk_space *= MAX_FILESIZE_BYTES;

		System.out.println("Total disk space (bytes): " + total_disk_space);
		System.out.println("Planned elements: " + planned_elements);
		System.out.println("Available elements: " + available_elements);
		System.out.println("Max file size (bytes): " + MAX_FILESIZE_BYTES);
		System.out.println("Elements per file: " + elementsPerFile);

		files = new RandomAccessFile[nfiles];

	}

	private String getFilenameFromCount(int cnt) {
		return folder + "/" + prefix + "." + cnt;
	}

	public void create() {

		System.out
				.println("Max "
						+ (planned_elements * (KEY_SIZE + OFFSET_SIZE) * HASHMAP_RELATIVE_SIZE));
		System.out.println("N Files " + nfiles);

		for (int cnt = 0; cnt < nfiles; cnt++) {
			try {
				System.out.println("Creating: " + getFilenameFromCount(cnt));
				RandomAccessFile f = new RandomAccessFile(
						getFilenameFromCount(cnt), "rw");
				f.setLength(0);
				f.setLength(MAX_FILESIZE_BYTES);
				files[cnt] = f;
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	public void load() {

		System.out
				.println("Max "
						+ (planned_elements * (KEY_SIZE + OFFSET_SIZE) * HASHMAP_RELATIVE_SIZE));
		System.out.println("N Files " + nfiles);

		for (int cnt = 0; cnt < nfiles; cnt++) {
			try {
				System.out.println("Creating: " + getFilenameFromCount(cnt));
				RandomAccessFile f = new RandomAccessFile(
						getFilenameFromCount(cnt), "rw");
				// f.setLength(0);
				// f.setLength(MAX_FILESIZE_BYTES);
				files[cnt] = f;
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	private RandomAccessFile getFileFromShahash(long shahash) {
		// System.out.println("selected file " + ((shahash) / elementsPerFile));
		return files[(int) ((shahash) / elementsPerFile)];
	}

	private long getOffsetFromShahash(long shahash) {
		return ((shahash) % (elementsPerFile)) * KEYPAIR_SIZE;
	}

	// shorter version of the hashmap that fits into our defined space
	public long shahash(byte[] sha) {
		long ret = 0;

		ret = new BigInteger(sha).longValue();
		if (ret < 0)
			ret *= -1;

		// System.out.println("Mini Hash : " + (ret % max_elements));
		return (ret % planned_elements) * HASHMAP_RELATIVE_SIZE;
	}

	@Override
	public byte[] put(byte[] key, byte[] value) {
		try {
			KeyPairChunk kpc = new KeyPairChunk(shahash(key));
			kpc.put(key, value);
			kpc.save();

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return key;
	}

	@Override
	public void clear() {
		create();
	}

	@Override
	public boolean containsKey(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsValue(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<java.util.Map.Entry<byte[], byte[]>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] get(Object okey) {
		byte[] key = (byte[]) okey;
		try {
			KeyPairChunk kpc = new KeyPairChunk(shahash(key));
			return kpc.getValue(key);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<byte[]> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putAll(Map<? extends byte[], ? extends byte[]> m) {
		// TODO Auto-generated method stub

	}

	@Override
	public byte[] remove(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Collection<byte[]> values() {
		// TODO Auto-generated method stub
		return null;
	}

	public long getMaxElements() {
		return planned_elements;
	}

	public void iterate(KeyPairIterator kpit) throws IOException {
		KeyPairChunk kpc = null;
		for (int cntf = 0; cntf < files.length; cntf++)
			for (long cnt = 0; cnt < elementsPerFile; cnt+=KEYPAIRS_IN_CHUNK) {
				long pos = cntf * elementsPerFile + cnt;

				if (cnt % KEYPAIRS_IN_CHUNK == 0)
					kpc = new KeyPairChunk(pos);

				for (KeyPair current : kpc.keypair) {
					// KeyPair current = kpc.keypair[(int) (cnt %
					// KEYPAIRS_IN_CHUNK)];
					if (!isNull(current.key)) {
						kpit.pair(pos, current.key, current.value);
					}
				}
			}
	}

	public class KeyPair {
		byte[] key = new byte[KEY_SIZE];
		byte[] value = new byte[OFFSET_SIZE];

		public KeyPair(byte arr[], int off) {
			for (int cnt = off; cnt < off + KEYPAIR_SIZE; cnt++) {
				int pos = cnt - off;
				if (pos < KEY_SIZE)
					key[pos] = arr[cnt];
				else
					value[pos - KEY_SIZE] = arr[cnt];
			}
		}

		public String toString() {
			try {
				return super.toString() + " >> " + new String(key, "UTF-8")
						+ " => " + new String(value, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return super.toString();
			}
		}
	}

	public class KeyPairChunk {
		KeyPair[] keypair = new KeyPair[SHA1PervasiveHashOffsetMap.KEYPAIRS_IN_CHUNK];
		byte data[] = null;

		private RandomAccessFile f;

		private long pos;

		public KeyPairChunk(long shahash) throws IOException {

			data = new byte[CHUNK_SIZE_BYTES];
			this.f = getFileFromShahash(shahash);
			this.pos = getOffsetFromShahash(shahash);

			f.seek(pos);
			f.read(data);

			for (int cnt = 0; cnt < KEYPAIRS_IN_CHUNK; cnt++) {
				// System.out.println("Chunk " + cnt);
				keypair[cnt] = new KeyPair(data, cnt * KEYPAIR_SIZE);
			}
		}

		public void put(byte[] k, byte[] v) {
			for (int cnt = 0; cnt < KEYPAIRS_IN_CHUNK; cnt++) {
				if (isNull(keypair[cnt].value)) {
					int relative_off = cnt * KEYPAIR_SIZE;
					System.arraycopy(k, 0, data, relative_off, k.length);
					System.arraycopy(v, 0, data, relative_off + KEY_SIZE,
							v.length);
					return;
				}
			}
			System.err.println("Collision!!");
		}

		public byte[] getKey() {
			KeyPair kp = keypair[0];
			return kp.key;
		}

		public byte[] getValue(byte[] key) {
			for (int cnt = 0; cnt < KEYPAIRS_IN_CHUNK; cnt++) {
				KeyPair kp = keypair[0];
				if (Arrays.equals(kp.key, key))
					return kp.value;
				if (isNull(kp.key))
					return null;
			}
			return null;
		}

		public void save() throws IOException {
			f.seek(pos);
			f.write(data);
		}

	}

	private static boolean isNull(byte[] val) {
		for (int cnt = 0; cnt < val.length; cnt++)
			if (val[cnt] != 0) {
				return false;
			}

		return true;
	}

	private static byte[] concat(byte[] A, byte[] B) {
		int aLen = A.length;
		int bLen = B.length;
		byte[] C = new byte[aLen + bLen];
		System.arraycopy(A, 0, C, 0, aLen);
		System.arraycopy(B, 0, C, aLen, bLen);
		return C;
	}
}
