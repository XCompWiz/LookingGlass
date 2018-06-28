package com.xcompwiz.lookingglass.proxyworld;

import java.util.LinkedList;
import java.util.List;

import com.xcompwiz.lookingglass.log.LoggerUtils;
import com.xcompwiz.lookingglass.network.ServerPacketDispatcher;
import com.xcompwiz.lookingglass.network.packet.PacketChunkInfo;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

/**
 * Finds 16x16x16 chunks exposed to the passed chunk location.
 * @author Ken Butler/shadowking97
 */
public class ChunkFinder {

	private final IChunkProvider	chunkProvider;
	private final int				rootX;
	private final int				rootZ;
	private final int				range;
	private final int				dimension;
	private final EntityPlayer		player;
	private ChunkData[][]			map;
	private List<BlockPos>	cc;
	private final int				d;
	private int						step;
	private int						stepRange;
	private long					startTime;

	/**
	 * Finds exposed chunks. Chunks must be loaded.
	 * @param root The chunk in chunk coordinates.
	 * @param dimension The target dimension
	 * @param chunkProvider The world server that contains the chunks
	 * @param player The player to send the chunks to
	 * @param range The radius of the chunkfinder.
	 * @return Sorted Chunk Data, by range. Prioritizes closest chunks.
	 */
	public ChunkFinder(BlockPos root, int dimension, IChunkProvider chunkProvider, EntityPlayer player, int range) {
		this.chunkProvider = chunkProvider;
		this.range = range;
		this.dimension = dimension;
		this.player = player;
		this.d = (range << 1) + 1;
		this.map = new ChunkData[d][d];
		this.rootX = root.getX() - range;
		this.rootZ = root.getZ() - range;
		this.stepRange = 16 - root.getY();
		if (root.getY() > stepRange) stepRange = root.getY();
		startTime = System.nanoTime();
		LoggerUtils.debug("ChunkFinder scan started at nano: " + startTime);
		for (int i = 0; i < d; i++) {
			for (int j = 0; j < d; j++) {
				map[i][j] = new ChunkData(i + rootX, j + rootZ);
				int x1 = i - range;
				int z1 = j - range;
				map[i][j].distance = x1 * x1 + z1 * z1;
			}
		}
		cc = new LinkedList<BlockPos>();
		cc.add(new BlockPos(range, root.getY(), range));
		step = 0;
		List<BlockPos> cc2 = new LinkedList<BlockPos>();
		while (step - 1 < stepRange && !cc.isEmpty()) {
			while (!cc.isEmpty()) {
				cc2.addAll(scan(chunkProvider, map, cc.get(0), range));
				cc.remove(0);
			}
			step++;
			cc.addAll(cc2);
			cc2.clear();
			if (step >= stepRange) {
				int range2 = step - stepRange + 1;
				range2 *= range2;
				int range3 = step - stepRange;
				if (range3 < 0) range3 = 0;
				range3 *= range3;
				int minStep = range - (step - stepRange);
				int maxStep = range + (step - stepRange) + 1;
				if (minStep < 0) minStep = 0;
				if (maxStep > d) maxStep = d;
				for (int i = minStep; i < maxStep; i++) {
					for (int j = minStep; j < maxStep; j++) {
						int dist = map[i][j].distance;
						if (map[i][j].doAdd() && dist < range2 && dist >= range3) {
							ChunkData data = map[i][j];
							Chunk c2 = chunkProvider.provideChunk(data.x, data.z);
							if (!c2.isLoaded()) c2 = chunkProvider.getLoadedChunk(data.x, data.z);

							ServerPacketDispatcher.getInstance().addPacket(player, PacketChunkInfo.createPacket(c2, true, data.levels(), dimension));
						}
					}
				}
			}
		}
	}

