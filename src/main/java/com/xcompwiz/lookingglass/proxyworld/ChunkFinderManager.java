package com.xcompwiz.lookingglass.proxyworld;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Ken Butler/shadowking97
 */
public class ChunkFinderManager {
	public static ChunkFinderManager	instance	= new ChunkFinderManager();

	private List<ChunkFinder>			finders;

	public ChunkFinderManager() {
		finders = new LinkedList<ChunkFinder>();
	}

	public void addFinder(ChunkFinder f) {
		finders.add(f);
	}

	public void tick() {
		for (int i = 0; i < finders.size(); ++i) {
			if (finders.get(i).findChunks()) {
				finders.remove(i);
				--i;
			}
		}
	}
}
