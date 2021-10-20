package com.tvd12.gamebox.entity;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.gamebox.manager.RoomManager;
import com.tvd12.gamebox.manager.SynchronizedRoomManager;

import java.util.concurrent.atomic.AtomicInteger;

class MMORoomGroup extends EzyLoggable {
	
	private volatile boolean active;
	private final long timeTickMillis;
	private final RoomManager<MMORoom> roomManager;
	private final static AtomicInteger COUNTER = new AtomicInteger();
	
	protected MMORoomGroup(Builder builder) {
		this.timeTickMillis = builder.timeTickMillis;
		this.roomManager = new SynchronizedRoomManager<>();
		this.start();
	}
	
	private void start() {
		Thread newThread = new Thread(this::loop);
		newThread.setName("game-box-mmo-room-group-" + COUNTER.incrementAndGet());
		newThread.start();
	}
	
	private void loop() {
		this.active = true;
		while (active) {
			try {
				long start = System.currentTimeMillis();
				this.updateRooms();
				long end = System.currentTimeMillis();
				long timeElapsed = end - start;
				if (timeElapsed < timeTickMillis) {
					Thread.sleep(timeTickMillis - timeElapsed);
				}
			} catch (Exception e) {
				logger.error("Room group loop error: ", e);
			}
		}
	}
	
	private void updateRooms() {
		this.roomManager.getRoomList().forEach(room -> {
			try {
				room.update();
			} catch (Exception e) {
				logger.warn("Update room: {} error", room, e);
			}
		});
	}
	
	public void addRoom(MMORoom room) {
		this.roomManager.addRoom(room);
	}
	
	public void removeRoom(MMORoom room) {
		this.roomManager.removeRoom(room);
	}
	
	public MMORoom getRoom(long roomId) {
		return this.roomManager.getRoom(roomId);
	}
	
	public MMORoom getRoom(String roomName) {
		return this.roomManager.getRoom(roomName);
	}
	
	public int getRoomCount() {
		return this.roomManager.getRoomCount();
	}
	
	public void destroy() {
		this.active = false;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder implements EzyBuilder<MMORoomGroup> {
		private long timeTickMillis;
		
		public Builder timeTickMillis(int timeTickMillis) {
			this.timeTickMillis = timeTickMillis;
			return this;
		}
		
		@Override
		public MMORoomGroup build() {
			return new MMORoomGroup(this);
		}
	}
}