	public boolean findChunks() {
		if (!cc.isEmpty()) {
			int tick = 0;
			List<BlockPos> cc2 = new LinkedList<BlockPos>();
			while (!cc.isEmpty() && tick < 15) {
				BlockPos ch = cc.get(0);
				cc2.addAll(scan(chunkProvider, map, ch, range));
				cc.remove(0);
				++tick;
			}
			if (!cc.isEmpty()) return false;

			step++;

			cc.addAll(cc2);
			cc2.clear();

			if (step >= stepRange) {
				int range2 = step - stepRange + 1;
				range2 *= range2;
				int range3 = step - stepRange;
				if (range3 < 0) range3 = 0;
				range3 *= range3;
				int minStep = range - (step - stepRange);
				int maxStep = range + (step - stepRange) + 1;
				if (minStep < 0) minStep = 0;
				if (maxStep > d) maxStep = d;
				for (int i = minStep; i < maxStep; i++) {
					for (int j = minStep; j < maxStep; j++) {
						int dist = map[i][j].distance;
						if (map[i][j].doAdd() && dist < range2 && dist >= range3) {
							ChunkData data = map[i][j];
							Chunk c2 = chunkProvider.provideChunk(data.x, data.z);
							if (!c2.isLoaded()) c2 = chunkProvider.getLoadedChunk(data.x, data.z);
							ServerPacketDispatcher.getInstance().addPacket(player, PacketChunkInfo.createPacket(c2, true, data.levels(), dimension));
						}
					}
				}
			}
			return false;
		}

		if (step >= stepRange) {
			int range2 = step - stepRange;
			range2 *= range2;
			for (int i = 0; i < d; i++) {
				for (int j = 0; j < d; j++) {
					int dist = map[i][j].distance;
					if (map[i][j].doAdd() && dist >= range2) {
						ChunkData data = map[i][j];
						Chunk c2 = chunkProvider.provideChunk(data.x, data.z);
						if (!c2.isLoaded()) c2 = chunkProvider.getLoadedChunk(data.x, data.z);
						ServerPacketDispatcher.getInstance().addPacket(player, PacketChunkInfo.createPacket(c2, true, data.levels(), dimension));
					}
				}
			}
		}
		LoggerUtils.debug("Scan finished. nanoseconds: " + (System.nanoTime() - startTime));
		return true;
	}

	/**
	 * Recursive function to find all chunk segments attached to the surface.
	 */
	private static List<BlockPos> scan(IChunkProvider chunkProvider, ChunkData[][] map, BlockPos coord, int range) {
		int rangeSqr = range * range;
		List<BlockPos> cc3 = new LinkedList<BlockPos>();
		int x = coord.getX();
		int y = coord.getY();
		int z = coord.getZ();
		ChunkData data = map[x][z];
		if (data.isAdded(y) || data.distance > rangeSqr) return cc3;
		data.add(y);
		Chunk c = chunkProvider.provideChunk(data.x, data.z);
		if (!c.isLoaded()) {
			c = chunkProvider.getLoadedChunk(data.x, data.z);
		}
		if (c.isEmptyBetween(y << 4, (y << 4) + 15)) {
			data.empty(y);
			if (x < (range << 1) && !(map[x + 1][z].isAdded(y) || map[x + 1][z].distance > rangeSqr || map[x + 1][z].distance < map[x][z].distance)) cc3.add(new BlockPos(x + 1, y, z));
			if (x > 0 && !(map[x - 1][z].isAdded(y) || map[x - 1][z].distance > rangeSqr || map[x - 1][z].distance < map[x][z].distance)) cc3.add(new BlockPos(x - 1, y, z));
			if (y < 15 && !(map[x][z].isAdded(y + 1) || map[x][z].distance > rangeSqr)) cc3.add(new BlockPos(x, y + 1, z));
			if (y > 0 && !(map[x][z].isAdded(y - 1) || map[x][z].distance > rangeSqr)) cc3.add(new BlockPos(x, y - 1, z));
			;
			if (z < (range << 1) && !(map[x][z + 1].isAdded(y) || map[x][z + 1].distance > rangeSqr || map[x][z + 1].distance < map[x][z].distance)) cc3.add(new BlockPos(x, y, z + 1));
			if (z > 0 && !(map[x][z - 1].isAdded(y) || map[x][z - 1].distance > rangeSqr || map[x][z - 1].distance < map[x][z].distance)) cc3.add(new BlockPos(x, y, z - 1));
		} else {
			boolean ok = false;
			if (z > 0 && !(map[x][z - 1].isAdded(y) || map[x][z - 1].distance > rangeSqr || map[x][z - 1].distance < map[x][z].distance)) {
				for (int i = 0; i < 16 && !ok; i++) {
					for (int l = 0; l < 16 && !ok; l++) {
						if (!isBlockNormalCubeDefault(c, l, (y << 4) + i, 0, false)) ok = true;
					}
				}
				if (ok) {
					cc3.add(new BlockPos(x, y, z - 1));
				}
				ok = false;
			}
			if (z < (range << 1) && !(map[x][z + 1].isAdded(y) || map[x][z + 1].distance > rangeSqr || map[x][z + 1].distance < map[x][z].distance)) {
				for (int i = 0; i < 16 && !ok; i++) {
					for (int l = 0; l < 16 && !ok; l++) {
						if (!isBlockNormalCubeDefault(c, l, (y << 4) + i, 15, false)) ok = true;
					}
				}
				if (ok) {
					cc3.add(new BlockPos(x, y, z + 1));
				}
				ok = false;
			}
			if (y > 0 && !(map[x][z].isAdded(y - 1) || map[x][z].distance > rangeSqr)) {
				for (int i = 0; i < 16 && !ok; i++) {
					for (int l = 0; l < 16 && !ok; l++) {
						if (!isBlockNormalCubeDefault(c, l, (y << 4), i, false)) ok = true;
					}
				}
				if (ok) {
					cc3.add(new BlockPos(x, y - 1, z));
				}
				ok = false;
			}
			if (y < 15 && !(map[x][z].isAdded(y + 1) || map[x][z].distance > rangeSqr)) {
				for (int i = 0; i < 16 && !ok; i++) {
					for (int l = 0; l < 16 && !ok; l++) {
						if (!isBlockNormalCubeDefault(c, l, (y << 4) + 15, i, false)) ok = true;
					}
				}
				if (ok) {
					cc3.add(new BlockPos(x, y + 1, z));
				}
				ok = false;
			}
			if (x > 0 && !(map[x - 1][z].isAdded(y) || map[x - 1][z].distance > rangeSqr || map[x - 1][z].distance < map[x][z].distance)) {
				for (int i = 0; i < 16 && !ok; i++) {
					for (int l = 0; l < 16 && !ok; l++) {
						if (!isBlockNormalCubeDefault(c, 0, (y << 4) + l, i, false)) ok = true;
					}
				}
				if (ok) {
					cc3.add(new BlockPos(x - 1, y, z));
				}
				ok = false;
			}
			if (x < (range << 1) && !(map[x + 1][z].isAdded(y) || map[x + 1][z].distance > rangeSqr || map[x + 1][z].distance < map[x][z].distance)) {
				for (int i = 0; i < 16 && !ok; i++) {
					for (int l = 0; l < 16 && !ok; l++) {
						if (!isBlockNormalCubeDefault(c, 15, (y << 4) + l, i, false)) ok = true;
					}
				}
				if (ok) {
					cc3.add(new BlockPos(x + 1, y, z));
				}
			}
		}
		return cc3;
	}

	public static boolean isBlockNormalCubeDefault(Chunk chunk, int par1, int par2, int par3, boolean par4) {
		if (par1 >= -30000000 && par3 >= -30000000 && par1 < 30000000 && par3 < 30000000) {
			if (chunk != null && !chunk.isEmpty()) {
				IBlockState block = chunk.getBlockState(par1 & 15, par2, par3 & 15);
				return block.isNormalCube();
			}
		}
		return par4;
	}

	public class ChunkData implements Comparable<ChunkData> {
		public int	x;
		public int	z;
		public int	added;
		public int	empty;
		public int	distance;

		public ChunkData(int x, int z) {
			this.x = x;
			this.z = z;
			added = 0;
		}

		public boolean isAdded(int level) {
			return (added & (1 << level)) != 0;
		}

		public boolean doAdd() {
			return (added ^ empty) != 0;
		}

		public boolean doAdd(int level) {
			return isAdded(level) && !isEmpty(level);
		}

		public void add(int level) {
			added |= 1 << level;
		}

		public boolean isEmpty(int level) {
			return (empty & (1 << level)) == 0;
		}

		public void empty(int level) {
			empty |= 1 << level;
		}

		public int levels() {
			return added ^ empty;
		}

		@Override
		public int compareTo(ChunkData arg0) {

			return this.distance - arg0.distance;
		}
	}
}
